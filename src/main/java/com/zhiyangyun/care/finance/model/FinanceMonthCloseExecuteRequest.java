package com.zhiyangyun.care.finance.model;

import lombok.Data;

@Data
public class FinanceMonthCloseExecuteRequest {
  private String month;
  private Boolean forceClose;
  private String remark;
}
