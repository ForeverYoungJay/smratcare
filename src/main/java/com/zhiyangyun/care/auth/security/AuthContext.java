package com.zhiyangyun.care.auth.security;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
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
    Map<?, ?> details = extractDetails(authentication);
    if (details != null) {
      Object orgId = details.get("orgId");
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
    Map<?, ?> details = extractDetails(authentication);
    if (details != null) {
      Object username = details.get("username");
      return username == null ? null : username.toString();
    }
    return null;
  }

  public static boolean hasRole(String roleCode) {
    if (roleCode == null || roleCode.isBlank()) {
      return false;
    }
    String normalized = roleCode.startsWith("ROLE_") ? roleCode.substring(5) : roleCode;
    return getRoleCodes().stream().anyMatch(item -> normalized.equalsIgnoreCase(item));
  }

  public static boolean isAdmin() {
    return hasRole("ADMIN") || hasRole("SYS_ADMIN") || hasRole("DIRECTOR");
  }

  public static Long resolveAccessibleOrgId(Long requestedOrgId) {
    Long currentOrgId = getOrgId();
    boolean admin = isAdmin();

    if (requestedOrgId == null) {
      if (currentOrgId != null) {
        return currentOrgId;
      }
      if (admin) {
        throw new IllegalArgumentException("Admin request requires orgId");
      }
      throw new IllegalArgumentException("Unable to determine orgId from token");
    }

    if (admin) {
      return requestedOrgId;
    }
    if (currentOrgId == null || !requestedOrgId.equals(currentOrgId)) {
      throw new AccessDeniedException("No permission to access another organization");
    }
    return requestedOrgId;
  }

  public static void requireOrgAccess(Long targetOrgId) {
    if (targetOrgId == null) {
      throw new IllegalArgumentException("Target orgId is required");
    }
    resolveAccessibleOrgId(targetOrgId);
  }

  public static List<String> getRoleCodes() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return List.of();
    }
    Map<?, ?> details = extractDetails(authentication);
    if (details != null) {
      Object grantedRoleCodes = details.get("grantedRoleCodes");
      List<String> granted = normalizeRoleCodes(grantedRoleCodes);
      if (!granted.isEmpty()) {
        return granted;
      }
      Object roleCodes = details.get("roleCodes");
      List<String> normalized = normalizeRoleCodes(roleCodes);
      if (!normalized.isEmpty()) {
        return normalized;
      }
    }
    if (authentication.getAuthorities() == null) {
      return List.of();
    }
    return authentication.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .map(auth -> auth != null && auth.startsWith("ROLE_") ? auth.substring(5) : auth)
        .filter(auth -> auth != null && !auth.isBlank())
        .collect(Collectors.toList());
  }

  public static boolean isMinisterOrHigher() {
    if (isAdmin()) {
      return true;
    }
    List<String> roleCodes = getRoleCodes();
    return hasLegacyManagerRole(roleCodes)
        || roleCodes.stream()
        .map(item -> item == null ? "" : item.toUpperCase(Locale.ROOT))
        .anyMatch(item -> item.endsWith("_MINISTER"));
  }

  private static boolean hasLegacyManagerRole(List<String> roleCodes) {
    if (roleCodes == null || roleCodes.isEmpty()) {
      return false;
    }
    return roleCodes.contains("DEPT_LEADER")
        || roleCodes.contains("LEADER")
        || roleCodes.contains("MANAGER")
        || roleCodes.contains("SUPERVISOR");
  }

  private static Map<?, ?> extractDetails(Authentication authentication) {
    if (authentication == null) {
      return null;
    }
    Object details = authentication.getDetails();
    if (details instanceof Map<?, ?> map) {
      return map;
    }
    return null;
  }

  private static List<String> normalizeRoleCodes(Object roleCodes) {
    if (!(roleCodes instanceof List<?> values)) {
      return List.of();
    }
    return values.stream()
        .filter(Objects::nonNull)
        .map(Object::toString)
        .filter(value -> value != null && !value.isBlank())
        .map(value -> value.toUpperCase(Locale.ROOT))
        .collect(Collectors.toList());
  }
}
