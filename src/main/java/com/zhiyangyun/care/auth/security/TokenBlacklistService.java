package com.zhiyangyun.care.auth.security;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {
  private static final String PREFIX = "jwt:blacklist:";
  private final StringRedisTemplate redisTemplate;
  private final JwtProperties jwtProperties;

  public TokenBlacklistService(StringRedisTemplate redisTemplate, JwtProperties jwtProperties) {
    this.redisTemplate = redisTemplate;
    this.jwtProperties = jwtProperties;
  }

  public void blacklist(String jti, long ttlMillis) {
    if (!jwtProperties.isBlacklistEnabled()) {
      return;
    }
    if (jti == null || jti.isBlank() || ttlMillis <= 0) {
      return;
    }
    try {
      redisTemplate.opsForValue().set(PREFIX + jti, "1", Duration.ofMillis(ttlMillis));
    } catch (Exception ignored) {
      // ignore redis errors
    }
  }

  public boolean isBlacklisted(String jti) {
    if (!jwtProperties.isBlacklistEnabled()) {
      return false;
    }
    if (jti == null || jti.isBlank()) {
      return false;
    }
    try {
      return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + jti));
    } catch (Exception ignored) {
      return false;
    }
  }
}
