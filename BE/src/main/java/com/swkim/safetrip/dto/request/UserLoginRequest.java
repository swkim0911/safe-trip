package com.swkim.safetrip.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {

    @Email
    @NotBlank(message = "please enter your e-mail address")
    private String email;

    @NotBlank(message = "please enter your password")
    private String password;
}
