package com.zhiyangyun.care.finance.model;

import lombok.Data;

@Data
public class FinanceCrossPeriodApprovalResponse {
  private String month;
  private Long approvalId;
  private String approvalStatus;
  private String approvalStatusLabel;
  private String approvalTitle;
  private String message;
}
