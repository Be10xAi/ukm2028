package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpRequest {

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    private String email;
}
