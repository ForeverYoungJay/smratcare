package com.zhiyangyun.care.auth.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PagePermissionPathHelper {
  private static final Map<String, String> LEGACY_TO_CANONICAL = Map.ofEntries(
      Map.entry("/oa/portal", "/workbench"),
      Map.entry("/oa/todo", "/workbench/todo"),
      Map.entry("/oa/my-info", "/workbench/my-info"),
      Map.entry("/oa/attendance-leave", "/workbench/attendance"),
      Map.entry("/oa/work-report", "/workbench/reports"),
      Map.entry("/hr/workbench", "/hr/overview"),
      Map.entry("/oa/activity", "/oa/activity-center/records"),
      Map.entry("/oa/activity-plan", "/oa/activity-center/plan"),
      Map.entry("/oa/survey/manage", "/oa/activity-center/survey-manage"),
      Map.entry("/oa/survey/stats", "/oa/activity-center/survey-stats"),
      Map.entry("/hr/points", "/hr/incentive/ledger"),
      Map.entry("/hr/points-rule", "/hr/incentive/rules"),
      Map.entry("/system/org-manage", "/system/site-config"),
      Map.entry("/system/org-manage/intro", "/system/site-config"),
      Map.entry("/system/org-manage/news", "/system/site-config"),
      Map.entry("/system/org-manage/life", "/system/site-config"),
      Map.entry("/system/app-version", "/system/site-config"),
      Map.entry("/system/message", "/system/site-config"),
      Map.entry("/system/dict", "/base-config")
  );
  private static final Map<String, List<String>> API_TO_PAGE_PREFIXES = Map.ofEntries(
      Map.entry("/api/admin/hr", List.of("/hr")),
      Map.entry("/api/admin/staff", List.of("/hr/profile/account-access", "/system/staff")),
      Map.entry("/api/admin/staff-roles", List.of("/hr/profile/account-access", "/system/staff")),
      Map.entry("/api/admin/roles", List.of("/system/role")),
      Map.entry("/api/admin/departments", List.of("/system/department")),
      Map.entry("/api/admin/orgs", List.of("/system/org-info")),
      Map.entry("/api/admin/family", List.of("/elder")),
      Map.entry("/api/admin/product-tag", List.of("/logistics/commerce/tag", "/logistics/commerce/product")),
      Map.entry("/api/admin/product-category", List.of("/logistics/commerce/category", "/logistics/commerce/product")),
      Map.entry("/api/admin/disease-forbidden", List.of("/logistics/commerce/risk")),
      Map.entry("/api/base-config", List.of("/base-config", "/system/site-config")),
      Map.entry("/api/dashboard", List.of("/portal", "/workbench")),
      Map.entry("/api/oa", List.of("/oa", "/workbench")),
      Map.entry("/api/schedule", List.of("/hr/attendance", "/workbench/attendance")),
      Map.entry("/api/attendance", List.of("/hr/attendance", "/workbench/attendance")),
      Map.entry("/api/marketing", List.of("/marketing")),
      Map.entry("/api/crm", List.of("/marketing")),
      Map.entry("/api/stats", List.of("/stats")),
      Map.entry("/api/report", List.of("/stats")),
      Map.entry("/api/medical-care", List.of("/medical-care", "/elder/resident-360", "/elder/in-hospital-overview", "/elder/status-change")),
      Map.entry("/api/health", List.of("/medical-care", "/health", "/elder/resident-360")),
      Map.entry("/api/medical", List.of("/medical-care", "/health", "/elder/resident-360")),
      Map.entry("/api/vital", List.of("/medical-care", "/health", "/elder/resident-360")),
      Map.entry("/api/assessment", List.of("/medical-care", "/elder/assessment", "/elder/resident-360")),
      Map.entry("/api/nursing", List.of("/medical-care")),
      Map.entry("/api/care", List.of("/medical-care")),
      Map.entry("/api/fire", List.of("/fire")),
      Map.entry("/api/guard", List.of("/fire")),
      Map.entry("/api/logistics", List.of("/logistics")),
      Map.entry("/api/life", List.of("/logistics")),
      Map.entry("/api/material", List.of("/logistics")),
      Map.entry("/api/material-center", List.of("/logistics")),
      Map.entry("/api/inventory", List.of("/logistics")),
      Map.entry("/api/asset", List.of("/elder", "/logistics")),
      Map.entry("/api/store", List.of("/logistics", "/marketing/contracts", "/medical-care/basic-diseases", "/hr/incentive")),
      Map.entry("/api/life/dining", List.of("/logistics")),
      Map.entry("/api/finance/fee", List.of("/finance", "/elder", "/elder/contracts-invoices", "/elder/status-change")),
      Map.entry("/api/finance", List.of("/finance", "/elder", "/elder/contracts-invoices", "/elder/status-change", "/medical-care/integrated-account")),
      Map.entry("/api/bill", List.of("/finance")),
      Map.entry("/api/admin/billing", List.of("/finance")),
      Map.entry("/api/card/account", List.of("/finance")),
      Map.entry("/api/elder/lifecycle", List.of("/elder", "/marketing/contracts", "/finance")),
      Map.entry("/api/elder", List.of("/elder", "/medical-care", "/marketing/contracts", "/finance")),
      Map.entry("/api/room", List.of("/elder")),
      Map.entry("/api/bed", List.of("/elder")),
      Map.entry("/api/standard", List.of("/medical-care", "/elder")),
      Map.entry("/api/admin/disease", List.of("/medical-care/basic-diseases", "/marketing/contracts", "/health/archive", "/logistics/commerce/risk")),
      Map.entry("/api/admin/survey", List.of("/oa/activity-center/survey-manage", "/oa/activity-center/survey-stats", "/oa"))
  );
  private static final Set<String> HIDDEN_PATHS = Set.of("/403");

  private PagePermissionPathHelper() {
  }

  public static String normalizePath(String path) {
    String base = String.valueOf(path == null ? "" : path)
        .split("\\?")[0]
        .split("#")[0]
        .trim();
    if (base.isEmpty()) {
      return "/";
    }
    String normalized = base.replaceAll("/+", "/");
    if (normalized.length() > 1 && normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized.startsWith("/") ? normalized : "/" + normalized;
  }

  public static String toCanonicalPath(String path) {
    String normalized = normalizePath(path);
    return LEGACY_TO_CANONICAL.getOrDefault(normalized, normalized);
  }

  public static List<String> normalizePaths(List<String> paths) {
    if (paths == null || paths.isEmpty()) {
      return List.of();
    }
    LinkedHashSet<String> normalized = new LinkedHashSet<>();
    for (String path : paths) {
      String canonical = toCanonicalPath(path);
      if (canonical == null || canonical.isBlank() || "/".equals(canonical) || HIDDEN_PATHS.contains(canonical)) {
        continue;
      }
      normalized.add(canonical);
      if ("/workbench".equals(canonical) || "/workbench/overview".equals(canonical)) {
        normalized.add("/portal");
      }
    }
    return new ArrayList<>(normalized);
  }

  public static List<String> parseAndNormalize(ObjectMapper objectMapper, String value) {
    if (objectMapper == null || value == null || value.isBlank()) {
      return List.of();
    }
    try {
      List<String> parsed = objectMapper.readValue(value, new TypeReference<List<String>>() {});
      return normalizePaths(parsed);
    } catch (Exception ignored) {
      return List.of();
    }
  }

  public static String normalizeJson(ObjectMapper objectMapper, String value) {
    List<String> normalized = parseAndNormalize(objectMapper, value);
    try {
      return objectMapper.writeValueAsString(normalized);
    } catch (Exception ignored) {
      return "[]";
    }
  }

  public static boolean hasPageAccess(List<String> pagePermissions, String path) {
    if (pagePermissions == null || pagePermissions.isEmpty()) {
      return false;
    }
    String normalizedPath = toCanonicalPath(path);
    for (String permissionPath : pagePermissions) {
      String normalizedPermission = toCanonicalPath(permissionPath);
      if (normalizedPath.equals(normalizedPermission) || normalizedPath.startsWith(normalizedPermission + "/")) {
        return true;
      }
    }
    return false;
  }

  public static boolean matchesApiRequest(List<String> pagePermissions, String requestUri) {
    if (pagePermissions == null || pagePermissions.isEmpty()) {
      return false;
    }
    String normalizedUri = normalizePath(requestUri);
    for (Map.Entry<String, List<String>> entry : API_TO_PAGE_PREFIXES.entrySet()) {
      if (!normalizedUri.startsWith(entry.getKey())) {
        continue;
      }
      for (String pagePath : entry.getValue()) {
        if (hasPageAccess(pagePermissions, pagePath)) {
          return true;
        }
      }
    }
    return false;
  }
}
