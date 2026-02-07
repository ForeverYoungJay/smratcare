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
