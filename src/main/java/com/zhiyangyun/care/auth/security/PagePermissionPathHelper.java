package com.zhiyangyun.care.auth.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PagePermissionPathHelper {
  private static final Map<String, String> LEGACY_TO_CANONICAL = loadRouteAliases();
  private static final Set<String> HIDDEN_PATHS = Set.of("/403");

  private PagePermissionPathHelper() {
  }

  private static Map<String, String> loadRouteAliases() {
    try (InputStream input = PagePermissionPathHelper.class.getClassLoader()
        .getResourceAsStream("security/route-aliases.json")) {
      if (input == null) return Map.of();
      return new ObjectMapper().readValue(input, new TypeReference<Map<String, String>>() {});
    } catch (Exception ignored) {
      return Map.of();
    }
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

}
