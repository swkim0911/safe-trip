package com.swkim.safetrip.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String email, String refreshToken, long expirationMillis) {
        long expirationSeconds = expirationMillis / 1000;
        redisTemplate.opsForValue().set("refresh:" + email, refreshToken, expirationSeconds, TimeUnit.SECONDS);
    }

    public void delete(String refreshToken, String email) {
        redisTemplate.delete("refresh:" + email);
    }

}
