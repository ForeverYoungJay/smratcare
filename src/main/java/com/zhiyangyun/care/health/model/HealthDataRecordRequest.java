package com.zhiyangyun.care.health.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HealthDataRecordRequest {
  private Long elderId;
  private String elderName;

  @NotBlank
  private String dataType;

  @NotBlank
  private String dataValue;

  @NotNull
  private LocalDateTime measuredAt;

  private String source;
  private Integer abnormalFlag;
  private String remark;
}
