package com.zhiyangyun.care.health.service;

import com.zhiyangyun.care.health.entity.HealthInspection;
import com.zhiyangyun.care.health.entity.HealthNursingLog;

public interface HealthInspectionClosureService {
  void syncFromInspection(HealthInspection inspection, Long createdBy);

  void syncFromNursingLog(HealthNursingLog nursingLog);

  void syncAfterNursingLogDeleted(Long orgId, Long sourceInspectionId);
}
