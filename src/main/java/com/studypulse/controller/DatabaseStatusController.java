package com.studypulse.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
@CrossOrigin(origins = "*")
public class DatabaseStatusController {

    private final DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    public DatabaseStatusController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            String productVersion = metaData.getDatabaseProductVersion();

            boolean isMySQL = productName != null && productName.toLowerCase().contains("mysql");
            boolean isAiven = datasourceUrl.toLowerCase().contains("aivencloud.com") ||
                    datasourceUrl.toLowerCase().contains("aiven");

            status.put("connected", true);
            status.put("productName", productName);
            status.put("productVersion", productVersion);
            status.put("isMySQL", isMySQL);
            status.put("isAivenCloud", isAiven);
            status.put("databaseType", productName);
            status.put("summary", isAiven
                    ? "🚀 Connected to Live Aiven Cloud MySQL Database (" + productName + ")!"
                    : isMySQL
                    ? "🐬 Connected to Live MySQL Database (studypro42_db)!"
                    : "⚡ Connected to Database (" + productName + ")");
            status.put("cloudProvider", isAiven ? "Aiven Cloud" : isMySQL ? "MySQL Database (Port 3306)" : "SQL Database");
        } catch (Exception e) {
            status.put("connected", false);
            status.put("error", e.getMessage());
            status.put("summary", "Database connection error: " + e.getMessage());
        }
        return ResponseEntity.ok(status);
    }
}
