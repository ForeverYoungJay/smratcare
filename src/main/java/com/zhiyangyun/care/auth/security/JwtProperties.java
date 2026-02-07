package com.zhiyangyun.care.auth.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
  private String secret;
  private long expirationMinutes = 120;
  private boolean blacklistEnabled = true;
}
