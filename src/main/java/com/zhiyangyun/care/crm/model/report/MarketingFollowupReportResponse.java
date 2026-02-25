package com.zhiyangyun.care.crm.model.report;

import java.util.List;
import lombok.Data;

@Data
public class MarketingFollowupReportResponse {
  private long totalLeads;
  private long consultCount;
  private long intentCount;
  private long reservationCount;
  private long invalidCount;
  private long overdueCount;
  private List<StageItem> stages;

  @Data
  public static class StageItem {
    private String name;
    private long count;
    private double rate;
  }
}
