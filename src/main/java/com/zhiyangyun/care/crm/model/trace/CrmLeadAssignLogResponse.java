package com.zhiyangyun.care.crm.model.trace;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrmLeadAssignLogResponse {
  private Long id;
  private Long leadId;
  private Long fromOwnerStaffId;
  private String fromOwnerStaffName;
  private Long toOwnerStaffId;
  private String toOwnerStaffName;
  private Long assignedBy;
  private String assignedByName;
  private LocalDateTime assignedAt;
  private String remark;
}
