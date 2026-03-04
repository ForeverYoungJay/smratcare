package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FinanceReconcileExceptionItem {
  private String exceptionType;
  private String exceptionTypeLabel;
  private Long billId;
  private Long paymentId;
  private Long elderId;
  private String elderName;
  private BigDecimal amount;
  private String detail;
  private String suggestion;
  private LocalDateTime occurredAt;
}
