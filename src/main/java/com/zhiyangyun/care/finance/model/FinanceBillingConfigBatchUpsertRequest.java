package com.zhiyangyun.care.finance.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class FinanceBillingConfigBatchUpsertRequest {
  @NotEmpty
  private List<@Valid FinanceBillingConfigUpsertRequest> items;
}
