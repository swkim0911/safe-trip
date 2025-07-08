package com.swkim.safetrip.jwt.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.swkim.safetrip.jwt.JwtService;
import com.swkim.safetrip.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationProcessingFilterTest {

    @InjectMocks
    JwtAuthenticationProcessingFilter filter;

    @Mock
    JwtService jwtService;

    @Mock
    UserService userService;

    @Mock
    FilterChain filterChain;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Test
    void 글_등록_요청이_아니라면_requiresAuthentication_false로_JWT_검증이_실행되지_않는다() throws Exception {
        // given
        when(request.getRequestURI()).thenReturn("/reports/1");
        when(request.getMethod()).thenReturn("GET");

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then (jwtService가 호출되지 않았고, filterChain이 동작하는지 확인)
        verify(jwtService, never()).extractAccessToken(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void 글_등록_요청이라면_JWT_검증이_실행된다() throws ServletException, IOException {
        // given
        when(request.getRequestURI()).thenReturn("/reports");
        when(request.getMethod()).thenReturn("POST");

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then (jwtService가 accessToken을 추출하는지 검증한다)
        verify(jwtService).extractAccessToken(any());
    }

    @Test
    void 액세스_토큰이_valid_하지만_이메일_claim이_없을_때_bad_credential_예외가_발생한다() throws ServletException, IOException {
        // given

        String secretKey = "eijnv329asic";

        String accessTokenWithoutEmailClaim = JWT.create()
                .withSubject("AccessToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .sign(Algorithm.HMAC512(secretKey));

        when(request.getRequestURI()).thenReturn("/reports");
        when(request.getMethod()).thenReturn("POST");

        when(jwtService.extractAccessToken(request)).thenReturn(Optional.of(accessTokenWithoutEmailClaim));
        when(jwtService.isTokenValid(accessTokenWithoutEmailClaim)).thenReturn(true);

        // when
        when(jwtService.extractEmail(accessTokenWithoutEmailClaim)).thenReturn(Optional.empty());

        // then
        Assertions.assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(BadCredentialsException.class);
    }
}