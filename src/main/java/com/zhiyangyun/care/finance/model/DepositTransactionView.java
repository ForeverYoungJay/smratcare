package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DepositTransactionView {
  private Long id;
  private Long elderId;
  private String elderName;
  private String txnType;
  private String txnTypeText;
  private BigDecimal amount;
  private BigDecimal balanceAfter;
  private String payMethod;
  private LocalDateTime occurredAt;
  private String reason;
  private String remark;
}
