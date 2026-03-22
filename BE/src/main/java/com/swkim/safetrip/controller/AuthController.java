package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.response.AuthTokensResponse;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.response.AccessTokenResponse;
import com.swkim.safetrip.global.exception.custom.RefreshTokenMissingException;
import com.swkim.safetrip.global.response.ApiResult;
import com.swkim.safetrip.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.swkim.safetrip.global.utils.CookieUtils.makeExpiredRefreshTokenCookie;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 인증을 시도하고, 성공 시 JWT 토큰을 반환한다.")
    @PostMapping("/login")
    public ApiResult<AccessTokenResponse> login(@RequestBody @Valid UserLoginRequest loginRequest, HttpServletResponse httpServletResponse) {
        AuthTokensResponse authTokensResponseDto = authService.login(loginRequest);
        httpServletResponse.addHeader("Set-Cookie", authTokensResponseDto.refreshTokenCookie().toString());

        return ApiResult.of(HttpStatus.OK.value(), "Login successful", authTokensResponseDto.accessTokenResponse());
    }

    @Operation(summary = "액세스 토큰 재발급", description = "리프레시 토큰을 기반으로 새로운 액세스 토큰을 발급합니다. (RTR)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "액세스 토큰이 성공적으로 재발급됨"),
            @ApiResponse(responseCode = "204", description = "액세스 토큰, 리프레시 토큰이 없다면 처음 접근이므로 비로그인 상태 유지"),
            @ApiResponse(responseCode = "400", description = "리프레시 토큰이 요청에 포함되지 않음")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshTokens(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse httpServletResponse) {
        if (refreshToken == null)  return ResponseEntity.noContent().build();
        if (refreshToken.isBlank()) throw new RefreshTokenMissingException();

        AuthTokensResponse authTokensResponseDto = authService.reIssueAccessToken(refreshToken);
        httpServletResponse.addHeader("Set-Cookie", authTokensResponseDto.refreshTokenCookie().toString());

        return ResponseEntity.ok(ApiResult.of(HttpStatus.OK.value(), "Access Token is reissued under RTR", authTokensResponseDto.accessTokenResponse()));
    }

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 무효화하여 로그아웃을 처리합니다.")
    @PostMapping("/logout")
    public ApiResult<Void> logout(@CookieValue(value = "refreshToken") String refreshToken, HttpServletResponse response) {
        authService.logout(refreshToken);
        response.addCookie(makeExpiredRefreshTokenCookie());

        return ApiResult.of(HttpStatus.OK.value(), "Logout complete", null);
    }
}
