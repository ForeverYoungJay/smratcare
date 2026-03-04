package com.zhiyangyun.care.logistics.task;

import com.zhiyangyun.care.logistics.model.LogisticsMaintenanceTodoGenerateResult;
import com.zhiyangyun.care.logistics.service.LogisticsEquipmentTodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LogisticsEquipmentTodoScheduler {
  private static final Logger log = LoggerFactory.getLogger(LogisticsEquipmentTodoScheduler.class);

  private final LogisticsEquipmentTodoService equipmentTodoService;

  @Value("${app.logistics.maintenance-todo.enabled:true}")
  private boolean maintenanceTodoEnabled;

  @Value("${app.logistics.maintenance-todo.days:30}")
  private int maintenanceTodoDays;

  public LogisticsEquipmentTodoScheduler(LogisticsEquipmentTodoService equipmentTodoService) {
    this.equipmentTodoService = equipmentTodoService;
  }

  @Scheduled(cron = "${app.logistics.maintenance-todo.cron:0 0 8 * * ?}")
  public void createMaintenanceTodosDaily() {
    if (!maintenanceTodoEnabled) {
      return;
    }
    try {
      LogisticsMaintenanceTodoGenerateResult result =
          equipmentTodoService.generateMaintenanceTodos(null, maintenanceTodoDays, 0L);
      equipmentTodoService.saveJobLog(null, "SCHEDULED", maintenanceTodoDays, result, "SUCCESS", null, 0L);
      log.info(
          "logistics-maintenance-todo created={}, skipped={}, matched={}",
          result.getCreatedCount(),
          result.getSkippedCount(),
          result.getTotalMatched());
    } catch (RuntimeException ex) {
      equipmentTodoService.saveJobLog(
          null,
          "SCHEDULED",
          maintenanceTodoDays,
          null,
          "FAILED",
          truncateMessage(ex.getMessage()),
          0L);
      log.error("logistics-maintenance-todo failed", ex);
    }
  }

  private String truncateMessage(String message) {
    if (message == null) {
      return null;
    }
    if (message.length() <= 500) {
      return message;
    }
    return message.substring(0, 500);
  }
}
