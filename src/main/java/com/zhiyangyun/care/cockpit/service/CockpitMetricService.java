package com.zhiyangyun.care.cockpit.service;

import com.zhiyangyun.care.cockpit.entity.CockpitMetricSnapshot;
import com.zhiyangyun.care.cockpit.model.CockpitOverviewResponse;

public interface CockpitMetricService {

  /** 计算并 upsert 指定机构指定日期的经营指标快照。 */
  CockpitMetricSnapshot snapshot(Long orgId, java.time.LocalDate statDate);

  /** 对所有机构生成当日快照（定时任务调用）。 */
  int snapshotAllOrgsToday();

  /** 经营总览：当日 KPI + 近 trendDays 日趋势。 */
  CockpitOverviewResponse overview(Long orgId, int trendDays);
}
