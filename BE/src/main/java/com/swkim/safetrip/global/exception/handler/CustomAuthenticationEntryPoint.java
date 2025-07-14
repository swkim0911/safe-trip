package com.swkim.safetrip.global.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.global.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        int code = HttpStatus.UNAUTHORIZED.value();
        String message = authException.getMessage();
        String result = "Authentication Failed";

        log.warn("Unauthorized access attempt: {} {} - Reason: {}",
                request.getMethod(),
                request.getRequestURI(),
                message);

        ApiResponse<String> apiResponse = ApiResponse.of(code, message, result);

        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
