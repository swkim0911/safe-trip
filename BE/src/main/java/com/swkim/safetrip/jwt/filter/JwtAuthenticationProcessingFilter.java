package com.swkim.safetrip.jwt.filter;

import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.jwt.JwtService;
import com.swkim.safetrip.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private static final String NO_CHECK_URL = "/login";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isNoCheckURL(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 리프레시 토큰이 있고 유효성 검증을 통과하면 액세스/리프레시 토큰 재발급
        jwtService.extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .ifPresentOrElse(
                        token -> checkRefreshTokenAndReIssueAccessToken(response, token),
                        () -> checkAccessTokenAndAuthentication(request, response, filterChain)
                );
    }

    private void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    String reIssuedRefreshToken = jwtService.reIssueRefreshToken(user);
                    jwtService.sendAccessAndRefreshToken(response, jwtService.createAccessToken(user.getEmail()), reIssuedRefreshToken);
                });
    }

    private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

    }

    private static boolean isNoCheckURL(HttpServletRequest request) {
        return request.getRequestURI().equals(NO_CHECK_URL);
    }
}
