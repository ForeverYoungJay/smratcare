package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FinanceCollectionFollowUpItem {
  private Long elderId;
  private String elderName;
  private Long primaryBillId;
  private String oldestBillMonth;
  private String latestBillMonth;
  private BigDecimal outstandingAmount;
  private BigDecimal balance;
  private Integer overdueMonths;
  private String riskLevel;
  private String riskLevelLabel;
  private String stage;
  private String stageLabel;
  private LocalDateTime lastPaymentAt;
  private LocalDate contractExpireDate;
  private String followUpReason;
  private String suggestion;
  private String actionPath;
  private String actionLabel;
  private String latestHandleStatus;
  private String latestHandleStatusLabel;
  private String latestHandleRemark;
  private String latestOwnerName;
  private String latestContactName;
  private String latestContactChannel;
  private String latestContactResult;
  private LocalDate promisedDate;
  private LocalDateTime nextReminderAt;
  private LocalDateTime latestHandleAt;
}
