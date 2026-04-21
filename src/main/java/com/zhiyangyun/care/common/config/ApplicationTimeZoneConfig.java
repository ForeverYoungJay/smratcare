package com.zhiyangyun.care.common.config;

import jakarta.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationTimeZoneConfig {
  @Value("${app.system-time-zone:Asia/Shanghai}")
  private String systemTimeZone;

  @PostConstruct
  public void init() {
    ZoneId zoneId = ZoneId.of(systemTimeZone);
    TimeZone timeZone = TimeZone.getTimeZone(zoneId);
    TimeZone.setDefault(timeZone);
    System.setProperty("user.timezone", timeZone.getID());
  }
}
