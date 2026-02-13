package com.zhiyangyun.care.service.task;

import com.zhiyangyun.care.service.CareTaskService;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CareTaskScheduler {
  private final CareTaskService careTaskService;

  public CareTaskScheduler(CareTaskService careTaskService) {
    this.careTaskService = careTaskService;
  }

  @Scheduled(cron = "0 5 0 * * ?")
  public void generateDailyTasks() {
    careTaskService.generateDailyTasks(null, LocalDate.now(), false);
  }
}
