package com.swkim.safetrip.dto;

import com.swkim.safetrip.dto.response.AccessTokenResponse;
import lombok.Builder;
import org.springframework.http.ResponseCookie;

@Builder
public record AuthTokensResponseDto (

    AccessTokenResponse accessTokenResponse,

    ResponseCookie refreshTokenCookie
){}
