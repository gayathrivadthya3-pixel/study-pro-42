package com.studypulse.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url:}")
    private String configuredUrl;

    @Value("${spring.datasource.username:root}")
    private String configuredUsername;

    @Value("${spring.datasource.password:root}")
    private String configuredPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        ParsedCredentials creds = resolveTargetCredentials();

        boolean canConnect = testConnection(creds);

        if (canConnect) {
            log.info("==================================================================");
            log.info("🚀 [StudyPro42] Successfully connected to Primary Database!");
            log.info("🔗 Target URL: {}", maskUrl(creds.jdbcUrl));
            log.info("👤 User: {}", creds.username);
            log.info("==================================================================");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(creds.jdbcUrl);
            config.setUsername(creds.username);
            config.setPassword(creds.password);
            config.setDriverClassName(creds.driverClassName);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(20000);
            config.setIdleTimeout(300000);
            config.setMaxLifetime(1800000);
            config.setPoolName("StudyPro42-PrimaryPool");
            return new HikariDataSource(config);
        }

        log.warn("==================================================================");
        log.warn("⚠️ [StudyPro42] Primary database at {} is unreachable.", maskUrl(creds.jdbcUrl));
        log.warn("💡 Activating resilient persistent embedded database (MySQL mode) at ./data/studypro42_db");
        log.warn("✅ Application will boot smoothly and remain 100% ONLINE on cloud deployments!");
        log.warn("==================================================================");

        try {
            File dataDir = new File("./data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
        } catch (Exception ignored) {}

        HikariConfig h2Config = new HikariConfig();
        h2Config.setJdbcUrl("jdbc:h2:file:./data/studypro42_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1");
        h2Config.setDriverClassName("org.h2.Driver");
        h2Config.setUsername("sa");
        h2Config.setPassword("");
        h2Config.setMaximumPoolSize(10);
        h2Config.setMinimumIdle(2);
        h2Config.setConnectionTimeout(20000);
        h2Config.setPoolName("StudyPro42-ResilientPool");
        return new HikariDataSource(h2Config);
    }

    private ParsedCredentials resolveTargetCredentials() {
        // Priority 1: Cloud environment variables (Render / Railway / Aiven)
        String envUrl = getFirstNonEmptyEnv("AIVEN_DB_URL", "MYSQL_URL", "DATABASE_URL", "SPRING_DATASOURCE_URL");
        String rawUrl = (envUrl != null && !envUrl.trim().isEmpty()) ? envUrl.trim() : configuredUrl;

        String user = getFirstNonEmptyEnv("AIVEN_DB_USERNAME", "MYSQL_USER", "MYSQLUSER", "DB_USERNAME");
        if (user == null || user.trim().isEmpty()) {
            user = configuredUsername;
        }

        String pass = getFirstNonEmptyEnv("AIVEN_DB_PASSWORD", "MYSQL_PASSWORD", "MYSQLPASSWORD", "DB_PASSWORD");
        if (pass == null) {
            pass = configuredPassword;
        }

        // Support individual host/port environment variables
        String envHost = getFirstNonEmptyEnv("AIVEN_DB_HOST", "MYSQL_HOST", "MYSQLHOST");
        if (envHost != null && !envHost.trim().isEmpty()) {
            String envPort = getFirstNonEmptyEnv("AIVEN_DB_PORT", "MYSQL_PORT", "MYSQLPORT");
            if (envPort == null || envPort.trim().isEmpty()) envPort = "3306";
            String envDb = getFirstNonEmptyEnv("AIVEN_DB_NAME", "MYSQL_DATABASE", "MYSQLDATABASE");
            if (envDb == null || envDb.trim().isEmpty()) envDb = "defaultdb";
            rawUrl = "jdbc:mysql://" + envHost.trim() + ":" + envPort.trim() + "/" + envDb.trim() + "?sslMode=REQUIRED";
        }

        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            rawUrl = "jdbc:mysql://localhost:3306/studypro42_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        }

        // Robust parsing of mysql:// URIs (e.g. Aiven Service URI: mysql://user:pass@host:port/dbname?ssl-mode=REQUIRED)
        if (rawUrl.startsWith("mysql://")) {
            try {
                String withoutScheme = rawUrl.substring(8);
                int atIdx = withoutScheme.lastIndexOf('@');
                if (atIdx != -1) {
                    String userPass = withoutScheme.substring(0, atIdx);
                    String hostPortRest = withoutScheme.substring(atIdx + 1);

                    if (userPass.contains(":")) {
                        String[] up = userPass.split(":", 2);
                        user = up[0];
                        pass = up[1];
                    } else {
                        user = userPass;
                    }

                    String query = null;
                    int qIdx = hostPortRest.indexOf('?');
                    if (qIdx != -1) {
                        query = hostPortRest.substring(qIdx + 1);
                        hostPortRest = hostPortRest.substring(0, qIdx);
                    }

                    String path = "/defaultdb";
                    int slashIdx = hostPortRest.indexOf('/');
                    if (slashIdx != -1) {
                        path = hostPortRest.substring(slashIdx);
                        hostPortRest = hostPortRest.substring(0, slashIdx);
                    }

                    String host = hostPortRest;
                    int port = 3306;
                    if (hostPortRest.contains(":")) {
                        String[] hp = hostPortRest.split(":", 2);
                        host = hp[0];
                        try {
                            port = Integer.parseInt(hp[1]);
                        } catch (NumberFormatException ignored) {}
                    }

                    if (query != null) {
                        query = query.replace("ssl-mode=", "sslMode=");
                    } else {
                        query = "sslMode=REQUIRED";
                    }

                    String cleanJdbc = "jdbc:mysql://" + host + ":" + port + path + "?" + query;
                    return new ParsedCredentials(cleanJdbc, user, pass, "com.mysql.cj.jdbc.Driver");
                }
            } catch (Exception e) {
                log.warn("Custom mysql:// parse warning: {}", e.getMessage());
            }
        }

        // Standardize ssl-mode in jdbc:mysql:// URLs if present
        if (rawUrl.startsWith("jdbc:mysql:") && rawUrl.contains("ssl-mode=")) {
            rawUrl = rawUrl.replace("ssl-mode=", "sslMode=");
        }

        String driver = "com.mysql.cj.jdbc.Driver";
        if (rawUrl.startsWith("jdbc:postgresql:")) {
            driver = "org.postgresql.Driver";
        } else if (rawUrl.startsWith("jdbc:h2:")) {
            driver = "org.h2.Driver";
        }

        return new ParsedCredentials(rawUrl, user, pass, driver);
    }

    private boolean testConnection(ParsedCredentials creds) {
        if (creds.jdbcUrl.startsWith("jdbc:h2:")) {
            return true;
        }

        try {
            Class.forName(creds.driverClassName);
            DriverManager.setLoginTimeout(4);

            String testUrl = creds.jdbcUrl;
            if (testUrl.startsWith("jdbc:mysql:") && !testUrl.contains("connectTimeout")) {
                testUrl += (testUrl.contains("?") ? "&" : "?") + "connectTimeout=4000&socketTimeout=4000";
            }

            try (Connection conn = DriverManager.getConnection(testUrl, creds.username, creds.password)) {
                return conn != null && conn.isValid(3);
            }
        } catch (Throwable t) {
            log.info("Direct database connection test to [{}] returned: {}", maskUrl(creds.jdbcUrl), t.getMessage());
            return false;
        }
    }

    private String getFirstNonEmptyEnv(String... names) {
        for (String name : names) {
            String val = System.getenv(name);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return null;
    }

    private String maskUrl(String url) {
        if (url == null) return "null";
        return url.replaceAll("(?i):([^/:@]+)@", ":****@");
    }

    private static class ParsedCredentials {
        final String jdbcUrl;
        final String username;
        final String password;
        final String driverClassName;

        ParsedCredentials(String jdbcUrl, String username, String password, String driverClassName) {
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
            this.driverClassName = driverClassName;
        }
    }
}
