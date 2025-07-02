package com.swkim.safetrip.jwt.filter;

import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private static final String NO_CHECK_URL = "/auth/login";

    private final JwtService jwtService;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isNoCheckURL(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = jwtService.extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        // 리프레시 토큰이 있고 유효성 검증을 통과하면 액세스/리프레시 토큰 재발급
        if(refreshToken != null){
            reIssueAccessAndRefreshToken(response, refreshToken);
            return;
        }

        checkAccessTokenAndAuthentication(request, response, filterChain);
    }

    private void reIssueAccessAndRefreshToken(HttpServletResponse response, String refreshToken) throws IOException{

        Optional<User> optionalUser = jwtService.getUserByRefreshToken(refreshToken);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String reIssuedRefreshToken = jwtService.reIssueRefreshToken(user);
            String reIssuedAccessToken = jwtService.issueAccessToken(user.getEmail());

            jwtService.addTokensToResponse(
                    response,
                    reIssuedAccessToken,
                    reIssuedRefreshToken
            );
        }
    }

    private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        jwtService.extractAccessToken(request)
                .filter(jwtService::isTokenValid)
                .ifPresent(accessToken -> jwtService.extractEmail(accessToken)
                        .ifPresent(email -> jwtService.getUserByEmail(email)
                                .ifPresent(this::saveAuthentication))
                );

        filterChain.doFilter(request, response);
    }

    private void saveAuthentication(User user){
        UserDetails userDetails = getUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authoritiesMapper.mapAuthorities(userDetails.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private UserDetails getUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    private boolean isNoCheckURL(HttpServletRequest request) {
        return request.getRequestURI().equals(NO_CHECK_URL);
    }
}
