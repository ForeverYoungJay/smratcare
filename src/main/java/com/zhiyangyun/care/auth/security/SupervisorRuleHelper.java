package com.zhiyangyun.care.auth.security;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SupervisorRuleHelper {
  private static final Set<String> DEPARTMENT_EMPLOYEE_ROLES = Set.of(
      "MEDICAL_EMPLOYEE", "NURSING_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "MARKETING_EMPLOYEE", "HR_EMPLOYEE");
  private static final Set<String> DEPARTMENT_MINISTER_ROLES = Set.of(
      "MEDICAL_MINISTER", "NURSING_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "MARKETING_MINISTER", "HR_MINISTER");
  private static final Set<String> SUPER_MANAGER_ROLES = Set.of("ADMIN", "SYS_ADMIN", "DIRECTOR", "DEAN");

  private SupervisorRuleHelper() {}

  public enum Level {
    EMPLOYEE,
    MINISTER,
    SUPER,
    UNKNOWN
  }

  public static List<String> normalizeRoleCodes(List<String> roleCodes) {
    return RoleCodeHelper.normalizeRoles(roleCodes);
  }

  public static boolean isDepartmentEmployee(List<String> roleCodes) {
    Set<String> set = roleSet(roleCodes);
    return DEPARTMENT_EMPLOYEE_ROLES.stream().anyMatch(set::contains);
  }

  public static boolean isDepartmentMinister(List<String> roleCodes) {
    Set<String> set = roleSet(roleCodes);
    return DEPARTMENT_MINISTER_ROLES.stream().anyMatch(set::contains);
  }

  public static boolean isSuperManager(List<String> roleCodes) {
    Set<String> set = roleSet(roleCodes);
    return SUPER_MANAGER_ROLES.stream().anyMatch(set::contains);
  }

  public static Level resolveLevel(List<String> roleCodes) {
    if (isSuperManager(roleCodes)) {
      return Level.SUPER;
    }
    if (isDepartmentMinister(roleCodes)) {
      return Level.MINISTER;
    }
    if (isDepartmentEmployee(roleCodes)) {
      return Level.EMPLOYEE;
    }
    return Level.UNKNOWN;
  }

  public static boolean canBeDirectLeader(
      Level targetLevel,
      Long targetDepartmentId,
      Long candidateDepartmentId,
      List<String> candidateRoleCodes) {
    if (targetLevel == Level.EMPLOYEE) {
      return isDepartmentMinister(candidateRoleCodes)
          && targetDepartmentId != null
          && targetDepartmentId.equals(candidateDepartmentId);
    }
    if (targetLevel == Level.MINISTER || targetLevel == Level.SUPER) {
      return isSuperManager(candidateRoleCodes);
    }
    if (isDepartmentMinister(candidateRoleCodes)
        && targetDepartmentId != null
        && targetDepartmentId.equals(candidateDepartmentId)) {
      return true;
    }
    return isSuperManager(candidateRoleCodes);
  }

  public static boolean canBeIndirectLeader(Level targetLevel, List<String> candidateRoleCodes) {
    if (targetLevel == Level.EMPLOYEE || targetLevel == Level.MINISTER || targetLevel == Level.SUPER) {
      return isSuperManager(candidateRoleCodes);
    }
    return isSuperManager(candidateRoleCodes);
  }

  private static Set<String> roleSet(List<String> roleCodes) {
    Set<String> set = new HashSet<>();
    if (roleCodes == null) {
      return set;
    }
    for (String roleCode : roleCodes) {
      if (roleCode == null || roleCode.isBlank()) {
        continue;
      }
      set.add(roleCode.trim().toUpperCase(Locale.ROOT));
    }
    return set;
  }
}
