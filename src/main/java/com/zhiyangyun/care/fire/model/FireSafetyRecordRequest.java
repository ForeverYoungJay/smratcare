package com.zhiyangyun.care.fire.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FireSafetyRecordRequest {
  @NotBlank
  private String recordType;

  @NotBlank
  private String title;

  private String location;

  @NotBlank
  private String inspectorName;

  @NotNull
  private LocalDateTime checkTime;

  private String status;

  private String issueDescription;

  private String actionTaken;

  private LocalDate nextCheckDate;
}
