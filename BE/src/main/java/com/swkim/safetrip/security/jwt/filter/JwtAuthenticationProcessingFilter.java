package com.swkim.safetrip.security.jwt.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.AccessTokenMissingException;
import com.swkim.safetrip.security.CustomUserDetails;
import com.swkim.safetrip.security.ProtectedEndpoint;
import com.swkim.safetrip.security.jwt.JwtProvider;
import com.swkim.safetrip.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final AuthenticationEntryPoint entryPoint;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean isProtected = requiresAuthentication(request);
        Optional<String> tokenOpt = jwtProvider.extractAccessToken(request);

        if (isProtected && tokenOpt.isEmpty()) {
            entryPoint.commence(request, response, new AccessTokenMissingException());
            return;
        }

        if (tokenOpt.isPresent()) {
            try {
                DecodedJWT decodedAccessToken = jwtProvider.verifyAccessToken(tokenOpt.get());
                String email = jwtProvider.extractEmail(decodedAccessToken)
                        .orElseThrow(() -> new BadCredentialsException("Email claim is missing"));
                User findUser = userService.findUserByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("The email does not exist"));
                saveAuthentication(findUser);
            } catch (AuthenticationException ex) {
                if (isProtected) {
                    entryPoint.commence(request, response, ex);
                    return;
                }
                // 비보호 엔드포인트: 토큰이 유효하지 않아도 비로그인으로 처리
            }
        }

        filterChain.doFilter(request, response);
    }

    private void saveAuthentication(User user){
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, authoritiesMapper.mapAuthorities(customUserDetails.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod().toUpperCase();

        return Arrays.stream(ProtectedEndpoint.values())
                .anyMatch(ep -> pathMatcher.match(ep.getPath(), requestURI) && ep.getMethod().equals(method));
    }


}
