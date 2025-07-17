package com.swkim.safetrip.service;

import com.swkim.safetrip.global.exception.custom.TokenHashingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String email, String refreshToken, long expirationMillis) {
        redisTemplate.opsForValue().set("refresh:" + email, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);
    }

    public void blacklistRefreshToken(String refreshToken, long ttl) {
        if (ttl > 0) {
            String key = getBlacklistKey(refreshToken);
            redisTemplate.opsForValue().set(
                    key,
                    "expired",
                    ttl,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public boolean isRefreshTokenBlacklisted(String refreshToken) {
        String key = getBlacklistKey(refreshToken);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get("refresh:" + email);
    }

    public void deleteRefreshToken(String email) {
        redisTemplate.delete("refresh:" + email);
    }

    private String getBlacklistKey(String token) {
        return "blacklist:refresh:" + hashToken(token);
    }

    // SHA-256 + Base64 인코딩 (JWT가 너무 길어서 Redis 키가 잘리지않게)
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new TokenHashingException();
        }
    }
}
