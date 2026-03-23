package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class HrSocialSecurityApplyRequest {
  private Long staffId;
  private Integer socialSecurityCompanyApply;
  private Integer socialSecurityNeedDirectorApproval;
  private BigDecimal socialSecurityMonthlyAmount;
  private String socialSecurityRemark;
}
