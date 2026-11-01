package com.studypulse.dto;

public class OtpRequestDto {
    private String mobileNumber;

    public OtpRequestDto() {}

    public OtpRequestDto(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}
