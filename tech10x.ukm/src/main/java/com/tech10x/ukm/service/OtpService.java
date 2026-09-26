package com.tech10x.ukm.service;

public interface OtpService {

    String generateOtp(String userId, String destination);
    void verifyOtp(String userId, String otp);
    String generateNumericOtp(int length);
}
