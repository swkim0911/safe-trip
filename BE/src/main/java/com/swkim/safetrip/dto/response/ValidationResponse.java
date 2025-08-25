package com.swkim.safetrip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ValidationResponse (

    @Schema(description = "형식이 유효한지 여부", example = "true")
    boolean isValidFormat,

    @Schema(description = "중복되지 않고 사용 가능한지 여부", example = "false")
    boolean isAvailable,

    @Schema(description = "검사 실패 사유", example = "Email is already in use or null")
    String reason
){}
