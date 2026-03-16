package com.zhiyangyun.care.finance.model;

import lombok.Data;

@Data
public class FinanceCrossPeriodApprovalRequest {
  private String month;
  private String actionType;
  private Long billId;
  private Long paymentId;
  private Long elderId;
  private String reason;
  private String remark;
}
