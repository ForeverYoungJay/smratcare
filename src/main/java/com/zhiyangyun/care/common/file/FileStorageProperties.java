package com.zhiyangyun.care.common.file;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.file-storage")
public class FileStorageProperties {
  private String baseDir = System.getProperty("user.home") + "/smartcare-uploads";
  private String urlPrefix = "/uploads";
  private long maxSizeBytes = 10 * 1024 * 1024;
  private List<String> allowedExtensions = new ArrayList<>(
      List.of("jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx", "xls", "xlsx", "txt", "zip"));
}
