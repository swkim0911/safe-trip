package com.swkim.safetrip.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.global.exception.handler.CustomAuthenticationEntryPoint;
import com.swkim.safetrip.jwt.JwtProvider;
import com.swkim.safetrip.jwt.filter.JwtAuthenticationProcessingFilter;
import com.swkim.safetrip.security.ProtectedEndpoint;
import com.swkim.safetrip.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // 공개 엔드포인트
                    auth.requestMatchers("/error").permitAll()
                            .requestMatchers(HttpMethod.GET, contextPath + "/reports/**").permitAll()
                            .requestMatchers(contextPath + "/users", contextPath + "/auth/login", contextPath + "/auth/refresh").permitAll();

                    // 보호 엔드포인트
                    Arrays.stream(ProtectedEndpoint.values())
                            .forEach(ep -> auth
                                    .requestMatchers(HttpMethod.valueOf(ep.getMethod()), contextPath + ep.getPath())
                                    .authenticated()
                            );
                    // 나머지 기본 정책
                    auth.anyRequest().permitAll();
                }).exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint()));

        http.addFilterAfter(jwtAuthenticationProcessingFilter(), LogoutFilter.class); // LogoutFilter ➔ JwtAuthenticationProcessingFilter

        return http.build();
    }

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationProcessingFilter(jwtProvider, userService, customAuthenticationEntryPoint());
    }

    @Bean
    public CustomAuthenticationEntryPoint customAuthenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint(objectMapper);
    }
}
