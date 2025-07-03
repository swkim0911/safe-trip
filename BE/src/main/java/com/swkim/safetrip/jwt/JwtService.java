package com.swkim.safetrip.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.UserNotFoundException;
import com.swkim.safetrip.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.name}")
    private String refreshName;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "email";
    private static final String BEARER = "Bearer ";

    private final UserRepository userRepository;

    private String createAccessToken(String email) {
        Date now = new Date();

        return JWT.create()
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + accessTokenExpirationPeriod))
                .withClaim(EMAIL_CLAIM, email)
                .sign(Algorithm.HMAC512(secretKey));
    }

    private String createRefreshToken() {
        Date now = new Date();

        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
    }

    public void addTokensToResponse(HttpServletResponse response, String accessToken, String refreshToken) throws IOException{
        response.setStatus(HttpServletResponse.SC_OK);
        addAccessTokenToResponse(response, accessToken);
        addRefreshTokenToResponse(response, refreshToken);
    }

    public String issueAccessToken(String email){
        return createAccessToken(email);
    }

    public String issueRefreshToken(){
        return createRefreshToken();
    }

    @Transactional
    public String reIssueRefreshToken(User user){
        String reIssueRefreshToken = createRefreshToken();
        user.updateRefreshToken(reIssueRefreshToken);

        return reIssueRefreshToken;
    }

    public Optional<User> getUserByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    public Optional<User> getUserByEmail(String email){
        return userRepository.findByEmail(email);
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

    public Optional<String> extractEmail(String accessToken) {
        try {
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(accessToken)
                    .getClaim(EMAIL_CLAIM)
                    .asString());
        }catch(JWTVerificationException e){
            log.error("Access Token is not valid.");
            return Optional.empty();
        }
    }

    public void addAccessTokenToResponse(HttpServletResponse response, String accessToken) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String json = String.format("{\"accessToken\": \"%s\"}", accessToken);
        response.getWriter().write(json);
    }

    public void addRefreshTokenToResponse(HttpServletResponse response, String refreshToken){
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)             // JS에서 접근 불가
                .secure(true)               // HTTPS 환경에서만 전송 (개발 중엔 false로 설정 가능)
                .path("/")                  // 모든 경로에 대해 전송됨
                .maxAge(Duration.ofDays(14))// 유효 기간
                .sameSite("Strict")        // CSRF 보호 (필요 시 "Lax"도 가능)
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Transactional
    public void saveRefreshToken(String email, String refreshToken){
        User user = findUser(email);
        user.updateRefreshToken(refreshToken);
    }

    @Transactional(readOnly = true)
    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    // verify: 서명, 토큰 구조, 만료 시간 검증
    public boolean isTokenValid(String token){
        try{
            JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(token);
            return true;
        }catch (JWTVerificationException e){
            log.error("Token is not valid");
            return false;
        }
    }
}
