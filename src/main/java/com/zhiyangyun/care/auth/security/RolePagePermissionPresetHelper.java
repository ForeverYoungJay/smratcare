package com.zhiyangyun.care.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zhiyangyun.care.auth.entity.Role;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class RolePagePermissionPresetHelper {
  private static final String PRESET_RESOURCE = "security/role-page-presets.json";
  private static volatile Map<String, RolePagePreset> rolePagePresets;

  private record RolePagePreset(String label, String description, List<String> paths) {}

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
    RolePagePreset preset = loadPresets().get(normalizedCode);
    if (preset == null || preset.paths() == null || preset.paths().isEmpty()) {
      return List.of();
    }
    return PagePermissionPathHelper.normalizePaths(preset.paths());
  }

  private static Map<String, RolePagePreset> loadPresets() {
    Map<String, RolePagePreset> current = rolePagePresets;
    if (current != null) return current;
    synchronized (RolePagePermissionPresetHelper.class) {
      if (rolePagePresets != null) return rolePagePresets;
      try (InputStream input = RolePagePermissionPresetHelper.class.getClassLoader().getResourceAsStream(PRESET_RESOURCE)) {
        if (input == null) return rolePagePresets = Map.of();
        rolePagePresets = new ObjectMapper().readValue(input, new TypeReference<Map<String, RolePagePreset>>() {});
      } catch (Exception ignored) {
        rolePagePresets = Map.of();
      }
      return rolePagePresets;
    }
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

}
