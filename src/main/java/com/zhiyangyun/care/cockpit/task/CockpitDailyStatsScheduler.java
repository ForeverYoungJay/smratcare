package com.zhiyangyun.care.cockpit.task;

import com.zhiyangyun.care.cockpit.service.CockpitDailyStatsService;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 经营驾驶舱 BI 每日预聚合定时任务：每日凌晨从业务表计算「昨日」的
 * 运营/财务/护理三张每日汇总（stats_daily_*），大屏只查汇总表不直查业务库。
 * 支持通过 POST /stats/executive/bi/recalc?date= 手动补算历史日期。
 */
@Component
public class CockpitDailyStatsScheduler {
  private static final Logger log = LoggerFactory.getLogger(CockpitDailyStatsScheduler.class);

  private final CockpitDailyStatsService cockpitDailyStatsService;

  public CockpitDailyStatsScheduler(CockpitDailyStatsService cockpitDailyStatsService) {
    this.cockpitDailyStatsService = cockpitDailyStatsService;
  }

  /** 每日 01:30 聚合昨日数据（晚于 01:10 的快照任务，错峰）。 */
  @Scheduled(cron = "0 30 1 * * ?")
  public void aggregateYesterday() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    try {
      int count = cockpitDailyStatsService.aggregateAllOrgs(yesterday);
      log.info("[CockpitBI] 每日汇总预聚合完成，date={}，机构数={}", yesterday, count);
    } catch (Exception ex) {
      log.error("[CockpitBI] 每日汇总预聚合失败，date={}", yesterday, ex);
    }
  }
}
