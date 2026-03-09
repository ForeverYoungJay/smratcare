package com.zhiyangyun.care.crm.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrmContractStageSummaryResponse {
  private long pendingAssessment;
  private long pendingBedSelect;
  private long pendingSign;
  private long signed;

  private long pendingAssessmentOverdue;
  private long pendingSignOverdue;

  private LocalDateTime generatedAt;
}
