package com.tech10x.ukm.repository;

import com.tech10x.ukm.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {


    @Query("select otp from OtpToken otp where otp.userId =:userId order by otp.createdAt desc limit 1")
    Optional<OtpToken> findTopByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);
}
