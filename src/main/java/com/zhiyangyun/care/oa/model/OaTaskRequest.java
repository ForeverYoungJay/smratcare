package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OaTaskRequest {
  @NotBlank
  private String title;

  private String description;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private String priority;

  private String status;

  private Long assigneeId;

  private String assigneeName;
}
