package com.studypulse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SmsService {

    // Fast2SMS API Key (popular free/instant OTP gateway for Indian mobile numbers)
    @Value("${sms.fast2sms.api-key:${FAST2SMS_API_KEY:${SMS_API_KEY:}}}")
    private String fast2smsApiKey;

    // Twilio credentials (for international SMS)
    @Value("${sms.twilio.account-sid:${TWILIO_ACCOUNT_SID:}}")
    private String twilioAccountSid;

    @Value("${sms.twilio.auth-token:${TWILIO_AUTH_TOKEN:}}")
    private String twilioAuthToken;

    @Value("${sms.twilio.phone-number:${TWILIO_PHONE_NUMBER:}}")
    private String twilioPhoneNumber;

    public synchronized void setFast2smsApiKey(String key) {
        if (key != null) {
            this.fast2smsApiKey = key.trim();
        }
    }

    public synchronized String getFast2smsApiKey() {
        return this.fast2smsApiKey;
    }

    public synchronized boolean isLiveConfigured() {
        return (fast2smsApiKey != null && !fast2smsApiKey.isBlank()) ||
                (twilioAccountSid != null && !twilioAccountSid.isBlank() && twilioAuthToken != null && !twilioAuthToken.isBlank());
    }

    /**
     * Dispatches real SMS with 6-digit OTP to the mobile phone number
     */
    public SmsDeliveryResult sendOtpSms(String mobileNumber, String otpCode) {
        String cleanNumber = mobileNumber.replaceAll("[^0-9]", "");
        // If 12 digits starting with 91, extract the 10 digits
        if (cleanNumber.length() == 12 && cleanNumber.startsWith("91")) {
            cleanNumber = cleanNumber.substring(2);
        }

        System.out.println("==================================================");
        System.out.println("📲 [SMS GATEWAY DISPATCH REQUEST]");
        System.out.println("Recipient Mobile Number: " + cleanNumber);
        System.out.println("OTP Code: " + otpCode);
        System.out.println("Live Gateway Configured: " + isLiveConfigured());
        System.out.println("==================================================");

        // 1. Try Fast2SMS (Indian numbers 10 digits)
        if (fast2smsApiKey != null && !fast2smsApiKey.isBlank() && cleanNumber.length() == 10) {
            try {
                return sendViaFast2Sms(cleanNumber, otpCode);
            } catch (Exception e) {
                System.err.println("Fast2SMS gateway error: " + e.getMessage());
                return new SmsDeliveryResult(false, "Fast2SMS error: " + e.getMessage(), true, otpCode);
            }
        }

        // 2. Try Twilio if credentials configured
        if (twilioAccountSid != null && !twilioAccountSid.isBlank() &&
                twilioAuthToken != null && !twilioAuthToken.isBlank()) {
            try {
                return sendViaTwilio(cleanNumber, otpCode);
            } catch (Exception e) {
                System.err.println("Twilio gateway error: " + e.getMessage());
                return new SmsDeliveryResult(false, "Twilio error: " + e.getMessage(), true, otpCode);
            }
        }

        // 3. Fallback when API key is not yet configured
        System.out.println("ℹ️ Notice: To enable live telecom SMS delivery directly to " + cleanNumber + ",");
        System.out.println("   provide your free Fast2SMS API key in application.properties or in the UI settings.");
        System.out.println("   (Fast2SMS gives free instant OTP route at https://www.fast2sms.com)");

        return new SmsDeliveryResult(
                true,
                "Live cellular SMS requires a Fast2SMS API key. For testing now, use code: " + otpCode,
                false,
                otpCode
        );
    }

    private SmsDeliveryResult sendViaFast2Sms(String mobile10Digits, String otpCode) throws Exception {
        // Fast2SMS Quick OTP Route API
        String requestUrl = "https://www.fast2sms.com/dev/bulkV2?" +
                "authorization=" + URLEncoder.encode(fast2smsApiKey.trim(), StandardCharsets.UTF_8) +
                "&variables_values=" + URLEncoder.encode(otpCode, StandardCharsets.UTF_8) +
                "&route=otp" +
                "&numbers=" + URLEncoder.encode(mobile10Digits, StandardCharsets.UTF_8);

        URL url = URI.create(requestUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "StudyPro42-App/1.0");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        int responseCode = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        System.out.println("Fast2SMS Gateway Response: " + response);
        boolean success = responseCode == 200 && response.toString().contains("\"return\":true");
        String msg = success
                ? "Real cellular SMS sent successfully to " + mobile10Digits + " via Fast2SMS telecom network!"
                : "Fast2SMS response: " + response;
        return new SmsDeliveryResult(success, msg, true, otpCode);
    }

    private SmsDeliveryResult sendViaTwilio(String mobile, String otpCode) throws Exception {
        String fullNumber = mobile.startsWith("+") ? mobile : "+91" + mobile;
        String endpoint = "https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid + "/Messages.json";
        String body = "From=" + URLEncoder.encode(twilioPhoneNumber, StandardCharsets.UTF_8) +
                "&To=" + URLEncoder.encode(fullNumber, StandardCharsets.UTF_8) +
                "&Body=" + URLEncoder.encode("Your Study Pro 42 verification code is: " + otpCode + ". Valid for 10 minutes.", StandardCharsets.UTF_8);

        URL url = URI.create(endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        String auth = twilioAccountSid + ":" + twilioAuthToken;
        String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        int responseCode = conn.getResponseCode();
        boolean success = responseCode == 200 || responseCode == 201;

        return new SmsDeliveryResult(success, "SMS dispatched to " + fullNumber + " via Twilio", true, otpCode);
    }

    public static class SmsDeliveryResult {
        private final boolean success;
        private final String message;
        private final boolean liveGatewayUsed;
        private final String otpCode;

        public SmsDeliveryResult(boolean success, String message, boolean liveGatewayUsed, String otpCode) {
            this.success = success;
            this.message = message;
            this.liveGatewayUsed = liveGatewayUsed;
            this.otpCode = otpCode;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public boolean isLiveGatewayUsed() {
            return liveGatewayUsed;
        }

        public String getOtpCode() {
            return otpCode;
        }
    }
}
