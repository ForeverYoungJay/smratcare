package com.zhiyangyun.care.fire.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.fire.entity.FireSafetyRecord;
import com.zhiyangyun.care.fire.mapper.FireSafetyRecordMapper;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FireSafetyReminderService {
  private static final List<String> ACTIVE_STATUSES = List.of("OPEN", "RUNNING");

  private final FireSafetyRecordMapper recordMapper;
  private final OaTodoMapper oaTodoMapper;
  private final OaTaskMapper oaTaskMapper;
  private final StaffMapper staffMapper;

  public FireSafetyReminderService(
      FireSafetyRecordMapper recordMapper,
      OaTodoMapper oaTodoMapper,
      OaTaskMapper oaTaskMapper,
      StaffMapper staffMapper) {
    this.recordMapper = recordMapper;
    this.oaTodoMapper = oaTodoMapper;
    this.oaTaskMapper = oaTaskMapper;
    this.staffMapper = staffMapper;
  }

  public int generateDailyReminders(Long orgId, LocalDate targetDate, Long operatorStaffId) {
    if (orgId == null || targetDate == null) {
      return 0;
    }
    LocalDateTime now = LocalDateTime.now();
    List<FireSafetyRecord> records = recordMapper.selectList(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(FireSafetyRecord::getOrgId, orgId)
        .eq(FireSafetyRecord::getDailyReminderEnabled, true)
        .isNotNull(FireSafetyRecord::getDailyReminderTime)
        .in(FireSafetyRecord::getStatus, ACTIVE_STATUSES)
        .orderByAsc(FireSafetyRecord::getDailyReminderTime)
        .orderByAsc(FireSafetyRecord::getId));

    int createdCount = 0;
    for (FireSafetyRecord record : records) {
      LocalDateTime dueTime = LocalDateTime.of(targetDate, record.getDailyReminderTime());
      if (dueTime.isAfter(now)) {
        continue;
      }
      String title = buildTodoTitle(record);
      String content = buildTodoContent(record, dueTime);
      OaTodo existing = oaTodoMapper.selectOne(Wrappers.lambdaQuery(OaTodo.class)
          .eq(OaTodo::getIsDeleted, 0)
          .eq(OaTodo::getOrgId, orgId)
          .eq(OaTodo::getTitle, title)
          .eq(OaTodo::getContent, content)
          .ge(OaTodo::getDueTime, targetDate.atStartOfDay())
          .lt(OaTodo::getDueTime, targetDate.plusDays(1).atStartOfDay())
          .last("LIMIT 1"));
      if (existing != null) {
        continue;
      }

      StaffAccount assignee = resolveAssignee(orgId, record.getInspectorName());
      OaTodo todo = new OaTodo();
      todo.setTenantId(orgId);
      todo.setOrgId(orgId);
      todo.setTitle(title);
      todo.setContent(content);
      todo.setDueTime(dueTime);
      todo.setStatus("OPEN");
      todo.setAssigneeId(assignee == null ? null : assignee.getId());
      todo.setAssigneeName(resolveAssigneeName(assignee, record.getInspectorName()));
      todo.setCreatedBy(operatorStaffId == null ? 0L : operatorStaffId);
      oaTodoMapper.insert(todo);
      syncTodoTask(todo);
      createdCount++;
    }
    return createdCount;
  }

  private String buildTodoTitle(FireSafetyRecord record) {
    StringBuilder sb = new StringBuilder("【消防检查提醒】");
    sb.append(record.getTitle() == null ? "未命名项目" : record.getTitle());
    if (record.getLocation() != null && !record.getLocation().isBlank()) {
      sb.append(" / ").append(record.getLocation());
    }
    return sb.toString();
  }

  private String buildTodoContent(FireSafetyRecord record, LocalDateTime dueTime) {
    StringBuilder sb = new StringBuilder("请按时完成消防检查。");
    if (record.getId() != null) {
      sb.append("记录ID：").append(record.getId());
    }
    if (record.getTitle() != null && !record.getTitle().isBlank()) {
      if (record.getId() != null) {
        sb.append("；");
      }
      sb.append("标题：").append(record.getTitle());
    }
    if (record.getLocation() != null && !record.getLocation().isBlank()) {
      sb.append("；位置：").append(record.getLocation());
    }
    if (record.getInspectorName() != null && !record.getInspectorName().isBlank()) {
      sb.append("；负责人：").append(record.getInspectorName());
    }
    if (record.getCheckRound() != null) {
      sb.append("；检查轮次：第").append(record.getCheckRound()).append("次");
    }
    sb.append("；计划检查时间：").append(dueTime);
    return sb.toString();
  }

  private void syncTodoTask(OaTodo todo) {
    if (todo == null || todo.getId() == null) {
      return;
    }
    OaTask task = oaTaskMapper.selectOne(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(OaTask::getOrgId, todo.getOrgId())
        .eq(OaTask::getSourceTodoId, todo.getId())
        .last("LIMIT 1"));
    if (task == null) {
      task = new OaTask();
      task.setTenantId(todo.getTenantId());
      task.setOrgId(todo.getOrgId());
      task.setCreatedBy(todo.getCreatedBy());
      task.setSourceTodoId(todo.getId());
    }
    task.setTitle("【代办】" + todo.getTitle());
    task.setDescription(todo.getContent());
    task.setStartTime(todo.getDueTime());
    task.setEndTime(todo.getDueTime());
    task.setPriority("HIGH");
    task.setStatus("OPEN");
    task.setAssigneeId(todo.getAssigneeId());
    task.setAssigneeName(todo.getAssigneeName());
    task.setCalendarType("DAILY");
    task.setPlanCategory("消防检查");
    task.setUrgency("HIGH");
    task.setEventColor("#d46b08");
    task.setIsRecurring(0);
    if (task.getId() == null) {
      oaTaskMapper.insert(task);
    } else {
      oaTaskMapper.updateById(task);
    }
  }

  private StaffAccount resolveAssignee(Long orgId, String inspectorName) {
    String normalizedName = normalizeText(inspectorName);
    if (normalizedName == null) {
      return null;
    }
    StaffAccount enabled = staffMapper.selectOne(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(StaffAccount::getOrgId, orgId)
        .eq(StaffAccount::getStatus, 1)
        .and(w -> w.eq(StaffAccount::getRealName, normalizedName)
            .or()
            .eq(StaffAccount::getUsername, normalizedName))
        .last("LIMIT 1"));
    if (enabled != null) {
      return enabled;
    }
    return staffMapper.selectOne(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(StaffAccount::getOrgId, orgId)
        .and(w -> w.eq(StaffAccount::getRealName, normalizedName)
            .or()
            .eq(StaffAccount::getUsername, normalizedName))
        .last("LIMIT 1"));
  }

  private String resolveAssigneeName(StaffAccount assignee, String fallbackName) {
    if (assignee == null) {
      return normalizeText(fallbackName);
    }
    String realName = normalizeText(assignee.getRealName());
    if (realName != null) {
      return realName;
    }
    return normalizeText(assignee.getUsername());
  }

  private String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
