package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class FinanceBillingConfigUpsertRequest {
  @NotBlank
  @Pattern(regexp = "^[A-Z0-9_]{3,64}$")
  private String configKey;

  @NotNull
  private BigDecimal configValue;

  @NotBlank
  @Pattern(regexp = "\\d{4}-\\d{2}")
  private String effectiveMonth;

  private Integer status = 1;

  private String remark;
}
