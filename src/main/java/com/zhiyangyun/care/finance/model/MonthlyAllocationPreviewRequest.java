package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class MonthlyAllocationPreviewRequest {
  @NotNull
  private BigDecimal totalAmount;
  private String allocationMonth;
  private String allocationName;
  private List<Long> elderIds;
  private String remark;
}
