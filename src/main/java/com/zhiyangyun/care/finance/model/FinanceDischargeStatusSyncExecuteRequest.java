package com.zhiyangyun.care.finance.model;

import lombok.Data;

@Data
public class FinanceDischargeStatusSyncExecuteRequest {
  private Long settlementId;
  private Long elderId;
  private Boolean processAll;
  private String remark;
}
