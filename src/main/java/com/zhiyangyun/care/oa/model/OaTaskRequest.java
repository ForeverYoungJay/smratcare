package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
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

  private String calendarType;

  private String planCategory;

  private String urgency;

  private String eventColor;

  private List<Long> collaboratorIds;

  private List<String> collaboratorNames;

  private Boolean recurring;

  private String recurrenceRule;

  private Integer recurrenceInterval;

  private Integer recurrenceCount;
}
