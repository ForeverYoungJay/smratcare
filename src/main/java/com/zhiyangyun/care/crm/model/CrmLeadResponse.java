package com.zhiyangyun.care.crm.model;

import lombok.Data;

@Data
public class CrmLeadResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String name;
  private String phone;
  private String source;
  private Integer status;
  private String nextFollowDate;
  private String remark;
}
