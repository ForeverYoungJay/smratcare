package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class IncidentReportRequest {
  private Long elderId;

  private String elderName;

  @NotBlank
  private String reporterName;

  @NotNull
  private LocalDateTime incidentTime;

  @NotBlank
  private String incidentType;

  private String level;

  @NotBlank
  private String description;

  private String actionTaken;

  private String status;
}
