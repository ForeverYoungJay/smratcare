package com.zhiyangyun.care.finance.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FinanceMonthCloseExecuteResponse {
  private boolean success;
  private String status;
  private String statusLabel;
  private String message;
  private String month;
  private String actorName;
  private LocalDateTime occurredAt;
}
