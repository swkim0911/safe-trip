package com.swkim.safetrip.global.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.global.exception.GeneralAuthenticationException;
import com.swkim.safetrip.global.response.ApiResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        final int errorCode = (authException instanceof GeneralAuthenticationException)
                ? ((GeneralAuthenticationException) authException).getError().getStatusCode()
                : HttpStatus.UNAUTHORIZED.value();

        final String message = authException.getMessage();
        final String data = "Authentication Failed";

        log.warn("Unauthorized access attempt: {} {} - Reason: {}",
                request.getMethod(),
                request.getRequestURI(),
                message);

        ApiResult<String> apiResult = ApiResult.of(errorCode, message, data);

        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // HTTP 상태는 401로 고정
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResult));
    }
}
