package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Supports two login modes on the same endpoint: pass {@code password} to
 * authenticate with a password, or {@code otp} to authenticate with a code
 * obtained from POST /login/otp. Exactly one of the two must be supplied.
 */
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    private String email;

    private String password;

    private String otp;
}
