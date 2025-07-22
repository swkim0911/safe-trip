package com.swkim.safetrip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailValidationResponse {

    @Schema(description = "이메일 형식이 유효한지 여부", example = "true")
    private boolean isValidFormat;

    @Schema(description = "이메일이 중복되지 않고 사용 가능한지 여부", example = "false")
    private boolean isAvailable;

    @Schema(description = "검사 실패 사유", example = "Email is already in use or null")
    private String reason;
}
