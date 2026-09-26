package com.tech10x.ukm.controller;

import com.tech10x.ukm.dto.request.LoginRequest;
import com.tech10x.ukm.dto.request.OtpRequest;
import com.tech10x.ukm.dto.request.RegisterRequest;
import com.tech10x.ukm.dto.response.AuthResponse;
import com.tech10x.ukm.dto.response.GenericResponse;
import com.tech10x.ukm.dto.response.UserResponse;
import com.tech10x.ukm.service.AuthService;
import com.tech10x.ukm.serviceimpl.AuthServiceImpl;
import com.tech10x.ukm.utils.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ukm/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<GenericResponse<UserResponse>> register( @Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success(List.of(user), "User registered"));
    }

    /** Sends an OTP to the account's registered email; required before logging in with otp. */
    @PostMapping("/login/otp")
    public ResponseEntity<GenericResponse<Object>> requestOtp(@Valid @RequestBody OtpRequest request) {
        authService.requestOtp(request.getEmail());
        return ResponseEntity.ok(ResponseUtil.success("OTP sent successfully"));
    }

    /** Logs in with either {@code password} or {@code otp} in the request body - exactly one must be set. */
    @PostMapping("/login")
    public ResponseEntity<GenericResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ResponseUtil.success(List.of(response), "Login successful"));
    }
}
