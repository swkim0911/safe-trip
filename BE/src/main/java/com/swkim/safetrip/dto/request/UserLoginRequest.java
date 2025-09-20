package com.swkim.safetrip.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserLoginRequest(

        @Email
        @NotBlank(message = "please enter your e-mail address")
        String email,

        @NotBlank(message = "please enter your password")
        String password
) {}