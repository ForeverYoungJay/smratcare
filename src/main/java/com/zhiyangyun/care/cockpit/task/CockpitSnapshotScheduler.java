package com.zhiyangyun.care.cockpit.task;

import com.zhiyangyun.care.cockpit.service.CockpitMetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 经营驾驶舱指标预聚合定时任务：每日凌晨为所有机构生成经营指标快照。
 */
@Component
public class CockpitSnapshotScheduler {
  private static final Logger log = LoggerFactory.getLogger(CockpitSnapshotScheduler.class);

  private final CockpitMetricService cockpitMetricService;

  public CockpitSnapshotScheduler(CockpitMetricService cockpitMetricService) {
    this.cockpitMetricService = cockpitMetricService;
  }

  /** 每日 01:10 生成当日快照。 */
  @Scheduled(cron = "0 10 1 * * ?")
  public void generateDailySnapshot() {
    try {
      int count = cockpitMetricService.snapshotAllOrgsToday();
      log.info("[Cockpit] 经营指标快照生成完成，机构数={}", count);
    } catch (Exception ex) {
      log.error("[Cockpit] 经营指标快照生成失败", ex);
    }
  }
}
