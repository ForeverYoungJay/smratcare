package com.zhiyangyun.care.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TokenProvider {
  /** 令牌类型声明：2FA 挑战令牌（不可作为正式访问令牌使用）。 */
  public static final String CLAIM_TOKEN_TYPE = "tokenType";
  public static final String TOKEN_TYPE_2FA_CHALLENGE = "2FA_CHALLENGE";
  /** 2FA 挑战令牌有效期（分钟）。 */
  public static final long TWO_FACTOR_CHALLENGE_MINUTES = 5;

  private final JwtProperties jwtProperties;
  private final Key key;

  public TokenProvider(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(Long staffId, String username, Long orgId, List<String> roles) {
    long now = System.currentTimeMillis();
    long expMillis = now + jwtProperties.getExpirationMinutes() * 60 * 1000;
    return Jwts.builder()
        .setSubject(String.valueOf(staffId))
        .setId(UUID.randomUUID().toString())
        .claim("username", username)
        .claim("orgId", orgId)
        .claim("roles", roles)
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(expMillis))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /** 生成正式令牌，允许按机构安全策略覆盖会话超时分钟数（<=0 时使用全局默认）。 */
  public String generateToken(Long staffId, String username, Long orgId, List<String> roles,
      long expirationMinutesOverride) {
    long minutes = expirationMinutesOverride > 0 ? expirationMinutesOverride : jwtProperties.getExpirationMinutes();
    long now = System.currentTimeMillis();
    long expMillis = now + minutes * 60 * 1000;
    return Jwts.builder()
        .setSubject(String.valueOf(staffId))
        .setId(UUID.randomUUID().toString())
        .claim("username", username)
        .claim("orgId", orgId)
        .claim("roles", roles)
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(expMillis))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /** 生成 2FA 挑战令牌：仅用于换取短信验证码校验，不能作为访问令牌（JwtAuthenticationFilter 会拒绝）。 */
  public String generateChallengeToken(Long staffId, String username, Long orgId) {
    long now = System.currentTimeMillis();
    long expMillis = now + TWO_FACTOR_CHALLENGE_MINUTES * 60 * 1000;
    return Jwts.builder()
        .setSubject(String.valueOf(staffId))
        .setId(UUID.randomUUID().toString())
        .claim("username", username)
        .claim("orgId", orgId)
        .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_2FA_CHALLENGE)
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(expMillis))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Claims parseToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public long getExpirationMillis(Claims claims) {
    if (claims == null || claims.getExpiration() == null) {
      return 0;
    }
    return claims.getExpiration().getTime();
  }
}
