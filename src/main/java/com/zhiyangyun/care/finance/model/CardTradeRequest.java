package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CardTradeRequest {
  @NotNull
  private Long cardAccountId;

  @NotNull
  private BigDecimal amount;

  private String remark;
}
