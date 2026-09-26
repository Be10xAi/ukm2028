package com.tech10x.ukm.serviceimpl;

import com.tech10x.ukm.entity.OtpToken;
import com.tech10x.ukm.exception.ApiException;
import com.tech10x.ukm.repository.OtpTokenRepository;
import com.tech10x.ukm.repositoryproxy.OtpRepository;
import com.tech10x.ukm.service.NotificationService;
import com.tech10x.ukm.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Generates and verifies login OTPs. No SMS/email gateway is wired up yet, so the
 * provider call when one is available.
 */
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpRepository otpRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.expiration-minutes:5}")
    private long otpExpirationMinutes;

    @Transactional
    public String generateOtp(String userId, String destination) {
        String otp = generateNumericOtp(otpLength);
        log.info("Otp generated successfully");

        OtpToken token = OtpToken.builder()
                .userId(userId)
                .otpHash(passwordEncoder.encode(otp))
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusSeconds(otpExpirationMinutes * 60))
                .used(false)
                .noOfAttempt(0)
                .build();
        otpRepository.save(token);
        log.info("Otp saved in table");
        return otp;
    }

    @Override
    @Transactional
    public void verifyOtp(String userId, String otp) {
        OtpToken token = otpRepository.findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "No OTP was requested for this account"));

        if ( token.isUsed()||token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "OTP has expired, please request a new one");
        }
        if (!passwordEncoder.matches(otp, token.getOtpHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
        }

        token.setUsed(true);
        otpRepository.save(token);
    }

    @Override
    public String generateNumericOtp(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }


}
