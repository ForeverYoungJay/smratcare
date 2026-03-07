package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceLedgerHealthResponse {
  private LocalDateTime checkedAt;
  private Long billCount;
  private Long paymentCount;
  private Long consumptionCount;
  private Long mismatchBillItemCount;
  private Long mismatchPaidCount;
  private Long missingPaymentFlowCount;
  private Long totalIssueCount;
  private List<IssueItem> issues = new ArrayList<>();

  @Data
  public static class IssueItem {
    private String issueType;
    private String issueTypeLabel;
    private Long billId;
    private Long paymentId;
    private Long elderId;
    private String elderName;
    private BigDecimal expectedAmount;
    private BigDecimal actualAmount;
    private String detail;
  }
}
