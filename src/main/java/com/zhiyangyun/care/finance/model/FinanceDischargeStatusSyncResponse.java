package com.zhiyangyun.care.finance.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceDischargeStatusSyncResponse {
  private LocalDateTime checkedAt;
  private Long settledCount;
  private Long issueCount;
  private List<Row> rows = new ArrayList<>();

  @Data
  public static class Row {
    private Long settlementId;
    private Long elderId;
    private String elderName;
    private LocalDateTime settledTime;
    private String settlementStatus;
    private Integer elderStatus;
    private String elderLifecycleStatus;
    private Long elderBedId;
    private Long occupiedBedId;
    private String issueType;
    private String issueMessage;
  }
}
