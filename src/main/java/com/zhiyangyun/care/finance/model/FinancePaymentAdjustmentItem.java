package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FinancePaymentAdjustmentItem {
  private String type;
  private String typeLabel;
  private Long billId;
  private Long paymentId;
  private Long elderId;
  private String elderName;
  private BigDecimal amount;
  private String status;
  private String detail;
  private String remark;
  private Long approvalId;
  private String approvalStatus;
  private String approvalStatusLabel;
  private String approvalTitle;
  private LocalDateTime occurredAt;
}
