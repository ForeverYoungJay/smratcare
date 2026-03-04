package com.zhiyangyun.care.logistics.service;

import com.zhiyangyun.care.logistics.model.LogisticsMaintenanceTodoGenerateResult;

public interface LogisticsEquipmentTodoService {
  LogisticsMaintenanceTodoGenerateResult generateMaintenanceTodos(Long orgId, int days, Long operatorStaffId);

  void saveJobLog(
      Long orgId,
      String triggerType,
      int days,
      LogisticsMaintenanceTodoGenerateResult result,
      String status,
      String errorMessage,
      Long operatorStaffId);
}
