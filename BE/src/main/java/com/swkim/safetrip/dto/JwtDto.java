package com.swkim.safetrip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
public class JwtDto {

    private String accessToken;

    private String refreshToken;
}
