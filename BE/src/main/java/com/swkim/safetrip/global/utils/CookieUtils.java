package com.swkim.safetrip.global.utils;

import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtils {

    public static Cookie makeExpiredRefreshTokenCookie() {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        return cookie;
    }

    public static ResponseCookie createRefreshTokenCookie(String refreshToken){
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)             // JS에서 접근 불가
                .secure(true)               // HTTPS 환경에서만 전송 (개발 중엔 false로 설정 가능)
                .path("/")                  // 모든 경로에 대해 전송됨
                .maxAge(Duration.ofDays(14))// 유효 기간
                .sameSite("Strict")        // CSRF 보호 (필요 시 "Lax"도 가능)
                .build();

    }
}
