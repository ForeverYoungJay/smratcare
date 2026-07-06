package com.zhiyangyun.care.cockpit.model;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/**
 * 经营驾驶舱 BI 总览：本月关键指标卡片（含环比）。
 */
@Data
public class CockpitBiSummaryResponse {
  private Long orgId;
  /** 统计月（yyyy-MM） */
  private String month;
  /** 汇总数据最新日期（无数据时为 null） */
  private LocalDate latestStatDate;
  private List<CockpitBiMetricCard> cards;
}
