package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "name is required")
    @Size(max = 60, message = "name must be at most 60 characters")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    @Size(max = 120, message = "email must be at most 120 characters")
    private String userId;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    @Size(max = 120, message = "email must be at most 120 characters")
    private String email;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "mobileNo must be 10-15 digits")
    private String mobileNo;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
    private String password;

    private LocalDate dob;

    private String gender;
}
