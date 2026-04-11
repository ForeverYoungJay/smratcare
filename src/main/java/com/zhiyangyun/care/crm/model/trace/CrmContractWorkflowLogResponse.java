package com.zhiyangyun.care.crm.model.trace;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrmContractWorkflowLogResponse {
  private Long id;
  private Long contractId;
  private Long leadId;
  private String actionType;
  private String beforeStatus;
  private String afterStatus;
  private String beforeFlowStage;
  private String afterFlowStage;
  private String beforeChangeWorkflow;
  private String afterChangeWorkflow;
  private String remark;
  private String snapshotJson;
  private Long operatedBy;
  private String operatedByName;
  private LocalDateTime operatedAt;
}
