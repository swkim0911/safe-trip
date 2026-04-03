package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.response.AccessTokenResponse;
import com.swkim.safetrip.dto.response.AuthTokensResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.InvalidRefreshTokenException;
import com.swkim.safetrip.security.jwt.JwtProvider;
import com.swkim.safetrip.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import static com.swkim.safetrip.global.utils.CookieUtils.createRefreshTokenCookie;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TokenService tokenService;
    private final JwtProvider jwtProvider;

    public AuthTokensResponse login(UserLoginRequest loginRequest) {
        String email = loginRequest.email();
        String password = loginRequest.password();

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        CustomUserDetails userDetails = (CustomUserDetails) authenticate.getPrincipal();

        String accessToken = jwtProvider.issueAccessToken(email, userDetails.getRole());
        String refreshToken = jwtProvider.issueRefreshToken(email);

        saveRefreshToken(email, refreshToken);

        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(refreshToken);
        AccessTokenResponse accessTokenResponse = AccessTokenResponse.builder()
                .accessToken(accessToken)
                .build();

        log.info("User logged in: {}", email);
        return AuthTokensResponse.builder()
                .accessTokenResponse(accessTokenResponse)
                .refreshTokenCookie(refreshTokenCookie)
                .build();

    }

    public AuthTokensResponse reIssueAccessToken(String refreshToken) {
        if (tokenService.isRefreshTokenBlacklisted(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        String extractedEmail = jwtProvider.verifyRefreshToken(refreshToken);

        User findUser = userService.findUserByEmail(extractedEmail).orElseThrow(InvalidRefreshTokenException::new);

        String reIssuedAccessToken = jwtProvider.issueAccessToken(findUser.getEmail(), findUser.getRole());
        String reIssuedRefreshToken = jwtProvider.issueRefreshToken(findUser.getEmail());

        long ttl = jwtProvider.getRefreshTokenRemainingMillis(refreshToken);
        tokenService.blacklistRefreshToken(refreshToken, ttl);

        saveRefreshToken(findUser.getEmail(), reIssuedRefreshToken);

        log.info("Access token reissued: {}", findUser.getEmail());
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(reIssuedRefreshToken);
        AccessTokenResponse accessTokenResponse = AccessTokenResponse.builder()
                .accessToken(reIssuedAccessToken)
                .build();

        return AuthTokensResponse.builder()
                .accessTokenResponse(accessTokenResponse)
                .refreshTokenCookie(refreshTokenCookie)
                .build();
    }

    public void logout(String refreshToken) {
        String email = jwtProvider.verifyRefreshToken(refreshToken);

        if(!tokenService.isValidRefreshToken(email, refreshToken)){
            throw new InvalidRefreshTokenException();
        }

        long ttl = jwtProvider.getRefreshTokenRemainingMillis(refreshToken);
        tokenService.blacklistRefreshToken(refreshToken, ttl);

        tokenService.deleteRefreshToken(email);
        log.info("User logged out: {}", email);
    }

    private void saveRefreshToken(String email, String refreshToken) {
        Long refreshTokenExpirationMillis = jwtProvider.getRefreshTokenExpirationMillis();
        tokenService.saveRefreshToken(email, refreshToken, refreshTokenExpirationMillis);
    }

}
