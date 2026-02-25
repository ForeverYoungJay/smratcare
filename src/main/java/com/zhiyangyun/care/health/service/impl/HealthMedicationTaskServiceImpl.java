package com.zhiyangyun.care.health.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.health.entity.HealthMedicationRegistration;
import com.zhiyangyun.care.health.entity.HealthMedicationSetting;
import com.zhiyangyun.care.health.entity.HealthMedicationTask;
import com.zhiyangyun.care.health.mapper.HealthMedicationSettingMapper;
import com.zhiyangyun.care.health.mapper.HealthMedicationTaskMapper;
import com.zhiyangyun.care.health.service.HealthMedicationTaskService;
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

  public HealthMedicationTaskServiceImpl(HealthMedicationSettingMapper settingMapper, HealthMedicationTaskMapper taskMapper) {
    this.settingMapper = settingMapper;
    this.taskMapper = taskMapper;
  }

  @Override
  @Transactional
  public void generateTasksForDate(LocalDate date) {
    List<HealthMedicationSetting> settings = settingMapper.selectList(Wrappers.lambdaQuery(HealthMedicationSetting.class)
        .eq(HealthMedicationSetting::getIsDeleted, 0)
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
}
