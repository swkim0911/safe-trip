package com.swkim.safetrip.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNicknameRequest(

    @NotBlank(message = "please enter nickname.")
    @Size(min = 2, max = 15, message = "nickname must be between 2 and 15 characters.")
    String nickname
) {}
