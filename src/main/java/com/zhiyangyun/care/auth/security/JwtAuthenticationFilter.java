package com.zhiyangyun.care.auth.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final TokenProvider tokenProvider;
  private final TokenBlacklistService tokenBlacklistService;

  public JwtAuthenticationFilter(TokenProvider tokenProvider, TokenBlacklistService tokenBlacklistService) {
    this.tokenProvider = tokenProvider;
    this.tokenBlacklistService = tokenBlacklistService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      try {
        Claims claims = tokenProvider.parseToken(token);
        String jti = claims.getId();
        if (tokenBlacklistService.isBlacklisted(jti)) {
          SecurityContextHolder.clearContext();
          filterChain.doFilter(request, response);
          return;
        }
        String username = claims.get("username", String.class);
        String staffId = claims.getSubject();
        Long orgId = claims.get("orgId", Long.class);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        List<SimpleGrantedAuthority> authorities = roles == null ? List.of() : roles.stream()
            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(staffId, null, authorities);
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        details.put("orgId", orgId);
        details.put("username", username);
        authentication.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (Exception ignored) {
        SecurityContextHolder.clearContext();
      }
    }
    filterChain.doFilter(request, response);
  }
}
