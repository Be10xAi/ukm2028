package com.tech10x.ukm.repositoryproxy;

import com.tech10x.ukm.entity.OtpToken;
import com.tech10x.ukm.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OtpRepository {

    private final OtpTokenRepository otpRepository;

    public Optional<OtpToken>findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(String userId)
    {
        return  otpRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
    }

    public  OtpToken save (OtpToken otpToken)
    {
        return otpRepository.save(otpToken);
    }


}
