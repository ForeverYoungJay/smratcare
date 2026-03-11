package com.zhiyangyun.care.health.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HealthDataRecordRequest {
  private Long elderId;
  @Size(max = 64, message = "elderName too long")
  private String elderName;

  @NotBlank
  @Pattern(regexp = "^(BP|HR|TEMP|SPO2|GLUCOSE|WEIGHT|OTHER)$", message = "invalid dataType")
  private String dataType;

  @NotBlank
  @Size(max = 128, message = "dataValue too long")
  private String dataValue;

  @NotNull
  private LocalDateTime measuredAt;

  @Size(max = 64, message = "source too long")
  private String source;
  private Integer abnormalFlag;
  @Size(max = 1000, message = "remark too long")
  private String remark;
}
