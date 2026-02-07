package com.zhiyangyun.care.task;

import com.zhiyangyun.care.service.CareTaskService;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyCareTaskScheduler {
  private final CareTaskService careTaskService;

  public DailyCareTaskScheduler(CareTaskService careTaskService) {
    this.careTaskService = careTaskService;
  }

  @Scheduled(cron = "0 5 0 * * ?")
  public void generateTodayTasks() {
    careTaskService.generateDailyTasks(LocalDate.now());
  }
}
