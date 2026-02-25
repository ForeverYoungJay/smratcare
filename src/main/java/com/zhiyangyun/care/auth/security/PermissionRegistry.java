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
    ROLE_PERMISSIONS.put("ADMIN", List.of(
        "oa.calendar.view", "oa.calendar.manage", "oa.calendar.stats",
        "life.birthday.view", "life.birthday.stats",
        "life.cleaning.view", "life.cleaning.manage", "life.cleaning.stats",
        "life.maintenance.view", "life.maintenance.manage", "life.maintenance.stats",
        "card.account.view", "card.account.manage",
        "card.recharge.view", "card.recharge.create", "card.recharge.stats",
        "card.consume.view", "card.consume.create", "card.consume.stats"));
    ROLE_PERMISSIONS.put("STAFF", List.of(
        "oa.calendar.view", "oa.calendar.stats",
        "life.birthday.view", "life.birthday.stats",
        "life.cleaning.view", "life.cleaning.manage", "life.cleaning.stats",
        "life.maintenance.view", "life.maintenance.manage", "life.maintenance.stats",
        "card.account.view",
        "card.recharge.view", "card.recharge.create", "card.recharge.stats",
        "card.consume.view", "card.consume.create", "card.consume.stats"));
  }

  public List<String> getPermissionsByRoles(List<String> roles) {
    if (roles == null || roles.isEmpty()) {
      return List.of();
    }
    Set<String> permissions = new LinkedHashSet<>();
    for (String role : roles) {
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
