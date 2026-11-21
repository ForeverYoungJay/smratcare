package com.zhiyangyun.care.nursing.model;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class NursingQualityMetricsResponse {
  private LocalDate dateFrom;
  private LocalDate dateTo;
  private List<Metric> metrics;
  private List<Workload> workloads;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Metric {
    private String key;
    private String label;
    private long numerator;
    private long denominator;
    /** 百分比 0-100，保留 1 位小数；分母为 0 时为 null */
    private Double rate;
  }

  @Data
  public static class Workload {
    private Long staffId;
    private String staffName;
    private long taskCount;
    private long doneCount;
    private long overdueCount;
  }
}
