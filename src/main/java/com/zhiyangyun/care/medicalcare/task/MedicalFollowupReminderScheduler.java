package com.zhiyangyun.care.medicalcare.task;

import com.zhiyangyun.care.medicalcare.service.MedicalFollowupService;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 每日为到期（含逾期）慢病随访计划生成护理待办提醒。 */
@Component
public class MedicalFollowupReminderScheduler {

  private final MedicalFollowupService followupService;

  public MedicalFollowupReminderScheduler(MedicalFollowupService followupService) {
    this.followupService = followupService;
  }

  @Scheduled(cron = "0 40 0 * * ?")
  public void generateDueReminders() {
    followupService.generateDueReminders(LocalDate.now());
  }
}
