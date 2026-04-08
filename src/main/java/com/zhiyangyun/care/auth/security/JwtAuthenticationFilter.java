package com.zhiyangyun.care.auth.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
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
          log.info("Rejected blacklisted token for path={}, jti={}", request.getRequestURI(), jti);
          SecurityContextHolder.clearContext();
          filterChain.doFilter(request, response);
          return;
        }
        String username = claims.get("username", String.class);
        String staffId = claims.getSubject();
        Long orgId = claims.get("orgId", Long.class);
        @SuppressWarnings("unchecked")
        List<String> roles = RoleCodeHelper.normalizeRoles(claims.get("roles", List.class));
        Set<String> authorityCodes = new LinkedHashSet<>(roles);
        if (shouldElevateToAdmin(request.getRequestURI(), authorityCodes)) {
          authorityCodes.add(RoleCodeHelper.ROLE_ADMIN);
        }
        List<SimpleGrantedAuthority> authorities = authorityCodes.stream()
            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(staffId, null, authorities);
        Map<String, Object> details = new HashMap<>();
        details.put("orgId", orgId);
        details.put("username", username);
        details.put("roleCodes", roles);
        details.put("grantedRoleCodes", authorityCodes.stream().collect(Collectors.toList()));
        authentication.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (Exception ex) {
        log.warn("Invalid token for path={}: {}", request.getRequestURI(), ex.getMessage());
        SecurityContextHolder.clearContext();
      }
    }
    filterChain.doFilter(request, response);
  }

  private boolean shouldElevateToAdmin(String requestUri, Set<String> roleCodes) {
    if (requestUri == null || requestUri.isBlank()) {
      return false;
    }
    if (roleCodes.contains(RoleCodeHelper.ROLE_ADMIN)
        || roleCodes.contains(RoleCodeHelper.ROLE_SYS_ADMIN)
        || roleCodes.contains(RoleCodeHelper.ROLE_DIRECTOR)) {
      return true;
    }
    if ((requestUri.startsWith("/api/admin/hr")
        || requestUri.startsWith("/api/hr")
        || requestUri.startsWith("/api/schedule")
        || requestUri.startsWith("/api/attendance"))
        && hasAnyRole(roleCodes, "HR_MINISTER")) {
      return true;
    }
    if ((requestUri.startsWith("/api/admin/staff")
        || requestUri.startsWith("/api/admin/staff-roles")
        || requestUri.startsWith("/api/admin/departments")
        || requestUri.startsWith("/api/admin/family"))
        && hasAnyRole(roleCodes, "HR_MINISTER")) {
      return true;
    }
    if (requestUri.startsWith("/api/assessment")
        && hasAnyRole(roleCodes, "MEDICAL_MINISTER", "NURSING_MINISTER")) {
      return true;
    }
    if ((requestUri.startsWith("/api/elder/lifecycle/medical-outing")
        || requestUri.startsWith("/api/elder/lifecycle/death-register"))
        && hasAnyRole(roleCodes, "MEDICAL_MINISTER", "NURSING_MINISTER")) {
      return true;
    }
    if ((requestUri.startsWith("/api/nursing") || requestUri.startsWith("/api/care"))
        && hasAnyRole(roleCodes, "NURSING_MINISTER")) {
      return true;
    }
    if (requestUri.startsWith("/api/finance")
        && hasAnyRole(roleCodes, "FINANCE_MINISTER")) {
      return true;
    }
    if ((requestUri.startsWith("/api/logistics")
        || requestUri.startsWith("/api/store")
        || requestUri.startsWith("/api/material")
        || requestUri.startsWith("/api/inventory")
        || requestUri.startsWith("/api/admin/product")
        || requestUri.startsWith("/api/admin/disease"))
        && hasAnyRole(roleCodes, "LOGISTICS_MINISTER")) {
      return true;
    }
    if ((requestUri.startsWith("/api/marketing") || requestUri.startsWith("/api/crm"))
        && hasAnyRole(roleCodes, "MARKETING_MINISTER")) {
      return true;
    }
    if ((requestUri.startsWith("/api/medical-care")
        || requestUri.startsWith("/api/health")
        || requestUri.startsWith("/api/medical"))
        && hasAnyRole(roleCodes, "MEDICAL_MINISTER")) {
      return true;
    }
    return false;
  }

  private boolean hasAnyRole(Set<String> roles, String... candidates) {
    if (roles == null || roles.isEmpty() || candidates == null) {
      return false;
    }
    for (String code : candidates) {
      if (code != null && roles.contains(code)) {
        return true;
      }
    }
    return false;
  }
}
