package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class FinanceStoreSalesItem {
  private String month;
  private BigDecimal amount;
}
