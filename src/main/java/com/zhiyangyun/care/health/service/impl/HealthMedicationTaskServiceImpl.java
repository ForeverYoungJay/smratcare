package com.zhiyangyun.care.health.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.health.entity.HealthMedicationRegistration;
import com.zhiyangyun.care.health.entity.HealthMedicationSetting;
import com.zhiyangyun.care.health.entity.HealthMedicationTask;
import com.zhiyangyun.care.health.mapper.HealthMedicationRegistrationMapper;
import com.zhiyangyun.care.health.mapper.HealthMedicationSettingMapper;
import com.zhiyangyun.care.health.mapper.HealthMedicationTaskMapper;
import com.zhiyangyun.care.health.model.HealthMedicationTaskActionRequest;
import com.zhiyangyun.care.health.service.HealthMedicationTaskService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthMedicationTaskServiceImpl implements HealthMedicationTaskService {
  private final HealthMedicationSettingMapper settingMapper;
  private final HealthMedicationTaskMapper taskMapper;
  private final HealthMedicationRegistrationMapper registrationMapper;

  public HealthMedicationTaskServiceImpl(
      HealthMedicationSettingMapper settingMapper,
      HealthMedicationTaskMapper taskMapper,
      HealthMedicationRegistrationMapper registrationMapper) {
    this.settingMapper = settingMapper;
    this.taskMapper = taskMapper;
    this.registrationMapper = registrationMapper;
  }

  @Override
  @Transactional
  public void generateTasksForDate(LocalDate date) {
    generateTasksForDate(null, date);
  }

  @Override
  @Transactional
  public void generateTasksForDate(Long orgId, LocalDate date) {
    List<HealthMedicationSetting> settings = settingMapper.selectList(Wrappers.lambdaQuery(HealthMedicationSetting.class)
        .eq(HealthMedicationSetting::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationSetting::getOrgId, orgId)
        .and(w -> w.isNull(HealthMedicationSetting::getStartDate)
            .or().le(HealthMedicationSetting::getStartDate, date))
        .and(w -> w.isNull(HealthMedicationSetting::getEndDate)
            .or().ge(HealthMedicationSetting::getEndDate, date)));
    for (HealthMedicationSetting setting : settings) {
      generateTasksForSetting(setting, date);
    }
  }

  @Override
  @Transactional
  public void generateTasksForSetting(HealthMedicationSetting setting, LocalDate date) {
    if (setting == null || setting.getOrgId() == null || setting.getElderId() == null) {
      return;
    }
    if (setting.getStartDate() != null && date.isBefore(setting.getStartDate())) {
      return;
    }
    if (setting.getEndDate() != null && date.isAfter(setting.getEndDate())) {
      return;
    }

    for (LocalTime time : resolveTimes(setting)) {
      LocalDateTime plannedTime = date.atTime(time);
      long exists = taskMapper.selectCount(Wrappers.lambdaQuery(HealthMedicationTask.class)
          .eq(HealthMedicationTask::getIsDeleted, 0)
          .eq(HealthMedicationTask::getOrgId, setting.getOrgId())
          .eq(HealthMedicationTask::getSettingId, setting.getId())
          .eq(HealthMedicationTask::getPlannedTime, plannedTime));
      if (exists > 0) {
        continue;
      }
      HealthMedicationTask task = new HealthMedicationTask();
      task.setTenantId(setting.getTenantId());
      task.setOrgId(setting.getOrgId());
      task.setSettingId(setting.getId());
      task.setElderId(setting.getElderId());
      task.setElderName(setting.getElderName());
      task.setDrugId(setting.getDrugId());
      task.setDrugName(setting.getDrugName());
      task.setTaskDate(date);
      task.setPlannedTime(plannedTime);
      task.setStatus("PENDING");
      task.setCreatedBy(setting.getCreatedBy());
      taskMapper.insert(task);
    }
  }

  @Override
  @Transactional
  public void completeTaskByRegistration(HealthMedicationRegistration registration) {
    if (registration == null || registration.getOrgId() == null || registration.getElderId() == null
        || registration.getRegisterTime() == null) {
      return;
    }
    clearLinkedTask(registration);
    LocalDate date = registration.getRegisterTime().toLocalDate();
    HealthMedicationTask task = taskMapper.selectOne(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(HealthMedicationTask::getOrgId, registration.getOrgId())
        .eq(HealthMedicationTask::getElderId, registration.getElderId())
        .eq(registration.getDrugId() != null, HealthMedicationTask::getDrugId, registration.getDrugId())
        .eq(registration.getDrugId() == null && registration.getDrugName() != null, HealthMedicationTask::getDrugName, registration.getDrugName())
        .eq(HealthMedicationTask::getTaskDate, date)
        .eq(HealthMedicationTask::getStatus, "PENDING")
        .le(HealthMedicationTask::getPlannedTime, registration.getRegisterTime().plusHours(2))
        .orderByDesc(HealthMedicationTask::getPlannedTime)
        .last("LIMIT 1"));

    if (task == null) {
      task = taskMapper.selectOne(Wrappers.lambdaQuery(HealthMedicationTask.class)
          .eq(HealthMedicationTask::getIsDeleted, 0)
          .eq(HealthMedicationTask::getOrgId, registration.getOrgId())
          .eq(HealthMedicationTask::getElderId, registration.getElderId())
          .eq(registration.getDrugId() != null, HealthMedicationTask::getDrugId, registration.getDrugId())
          .eq(registration.getDrugId() == null && registration.getDrugName() != null, HealthMedicationTask::getDrugName, registration.getDrugName())
          .eq(HealthMedicationTask::getTaskDate, date)
          .eq(HealthMedicationTask::getStatus, "PENDING")
          .ge(HealthMedicationTask::getPlannedTime, registration.getRegisterTime().minusHours(2))
          .orderByAsc(HealthMedicationTask::getPlannedTime)
          .last("LIMIT 1"));
    }

    if (task != null) {
      task.setStatus("DONE");
      task.setRegistrationId(registration.getId());
      task.setDoneTime(registration.getRegisterTime());
      taskMapper.updateById(task);
    }
  }

  @Override
  @Transactional
  public void unlinkTaskByRegistration(HealthMedicationRegistration registration) {
    if (registration == null) {
      return;
    }
    clearLinkedTask(registration);
  }

  @Override
  @Transactional
  public HealthMedicationTask completeTask(Long orgId, Long staffId, String staffName, Long taskId,
      HealthMedicationTaskActionRequest request) {
    HealthMedicationTask task = resolveTask(orgId, taskId);
    if ("DONE".equals(task.getStatus())) {
      return task;
    }
    HealthMedicationTaskActionRequest payload = request == null ? new HealthMedicationTaskActionRequest() : request;
    HealthMedicationSetting setting = task.getSettingId() == null ? null : settingMapper.selectById(task.getSettingId());
    LocalDateTime doneTime = payload.getDoneTime() == null ? LocalDateTime.now() : payload.getDoneTime();

    HealthMedicationRegistration registration = new HealthMedicationRegistration();
    registration.setTenantId(task.getTenantId());
    registration.setOrgId(task.getOrgId());
    registration.setElderId(task.getElderId());
    registration.setElderName(task.getElderName());
    registration.setDrugId(task.getDrugId());
    registration.setDrugName(task.getDrugName());
    registration.setRegisterTime(doneTime);
    registration.setDosageTaken(resolveDosage(payload.getDosageTaken(), setting));
    registration.setUnit(resolveUnit(payload.getUnit(), setting));
    registration.setNurseName(defaultText(payload.getNurseName(), staffName, "未填写"));
    registration.setRemark(defaultText(payload.getRemark(), "用药任务确认发药"));
    registration.setCreatedBy(staffId);
    registrationMapper.insert(registration);

    task.setStatus("DONE");
    task.setRegistrationId(registration.getId());
    task.setDoneTime(doneTime);
    task.setRemark(appendTaskRemark(task.getRemark(), payload.getRemark()));
    taskMapper.updateById(task);
    return task;
  }

  @Override
  @Transactional
  public HealthMedicationTask markTaskMissed(Long orgId, Long staffId, String staffName, Long taskId,
      HealthMedicationTaskActionRequest request) {
    HealthMedicationTask task = resolveTask(orgId, taskId);
    if ("DONE".equals(task.getStatus())) {
      throw new IllegalStateException("已完成任务不能标记漏服");
    }
    HealthMedicationTaskActionRequest payload = request == null ? new HealthMedicationTaskActionRequest() : request;
    task.setStatus("MISSED");
    task.setDoneTime(payload.getDoneTime() == null ? LocalDateTime.now() : payload.getDoneTime());
    task.setRemark(appendTaskRemark(task.getRemark(), defaultText(payload.getRemark(), "漏服/拒服，记录人：" + defaultText(staffName, "未填写"))));
    taskMapper.updateById(task);
    return task;
  }

  private List<LocalTime> resolveTimes(HealthMedicationSetting setting) {
    List<LocalTime> times = new ArrayList<>();
    if (setting.getMedicationTime() != null && !setting.getMedicationTime().isBlank()) {
      for (String token : setting.getMedicationTime().split(",")) {
        String normalized = token.trim();
        if (normalized.isEmpty()) {
          continue;
        }
        try {
          times.add(LocalTime.parse(normalized));
        } catch (DateTimeParseException ex) {
          // ignore invalid custom time to keep scheduler robust
        }
      }
    }
    if (!times.isEmpty()) {
      return times;
    }

    String frequency = setting.getFrequency() == null ? "QD" : setting.getFrequency().trim().toUpperCase(Locale.ROOT);
    return switch (frequency) {
      case "BID" -> List.of(LocalTime.of(9, 0), LocalTime.of(21, 0));
      case "TID" -> List.of(LocalTime.of(8, 0), LocalTime.of(14, 0), LocalTime.of(20, 0));
      case "QID" -> List.of(LocalTime.of(6, 0), LocalTime.of(12, 0), LocalTime.of(18, 0), LocalTime.of(22, 0));
      case "PRN" -> List.of(LocalTime.of(9, 0));
      default -> List.of(LocalTime.of(9, 0));
    };
  }

  private void clearLinkedTask(HealthMedicationRegistration registration) {
    if (registration.getId() == null || registration.getOrgId() == null) {
      return;
    }
    List<HealthMedicationTask> linkedTasks = taskMapper.selectList(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(HealthMedicationTask::getOrgId, registration.getOrgId())
        .eq(HealthMedicationTask::getRegistrationId, registration.getId()));
    for (HealthMedicationTask linkedTask : linkedTasks) {
      linkedTask.setStatus("PENDING");
      linkedTask.setRegistrationId(null);
      linkedTask.setDoneTime(null);
      taskMapper.updateById(linkedTask);
    }
  }

  private HealthMedicationTask resolveTask(Long orgId, Long taskId) {
    if (taskId == null) {
      throw new IllegalArgumentException("用药任务不存在");
    }
    HealthMedicationTask task = taskMapper.selectById(taskId);
    if (task == null || Integer.valueOf(1).equals(task.getIsDeleted())
        || orgId != null && !orgId.equals(task.getOrgId())) {
      throw new IllegalArgumentException("用药任务不存在");
    }
    return task;
  }

  private BigDecimal resolveDosage(BigDecimal requested, HealthMedicationSetting setting) {
    if (requested != null && requested.compareTo(BigDecimal.ZERO) > 0) {
      return requested;
    }
    BigDecimal parsed = parseDosageNumber(setting == null ? null : setting.getDosage());
    return parsed == null ? BigDecimal.ONE : parsed;
  }

  private String resolveUnit(String requested, HealthMedicationSetting setting) {
    if (requested != null && !requested.isBlank()) {
      return requested.trim();
    }
    String dosage = setting == null ? null : setting.getDosage();
    if (dosage == null || dosage.isBlank()) {
      return "次";
    }
    String unit = dosage.replaceFirst("^[0-9]+(\\.[0-9]+)?", "").trim();
    return unit.isBlank() ? "次" : unit;
  }

  private BigDecimal parseDosageNumber(String dosage) {
    if (dosage == null || dosage.isBlank()) {
      return null;
    }
    StringBuilder number = new StringBuilder();
    for (int i = 0; i < dosage.length(); i += 1) {
      char ch = dosage.charAt(i);
      if (Character.isDigit(ch) || ch == '.') {
        number.append(ch);
      } else {
        break;
      }
    }
    if (number.length() == 0) {
      return null;
    }
    try {
      return new BigDecimal(number.toString());
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private String appendTaskRemark(String original, String addition) {
    if (addition == null || addition.isBlank()) {
      return original;
    }
    if (original == null || original.isBlank()) {
      return addition.trim();
    }
    return original + "；" + addition.trim();
  }

  private String defaultText(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value.trim();
  }

  private String defaultText(String value, String fallback, String finalFallback) {
    return value == null || value.isBlank() ? defaultText(fallback, finalFallback) : value.trim();
  }
}
