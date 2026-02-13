package com.zhiyangyun.care.crm.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CrmLeadRequest {
  private Long tenantId;
  private Long orgId;
  @NotBlank
  private String name;
  private String phone;
  private String source;
  private Integer status = 0;
  private String nextFollowDate;
  private String remark;
  private Long createdBy;
}
