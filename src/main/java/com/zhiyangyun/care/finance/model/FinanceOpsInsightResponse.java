package com.zhiyangyun.care.finance.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceOpsInsightResponse {
  private LocalDateTime generatedAt;
  private Integer totalInsights;
  private Integer highPriorityCount;
  private List<Item> items = new ArrayList<>();

  @Data
  public static class Item {
    private String level;
    private String title;
    private String detail;
    private String suggestion;
    private String actionPath;
    private String actionLabel;
    private Long affectedCount;
  }
}
