package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FinanceBillBindElderRequest {
  @NotNull
  private Long elderId;
  private String remark;
  private Long crossPeriodApprovalId;
}
