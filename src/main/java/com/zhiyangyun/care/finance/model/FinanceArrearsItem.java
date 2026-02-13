package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class FinanceArrearsItem {
  private Long elderId;
  private String elderName;
  private BigDecimal outstandingAmount;
}
