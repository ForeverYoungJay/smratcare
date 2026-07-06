package com.zhiyangyun.care.cockpit.service;

import java.time.LocalDate;

/**
 * 经营驾驶舱 BI 每日预聚合服务：从业务表计算指定日的运营/财务/护理汇总并 upsert 到
 * stats_daily_operation / stats_daily_finance / stats_daily_care 三张汇总表。
 */
public interface CockpitDailyStatsService {

  /** 计算并 upsert 指定机构指定日期的三张每日汇总（重算幂等）。 */
  void aggregate(Long orgId, LocalDate statDate);

  /** 对所有机构聚合指定日期（定时任务/全量重算用），返回处理机构数。 */
  int aggregateAllOrgs(LocalDate statDate);
}
