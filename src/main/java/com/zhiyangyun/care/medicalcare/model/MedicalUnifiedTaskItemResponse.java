package com.zhiyangyun.care.medicalcare.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MedicalUnifiedTaskItemResponse {
  private String id;
  private String module;
  private Long residentId;
  private String residentName;
  private String taskTitle;
  private String assignee;
  private LocalDateTime plannedTime;
  private String priority;
  private String status;
  private Long sourceId;
  private Boolean overdue;
  private Long overdueMinutes;
  private Integer riskScore;
  private String riskReason;
  private String suggestedRoute;
}
