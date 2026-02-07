package com.zhiyangyun.care.auth.security;

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
}
