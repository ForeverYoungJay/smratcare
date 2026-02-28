package com.zhiyangyun.care.crm.model;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MarketingPlanApprovalRequest {
  @Size(max = 500, message = "审批意见长度不能超过 500")
  private String remark;
}
