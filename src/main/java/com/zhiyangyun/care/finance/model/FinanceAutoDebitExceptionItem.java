package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class FinanceAutoDebitExceptionItem {
  private Long billId;
  private Long elderId;
  private String elderName;
  private String billMonth;
  private BigDecimal outstandingAmount;
  private BigDecimal balance;
  private String reasonCode;
  private String reasonLabel;
  private String suggestion;
}
