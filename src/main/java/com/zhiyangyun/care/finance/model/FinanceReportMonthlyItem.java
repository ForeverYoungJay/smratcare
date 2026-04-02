package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class FinanceReportMonthlyItem {
  private String month;
  private BigDecimal amount;
  private BigDecimal billedAmount;
  private BigDecimal receivedAmount;
  private BigDecimal refundAmount;
  private BigDecimal netAmount;
}
