package com.swkim.safetrip.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (status >= 500) {
                log.error("method={} uri={} status={} duration={}ms",
                        request.getMethod(), request.getRequestURI(), status, duration);
            } else if (status >= 400) {
                log.warn("method={} uri={} status={} duration={}ms",
                        request.getMethod(), request.getRequestURI(), status, duration);
            } else {
                log.info("method={} uri={} status={} duration={}ms",
                        request.getMethod(), request.getRequestURI(), status, duration);
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Actuator 헬스체크는 로그 제외 (노이즈 방지)
        return request.getRequestURI().startsWith("/actuator");
    }
}
