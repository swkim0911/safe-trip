package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.AuthTokensResponseDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.response.AccessTokenResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.InvalidRefreshTokenException;
import com.swkim.safetrip.global.exception.custom.UserNotFoundException;
import com.swkim.safetrip.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TokenService tokenService;
    private final JwtUtils jwtUtils;

    public AuthTokensResponseDto login(UserLoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        authenticationManager.authenticate(authenticationToken);

        User findUser = userService.getUserByEmail(email).orElseThrow(UserNotFoundException::new);

        String accessToken = jwtUtils.issueAccessToken(email, findUser.getRole());
        String refreshToken = jwtUtils.issueRefreshToken(email);

        saveRefreshToken(findUser, refreshToken);

        ResponseCookie refreshTokenCookie = jwtUtils.createRefreshTokenCookie(refreshToken);
        AccessTokenResponse accessTokenResponse = AccessTokenResponse.builder()
                .accessToken(accessToken)
                .build();

        return AuthTokensResponseDto.builder()
                .accessTokenResponse(accessTokenResponse)
                .refreshTokenCookie(refreshTokenCookie)
                .build();

    }
    public AuthTokensResponseDto reIssueAccessToken(String refreshToken) {
        if (tokenService.isRefreshTokenBlacklisted(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        String extractedEmail = jwtUtils.verifyRefreshToken(refreshToken);

        User findUser = userService.getUserByEmail(extractedEmail).orElseThrow(InvalidRefreshTokenException::new);

        String reIssuedAccessToken = jwtUtils.issueAccessToken(findUser.getEmail(), findUser.getRole());
        String reIssuedRefreshToken = jwtUtils.issueRefreshToken(findUser.getEmail());

        long ttl = jwtUtils.getRefreshTokenRemainingMillis(refreshToken);
        tokenService.blacklistRefreshToken(refreshToken, ttl);

        saveRefreshToken(findUser, reIssuedRefreshToken);

        ResponseCookie refreshTokenCookie = jwtUtils.createRefreshTokenCookie(reIssuedRefreshToken);
        AccessTokenResponse accessTokenResponse = AccessTokenResponse.builder()
                .accessToken(reIssuedAccessToken)
                .build();

        return AuthTokensResponseDto.builder()
                .accessTokenResponse(accessTokenResponse)
                .refreshTokenCookie(refreshTokenCookie)
                .build();
    }

    private void saveRefreshToken(User findUser, String refreshToken) {
        Long refreshTokenExpirationMillis = jwtUtils.getRefreshTokenExpirationMillis();
        tokenService.saveRefreshToken(findUser.getEmail(), refreshToken, refreshTokenExpirationMillis);
    }

}
