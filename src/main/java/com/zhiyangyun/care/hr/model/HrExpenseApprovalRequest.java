package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrExpenseApprovalRequest {
  private String title;
  private String expenseType;
  private String scene;
  private BigDecimal amount;
  private String status;
  private LocalDateTime applyStartTime;
  private LocalDateTime applyEndTime;
  private String remark;
}
