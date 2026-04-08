package com.zhiyangyun.care.common.file.service.impl;

import com.zhiyangyun.care.common.file.FileStorageProperties;
import com.zhiyangyun.care.common.file.model.UploadFileResponse;
import com.zhiyangyun.care.common.file.service.FileStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(prefix = "app.file-storage", name = "provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageServiceImpl implements FileStorageService {
  private static final Set<String> DANGEROUS_CONTENT_TYPES = Set.of(
      "text/html",
      "application/javascript",
      "text/javascript",
      "application/x-javascript",
      "application/x-msdownload",
      "application/x-sh",
      "application/x-bat",
      "application/x-csh",
      "application/x-httpd-php",
      "text/x-php");
  private final FileStorageProperties properties;

  public LocalFileStorageServiceImpl(FileStorageProperties properties) {
    this.properties = properties;
  }

  @Override
  public UploadFileResponse upload(MultipartFile file, String bizType) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("上传文件不能为空");
    }
    if (file.getSize() > properties.getMaxSizeBytes()) {
      throw new IllegalArgumentException("文件大小超限，最大支持 " + (properties.getMaxSizeBytes() / 1024 / 1024) + "MB");
    }
    String originalName = safeFileName(file.getOriginalFilename());
    String extension = resolveExtension(originalName);
    validateExtension(extension);
    validateContentType(file.getContentType());
    String safeBizType = sanitizeBizType(bizType);
    String storageName = UUID.randomUUID().toString().replace("-", "") + extension;
    LocalDate today = LocalDate.now();
    Path baseDir = Paths.get(properties.getBaseDir()).toAbsolutePath().normalize();
    Path dir = baseDir.resolve(Paths.get(safeBizType, String.valueOf(today.getYear()),
        String.format("%02d", today.getMonthValue()), String.format("%02d", today.getDayOfMonth()))).normalize();
    Path target = dir.resolve(storageName).normalize();
    ensureSafePath(baseDir, target);
    try {
      Files.createDirectories(dir);
      file.transferTo(target);
    } catch (IOException ex) {
      throw new IllegalStateException("文件上传失败", ex);
    }

    String relativePath = "/" + safeBizType + "/" + today.getYear() + "/"
        + String.format("%02d", today.getMonthValue()) + "/"
        + String.format("%02d", today.getDayOfMonth()) + "/" + storageName;

    UploadFileResponse response = new UploadFileResponse();
    response.setFileName(storageName);
    response.setOriginalFileName(originalName);
    response.setFileUrl(buildFileUrl(relativePath));
    response.setFileType(file.getContentType());
    response.setFileSize(file.getSize());
    return response;
  }

  private String sanitizeBizType(String bizType) {
    if (bizType == null || bizType.isBlank()) {
      return "common";
    }
    String normalized = bizType.trim().replaceAll("[^a-zA-Z0-9\\-_]", "-");
    if (normalized.length() > 64) {
      normalized = normalized.substring(0, 64);
    }
    return normalized.isBlank() ? "common" : normalized;
  }

  private String resolveExtension(String originalName) {
    if (originalName == null || originalName.isBlank()) {
      return "";
    }
    int idx = originalName.lastIndexOf('.');
    if (idx < 0 || idx == originalName.length() - 1) {
      return "";
    }
    String ext = originalName.substring(idx).toLowerCase();
    return ext.length() > 12 ? "" : ext;
  }

  private void validateExtension(String extension) {
    if (extension == null || extension.isBlank()) {
      throw new IllegalArgumentException("文件类型不支持");
    }
    String ext = extension.startsWith(".") ? extension.substring(1) : extension;
    List<String> allowed = properties.getAllowedExtensions();
    if (allowed == null || allowed.isEmpty()) {
      return;
    }
    boolean matched = allowed.stream().anyMatch(item -> item != null && item.equalsIgnoreCase(ext));
    if (!matched) {
      throw new IllegalArgumentException("文件类型不支持，允许类型: " + String.join(", ", allowed));
    }
  }

  private String safeFileName(String originalName) {
    if (originalName == null || originalName.isBlank()) {
      return "unnamed";
    }
    String normalized = originalName.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    if (normalized.length() > 128) {
      return normalized.substring(normalized.length() - 128);
    }
    return normalized;
  }

  private void validateContentType(String contentType) {
    if (contentType == null || contentType.isBlank()) {
      return;
    }
    String normalized = contentType.trim().toLowerCase(Locale.ROOT);
    int separator = normalized.indexOf(';');
    if (separator >= 0) {
      normalized = normalized.substring(0, separator).trim();
    }
    if ("application/octet-stream".equals(normalized)) {
      return;
    }
    if (DANGEROUS_CONTENT_TYPES.contains(normalized)
        || normalized.contains("javascript")
        || normalized.contains("shellscript")
        || normalized.contains("executable")) {
      throw new IllegalArgumentException("文件内容类型不支持");
    }
  }

  private void ensureSafePath(Path baseDir, Path target) {
    if (baseDir == null || target == null || !target.startsWith(baseDir)) {
      throw new IllegalStateException("文件存储路径非法");
    }
  }

  private String normalizePrefix(String prefix) {
    String value = (prefix == null || prefix.isBlank()) ? "/uploads" : prefix.trim();
    if (value.startsWith("http://") || value.startsWith("https://")) {
      return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
    if (!value.startsWith("/")) {
      value = "/" + value;
    }
    if (value.endsWith("/")) {
      value = value.substring(0, value.length() - 1);
    }
    return value;
  }

  private String buildFileUrl(String relativePath) {
    String publicBase = properties.getPublicBaseUrl();
    if (publicBase != null && !publicBase.isBlank()) {
      String base = publicBase.trim().endsWith("/") ? publicBase.trim().substring(0, publicBase.trim().length() - 1)
          : publicBase.trim();
      return base + relativePath;
    }
    return normalizePrefix(properties.getUrlPrefix()) + relativePath;
  }
}
