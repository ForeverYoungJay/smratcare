package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class FinancePaymentAdjustmentApprovalRequest {
  private String type;
  private Long billId;
  private Long paymentId;
  private Long elderId;
  private String elderName;
  private BigDecimal amount;
  private String reason;
  private String remark;
}
