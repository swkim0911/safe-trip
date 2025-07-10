package com.swkim.safetrip.dto;

import com.swkim.safetrip.dto.response.AccessTokenResponse;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseCookie;

@Getter
@Builder
public class AuthTokensResponseDto {

    private AccessTokenResponse accessTokenResponse;

    private ResponseCookie refreshTokenCookie;
}
