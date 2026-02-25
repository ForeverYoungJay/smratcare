package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class MonthlyAllocationCreateRequest {
  @NotBlank
  private String allocationMonth;
  @NotBlank
  private String allocationName;
  @NotNull
  private BigDecimal totalAmount;
  @NotNull
  private Integer targetCount;
  private String remark;
}
