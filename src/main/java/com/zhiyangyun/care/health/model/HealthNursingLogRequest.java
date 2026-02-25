package com.zhiyangyun.care.health.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HealthNursingLogRequest {
  private Long elderId;
  private String elderName;
  private Long sourceInspectionId;

  @NotNull
  private LocalDateTime logTime;

  @NotBlank
  private String logType;

  @NotBlank
  private String content;

  private String staffName;
  @Pattern(regexp = "^(PENDING|DONE|CLOSED)?$", message = "invalid nursing log status")
  private String status;
  private String remark;
}
