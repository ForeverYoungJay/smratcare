package com.zhiyangyun.care.smart.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SmartDeviceEventRequest {
  @NotBlank
  private String deviceCode;
  @NotBlank
  private String eventType;
  private String eventLevel;
  private LocalDateTime eventTime;
  private JsonNode payload;
  private String location;
  private Long elderId;
}
