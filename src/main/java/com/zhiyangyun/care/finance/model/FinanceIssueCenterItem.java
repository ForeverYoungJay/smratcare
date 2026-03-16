package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FinanceIssueCenterItem {
  private String sourceModule;
  private String sourceModuleLabel;
  private String issueType;
  private String issueTypeLabel;
  private String riskLevel;
  private String riskLevelLabel;
  private Long billId;
  private Long paymentId;
  private Long elderId;
  private String elderName;
  private BigDecimal amount;
  private String detail;
  private String suggestion;
  private String actionPath;
  private String actionLabel;
  private String latestHandleStatus;
  private String latestHandleStatusLabel;
  private String latestHandleRemark;
  private String latestOwnerName;
  private LocalDate latestDueDate;
  private String latestReviewResult;
  private LocalDateTime latestHandleAt;
  private LocalDateTime occurredAt;
}
