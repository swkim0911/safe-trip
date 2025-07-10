package com.swkim.safetrip.jwt.filter;

import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.UserNotFoundException;
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
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserService userService;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!requiresAuthentication(request)){
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = jwtUtils.extractAccessToken(request)
                .filter(jwtUtils::isTokenValid)
                .orElse(null);

        // 1. 요청에 accesstoken 토큰이 valid (secretKey, exp 체크) 하면 authentication 저장
        if(accessToken != null){
            String email = jwtUtils.extractEmail(accessToken).orElseThrow(() -> new BadCredentialsException("Invalid Token"));
            try{
                User findUser = userService.getUserByEmail(email);
                saveAuthentication(findUser);
            }catch(UserNotFoundException e){
                throw new UsernameNotFoundException("The email does not exist");
            }
            filterChain.doFilter(request, response);
        }
        // access 토큰이 만료된 경우.
        // access 토큰이 not valid한 경우
        // access 토큰이 없는 경우
        // "/report" post 요청인데 accessToken이 없는 경우에는 로그인 화면을 띄워야 한다.
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
