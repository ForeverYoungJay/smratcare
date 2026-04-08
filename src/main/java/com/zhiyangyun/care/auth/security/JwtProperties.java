package com.zhiyangyun.care.auth.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
  @NotBlank(message = "security.jwt.secret 不能为空")
  private String secret;
  @Min(value = 1, message = "security.jwt.expiration-minutes 必须大于 0")
  private long expirationMinutes = 120;
  private boolean blacklistEnabled = true;
}
