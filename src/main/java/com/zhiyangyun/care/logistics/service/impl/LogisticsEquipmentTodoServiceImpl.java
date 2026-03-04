package com.zhiyangyun.care.logistics.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.logistics.entity.LogisticsEquipmentArchive;
import com.zhiyangyun.care.logistics.entity.LogisticsMaintenanceTodoJobLog;
import com.zhiyangyun.care.logistics.mapper.LogisticsEquipmentArchiveMapper;
import com.zhiyangyun.care.logistics.mapper.LogisticsMaintenanceTodoJobLogMapper;
import com.zhiyangyun.care.logistics.model.LogisticsMaintenanceTodoGenerateResult;
import com.zhiyangyun.care.logistics.service.LogisticsEquipmentTodoService;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class LogisticsEquipmentTodoServiceImpl implements LogisticsEquipmentTodoService {
  private final LogisticsEquipmentArchiveMapper equipmentMapper;
  private final LogisticsMaintenanceTodoJobLogMapper jobLogMapper;
  private final OaTodoMapper oaTodoMapper;
  private final OaTaskMapper oaTaskMapper;

  public LogisticsEquipmentTodoServiceImpl(
      LogisticsEquipmentArchiveMapper equipmentMapper,
      LogisticsMaintenanceTodoJobLogMapper jobLogMapper,
      OaTodoMapper oaTodoMapper,
      OaTaskMapper oaTaskMapper) {
    this.equipmentMapper = equipmentMapper;
    this.jobLogMapper = jobLogMapper;
    this.oaTodoMapper = oaTodoMapper;
    this.oaTaskMapper = oaTaskMapper;
  }

  @Override
  public LogisticsMaintenanceTodoGenerateResult generateMaintenanceTodos(
      Long orgId,
      int days,
      Long operatorStaffId) {
    LocalDateTime now = LocalDateTime.now();
    int effectiveDays = Math.max(days, 1);
    LocalDateTime dueEnd = now.plusDays(effectiveDays);
    List<LogisticsEquipmentArchive> dueSoonEquipments = equipmentMapper.selectList(
        Wrappers.lambdaQuery(LogisticsEquipmentArchive.class)
            .eq(LogisticsEquipmentArchive::getIsDeleted, 0)
            .eq(orgId != null, LogisticsEquipmentArchive::getOrgId, orgId)
            .in(LogisticsEquipmentArchive::getStatus, List.of("ENABLED", "MAINTENANCE"))
            .isNotNull(LogisticsEquipmentArchive::getNextMaintainedAt)
            .between(LogisticsEquipmentArchive::getNextMaintainedAt, now, dueEnd));

    long created = 0;
    long skipped = 0;
    for (LogisticsEquipmentArchive equipment : dueSoonEquipments) {
      Long currentOrgId = equipment.getOrgId();
      if (currentOrgId == null) {
        continue;
      }
      String title = buildTodoTitle(equipment);
      LocalDate dueDate = equipment.getNextMaintainedAt().toLocalDate();
      OaTodo existing = oaTodoMapper.selectOne(Wrappers.lambdaQuery(OaTodo.class)
          .eq(OaTodo::getIsDeleted, 0)
          .eq(OaTodo::getOrgId, currentOrgId)
          .eq(OaTodo::getStatus, "OPEN")
          .eq(OaTodo::getTitle, title)
          .ge(OaTodo::getDueTime, dueDate.atStartOfDay())
          .lt(OaTodo::getDueTime, dueDate.plusDays(1).atStartOfDay())
          .last("LIMIT 1"));
      if (existing != null) {
        skipped++;
        continue;
      }
      OaTodo todo = new OaTodo();
      todo.setTenantId(currentOrgId);
      todo.setOrgId(currentOrgId);
      todo.setTitle(title);
      todo.setContent(buildMaintenanceDescription(equipment));
      todo.setDueTime(equipment.getNextMaintainedAt());
      todo.setStatus("OPEN");
      todo.setAssigneeName(equipment.getMaintainerName());
      todo.setCreatedBy(operatorStaffId == null ? 0L : operatorStaffId);
      oaTodoMapper.insert(todo);
      syncTodoTask(todo);
      created++;
    }

    LogisticsMaintenanceTodoGenerateResult result = new LogisticsMaintenanceTodoGenerateResult();
    result.setCreatedCount(created);
    result.setSkippedCount(skipped);
    result.setTotalMatched((long) dueSoonEquipments.size());
    return result;
  }

  @Override
  public void saveJobLog(
      Long orgId,
      String triggerType,
      int days,
      LogisticsMaintenanceTodoGenerateResult result,
      String status,
      String errorMessage,
      Long operatorStaffId) {
    LogisticsMaintenanceTodoJobLog log = new LogisticsMaintenanceTodoJobLog();
    log.setTenantId(orgId);
    log.setOrgId(orgId);
    log.setTriggerType(triggerType);
    log.setStatus(status);
    log.setDays(Math.max(days, 1));
    log.setTotalMatched(result == null ? 0L : result.getTotalMatched());
    log.setCreatedCount(result == null ? 0L : result.getCreatedCount());
    log.setSkippedCount(result == null ? 0L : result.getSkippedCount());
    log.setErrorMessage(errorMessage);
    log.setExecutedAt(LocalDateTime.now());
    log.setCreatedBy(operatorStaffId);
    jobLogMapper.insert(log);
  }

  private String buildTodoTitle(LogisticsEquipmentArchive equipment) {
    return "【设备维保提醒】" + equipment.getEquipmentCode() + " / " + equipment.getEquipmentName();
  }

  private String buildMaintenanceDescription(LogisticsEquipmentArchive equipment) {
    StringBuilder sb = new StringBuilder();
    sb.append("设备维保提醒：");
    sb.append(equipment.getEquipmentCode()).append(" / ").append(equipment.getEquipmentName());
    if (equipment.getLocation() != null && !equipment.getLocation().isBlank()) {
      sb.append("，位置：").append(equipment.getLocation());
    }
    if (equipment.getSerialNo() != null && !equipment.getSerialNo().isBlank()) {
      sb.append("，序列号：").append(equipment.getSerialNo());
    }
    sb.append("，计划维保时间：").append(Objects.toString(equipment.getNextMaintainedAt(), "-"));
    return sb.toString();
  }

  private void syncTodoTask(OaTodo todo) {
    if (todo == null || todo.getId() == null) {
      return;
    }
    Long orgId = todo.getOrgId();
    OaTask task = oaTaskMapper.selectOne(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(OaTask::getSourceTodoId, todo.getId())
        .last("LIMIT 1"));
    if (task == null) {
      task = new OaTask();
      task.setTenantId(todo.getTenantId());
      task.setOrgId(todo.getOrgId());
      task.setCreatedBy(todo.getCreatedBy());
      task.setSourceTodoId(todo.getId());
    }
    task.setTitle("【代办】" + (todo.getTitle() == null ? "" : todo.getTitle()));
    task.setDescription(todo.getContent());
    task.setStartTime(todo.getDueTime());
    task.setEndTime(todo.getDueTime());
    task.setPriority("HIGH");
    task.setStatus("OPEN");
    task.setAssigneeId(todo.getAssigneeId());
    task.setAssigneeName(todo.getAssigneeName());
    task.setCalendarType("DAILY");
    task.setPlanCategory("设备维保");
    task.setUrgency("HIGH");
    task.setEventColor("#fa8c16");
    task.setIsRecurring(0);
    if (task.getId() == null) {
      oaTaskMapper.insert(task);
    } else {
      oaTaskMapper.updateById(task);
    }
  }
}
