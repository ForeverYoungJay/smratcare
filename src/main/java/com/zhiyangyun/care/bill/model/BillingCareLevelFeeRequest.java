package com.zhiyangyun.care.bill.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class BillingCareLevelFeeRequest {
  @NotNull
  private Long orgId;

  @NotBlank
  private String careLevel;

  @NotNull
  private BigDecimal feeMonthly;

  @NotBlank
  @Pattern(regexp = "\\d{4}-\\d{2}")
  private String effectiveMonth;

  private Integer status = 1;

  private String remark;
}
