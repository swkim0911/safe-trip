package com.swkim.safetrip.jwt.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.entity.enums.Role;
import com.swkim.safetrip.global.exception.custom.AccessTokenMissingException;
import com.swkim.safetrip.global.exception.handler.CustomAuthenticationEntryPoint;
import com.swkim.safetrip.jwt.JwtUtils;
import com.swkim.safetrip.security.CustomUserDetails;
import com.swkim.safetrip.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationProcessingFilterTest {

    @InjectMocks
    JwtAuthenticationProcessingFilter filter;

    @Mock
    JwtUtils jwtUtils;

    @Mock
    UserService userService;

    @Mock
    FilterChain filterChain;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    CustomAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 글_등록_요청이_아니라면_requiresAuthentication_false로_JWT_검증이_실행되지_않는다() throws Exception {
        // given
        when(request.getRequestURI()).thenReturn("/reports/1");
        when(request.getMethod()).thenReturn("GET");

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then (jwtService가 호출되지 않았고, filterChain이 동작하는지 확인)
        verify(jwtUtils, never()).extractAccessToken(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void 글_등록_요청에_액세스_토큰이_없다면_entryPoint가_호출된다() throws ServletException, IOException {
        // given
        given(request.getRequestURI()).willReturn("/reports");
        given(request.getMethod()).willReturn("POST");
        given(jwtUtils.extractAccessToken(request)).willReturn(Optional.empty());

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(entryPoint).commence(any(),any(), argThat(ex -> ex instanceof AccessTokenMissingException));
    }

    @Test
    void valid_액세스_토큰이_이메일_클레임이_있고_이메일로부터_유저를_찾을_수_있을_때_Authentication을_저장한다() throws ServletException, IOException {
        // given
        String secretKey = "eijnv329asic";
        String email = "test@gmail.com";

        String accessToken = JWT.create()
                .withSubject("AccessToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .withClaim("email", email)
                .sign(Algorithm.HMAC512(secretKey));

        DecodedJWT decodedJWT = mock(DecodedJWT.class);

        given(request.getRequestURI()).willReturn("/reports");
        given(request.getMethod()).willReturn("POST");

        given(jwtUtils.extractAccessToken(request)).willReturn(Optional.of(accessToken));
        given(jwtUtils.verifyAccessToken(eq(accessToken))).willReturn(decodedJWT);
        given(jwtUtils.extractEmail(eq(decodedJWT))).willReturn(Optional.of(email));

        User user = User.builder()
                .email(email)
                .password("password")
                .role(Role.USER)
                .build();

        given(userService.findUserByEmail(email)).willReturn(Optional.of(user));

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assertions.assertThat(authentication).isNotNull();
        Assertions.assertThat(((CustomUserDetails) authentication.getPrincipal()).getUsername()).isEqualTo(email);
    }

    @Test
    void 액세스_토큰이_valid_하지만_이메일_클레임이_없을_때_예외가_발생한다() throws ServletException, IOException {
        // given
        String secretKey = "eijnv329asic";

        String accessTokenWithoutEmailClaim = JWT.create()
                .withSubject("AccessToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .sign(Algorithm.HMAC512(secretKey));

        DecodedJWT decodedJWT = mock(DecodedJWT.class);

        given(request.getRequestURI()).willReturn("/reports");
        given(request.getMethod()).willReturn("POST");

        given(jwtUtils.extractAccessToken(request)).willReturn(Optional.of(accessTokenWithoutEmailClaim));
        given(jwtUtils.verifyAccessToken(eq(accessTokenWithoutEmailClaim))).willReturn(decodedJWT);
        given(jwtUtils.extractEmail(eq(decodedJWT))).willReturn(Optional.empty());

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(entryPoint).commence(eq(request), eq(response), argThat(ex -> ex instanceof BadCredentialsException));
    }

    @Test
    void valid_액세스_토큰이_이메일_클레임이_있지만_이메일로부터_유저를_찾을_수_없을_때_예외가_발생한다() throws ServletException, IOException {
        // given
        String secretKey = "eijnv329asic";
        String email = "test@gmail.com";

        String accessToken = JWT.create()
                .withSubject("AccessToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .withClaim("email", email)
                .sign(Algorithm.HMAC512(secretKey));

        DecodedJWT decodedJWT = mock(DecodedJWT.class);

        given(request.getRequestURI()).willReturn("/reports");
        given(request.getMethod()).willReturn("POST");

        given(jwtUtils.extractAccessToken(request)).willReturn(Optional.of(accessToken));
        given(jwtUtils.verifyAccessToken(eq(accessToken))).willReturn(decodedJWT);
        given(jwtUtils.extractEmail(eq(decodedJWT))).willReturn(Optional.of(email));
        given(userService.findUserByEmail(email)).willThrow(UsernameNotFoundException.class);

        doNothing().when(entryPoint).commence(any(), any(), any());

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(entryPoint).commence(eq(request), eq(response), argThat(ex -> ex instanceof UsernameNotFoundException));
    }
}