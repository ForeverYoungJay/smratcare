package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrExpenseItemResponse {
  private Long id;
  private String expenseType;
  private String title;
  private String applicantName;
  private BigDecimal amount;
  private String status;
  private LocalDateTime applyStartTime;
  private LocalDateTime applyEndTime;
  private LocalDateTime createTime;
  private String remark;
}
