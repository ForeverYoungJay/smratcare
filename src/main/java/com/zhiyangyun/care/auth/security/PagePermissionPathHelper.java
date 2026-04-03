package com.zhiyangyun.care.auth.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class PagePermissionPathHelper {
  private static final Map<String, String> LEGACY_TO_CANONICAL = Map.ofEntries(
      Map.entry("/portal", "/workbench"),
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
      if (canonical == null || canonical.isBlank() || "/".equals(canonical)) {
        continue;
      }
      normalized.add(canonical);
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
}
