package com.zhiyangyun.care.operations.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.health.entity.HealthMedicationTask;
import com.zhiyangyun.care.health.mapper.HealthMedicationTaskMapper;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.operations.notify.StaffSubscribeMessageSender;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 员工移动端超时待办提醒：每 15 分钟扫描今日已过计划时间仍未完成的
 * 护理任务（CareTaskDaily）与用药任务（HealthMedicationTask），
 * 为对应负责员工生成 OA 待办（oa_todo），并通过 {@link StaffSubscribeMessageSender}
 * 推送提醒（默认实现仅记录日志）。
 *
 * <p>去重策略：待办标题中带任务 id，按「同一负责员工 + 同一标题 + 当天创建」查重，
 * 保证当天同一任务只提醒一次。无负责员工的任务跳过并记录 debug 日志。
 */
@Component
public class StaffMobileOverdueReminderScheduler {
  private static final Logger log = LoggerFactory.getLogger(StaffMobileOverdueReminderScheduler.class);
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
  private static final int SCAN_LIMIT = 200;

  private final CareTaskDailyMapper careTaskDailyMapper;
  private final HealthMedicationTaskMapper healthMedicationTaskMapper;
  private final OaTodoMapper todoMapper;
  private final StaffSubscribeMessageSender subscribeMessageSender;

  public StaffMobileOverdueReminderScheduler(
      CareTaskDailyMapper careTaskDailyMapper,
      HealthMedicationTaskMapper healthMedicationTaskMapper,
      OaTodoMapper todoMapper,
      StaffSubscribeMessageSender subscribeMessageSender) {
    this.careTaskDailyMapper = careTaskDailyMapper;
    this.healthMedicationTaskMapper = healthMedicationTaskMapper;
    this.todoMapper = todoMapper;
    this.subscribeMessageSender = subscribeMessageSender;
  }

  @Scheduled(cron = "0 */15 * * * *")
  public void remindOverdueStaffMobileTasks() {
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();
    remindOverdueCareTasks(today, now);
    remindOverdueMedicationTasks(today, now);
  }

  private void remindOverdueCareTasks(LocalDate today, LocalDateTime now) {
    List<CareTaskDaily> tasks = careTaskDailyMapper.selectList(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(CareTaskDaily::getTaskDate, today)
        .ne(CareTaskDaily::getStatus, "DONE")
        .isNotNull(CareTaskDaily::getPlanTime)
        .lt(CareTaskDaily::getPlanTime, now)
        .last("limit " + SCAN_LIMIT));
    for (CareTaskDaily task : tasks) {
      Long staffId = task.getAssignedStaffId();
      if (staffId == null) {
        log.debug("护理任务 {} 未分配负责员工，跳过超时提醒", task.getId());
        continue;
      }
      String planTimeText = task.getPlanTime() == null ? "" : task.getPlanTime().format(TIME_FMT);
      String title = "护理任务超时提醒：任务#" + task.getId()
          + (task.getElderId() == null ? "" : "（老人 #" + task.getElderId() + "）");
      String content = "今日护理任务计划时间 " + planTimeText + " 已超时未完成，请尽快处理并在员工端提交回执。";
      createTodoOnce(task.getTenantId(), task.getOrgId(), staffId, title, content, today, planTimeText);
    }
  }

  private void remindOverdueMedicationTasks(LocalDate today, LocalDateTime now) {
    List<HealthMedicationTask> tasks = healthMedicationTaskMapper.selectList(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(HealthMedicationTask::getTaskDate, today)
        .eq(HealthMedicationTask::getStatus, "PENDING")
        .isNotNull(HealthMedicationTask::getPlannedTime)
        .lt(HealthMedicationTask::getPlannedTime, now)
        .last("limit " + SCAN_LIMIT));
    for (HealthMedicationTask task : tasks) {
      // HealthMedicationTask 没有分配人字段，退化为提醒登记人（createdBy）；无登记人则跳过
      Long staffId = task.getCreatedBy();
      if (staffId == null) {
        log.debug("用药任务 {} 无负责员工（createdBy 为空），跳过超时提醒", task.getId());
        continue;
      }
      String planTimeText = task.getPlannedTime() == null ? "" : task.getPlannedTime().format(TIME_FMT);
      String title = "用药任务超时提醒：任务#" + task.getId()
          + "（" + safeText(task.getElderName(), "老人") + " · " + safeText(task.getDrugName(), "用药") + "）";
      String content = "今日用药任务计划时间 " + planTimeText + " 已超时未确认，请尽快核对发药并确认。";
      createTodoOnce(task.getTenantId(), task.getOrgId(), staffId, title, content, today, planTimeText);
    }
  }

  /** 当天同一任务（同标题+同负责员工）只生成一次待办；创建成功后同步推送订阅消息。 */
  private void createTodoOnce(Long tenantId, Long orgId, Long staffId, String title, String content,
      LocalDate today, String planTimeText) {
    OaTodo exists = todoMapper.selectOne(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(OaTodo::getAssigneeId, staffId)
        .eq(OaTodo::getTitle, title)
        .ge(OaTodo::getCreateTime, today.atStartOfDay())
        .last("LIMIT 1"));
    if (exists != null) {
      return;
    }
    LocalDateTime now = LocalDateTime.now();
    OaTodo todo = new OaTodo();
    todo.setTenantId(tenantId == null ? orgId : tenantId);
    todo.setOrgId(orgId);
    todo.setTitle(title);
    todo.setContent(content);
    todo.setDueTime(today.atTime(23, 59));
    todo.setStatus("OPEN");
    todo.setAssigneeId(staffId);
    todo.setCreatedBy(0L);
    todo.setCreateTime(now);
    todo.setUpdateTime(now);
    todo.setIsDeleted(0);
    todoMapper.insert(todo);
    subscribeMessageSender.sendOverdueReminder(staffId, title, planTimeText);
  }

  private String safeText(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
  }
}
