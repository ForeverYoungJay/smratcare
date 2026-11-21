package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class DepositStandardRequest {
  private Long id;
  @NotBlank
  @Size(max = 100)
  private String standardName;
  /** 留空表示通用默认标准。 */
  @Size(max = 32)
  private String careLevel;
  @NotNull
  @DecimalMin(value = "0", message = "押金标准不能为负数")
  private BigDecimal amount;
  @NotNull
  private LocalDate effectiveFrom;
  @Size(max = 200)
  private String remark;
}
