package com.zhiyangyun.care.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.Role;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component("perm")
public class PermissionChecker {
  private static final Map<String, List<String>> PERMISSION_ROUTE_FALLBACKS = new LinkedHashMap<>();

  static {
    PERMISSION_ROUTE_FALLBACKS.put("oa.calendar.view", List.of(
        "/oa/work-execution/task",
        "/oa/work-execution/calendar"
    ));
    PERMISSION_ROUTE_FALLBACKS.put("oa.calendar.manage", List.of(
        "/oa/work-execution/task",
        "/oa/work-execution/calendar"
    ));
    PERMISSION_ROUTE_FALLBACKS.put("oa.calendar.stats", List.of(
        "/oa/work-execution/task",
        "/oa/work-execution/calendar"
    ));
  }

  private final PermissionRegistry permissionRegistry;
  private final RoleMapper roleMapper;
  private final ObjectMapper objectMapper;

  public PermissionChecker(PermissionRegistry permissionRegistry, RoleMapper roleMapper, ObjectMapper objectMapper) {
    this.permissionRegistry = permissionRegistry;
    this.roleMapper = roleMapper;
    this.objectMapper = objectMapper;
  }

  public boolean has(String permissionCode) {
    if (permissionCode == null || permissionCode.isBlank()) {
      return false;
    }
    List<String> roleCodes = RoleCodeHelper.normalizeRoles(AuthContext.getRoleCodes());
    if (roleCodes.contains(RoleCodeHelper.ROLE_ADMIN)
        || roleCodes.contains(RoleCodeHelper.ROLE_SYS_ADMIN)) {
      return true;
    }
    if (permissionRegistry.getPermissionsByRoles(roleCodes).contains(permissionCode)) {
      return true;
    }
    return hasRoutePermissionFallback(permissionCode);
  }

  public boolean hasAny(String... permissionCodes) {
    if (permissionCodes == null || permissionCodes.length == 0) {
      return false;
    }
    for (String code : permissionCodes) {
      if (has(code)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasRoutePermissionFallback(String permissionCode) {
    List<String> candidateRoutes = PERMISSION_ROUTE_FALLBACKS.get(permissionCode);
    if (candidateRoutes == null || candidateRoutes.isEmpty()) {
      return false;
    }
    Long staffId = AuthContext.getStaffId();
    Long orgId = AuthContext.getOrgId();
    if (staffId == null || orgId == null) {
      return false;
    }
    List<Role> roles = roleMapper.selectRolesByStaff(staffId, orgId);
    List<String> pagePermissions = roles.stream()
        .filter(role -> role != null)
        .flatMap(role -> PagePermissionPathHelper.parseAndNormalize(objectMapper, role.getRoutePermissionsJson()).stream())
        .distinct()
        .toList();
    if (pagePermissions.isEmpty()) {
      return false;
    }
    return candidateRoutes.stream().anyMatch(route -> hasPagePermission(pagePermissions, route));
  }

  private boolean hasPagePermission(List<String> pagePermissions, String route) {
    String normalizedRoute = PagePermissionPathHelper.toCanonicalPath(route);
    return pagePermissions.stream()
        .map(PagePermissionPathHelper::toCanonicalPath)
        .anyMatch(permissionPath -> normalizedRoute.equals(permissionPath)
            || normalizedRoute.startsWith(permissionPath + "/"));
  }
}
