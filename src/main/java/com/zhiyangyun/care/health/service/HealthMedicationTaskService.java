package com.zhiyangyun.care.health.service;

import com.zhiyangyun.care.health.entity.HealthMedicationRegistration;
import com.zhiyangyun.care.health.entity.HealthMedicationSetting;
import com.zhiyangyun.care.health.entity.HealthMedicationTask;
import com.zhiyangyun.care.health.model.HealthMedicationTaskActionRequest;
import java.time.LocalDate;

public interface HealthMedicationTaskService {
  void generateTasksForDate(LocalDate date);

  void generateTasksForDate(Long orgId, LocalDate date);

  void generateTasksForSetting(HealthMedicationSetting setting, LocalDate date);

  void completeTaskByRegistration(HealthMedicationRegistration registration);

  void unlinkTaskByRegistration(HealthMedicationRegistration registration);

  HealthMedicationTask completeTask(Long orgId, Long staffId, String staffName, Long taskId,
      HealthMedicationTaskActionRequest request);

  HealthMedicationTask markTaskMissed(Long orgId, Long staffId, String staffName, Long taskId,
      HealthMedicationTaskActionRequest request);
}
