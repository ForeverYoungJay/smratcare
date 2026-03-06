package com.zhiyangyun.care.auth.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class RoleCodeHelper {
  public static final String ROLE_ADMIN = "ADMIN";
  public static final String ROLE_SYS_ADMIN = "SYS_ADMIN";
  public static final String ROLE_DIRECTOR = "DIRECTOR";
  public static final String ROLE_STAFF = "STAFF";
  public static final String ROLE_MARKETING_EMPLOYEE = "MARKETING_EMPLOYEE";
  public static final String ROLE_MARKETING_MINISTER = "MARKETING_MINISTER";
  public static final String LEGACY_ROLE_OPERATOR = "OPERATOR";
  public static final String LEGACY_ROLE_MANAGER = "MANAGER";
  private static final Set<String> DEPARTMENT_EMPLOYEE_ROLES =
      Set.of("MEDICAL_EMPLOYEE", "NURSING_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE",
          "MARKETING_EMPLOYEE", "HR_EMPLOYEE");
  private static final Set<String> DEPARTMENT_MINISTER_ROLES =
      Set.of("MEDICAL_MINISTER", "NURSING_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER",
          "MARKETING_MINISTER", "HR_MINISTER");
  private static final Set<String> DEPARTMENT_ROLES;

  static {
    Set<String> roles = new HashSet<>(DEPARTMENT_EMPLOYEE_ROLES);
    roles.addAll(DEPARTMENT_MINISTER_ROLES);
    DEPARTMENT_ROLES = Collections.unmodifiableSet(roles);
  }

  private RoleCodeHelper() {}

  public static List<String> normalizeRoles(List<String> roles) {
    Set<String> normalized = new LinkedHashSet<>();
    if (roles != null) {
      for (String role : roles) {
        if (role == null || role.isBlank()) {
          continue;
        }
        normalized.add(role.trim().toUpperCase());
      }
    }
    if (normalized.contains(ROLE_SYS_ADMIN) || normalized.contains(ROLE_DIRECTOR)) {
      normalized.add(ROLE_ADMIN);
    }
    if (normalized.contains(LEGACY_ROLE_OPERATOR)) {
      normalized.add(ROLE_MARKETING_EMPLOYEE);
    }
    if (normalized.contains(LEGACY_ROLE_MANAGER)) {
      normalized.add(ROLE_MARKETING_MINISTER);
    }
    if (normalized.stream().anyMatch(RoleCodeHelper::isDepartmentRole)) {
      normalized.add(ROLE_STAFF);
    }
    return new ArrayList<>(normalized);
  }

  public static boolean isDepartmentRole(String roleCode) {
    if (roleCode == null || roleCode.isBlank()) {
      return false;
    }
    return DEPARTMENT_ROLES.contains(roleCode.trim().toUpperCase());
  }

  public static boolean isDepartmentMinisterRole(String roleCode) {
    if (roleCode == null || roleCode.isBlank()) {
      return false;
    }
    return DEPARTMENT_MINISTER_ROLES.contains(roleCode.trim().toUpperCase());
  }
}
