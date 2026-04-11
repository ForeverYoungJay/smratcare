package com.zhiyangyun.care.crm.model.trace;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrmStageTransitionLogResponse {
  private Long id;
  private String entityType;
  private Long leadId;
  private Long contractId;
  private String transitionType;
  private String source;
  private String fromStage;
  private String toStage;
  private String fromStatus;
  private String toStatus;
  private String fromOwnerDept;
  private String toOwnerDept;
  private String remark;
  private Long operatedBy;
  private String operatedByName;
  private LocalDateTime operatedAt;
}
