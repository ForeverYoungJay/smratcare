package com.zhiyangyun.care.logistics.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.logistics.entity.LogisticsVideoPolicy;
import com.zhiyangyun.care.logistics.mapper.LogisticsVideoPolicyMapper;
import com.zhiyangyun.care.logistics.service.LogisticsVideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LogisticsVideoCleanupScheduler {
  private static final Logger log = LoggerFactory.getLogger(LogisticsVideoCleanupScheduler.class);
  private final LogisticsVideoPolicyMapper policies;
  private final LogisticsVideoService service;

  public LogisticsVideoCleanupScheduler(LogisticsVideoPolicyMapper policies, LogisticsVideoService service) {
    this.policies = policies;
    this.service = service;
  }

  @Scheduled(cron = "${app.logistics.video.cleanup-cron:0 * * * * ?}")
  public void cleanup() {
    for (LogisticsVideoPolicy policy : policies.selectList(Wrappers.lambdaQuery(LogisticsVideoPolicy.class)
        .eq(LogisticsVideoPolicy::getAutoCleanup, true))) {
      try { service.cleanup(policy.getOrgId()); }
      catch (RuntimeException ex) { log.error("录像到期清理失败 orgId={}", policy.getOrgId(), ex); }
    }
  }
}
