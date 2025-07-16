package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.AuthTokensResponseDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.response.AccessTokenResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.RefreshTokenReuseDetectedException;
import com.swkim.safetrip.global.exception.custom.UserNotFoundException;
import com.swkim.safetrip.jwt.JwtUtils;
import com.swkim.safetrip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final JwtUtils jwtUtils;

    @Transactional
    public AuthTokensResponseDto login(UserLoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        authenticationManager.authenticate(authenticationToken);

        String accessToken = jwtUtils.issueAccessToken(email);
        String refreshToken = jwtUtils.issueRefreshToken();

        User findUser = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        Long refreshTokenExpirationMillis = jwtUtils.getRefreshTokenExpirationMillis();
        tokenService.saveRefreshToken(findUser.getEmail(), refreshToken, refreshTokenExpirationMillis);

        ResponseCookie refreshTokenCookie = jwtUtils.createRefreshTokenCookie(refreshToken);
        AccessTokenResponse accessTokenResponse = AccessTokenResponse.builder()
                .accessToken(accessToken)
                .build();

        return AuthTokensResponseDto.builder()
                .accessTokenResponse(accessTokenResponse)
                .refreshTokenCookie(refreshTokenCookie)
                .build();

    }

    @Transactional
    public AuthTokensResponseDto reIssueAccessToken(String refreshToken) {
        jwtUtils.validateRefreshToken(refreshToken);

        User findUser = getUserByRefreshToken(refreshToken);

        String reIssuedAccessToken = jwtUtils.issueAccessToken(findUser.getEmail());
        String reIssuedRefreshToken = jwtUtils.issueRefreshToken();

        findUser.updateRefreshToken(reIssuedRefreshToken);

        ResponseCookie refreshTokenCookie = jwtUtils.createRefreshTokenCookie(reIssuedRefreshToken);
        AccessTokenResponse accessTokenResponse = AccessTokenResponse.builder()
                .accessToken(reIssuedAccessToken)
                .build();

        return AuthTokensResponseDto.builder()
                .accessTokenResponse(accessTokenResponse)
                .refreshTokenCookie(refreshTokenCookie)
                .build();
    }


    @Transactional(readOnly = true)
    public User getUserByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken).orElseThrow(RefreshTokenReuseDetectedException::new);
    }

}
