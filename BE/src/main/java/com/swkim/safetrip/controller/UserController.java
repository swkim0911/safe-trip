package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.LoginResultDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.dto.response.AccessTokenResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.MissingRefreshTokenException;
import com.swkim.safetrip.global.response.ApiResponse;
import com.swkim.safetrip.jwt.JwtUtils;
import com.swkim.safetrip.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    public ResponseEntity<ApiResponse<AccessTokenResponse>> login(@RequestBody @Valid UserLoginRequest loginRequest, HttpServletResponse httpServletResponse) {
        LoginResultDto loginResultDto = userService.login(loginRequest);
        httpServletResponse.addHeader("Set-Cookie", loginResultDto.getRefreshTokenCookie().toString());

        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Login successful", loginResultDto.getAccessTokenResponse()));
    }

    @PostMapping("/auth/refreshToken")
    public ApiResponse<AccessTokenResponse> reIssueAccessToken(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse httpServletResponse) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new MissingRefreshTokenException();
        }
        // todo: 리팩토링은 나중에 하자
        Optional<User> optionalUser = jwtUtils.getUserByRefreshToken(refreshToken);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String reIssuedRefreshToken = jwtUtils.reIssueRefreshToken(user);
            String reIssuedAccessToken = jwtUtils.issueAccessToken(user.getEmail());

            jwtUtils.addTokensToResponse(
                    httpServletResponse,
                    reIssuedAccessToken,
                    reIssuedRefreshToken
            );
        }

        AccessTokenResponse accessTokenResponse = userService.reIssueAccessToken(refreshToken);

        return ApiResponse.of(HttpStatus.OK.value(), "Access Token is Reissued with RTR", accessTokenResponse);
    }
}
