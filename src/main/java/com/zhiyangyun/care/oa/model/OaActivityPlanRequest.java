package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OaActivityPlanRequest {
  @NotBlank
  private String title;

  private String activityType;

  @NotNull
  private LocalDate planDate;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private String location;

  private String organizer;

  private String participantTarget;

  private String participantTags;

  private String participantElderIds;

  private Integer estimatedCount;

  private Boolean needMedicalStaff;

  private String riskLevel;

  private Boolean needBudget;

  private BigDecimal budgetAmount;

  private String budgetDescription;

  private String status;

  private String content;

  private String schemeAttachmentName;

  private String schemeAttachmentUrl;

  private String remark;
}
