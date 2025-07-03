package com.swkim.safetrip.jwt.filter;

import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.naming.factory.SendMailFactory;
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

    private final JwtService jwtService;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!requiresAuthentication(request)){
            filterChain.doFilter(request, response);
            return;
        }
        // 1. 요청에 accesstoken 토큰이 valid 한지 확인
        String accessToken = jwtService.extractAccessToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        if(accessToken != null){
            // 3. 유효기간 내에 있으면 saveAuthentication
        }


//        String refreshToken = jwtService.extractRefreshToken(request)
//                .filter(jwtService::isTokenValid)
//                .orElse(null);
//
//        // 리프레시 토큰이 있고 유효성 검증을 통과하면 액세스/리프레시 토큰 재발급
//        if(refreshToken != null){
//            reIssueAccessAndRefreshToken(response, refreshToken); // 리프래시 토큰이 있을 때는 왜 filter로 보내지 않지
//            return;
//        }
//        String accessToken = jwtService.extractAccessToken(request)
//                .filter(jwtService::isTokenValid)
//                .orElse(null);
//
//        if (accessToken != null) {
//            String email = jwtService.extractEmail(accessToken).orElse(null);
//            if (email != null) {
//                User user = jwtService.getUserByEmail(email).orElse(null);
//                if (user != null) {
//                    saveAuthentication(user);
//                }
//            }
//            filterChain.doFilter(request, response);
//        }
        // "/report" post 요청인데 accessToken이 없는 경우에는 로그인 화면을 띄워야 한다.
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

    private boolean requiresAuthentication(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        return "/reports".equals(requestURI) && "POST".equalsIgnoreCase(method);
    }
}
