package com.zhiyangyun.care.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.Role;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class RolePagePermissionPresetHelper {
  private static final List<String> COMMON_PERSONAL_PATHS = List.of(
      "/portal",
      "/workbench",
      "/workbench/overview",
      "/workbench/todo",
      "/workbench/my-info",
      "/workbench/attendance",
      "/workbench/reports",
      "/workbench/approvals");

  private static final List<String> ADMIN_MANAGEMENT_PATHS = List.of(
      "/elder",
      "/medical-care",
      "/care",
      "/finance",
      "/logistics",
      "/marketing",
      "/hr",
      "/oa",
      "/stats",
      "/system",
      "/base-config");

  private static final Map<String, List<String>> ROLE_PAGE_PRESETS = Map.ofEntries(
      Map.entry("NURSING_MINISTER", concat(COMMON_PERSONAL_PATHS, "/medical-care", "/elder", "/oa/approval", "/base-config")),
      Map.entry("NURSING_EMPLOYEE", concat(COMMON_PERSONAL_PATHS, "/medical-care", "/oa/approval")),
      Map.entry("HR_MINISTER", concat(COMMON_PERSONAL_PATHS, "/hr", "/oa", "/stats", "/system/site-config", "/system/role", "/base-config")),
      Map.entry("HR_EMPLOYEE", concat(COMMON_PERSONAL_PATHS, "/hr", "/oa", "/stats", "/base-config")),
      Map.entry("LOGISTICS_MINISTER", concat(COMMON_PERSONAL_PATHS, "/logistics", "/fire")),
      Map.entry("LOGISTICS_EMPLOYEE", concat(COMMON_PERSONAL_PATHS, "/logistics")),
      Map.entry("GUARD", concat(COMMON_PERSONAL_PATHS, "/fire")),
      Map.entry("MEDICAL_MINISTER", concat(COMMON_PERSONAL_PATHS, "/medical-care", "/elder", "/oa/approval")),
      Map.entry("MEDICAL_EMPLOYEE", concat(COMMON_PERSONAL_PATHS, "/medical-care", "/elder")),
      Map.entry("FINANCE_MINISTER", concat(COMMON_PERSONAL_PATHS, "/finance", "/oa/approval", "/stats")),
      Map.entry("FINANCE_EMPLOYEE", concat(COMMON_PERSONAL_PATHS, "/finance", "/oa/approval")),
      Map.entry("MARKETING_MINISTER", concat(COMMON_PERSONAL_PATHS, "/marketing", "/stats")),
      Map.entry("MARKETING_EMPLOYEE", concat(COMMON_PERSONAL_PATHS, "/marketing")),
      Map.entry("ADMIN", concat(COMMON_PERSONAL_PATHS, ADMIN_MANAGEMENT_PATHS)),
      Map.entry("DIRECTOR", concat(COMMON_PERSONAL_PATHS, ADMIN_MANAGEMENT_PATHS)),
      Map.entry("SYS_ADMIN", concat(COMMON_PERSONAL_PATHS, ADMIN_MANAGEMENT_PATHS))
  );

  private RolePagePermissionPresetHelper() {
  }

  public static List<String> resolveEffectivePaths(ObjectMapper objectMapper, Role role) {
    if (role == null) {
      return List.of();
    }
    List<String> explicitPaths = PagePermissionPathHelper.parseAndNormalize(objectMapper, role.getRoutePermissionsJson());
    if (!explicitPaths.isEmpty()) {
      return explicitPaths;
    }
    return getRecommendedPaths(role.getRoleCode(), role.getRoleName());
  }

  public static List<String> getRecommendedPaths(String roleCode, String roleName) {
    String normalizedCode = normalizeRoleCode(roleCode, roleName);
    List<String> preset = ROLE_PAGE_PRESETS.get(normalizedCode);
    if (preset == null || preset.isEmpty()) {
      return List.of();
    }
    return PagePermissionPathHelper.normalizePaths(preset);
  }

  private static String normalizeRoleCode(String roleCode, String roleName) {
    String code = roleCode == null ? "" : roleCode.trim().toUpperCase(Locale.ROOT);
    if (!code.isEmpty()) {
      return code;
    }
    String name = roleName == null ? "" : roleName.trim().toUpperCase(Locale.ROOT);
    if (name.contains("系统管理员")) return "SYS_ADMIN";
    if (name.contains("院长")) return "DIRECTOR";
    if (name.contains("护理") && name.contains("部长")) return "NURSING_MINISTER";
    if (name.contains("护理")) return "NURSING_EMPLOYEE";
    if (name.contains("人事") && name.contains("部长")) return "HR_MINISTER";
    if (name.contains("人事")) return "HR_EMPLOYEE";
    if (name.contains("医务") && name.contains("部长")) return "MEDICAL_MINISTER";
    if (name.contains("医务")) return "MEDICAL_EMPLOYEE";
    if (name.contains("财务") && name.contains("部长")) return "FINANCE_MINISTER";
    if (name.contains("财务")) return "FINANCE_EMPLOYEE";
    if (name.contains("后勤") && name.contains("部长")) return "LOGISTICS_MINISTER";
    if (name.contains("后勤")) return "LOGISTICS_EMPLOYEE";
    if (name.contains("营销") && name.contains("部长")) return "MARKETING_MINISTER";
    if (name.contains("营销")) return "MARKETING_EMPLOYEE";
    if (name.contains("消防")) return "GUARD";
    return "";
  }

  private static List<String> concat(List<String> base, String... extra) {
    LinkedHashSet<String> merged = new LinkedHashSet<>(base);
    if (extra != null) {
      for (String item : extra) {
        if (item != null && !item.isBlank()) {
          merged.add(item);
        }
      }
    }
    return new ArrayList<>(merged);
  }

  private static List<String> concat(List<String> base, List<String> extra) {
    LinkedHashSet<String> merged = new LinkedHashSet<>(base);
    if (extra != null) {
      for (String item : extra) {
        if (item != null && !item.isBlank()) {
          merged.add(item);
        }
      }
    }
    return new ArrayList<>(merged);
  }
}
