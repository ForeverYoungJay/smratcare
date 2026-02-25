package com.zhiyangyun.care.health.service;

import com.zhiyangyun.care.health.entity.HealthMedicationRegistration;
import com.zhiyangyun.care.health.entity.HealthMedicationSetting;
import java.time.LocalDate;

public interface HealthMedicationTaskService {
  void generateTasksForDate(LocalDate date);

  void generateTasksForSetting(HealthMedicationSetting setting, LocalDate date);

  void completeTaskByRegistration(HealthMedicationRegistration registration);
}
