package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.auth.security.AuthContext;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthContextTest {
  @AfterEach
  void clearAuth() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void elevated_compat_admin_should_not_be_treated_as_real_admin_for_org_scope() {
    var auth = new UsernamePasswordAuthenticationToken(
        "7001",
        "N/A",
        List.of(
            new SimpleGrantedAuthority("ROLE_FINANCE_MINISTER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")));
    auth.setDetails(Map.of(
        "orgId", 1L,
        "username", "finance-minister",
        "roleCodes", List.of("FINANCE_MINISTER"),
        "grantedRoleCodes", List.of("FINANCE_MINISTER", "ADMIN")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    assertTrue(AuthContext.hasRole("FINANCE_MINISTER"));
    assertFalse(AuthContext.hasRole("ADMIN"));
    assertFalse(AuthContext.isAdmin());
    assertThrows(AccessDeniedException.class, () -> AuthContext.requireOrgAccess(2L));
  }
}
