package com.zhiyangyun.care.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.Role;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

class RoleAuthorityIsolationTest {

  @Test
  void normalizeRolesKeepsAdministrativeRolesDistinct() {
    assertThat(RoleCodeHelper.normalizeRoles(List.of("SYS_ADMIN")))
        .containsExactly("SYS_ADMIN");
    assertThat(RoleCodeHelper.normalizeRoles(List.of("DIRECTOR")))
        .containsExactly("DIRECTOR");
    assertThat(RoleCodeHelper.normalizeRoles(List.of("ADMIN")))
        .containsExactly("ADMIN");
  }

  @Test
  void authenticationFilterKeepsPagePermissionsOutOfGrantedAuthorities() throws Exception {
    TokenProvider tokenProvider = mock(TokenProvider.class);
    TokenBlacklistService blacklistService = mock(TokenBlacklistService.class);
    RoleMapper roleMapper = mock(RoleMapper.class);
    Claims claims = mock(Claims.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    Role role = new Role();
    role.setRoutePermissionsJson("[\"/system/role\"]");

    when(request.getHeader("Authorization")).thenReturn("Bearer token");
    when(request.getRequestURI()).thenReturn("/api/admin/roles");
    when(tokenProvider.parseToken("token")).thenReturn(claims);
    when(claims.get(TokenProvider.CLAIM_TOKEN_TYPE, String.class)).thenReturn(null);
    when(claims.getId()).thenReturn("jti");
    when(claims.getSubject()).thenReturn("42");
    when(claims.get("username", String.class)).thenReturn("employee");
    when(claims.get("orgId", Long.class)).thenReturn(1L);
    when(claims.get("roles", List.class)).thenReturn(List.of("MARKETING_EMPLOYEE"));
    when(blacklistService.isBlacklisted("jti")).thenReturn(false);
    when(roleMapper.selectRolesByStaff(42L, 1L)).thenReturn(List.of(role));

    try {
      new JwtAuthenticationFilter(tokenProvider, blacklistService, roleMapper, new ObjectMapper())
          .doFilterInternal(request, response, chain);
      assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
          .extracting(Object::toString)
          .contains("ROLE_MARKETING_EMPLOYEE", "ROLE_STAFF")
          .doesNotContain("ROLE_ADMIN", "ROLE_SYS_ADMIN", "ROLE_DIRECTOR");
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  @Test
  void departmentEmployeeIsNotPromotedToMinisterOrAnotherDepartment() {
    assertThat(RoleCodeHelper.normalizeRoles(List.of("NURSING_EMPLOYEE")))
        .contains("NURSING_EMPLOYEE", "STAFF")
        .doesNotContain("NURSING_MINISTER", "MEDICAL_EMPLOYEE", "ADMIN");
  }

  @Test
  void rolePagePresetsKeepAdministrativePurposesSeparate() {
    assertThat(RolePagePermissionPresetHelper.getRecommendedPaths("DIRECTOR", null))
        .contains("/portal", "/stats")
        .doesNotContain("/system", "/base-config");
    assertThat(RolePagePermissionPresetHelper.getRecommendedPaths("SYS_ADMIN", null))
        .contains("/system", "/base-config")
        .doesNotContain("/portal", "/medical-care", "/finance");
    assertThat(RolePagePermissionPresetHelper.getRecommendedPaths("ADMIN", null))
        .contains("/portal", "/base-config")
        .doesNotContain("/system");
    assertThat(RolePagePermissionPresetHelper.getRecommendedPaths("NURSING_EMPLOYEE", null))
        .contains("/workbench/overview", "/care")
        .doesNotContain("/portal", "/finance", "/system");
  }

  @Test
  void databaseLegacyPermissionPathResolvesToCanonicalPath() {
    assertThat(PagePermissionPathHelper.parseAndNormalize(
        new ObjectMapper(), "[\"/oa/todo\",\"/system/dict\"]"))
        .containsExactly("/workbench/todo", "/base-config");
  }

  @Test
  void legacyStaffGetsOnlyThePersonalWorkbenchPreset() {
    assertThat(RolePagePermissionPresetHelper.getRecommendedPaths("STAFF", null))
        .contains("/workbench", "/workbench/todo", "/workbench/my-info")
        .doesNotContain("/portal", "/system", "/base-config");
  }
}
