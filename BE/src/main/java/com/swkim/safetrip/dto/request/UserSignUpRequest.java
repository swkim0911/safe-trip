package com.swkim.safetrip.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserSignUpRequest (

    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Invalid email format. (e.g., user@example.com)"
    )
    @Email
    @NotBlank(message = "please enter your e-mail address.")
    String email,


    @Size(min = 8, max = 20, message = "password must be between 8 and 20 characters.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).*$",
            message = "password must include letters, numbers, and special characters."
    )
    @NotBlank(message = "please enter your password.")
    String password,

    @NotBlank(message = "please enter nickname.")
    @Size(min = 2, max = 15, message = "nickname must be between 2 and 15 characters.")
    String nickname
){}
