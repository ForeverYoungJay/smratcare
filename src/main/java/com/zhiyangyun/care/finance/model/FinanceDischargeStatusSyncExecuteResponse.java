package com.zhiyangyun.care.finance.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceDischargeStatusSyncExecuteResponse {
  private int processedCount;
  private int successCount;
  private int failCount;
  private List<RowResult> results = new ArrayList<>();

  @Data
  public static class RowResult {
    private Long settlementId;
    private Long elderId;
    private boolean success;
    private String message;
  }
}
