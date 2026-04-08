package com.zhiyangyun.care.auth.config;

import com.zhiyangyun.care.auth.security.JwtProperties;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SecurityStartupValidator implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(SecurityStartupValidator.class);
  private static final String DEFAULT_SECRET = "change-me-please-change-me-please-change-me";

  private final JwtProperties jwtProperties;
  private final Environment environment;

  public SecurityStartupValidator(JwtProperties jwtProperties, Environment environment) {
    this.jwtProperties = jwtProperties;
    this.environment = environment;
  }

  @Override
  public void run(ApplicationArguments args) {
    validateJwtSecret();
  }

  private void validateJwtSecret() {
    String secret = jwtProperties.getSecret() == null ? "" : jwtProperties.getSecret().trim();
    boolean prodProfile = Arrays.stream(environment.getActiveProfiles())
        .anyMatch(profile -> "prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile));
    if (secret.length() < 32) {
      String message = "security.jwt.secret 长度过短，至少需要 32 个字符";
      if (prodProfile) {
        throw new IllegalStateException(message);
      }
      log.warn(message);
    }
    if (DEFAULT_SECRET.equals(secret)) {
      String message = "security.jwt.secret 仍在使用默认值，请在部署前替换";
      if (prodProfile) {
        throw new IllegalStateException(message);
      }
      log.warn(message);
    }
  }
}
