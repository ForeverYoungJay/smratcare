package com.zhiyangyun.care.fire.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.Org;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.fire.service.FireSafetyReminderService;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FireSafetyReminderScheduler {
  private static final Logger log = LoggerFactory.getLogger(FireSafetyReminderScheduler.class);

  private final FireSafetyReminderService reminderService;
  private final OrgMapper orgMapper;

  @Value("${app.fire.daily-reminder.enabled:true}")
  private boolean dailyReminderEnabled;

  public FireSafetyReminderScheduler(
      FireSafetyReminderService reminderService,
      OrgMapper orgMapper) {
    this.reminderService = reminderService;
    this.orgMapper = orgMapper;
  }

  @Scheduled(cron = "${app.fire.daily-reminder.cron:0 */10 * * * ?}")
  public void createDailyReminders() {
    if (!dailyReminderEnabled) {
      return;
    }
    LocalDate today = LocalDate.now();
    List<Org> orgs = orgMapper.selectList(Wrappers.lambdaQuery(Org.class)
        .eq(Org::getIsDeleted, 0)
        .eq(Org::getStatus, 1));
    for (Org org : orgs) {
      Long orgId = org == null ? null : org.getId();
      if (orgId == null) {
        continue;
      }
      try {
        int createdCount = reminderService.generateDailyReminders(orgId, today, 0L);
        if (createdCount > 0) {
          log.info("fire-daily-reminder orgId={} created={}", orgId, createdCount);
        }
      } catch (RuntimeException ex) {
        log.error("fire-daily-reminder failed for orgId={}", orgId, ex);
      }
    }
  }
}
