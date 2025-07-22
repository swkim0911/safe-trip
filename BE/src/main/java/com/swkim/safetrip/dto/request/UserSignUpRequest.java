package com.swkim.safetrip.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpRequest {

    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Invalid email format. (e.g., user@example.com)"
    )
    @Email
    @NotBlank(message = "please enter your e-mail address.")
    private String email;


    @Size(min = 8, max = 20, message = "password must be between 8 and 20 characters.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).*$",
            message = "password must include letters, numbers, and special characters."
    )
    @NotBlank(message = "please enter your password.")
    private String password;

    @Size(min = 2, max = 15, message = "nickname must be between 2 and 15 characters.")
    @Pattern(
            regexp = "^[a-zA-Z0-9가-힣_-]+$",
            message = "nickname can contain Korean, English letters, digits, _ and - only."
    )
    @NotBlank(message = "please enter nickname.")
    private String nickname;
}
