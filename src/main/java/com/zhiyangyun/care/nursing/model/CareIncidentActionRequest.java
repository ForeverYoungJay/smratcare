package com.zhiyangyun.care.nursing.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CareIncidentActionRequest {
  /** ACCEPT/PROCESS/ESCALATE/SUBMIT_REVIEW/CLOSE/REOPEN/CORRECT/ASSIGN */
  @NotBlank
  private String action;
  private String note;
  private Long ownerId;
  private String ownerName;
  /** MEDICAL/LOGISTICS/ADMIN_DUTY */
  private String escalateTo;
}
