package com.zhiyangyun.care.finance.model;

import lombok.Data;

@Data
public class FinanceMonthUnlockRequest {
  private String month;
  private String reason;
  private String remark;
}
