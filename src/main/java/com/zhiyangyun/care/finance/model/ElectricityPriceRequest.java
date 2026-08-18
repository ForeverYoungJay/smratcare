package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ElectricityPriceRequest {
  @NotNull
  @DecimalMin(value = "0.0001", message = "电价必须大于 0")
  private BigDecimal unitPrice;
  @NotNull
  private LocalDate effectiveFrom;
  @Size(max = 200)
  private String remark;
}
