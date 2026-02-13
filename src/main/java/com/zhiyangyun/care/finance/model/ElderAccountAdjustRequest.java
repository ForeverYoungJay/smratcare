package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ElderAccountAdjustRequest {
  private Long elderId;

  private String elderName;

  @NotNull
  private BigDecimal amount;

  @NotNull
  private String direction;

  private String remark;
}
