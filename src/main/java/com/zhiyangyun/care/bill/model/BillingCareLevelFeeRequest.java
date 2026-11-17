package com.zhiyangyun.care.bill.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class BillingCareLevelFeeRequest {
  @NotNull
  private Long orgId;

  @NotBlank
  private String careLevel;

  @NotNull
  @DecimalMin("0.00")
  private BigDecimal feeMonthly;

  @NotBlank
  @Pattern(regexp = "\\d{4}-\\d{2}")
  private String effectiveMonth;

  @Min(0)
  @Max(1)
  private Integer status = 1;

  private String remark;
}
