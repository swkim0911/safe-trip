package com.swkim.safetrip.jwt.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.AccessTokenMissingException;
import com.swkim.safetrip.jwt.JwtUtils;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
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
            String accessToken = jwtUtils.extractAccessToken(request)
                    .orElseThrow(AccessTokenMissingException::new);

            DecodedJWT decodedAccessToken = jwtUtils.verifyAccessToken(accessToken);
            String email = jwtUtils.extractEmail(decodedAccessToken).orElseThrow(() -> new BadCredentialsException("Email claim is missing"));

            User findUser = userService.findUserByEmail(email).orElseThrow(() -> new UsernameNotFoundException("The email does not exist"));
            saveAuthentication(findUser);

            filterChain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            entryPoint.commence(request, response, ex);
        }

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

    private boolean requiresAuthentication(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        return "/reports".equals(requestURI) && "POST".equalsIgnoreCase(method);
    }
}
