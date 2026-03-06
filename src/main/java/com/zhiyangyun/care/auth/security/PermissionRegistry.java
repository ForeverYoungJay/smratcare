package com.zhiyangyun.care.auth.security;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PermissionRegistry {
  private static final Map<String, List<String>> ROLE_PERMISSIONS = new LinkedHashMap<>();

  static {
    List<String> adminPermissions = List.of(
        "oa.calendar.view", "oa.calendar.manage", "oa.calendar.stats",
        "life.birthday.view", "life.birthday.stats",
        "life.cleaning.view", "life.cleaning.manage", "life.cleaning.stats",
        "life.maintenance.view", "life.maintenance.manage", "life.maintenance.stats",
        "card.account.view", "card.account.manage",
        "card.recharge.view", "card.recharge.create", "card.recharge.stats",
        "card.consume.view", "card.consume.create", "card.consume.stats");
    ROLE_PERMISSIONS.put("ADMIN", adminPermissions);
    ROLE_PERMISSIONS.put("SYS_ADMIN", adminPermissions);
    ROLE_PERMISSIONS.put("DIRECTOR", adminPermissions);
    List<String> staffPermissions = List.of(
        "oa.calendar.view", "oa.calendar.stats",
        "life.birthday.view", "life.birthday.stats",
        "life.cleaning.view", "life.cleaning.manage", "life.cleaning.stats",
        "life.maintenance.view", "life.maintenance.manage", "life.maintenance.stats",
        "card.account.view",
        "card.recharge.view", "card.recharge.create", "card.recharge.stats",
        "card.consume.view", "card.consume.create", "card.consume.stats");
    ROLE_PERMISSIONS.put("STAFF", staffPermissions);
    ROLE_PERMISSIONS.put("MEDICAL_EMPLOYEE", staffPermissions);
    ROLE_PERMISSIONS.put("NURSING_EMPLOYEE", staffPermissions);
    ROLE_PERMISSIONS.put("FINANCE_EMPLOYEE", staffPermissions);
    ROLE_PERMISSIONS.put("LOGISTICS_EMPLOYEE", staffPermissions);
    ROLE_PERMISSIONS.put("MARKETING_EMPLOYEE", staffPermissions);
    ROLE_PERMISSIONS.put("HR_EMPLOYEE", staffPermissions);
    ROLE_PERMISSIONS.put("MEDICAL_MINISTER", adminPermissions);
    ROLE_PERMISSIONS.put("NURSING_MINISTER", adminPermissions);
    ROLE_PERMISSIONS.put("FINANCE_MINISTER", adminPermissions);
    ROLE_PERMISSIONS.put("LOGISTICS_MINISTER", adminPermissions);
    ROLE_PERMISSIONS.put("MARKETING_MINISTER", adminPermissions);
    ROLE_PERMISSIONS.put("HR_MINISTER", adminPermissions);
  }

  public List<String> getPermissionsByRoles(List<String> roles) {
    List<String> normalizedRoles = RoleCodeHelper.normalizeRoles(roles);
    if (normalizedRoles.isEmpty()) {
      return List.of();
    }
    Set<String> permissions = new LinkedHashSet<>();
    for (String role : normalizedRoles) {
      if (role == null || role.isBlank()) {
        continue;
      }
      List<String> rolePermissions = ROLE_PERMISSIONS.get(role);
      if (rolePermissions != null) {
        permissions.addAll(rolePermissions);
      }
    }
    return new ArrayList<>(permissions);
  }
}
