package com.zhiyangyun.care.auth.security;

import java.util.List;
import org.springframework.stereotype.Component;

@Component("perm")
public class PermissionChecker {
  private final PermissionRegistry permissionRegistry;

  public PermissionChecker(PermissionRegistry permissionRegistry) {
    this.permissionRegistry = permissionRegistry;
  }

  public boolean has(String permissionCode) {
    if (permissionCode == null || permissionCode.isBlank()) {
      return false;
    }
    List<String> roleCodes = AuthContext.getRoleCodes();
    if (roleCodes.contains("ADMIN")) {
      return true;
    }
    return permissionRegistry.getPermissionsByRoles(roleCodes).contains(permissionCode);
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
}
