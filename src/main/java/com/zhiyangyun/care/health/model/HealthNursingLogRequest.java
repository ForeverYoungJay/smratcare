package com.zhiyangyun.care.health.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HealthNursingLogRequest {
  private Long elderId;
  @Size(max = 64, message = "elderName too long")
  private String elderName;
  private Long sourceInspectionId;

  @NotNull
  private LocalDateTime logTime;

  @NotBlank
  @Pattern(regexp = "^(ROUTINE|INSPECTION_FOLLOW_UP|INCIDENT)$", message = "invalid logType")
  private String logType;

  @NotBlank
  @Size(max = 2000, message = "content too long")
  private String content;

  @Size(max = 64, message = "staffName too long")
  private String staffName;
  @Pattern(regexp = "^(PENDING|DONE|CLOSED)?$", message = "invalid nursing log status")
  private String status;
  @Size(max = 1000, message = "remark too long")
  private String remark;
}
