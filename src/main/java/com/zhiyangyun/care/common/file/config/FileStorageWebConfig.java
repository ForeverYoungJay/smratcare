package com.zhiyangyun.care.common.file.config;

import com.zhiyangyun.care.common.file.FileStorageProperties;
import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileStorageWebConfig implements WebMvcConfigurer {
  private final FileStorageProperties properties;

  public FileStorageWebConfig(FileStorageProperties properties) {
    this.properties = properties;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String prefix = normalizePrefix(properties.getUrlPrefix());
    String location = Paths.get(properties.getBaseDir()).toAbsolutePath().normalize().toUri().toString();
    registry.addResourceHandler(prefix + "/**").addResourceLocations(location);
  }

  private String normalizePrefix(String value) {
    String prefix = (value == null || value.isBlank()) ? "/uploads" : value.trim();
    if (!prefix.startsWith("/")) {
      prefix = "/" + prefix;
    }
    if (prefix.endsWith("/")) {
      prefix = prefix.substring(0, prefix.length() - 1);
    }
    return prefix;
  }
}
