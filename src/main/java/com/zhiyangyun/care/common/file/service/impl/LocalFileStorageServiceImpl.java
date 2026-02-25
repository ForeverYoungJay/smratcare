package com.zhiyangyun.care.common.file.service.impl;

import com.zhiyangyun.care.common.file.FileStorageProperties;
import com.zhiyangyun.care.common.file.model.UploadFileResponse;
import com.zhiyangyun.care.common.file.service.FileStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {
  private final FileStorageProperties properties;

  public LocalFileStorageServiceImpl(FileStorageProperties properties) {
    this.properties = properties;
  }

  @Override
  public UploadFileResponse upload(MultipartFile file, String bizType) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("上传文件不能为空");
    }
    String safeBizType = sanitizeBizType(bizType);
    String extension = resolveExtension(file.getOriginalFilename());
    String storageName = UUID.randomUUID().toString().replace("-", "") + extension;
    LocalDate today = LocalDate.now();
    Path dir = Paths.get(properties.getBaseDir(), safeBizType, String.valueOf(today.getYear()),
        String.format("%02d", today.getMonthValue()), String.format("%02d", today.getDayOfMonth()));
    Path target = dir.resolve(storageName);
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
    response.setOriginalFileName(file.getOriginalFilename());
    response.setFileUrl(normalizePrefix(properties.getUrlPrefix()) + relativePath);
    response.setFileType(file.getContentType());
    response.setFileSize(file.getSize());
    return response;
  }

  private String sanitizeBizType(String bizType) {
    if (bizType == null || bizType.isBlank()) {
      return "common";
    }
    String normalized = bizType.trim().replaceAll("[^a-zA-Z0-9\\-_]", "-");
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

  private String normalizePrefix(String prefix) {
    String value = (prefix == null || prefix.isBlank()) ? "/uploads" : prefix.trim();
    if (!value.startsWith("/")) {
      value = "/" + value;
    }
    if (value.endsWith("/")) {
      value = value.substring(0, value.length() - 1);
    }
    return value;
  }
}
