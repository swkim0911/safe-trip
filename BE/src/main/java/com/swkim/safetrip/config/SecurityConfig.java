package com.swkim.safetrip.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.global.exception.handler.CustomAuthenticationEntryPoint;
import com.swkim.safetrip.jwt.JwtService;
import com.swkim.safetrip.jwt.filter.JwtAuthenticationProcessingFilter;
import com.swkim.safetrip.login.filter.JsonUsernamePasswordAuthenticationFilter;
import com.swkim.safetrip.login.handler.LoginFailureHandler;
import com.swkim.safetrip.login.handler.LoginSuccessHandler;
import com.swkim.safetrip.login.service.LoginService;
import com.swkim.safetrip.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
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
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/reports/**").permitAll()
                        .requestMatchers("/users", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/reports").authenticated()
                        .anyRequest().authenticated()
                ).exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint()));

        http.addFilterAfter(jwtAuthenticationProcessingFilter(), LogoutFilter.class); // LogoutFilter ➔ JwtAuthenticationProcessingFilter

        return http.build();
    }

//    @Bean
//    public LoginSuccessHandler loginSuccessHandler() {
//        return new LoginSuccessHandler(jwtService);
//    }
//
//    @Bean
//    public LoginFailureHandler loginFailureHandler() {
//        return new LoginFailureHandler(objectMapper);
//    }

//    @Bean
//    public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordAuthenticationFilter() {
//        JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordAuthenticationFilter =
//                new JsonUsernamePasswordAuthenticationFilter(objectMapper);
//
//        jsonUsernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationManager());
//        jsonUsernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
//        jsonUsernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(loginFailureHandler());
//
//        return jsonUsernamePasswordAuthenticationFilter;
//    }

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationProcessingFilter(jwtService, userService);
    }

    @Bean
    public CustomAuthenticationEntryPoint customAuthenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint(objectMapper);
    }
}
