package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FinanceBillingConfigRollbackRequest {
  @NotNull
  private Long logId;
}
