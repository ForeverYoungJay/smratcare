package com.zhiyangyun.care.finance.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FinanceHandleLogItem {
  private Long id;
  private String sourceModule;
  private String sourceModuleLabel;
  private String issueType;
  private String issueTypeLabel;
  private String status;
  private String statusLabel;
  private String stage;
  private String stageLabel;
  private String nextAction;
  private String remark;
  private LocalDate promisedDate;
  private String ownerName;
  private LocalDate dueDate;
  private String reviewResult;
  private String contactName;
  private String contactChannel;
  private String contactResult;
  private LocalDateTime nextReminderAt;
  private Long billId;
  private Long paymentId;
  private Long elderId;
  private String actorName;
  private LocalDateTime createTime;
}
