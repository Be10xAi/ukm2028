package com.tech10x.ukm.service;

import com.tech10x.ukm.dto.request.LoginRequest;
import com.tech10x.ukm.dto.request.RegisterRequest;
import com.tech10x.ukm.dto.response.AuthResponse;
import com.tech10x.ukm.dto.response.UserResponse;
import com.tech10x.ukm.entity.Users;

public interface AuthService {


    UserResponse register(RegisterRequest request);

    void requestOtp(String email);

    AuthResponse login(LoginRequest request);

    void loginWithPassword(Users user, String rawPassword);

    Users findByEmailOrThrow(String email);

    UserResponse toUserResponse(Users user);
}
