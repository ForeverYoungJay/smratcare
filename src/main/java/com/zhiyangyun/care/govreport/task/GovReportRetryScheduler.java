package com.zhiyangyun.care.govreport.task;

import com.zhiyangyun.care.govreport.service.GovReportService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 定时重试失败的监管/医保上报任务。 */
@Component
public class GovReportRetryScheduler {

  private static final int MAX_RETRY = 5;

  private final GovReportService govReportService;

  public GovReportRetryScheduler(GovReportService govReportService) {
    this.govReportService = govReportService;
  }

  @Scheduled(cron = "0 */30 * * * ?")
  public void retryFailedTasks() {
    govReportService.retryFailed(MAX_RETRY);
  }
}
