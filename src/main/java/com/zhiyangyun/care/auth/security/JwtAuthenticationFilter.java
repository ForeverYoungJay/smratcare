package com.zhiyangyun.care.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.Role;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
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
  private final RoleMapper roleMapper;
  private final ObjectMapper objectMapper;

  public JwtAuthenticationFilter(
      TokenProvider tokenProvider,
      TokenBlacklistService tokenBlacklistService,
      RoleMapper roleMapper,
      ObjectMapper objectMapper) {
    this.tokenProvider = tokenProvider;
    this.tokenBlacklistService = tokenBlacklistService;
    this.roleMapper = roleMapper;
    this.objectMapper = objectMapper;
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
        // 2FA 挑战令牌仅用于第二步短信验证码校验，不能作为访问令牌
        if (TokenProvider.TOKEN_TYPE_2FA_CHALLENGE.equals(claims.get(TokenProvider.CLAIM_TOKEN_TYPE, String.class))) {
          log.info("Rejected 2FA challenge token used as access token for path={}", request.getRequestURI());
          SecurityContextHolder.clearContext();
          filterChain.doFilter(request, response);
          return;
        }
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
        List<String> pagePermissions = loadPagePermissions(staffId, orgId);
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
        details.put("pagePermissions", pagePermissions);
        authentication.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (Exception ex) {
        log.warn("Invalid token for path={}: {}", request.getRequestURI(), ex.getMessage());
        SecurityContextHolder.clearContext();
      }
    }
    filterChain.doFilter(request, response);
  }

  private List<String> loadPagePermissions(String staffId, Long orgId) {
    if (!StringUtils.hasText(staffId) || orgId == null) {
      return List.of();
    }
    try {
      Long resolvedStaffId = Long.valueOf(staffId);
      return roleMapper.selectRolesByStaff(resolvedStaffId, orgId).stream()
          .filter(role -> role != null)
          .map(Role::getRoutePermissionsJson)
          .flatMap(value -> PagePermissionPathHelper.parseAndNormalize(objectMapper, value).stream())
          .distinct()
          .toList();
    } catch (Exception ignored) {
      return List.of();
    }
  }

}
