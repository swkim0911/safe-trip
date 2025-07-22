package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.AuthTokensResponseDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.response.AccessTokenResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.InvalidRefreshTokenException;
import com.swkim.safetrip.jwt.JwtUtils;
import com.swkim.safetrip.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        CustomUserDetails userDetails = (CustomUserDetails) authenticate.getPrincipal();

        String accessToken = jwtUtils.issueAccessToken(email, userDetails.getRole());
        String refreshToken = jwtUtils.issueRefreshToken(email);

        saveRefreshToken(email, refreshToken);

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

        User findUser = userService.findUserByEmail(extractedEmail).orElseThrow(InvalidRefreshTokenException::new);

        String reIssuedAccessToken = jwtUtils.issueAccessToken(findUser.getEmail(), findUser.getRole());
        String reIssuedRefreshToken = jwtUtils.issueRefreshToken(findUser.getEmail());

        long ttl = jwtUtils.getRefreshTokenRemainingMillis(refreshToken);
        tokenService.blacklistRefreshToken(refreshToken, ttl);

        saveRefreshToken(findUser.getEmail(), reIssuedRefreshToken);

        ResponseCookie refreshTokenCookie = jwtUtils.createRefreshTokenCookie(reIssuedRefreshToken);
        AccessTokenResponse accessTokenResponse = AccessTokenResponse.builder()
                .accessToken(reIssuedAccessToken)
                .build();

        return AuthTokensResponseDto.builder()
                .accessTokenResponse(accessTokenResponse)
                .refreshTokenCookie(refreshTokenCookie)
                .build();
    }

    public void logout(String refreshToken) {
        String email = jwtUtils.verifyRefreshToken(refreshToken);

        if(!tokenService.isValidRefreshToken(email, refreshToken)){
            throw new InvalidRefreshTokenException();
        }

        long ttl = jwtUtils.getRefreshTokenRemainingMillis(refreshToken);
        tokenService.blacklistRefreshToken(refreshToken, ttl);

        tokenService.deleteRefreshToken(email);

    }

    private void saveRefreshToken(String email, String refreshToken) {
        Long refreshTokenExpirationMillis = jwtUtils.getRefreshTokenExpirationMillis();
        tokenService.saveRefreshToken(email, refreshToken, refreshTokenExpirationMillis);
    }

}
