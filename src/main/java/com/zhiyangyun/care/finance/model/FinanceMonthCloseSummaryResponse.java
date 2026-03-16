package com.zhiyangyun.care.finance.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceMonthCloseSummaryResponse {
  private String month;
  private Integer completionRate;
  private Integer totalSteps;
  private Integer completedSteps;
  private Integer blockedSteps;
  private Integer warningSteps;
  private Long billCount;
  private Long settledBillCount;
  private Long outstandingBillCount;
  private Long paymentCount;
  private Long adjustmentCount;
  private Long unlinkedInvoiceCount;
  private Long issueCount;
  private String closeStatus;
  private String closeStatusLabel;
  private String closeRemark;
  private String closedBy;
  private java.time.LocalDateTime closedAt;
  private Boolean locked;
  private String lockStatusLabel;
  private String unlockedBy;
  private java.time.LocalDateTime unlockedAt;
  private String unlockReason;
  private Boolean canClose;
  private List<String> notes = new ArrayList<>();
  private List<Step> steps = new ArrayList<>();

  @Data
  public static class Step {
    private String key;
    private String label;
    private String status;
    private String statusLabel;
    private Long count;
    private String detail;
    private String actionPath;
    private String actionLabel;
  }
}
