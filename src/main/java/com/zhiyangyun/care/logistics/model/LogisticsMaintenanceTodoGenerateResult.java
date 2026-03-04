package com.zhiyangyun.care.logistics.model;

import lombok.Data;

@Data
public class LogisticsMaintenanceTodoGenerateResult {
  private long createdCount;
  private long skippedCount;
  private long totalMatched;
}
