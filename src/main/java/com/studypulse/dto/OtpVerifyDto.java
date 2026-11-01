package com.studypulse.dto;

public class OtpVerifyDto {
    private String mobileNumber;
    private String otpCode;
    private String name;

    public OtpVerifyDto() {}

    public OtpVerifyDto(String mobileNumber, String otpCode, String name) {
        this.mobileNumber = mobileNumber;
        this.otpCode = otpCode;
        this.name = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
