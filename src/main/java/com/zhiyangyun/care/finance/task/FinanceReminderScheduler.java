package com.zhiyangyun.care.finance.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.Org;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.finance.service.FinanceReminderService;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 财务提醒定时任务：18 号生成下月代养费提醒，每日生成押金与电费提醒。 */
@Component
public class FinanceReminderScheduler {
  private static final Logger log = LoggerFactory.getLogger(FinanceReminderScheduler.class);

  private final FinanceReminderService reminderService;
  private final OrgMapper orgMapper;

  @Value("${app.finance.reminder.enabled:true}")
  private boolean reminderEnabled;

  public FinanceReminderScheduler(FinanceReminderService reminderService, OrgMapper orgMapper) {
    this.reminderService = reminderService;
    this.orgMapper = orgMapper;
  }

  /** 每月 18 号 09:00：下月代养费提醒。 */
  @Scheduled(cron = "${app.finance.reminder.care-fee-cron:0 0 9 18 * ?}")
  public void createNextMonthCareFeeReminders() {
    if (!reminderEnabled) {
      return;
    }
    LocalDate today = LocalDate.now();
    for (Long orgId : activeOrgIds()) {
      try {
        int created = reminderService.generateNextMonthCareFeeReminder(orgId, today);
        if (created > 0) {
          log.info("finance-reminder care-fee orgId={} created={}", orgId, created);
        }
      } catch (RuntimeException ex) {
        log.error("finance-reminder care-fee failed for orgId={}", orgId, ex);
      }
    }
  }

  /** 每日 09:10：押金未缴清、电费未登记/未缴提醒。 */
  @Scheduled(cron = "${app.finance.reminder.daily-cron:0 10 9 * * ?}")
  public void createDailyReminders() {
    if (!reminderEnabled) {
      return;
    }
    LocalDate today = LocalDate.now();
    for (Long orgId : activeOrgIds()) {
      try {
        int deposit = reminderService.generateDepositShortfallReminders(orgId, today);
        int electricity = reminderService.generateElectricityReminders(orgId, today);
        if (deposit + electricity > 0) {
          log.info("finance-reminder daily orgId={} deposit={} electricity={}", orgId, deposit, electricity);
        }
      } catch (RuntimeException ex) {
        log.error("finance-reminder daily failed for orgId={}", orgId, ex);
      }
    }
  }

  private List<Long> activeOrgIds() {
    return orgMapper.selectList(
            Wrappers.lambdaQuery(Org.class)
                .eq(Org::getIsDeleted, 0)
                .eq(Org::getStatus, 1))
        .stream()
        .map(Org::getId)
        .filter(java.util.Objects::nonNull)
        .toList();
  }
}
