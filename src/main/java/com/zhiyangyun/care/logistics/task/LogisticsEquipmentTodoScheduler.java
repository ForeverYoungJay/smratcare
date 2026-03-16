package com.zhiyangyun.care.logistics.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.Org;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.logistics.model.LogisticsMaintenanceTodoGenerateResult;
import com.zhiyangyun.care.logistics.service.LogisticsEquipmentTodoService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LogisticsEquipmentTodoScheduler {
  private static final Logger log = LoggerFactory.getLogger(LogisticsEquipmentTodoScheduler.class);

  private final LogisticsEquipmentTodoService equipmentTodoService;
  private final OrgMapper orgMapper;

  @Value("${app.logistics.maintenance-todo.enabled:true}")
  private boolean maintenanceTodoEnabled;

  @Value("${app.logistics.maintenance-todo.days:30}")
  private int maintenanceTodoDays;

  public LogisticsEquipmentTodoScheduler(
      LogisticsEquipmentTodoService equipmentTodoService,
      OrgMapper orgMapper) {
    this.equipmentTodoService = equipmentTodoService;
    this.orgMapper = orgMapper;
  }

  @Scheduled(cron = "${app.logistics.maintenance-todo.cron:0 0 8 * * ?}")
  public void createMaintenanceTodosDaily() {
    if (!maintenanceTodoEnabled) {
      return;
    }
    List<Org> orgs = orgMapper.selectList(Wrappers.lambdaQuery(Org.class)
        .eq(Org::getIsDeleted, 0)
        .eq(Org::getStatus, 1));
    for (Org org : orgs) {
      Long orgId = org == null ? null : org.getId();
      if (orgId == null) {
        continue;
      }
      try {
        LogisticsMaintenanceTodoGenerateResult result =
            equipmentTodoService.generateMaintenanceTodos(orgId, maintenanceTodoDays, 0L);
        equipmentTodoService.saveJobLog(orgId, "SCHEDULED", maintenanceTodoDays, result, "SUCCESS", null, 0L);
        log.info(
            "logistics-maintenance-todo orgId={} created={}, skipped={}, matched={}",
            orgId,
            result.getCreatedCount(),
            result.getSkippedCount(),
            result.getTotalMatched());
      } catch (RuntimeException ex) {
        equipmentTodoService.saveJobLog(
            orgId,
            "SCHEDULED",
            maintenanceTodoDays,
            null,
            "FAILED",
            truncateMessage(ex.getMessage()),
            0L);
        log.error("logistics-maintenance-todo failed for orgId={}", orgId, ex);
      }
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
