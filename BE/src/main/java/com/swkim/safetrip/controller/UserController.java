package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.AuthTokensResponseDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.dto.response.AccessTokenResponse;
import com.swkim.safetrip.global.exception.custom.RefreshTokenMissingException;
import com.swkim.safetrip.global.response.ApiResponse;
import com.swkim.safetrip.jwt.JwtUtils;
import com.swkim.safetrip.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> signup(@RequestBody @Valid UserSignUpRequest signUpRequest) {
        Long userId = userService.signup(signUpRequest);

        return ApiResponse.of(HttpStatus.CREATED.value(), "Your membership has been registered.", userId);
    }

    @PostMapping("/auth/login")
    public ApiResponse<AccessTokenResponse> login(@RequestBody @Valid UserLoginRequest loginRequest, HttpServletResponse httpServletResponse) {
        AuthTokensResponseDto authTokensResponseDto = userService.login(loginRequest);
        httpServletResponse.addHeader("Set-Cookie", authTokensResponseDto.getRefreshTokenCookie().toString());

        return ApiResponse.of(HttpStatus.OK.value(), "Login successful", authTokensResponseDto.getAccessTokenResponse());
    }

    @PostMapping("/auth/refresh")
    public ApiResponse<AccessTokenResponse> refreshTokens(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse httpServletResponse) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenMissingException();
        }
        AuthTokensResponseDto authTokensResponseDto = userService.reIssueAccessToken(refreshToken);
        httpServletResponse.addHeader("Set-Cookie", authTokensResponseDto.getRefreshTokenCookie().toString());

        return ApiResponse.of(HttpStatus.OK.value(), "Access Token is Reissued with in RTR", authTokensResponseDto.getAccessTokenResponse());
    }
}
