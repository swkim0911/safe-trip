package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.AuthTokensResponseDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.response.AccessTokenResponse;
import com.swkim.safetrip.global.exception.custom.RefreshTokenMissingException;
import com.swkim.safetrip.global.response.ApiResponse;
import com.swkim.safetrip.jwt.JwtUtils;
import com.swkim.safetrip.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @PostMapping("/auth/login")
    public ApiResponse<AccessTokenResponse> login(@RequestBody @Valid UserLoginRequest loginRequest, HttpServletResponse httpServletResponse) {
        AuthTokensResponseDto authTokensResponseDto = authService.login(loginRequest);
        httpServletResponse.addHeader("Set-Cookie", authTokensResponseDto.getRefreshTokenCookie().toString());

        return ApiResponse.of(HttpStatus.OK.value(), "Login successful", authTokensResponseDto.getAccessTokenResponse());
    }

    @PostMapping("/auth/refresh")
    public ApiResponse<AccessTokenResponse> refreshTokens(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse httpServletResponse) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenMissingException();
        }
        AuthTokensResponseDto authTokensResponseDto = authService.reIssueAccessToken(refreshToken);
        httpServletResponse.addHeader("Set-Cookie", authTokensResponseDto.getRefreshTokenCookie().toString());

        return ApiResponse.of(HttpStatus.OK.value(), "Access Token is reissued under RTR", authTokensResponseDto.getAccessTokenResponse());
    }

    @PostMapping("/auth/logout")
    public ApiResponse<Void> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        authService.logout(email);

        return ApiResponse.of(HttpStatus.OK.value(), "Logout complete", null);
    }

}
