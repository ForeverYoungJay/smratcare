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
  private String provider = "local";
  private String baseDir = System.getProperty("user.home") + "/smartcare-uploads";
  private String urlPrefix = "/uploads";
  private String publicBaseUrl;
  private long maxSizeBytes = 10 * 1024 * 1024;
  private List<String> allowedExtensions = new ArrayList<>(
      List.of("jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx", "xls", "xlsx", "txt", "zip",
          "mp3", "m4a", "wav", "aac", "amr"));

  private Oss oss = new Oss();

  @Data
  public static class Oss {
    private String endpoint;
    private String bucket;
    private String accessKeyId;
    private String accessKeySecret;
    private String basePath = "smartcare";
  }
}
