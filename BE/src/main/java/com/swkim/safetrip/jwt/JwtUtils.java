package com.swkim.safetrip.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.swkim.safetrip.entity.enums.Role;
import com.swkim.safetrip.global.exception.custom.AccessTokenExpiredException;
import com.swkim.safetrip.global.exception.custom.InvalidAccessTokenException;
import com.swkim.safetrip.global.exception.custom.InvalidRefreshTokenException;
import com.swkim.safetrip.global.exception.custom.RefreshTokenExpiredException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationMillis;

    @Getter
    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationMillis;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.name}")
    private String refreshName;

    private static final String ROLE_CLAIM = "role";
    private static final String BEARER = "Bearer ";

    private String createAccessToken(String email, Role role) {
        Date now = new Date();
        String roleName = role.getKey();

        return JWT.create()
                .withSubject(email)
                .withExpiresAt(new Date(now.getTime() + accessTokenExpirationMillis))
                .withClaim(ROLE_CLAIM, roleName)
                .sign(Algorithm.HMAC512(secretKey));
    }

    private String createRefreshToken(String email) {
        Date now = new Date();

        return JWT.create()
                .withSubject(email)
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpirationMillis))
                .sign(Algorithm.HMAC512(secretKey));
    }

    public String issueAccessToken(String email, Role role){
        return createAccessToken(email, role);
    }

    public String issueRefreshToken(String email){
        return createRefreshToken(email);
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER))
                .map(accessToken -> accessToken.replace(BEARER, ""));
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (null == cookies) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> refreshName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public Optional<String> extractEmail(DecodedJWT decodedAccessToken) {
        return Optional.ofNullable(decodedAccessToken.getSubject());
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken){
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)             // JS에서 접근 불가
                .secure(true)               // HTTPS 환경에서만 전송 (개발 중엔 false로 설정 가능)
                .path("/")                  // 모든 경로에 대해 전송됨
                .maxAge(Duration.ofDays(14))// 유효 기간
                .sameSite("Strict")        // CSRF 보호 (필요 시 "Lax"도 가능)
                .build();

    }

    // verify: 서명, 토큰 구조, 만료 시간 검증
    public DecodedJWT verifyRefreshToken(String refreshToken) {
        try {
            return JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(refreshToken);
        } catch (TokenExpiredException e) {
            log.info("Refresh token expired for user: {}", refreshToken);
            throw new RefreshTokenExpiredException();
        } catch (JWTVerificationException e) {
            log.warn("Invalid refresh token detected: {}", refreshToken);
            throw new InvalidRefreshTokenException();
        }
    }

    public DecodedJWT verifyAccessToken(String accessToken) {
        try {
            return JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(accessToken);
        } catch (TokenExpiredException e) {
            log.info("Access token expired for user: {}", accessToken);
            throw new AccessTokenExpiredException();
        } catch (JWTVerificationException e) {
            log.warn("Invalid access token detected: {}", accessToken);
            throw new InvalidAccessTokenException();
        }
    }
}
