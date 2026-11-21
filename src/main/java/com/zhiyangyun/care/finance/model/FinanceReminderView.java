package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FinanceReminderView {
  private Long id;
  private String reminderType;
  private String reminderTypeText;
  private String title;
  private String content;
  private String severity;
  private String bizMonth;
  private Long elderId;
  private String elderName;
  private Long roomId;
  private BigDecimal amount;
  private String actionPath;
  private String status;
  private String statusText;
  private LocalDateTime handledAt;
  private String handleRemark;
  private LocalDateTime createTime;
}
