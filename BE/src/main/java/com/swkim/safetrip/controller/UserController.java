package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.JwtDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.dto.response.AccessTokenResponse;
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
    private final JwtUtils jwtUtils; // todo: 의존하지 않기

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> signup(@RequestBody @Valid UserSignUpRequest signUpRequest) {
        Long userId = userService.signup(signUpRequest);

        return ApiResponse.of(HttpStatus.CREATED.value(), "Your membership has been registered.", userId);
    }

    @PostMapping("/auth/login")
    public ApiResponse<AccessTokenResponse> login(@RequestBody @Valid UserLoginRequest loginRequest, HttpServletResponse httpServletResponse) {
        JwtDto jwtDto = userService.login(loginRequest);
        jwtUtils.addRefreshTokenToResponse(httpServletResponse, jwtDto.getRefreshToken());

        AccessTokenResponse loginResponse = AccessTokenResponse
                .builder()
                .accessToken(jwtDto.getAccessToken())
                .build();

        return ApiResponse.of(HttpStatus.OK.value(), "Login successful", loginResponse);
    }

    @PostMapping("/auth/refreshToken")
    public ApiResponse<AccessTokenResponse> reIssueAccessToken(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse httpServletResponse) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new MissingRefreshTokenException("Refresh token is Empty");
        }

        AccessTokenResponse accessTokenResponse = userService.reIssueAccessToken(refreshToken, httpServletResponse);
        return ApiResponse.of(HttpStatus.OK.value(), "Access Token is Reissued with RTR", accessTokenResponse);
    }
}
