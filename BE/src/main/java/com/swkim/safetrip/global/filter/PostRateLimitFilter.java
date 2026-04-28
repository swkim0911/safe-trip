package com.swkim.safetrip.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.global.response.ApiResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class PostRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 20;
    private static final int WINDOW_SECONDS = 60;
    private static final String KEY_PREFIX = "rate_limit:mutate:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!isMutatingMethod(request.getMethod()) || isExcluded(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = resolveClientIp(request);
        String key = KEY_PREFIX + ip;

        var ops = redisTemplate.opsForValue();
        if (ops == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Long count = ops.increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        if (count != null && count > MAX_REQUESTS) {
            log.warn("Rate limit exceeded: ip={}, count={}", ip, count);
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            ApiResult<Void> result = ApiResult.of(429, "Too many requests. Please try again later.", null);
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isMutatingMethod(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    // 토큰 갱신/로그아웃은 자동으로 자주 발생하므로 제외
    private boolean isExcluded(String uri) {
        return uri.contains("/auth/refresh") || uri.contains("/auth/logout");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
