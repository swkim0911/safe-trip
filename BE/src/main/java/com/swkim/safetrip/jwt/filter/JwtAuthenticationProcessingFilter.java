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
import java.util.Optional;

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
                        token -> checkRefreshTokenAndSetReIssuedAccessAndRefreshTokens(response, token),
                        () -> checkAccessTokenAndAuthentication(request, response, filterChain)
                );
    }

    private void checkRefreshTokenAndSetReIssuedAccessAndRefreshTokens(HttpServletResponse response, String refreshToken) {
        checkRefreshToken(refreshToken)
                .ifPresent(user -> {
                    String reIssuedRefreshToken = jwtService.reIssueRefreshToken(user);
                    jwtService.setAccessAndRefreshToken(response, jwtService.createAccessToken(user.getEmail()), reIssuedRefreshToken);
                });
    }

    private Optional<User> checkRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        jwtService.extractAccessToken(request)
                .filter(jwtService::isTokenValid)
                .ifPresent(accessToken -> jwtService.extractEmail(accessToken)
                        .ifPresent(email -> userRepository.findByEmail(email)
                                .ifPresent(myUser -> saveAuthentication(myUser)))
                );

        filterChain.doFilter(request, response);
    }



    private boolean isNoCheckURL(HttpServletRequest request) {
        return request.getRequestURI().equals(NO_CHECK_URL);
    }
}
