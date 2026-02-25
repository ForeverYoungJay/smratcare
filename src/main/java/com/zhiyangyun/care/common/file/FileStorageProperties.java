package com.zhiyangyun.care.common.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.file-storage")
public class FileStorageProperties {
  private String baseDir = System.getProperty("user.home") + "/smartcare-uploads";
  private String urlPrefix = "/uploads";
}
