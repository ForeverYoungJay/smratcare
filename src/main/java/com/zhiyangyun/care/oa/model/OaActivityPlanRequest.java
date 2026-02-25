package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OaActivityPlanRequest {
  @NotBlank
  private String title;

  @NotNull
  private LocalDate planDate;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private String location;

  private String organizer;

  private String participantTarget;

  private String status;

  private String content;

  private String remark;
}
