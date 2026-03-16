package com.zhiyangyun.care.finance.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FinanceHandleActionRequest {
  private String sourceModule;
  private String issueType;
  private Long billId;
  private Long paymentId;
  private Long elderId;
  private String status;
  private String stage;
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
}
