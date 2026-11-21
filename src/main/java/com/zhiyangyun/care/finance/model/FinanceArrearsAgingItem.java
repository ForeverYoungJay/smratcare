package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class FinanceArrearsAgingItem {
  private String key;
  private String label;
  private BigDecimal amount = BigDecimal.ZERO;
  private long billCount;
}
