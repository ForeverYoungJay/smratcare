package com.zhiyangyun.care.finance.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FinanceBillingConfigSnapshotItem {
  private String effectiveMonth;
  private Long configCount;
  private LocalDateTime latestUpdateTime;
}
