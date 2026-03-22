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

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final AuthenticationEntryPoint entryPoint;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!requiresAuthentication(request)){
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String accessToken = jwtProvider.extractAccessToken(request)
                    .orElseThrow(AccessTokenMissingException::new);

            DecodedJWT decodedAccessToken = jwtProvider.verifyAccessToken(accessToken);
            String email = jwtProvider.extractEmail(decodedAccessToken).orElseThrow(() -> new BadCredentialsException("Email claim is missing"));

            User findUser = userService.findUserByEmail(email).orElseThrow(() -> new UsernameNotFoundException("The email does not exist"));
            saveAuthentication(findUser);

            filterChain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            entryPoint.commence(request, response, ex);
        }

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
                .anyMatch(ep -> ep.getPath().equals(requestURI) && ep.getMethod().equals(method));
    }


}
