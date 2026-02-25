package com.zhiyangyun.care.auth.security;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthContext {
  private AuthContext() {}

  public static Long getStaffId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getPrincipal() == null) {
      return null;
    }
    String principal = authentication.getName();
    if (principal == null) {
      return null;
    }
    try {
      return Long.valueOf(principal);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  public static Long getOrgId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return null;
    }
    Object details = authentication.getDetails();
    if (details instanceof java.util.Map) {
      java.util.Map map = (java.util.Map) details;
      Object orgId = map.get("orgId");
      if (orgId instanceof Number) {
        return ((Number) orgId).longValue();
      }
    }
    return null;
  }

  public static String getUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return null;
    }
    Object details = authentication.getDetails();
    if (details instanceof java.util.Map) {
      java.util.Map map = (java.util.Map) details;
      Object username = map.get("username");
      return username == null ? null : username.toString();
    }
    return null;
  }

  public static boolean hasRole(String roleCode) {
    if (roleCode == null || roleCode.isBlank()) {
      return false;
    }
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getAuthorities() == null) {
      return false;
    }
    String normalized = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;
    return authentication.getAuthorities().stream()
        .anyMatch(item -> normalized.equalsIgnoreCase(item.getAuthority()));
  }

  public static boolean isAdmin() {
    return hasRole("ADMIN");
  }

  public static List<String> getRoleCodes() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getAuthorities() == null) {
      return List.of();
    }
    return authentication.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .map(auth -> auth != null && auth.startsWith("ROLE_") ? auth.substring(5) : auth)
        .filter(auth -> auth != null && !auth.isBlank())
        .collect(Collectors.toList());
  }
}
