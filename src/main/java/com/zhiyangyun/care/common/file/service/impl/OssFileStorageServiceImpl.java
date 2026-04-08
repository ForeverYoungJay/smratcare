package com.zhiyangyun.care.common.file.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.zhiyangyun.care.common.file.FileStorageProperties;
import com.zhiyangyun.care.common.file.model.UploadFileResponse;
import com.zhiyangyun.care.common.file.service.FileStorageService;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(prefix = "app.file-storage", name = "provider", havingValue = "oss")
public class OssFileStorageServiceImpl implements FileStorageService {
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

  public OssFileStorageServiceImpl(FileStorageProperties properties) {
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

    FileStorageProperties.Oss oss = properties.getOss();
    String endpoint = requireText(oss.getEndpoint(), "未配置 OSS endpoint");
    String bucket = requireText(oss.getBucket(), "未配置 OSS bucket");
    String accessKeyId = requireText(oss.getAccessKeyId(), "未配置 OSS accessKeyId");
    String accessKeySecret = requireText(oss.getAccessKeySecret(), "未配置 OSS accessKeySecret");

    String originalName = safeFileName(file.getOriginalFilename());
    String extension = resolveExtension(originalName);
    validateExtension(extension);
    validateContentType(file.getContentType());
    String objectName = buildObjectName(oss.getBasePath(), sanitizeBizType(bizType), extension);

    OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    try (InputStream inputStream = file.getInputStream()) {
      client.putObject(bucket, objectName, inputStream);
    } catch (Exception ex) {
      throw new IllegalStateException("OSS 上传失败", ex);
    } finally {
      client.shutdown();
    }

    UploadFileResponse response = new UploadFileResponse();
    response.setFileName(objectName.substring(objectName.lastIndexOf('/') + 1));
    response.setOriginalFileName(originalName);
    response.setFileUrl(buildPublicUrl(endpoint, bucket, objectName));
    response.setFileType(file.getContentType());
    response.setFileSize(file.getSize());
    return response;
  }

  private String buildObjectName(String basePath, String bizType, String extension) {
    String safeBase = (basePath == null || basePath.isBlank()) ? "smartcare" : basePath.trim().replaceAll("^/+|/+$", "");
    LocalDate today = LocalDate.now();
    String storageName = UUID.randomUUID().toString().replace("-", "") + extension;
    return safeBase + "/" + bizType + "/" + today.getYear() + "/"
        + String.format("%02d", today.getMonthValue()) + "/"
        + String.format("%02d", today.getDayOfMonth()) + "/" + storageName;
  }

  private String buildPublicUrl(String endpoint, String bucket, String objectName) {
    String publicBase = properties.getPublicBaseUrl();
    if (publicBase != null && !publicBase.isBlank()) {
      String base = publicBase.trim();
      if (base.endsWith("/")) {
        base = base.substring(0, base.length() - 1);
      }
      return base + "/" + objectName;
    }
    String ep = endpoint.trim();
    if (!ep.startsWith("http://") && !ep.startsWith("https://")) {
      ep = "https://" + ep;
    }
    if (ep.endsWith("/")) {
      ep = ep.substring(0, ep.length() - 1);
    }
    String host = ep.replaceFirst("^https?://", "");
    return "https://" + bucket + "." + host + "/" + objectName;
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

  private String requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(message);
    }
    return value.trim();
  }
}
