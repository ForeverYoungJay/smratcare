package com.zhiyangyun.care.health.task;

import com.zhiyangyun.care.health.service.HealthMedicationTaskService;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HealthMedicationTaskScheduler {
  private final HealthMedicationTaskService medicationTaskService;

  public HealthMedicationTaskScheduler(HealthMedicationTaskService medicationTaskService) {
    this.medicationTaskService = medicationTaskService;
  }

  @Scheduled(cron = "0 */30 * * * *")
  public void generateTodayPendingTasks() {
    medicationTaskService.generateTasksForDate(LocalDate.now());
  }
}
