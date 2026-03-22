package com.swkim.safetrip.dto.response;

import lombok.Builder;
import org.springframework.http.ResponseCookie;

@Builder
public record AuthTokensResponse(

    AccessTokenResponse accessTokenResponse,

    ResponseCookie refreshTokenCookie
){}
