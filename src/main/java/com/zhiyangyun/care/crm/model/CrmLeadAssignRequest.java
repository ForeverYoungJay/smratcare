package com.zhiyangyun.care.crm.model;

import lombok.Data;

@Data
public class CrmLeadAssignRequest {
  private Long ownerStaffId;
  private String ownerStaffName;
  private String remark;
}
