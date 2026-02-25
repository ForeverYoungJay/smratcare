package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MaintenanceRequestCreateRequest {
  private Long roomId;

  @NotBlank
  private String reporterName;

  private String assigneeName;

  @NotBlank
  private String issueType;

  @NotBlank
  private String description;

  private String priority;

  private String status;

  private LocalDateTime reportedAt;

  private String remark;
}
