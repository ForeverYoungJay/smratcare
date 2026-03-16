package com.zhiyangyun.care.logistics.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.logistics.entity.LogisticsMaintenanceTodoJobLog;
import com.zhiyangyun.care.logistics.mapper.LogisticsMaintenanceTodoJobLogMapper;
import com.zhiyangyun.care.logistics.model.LogisticsMaintenanceTodoGenerateResult;
import com.zhiyangyun.care.logistics.model.LogisticsMaintenanceTodoJobLogOverviewResponse;
import com.zhiyangyun.care.logistics.service.LogisticsEquipmentTodoService;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logistics/maintenance-todo-job-log")
public class LogisticsMaintenanceTodoJobLogController {
  private static final int RERUN_MAX_LOOKBACK_DAYS = 30;
  private static final int RERUN_COOLDOWN_SECONDS = 120;
  @Value("${app.logistics.maintenance-todo.cron:0 0 8 * * ?}")
  private String maintenanceTodoCron;

  private final LogisticsMaintenanceTodoJobLogMapper jobLogMapper;
  private final LogisticsEquipmentTodoService equipmentTodoService;

  public LogisticsMaintenanceTodoJobLogController(
      LogisticsMaintenanceTodoJobLogMapper jobLogMapper,
      LogisticsEquipmentTodoService equipmentTodoService) {
    this.jobLogMapper = jobLogMapper;
    this.equipmentTodoService = equipmentTodoService;
  }

  @GetMapping("/page")
  public Result<IPage<LogisticsMaintenanceTodoJobLog>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String triggerType,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(LogisticsMaintenanceTodoJobLog.class)
        .eq(LogisticsMaintenanceTodoJobLog::getIsDeleted, 0)
        .eq(orgId != null, LogisticsMaintenanceTodoJobLog::getOrgId, orgId)
        .eq(triggerType != null && !triggerType.isBlank(), LogisticsMaintenanceTodoJobLog::getTriggerType, triggerType)
        .eq(status != null && !status.isBlank(), LogisticsMaintenanceTodoJobLog::getStatus, status)
        .orderByDesc(LogisticsMaintenanceTodoJobLog::getExecutedAt)
        .orderByDesc(LogisticsMaintenanceTodoJobLog::getCreateTime);
    return Result.ok(jobLogMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/overview")
  public Result<LogisticsMaintenanceTodoJobLogOverviewResponse> overview(
      @RequestParam(defaultValue = "7") int days) {
    int windowDays = Math.max(days, 1);
    Long orgId = AuthContext.getOrgId();
    LocalDateTime now = LocalDateTime.now();
    List<LogisticsMaintenanceTodoJobLog> recentLogs = jobLogMapper.selectList(
        Wrappers.lambdaQuery(LogisticsMaintenanceTodoJobLog.class)
            .eq(LogisticsMaintenanceTodoJobLog::getIsDeleted, 0)
            .eq(orgId != null, LogisticsMaintenanceTodoJobLog::getOrgId, orgId)
            .ge(LogisticsMaintenanceTodoJobLog::getExecutedAt, now.minusDays(windowDays))
            .orderByDesc(LogisticsMaintenanceTodoJobLog::getExecutedAt)
            .orderByDesc(LogisticsMaintenanceTodoJobLog::getCreateTime));
    LogisticsMaintenanceTodoJobLog latest = findLatestLog(orgId);
    LogisticsMaintenanceTodoJobLog latestFailed = findLatestFailedLog(orgId);

    LogisticsMaintenanceTodoJobLogOverviewResponse response = new LogisticsMaintenanceTodoJobLogOverviewResponse();
    response.setRecentWindowDays(windowDays);
    response.setRecentTotalRuns(recentLogs.size());
    response.setRecentSuccessRuns(recentLogs.stream().filter(item -> "SUCCESS".equals(item.getStatus())).count());
    response.setRecentFailedRuns(recentLogs.stream().filter(item -> "FAILED".equals(item.getStatus())).count());
    if (latest != null) {
      response.setLastExecutedAt(latest.getExecutedAt());
      response.setLastStatus(latest.getStatus());
      response.setLastTriggerType(latest.getTriggerType());
      response.setLastDays(latest.getDays());
      response.setLastCreatedCount(latest.getCreatedCount());
      response.setLastSkippedCount(latest.getSkippedCount());
      response.setLastErrorMessage(latest.getErrorMessage());
    }
    if (latestFailed != null) {
      response.setLastFailedLogId(latestFailed.getId());
      response.setLastFailedExecutedAt(latestFailed.getExecutedAt());
      response.setLastFailedErrorMessage(latestFailed.getErrorMessage());
    }
    return Result.ok(response);
  }

  @PostMapping("/{id}/rerun")
  public Result<Map<String, Long>> rerun(@PathVariable Long id) {
    LogisticsMaintenanceTodoJobLog log = findAccessibleLog(id);
    return Result.ok(executeRerun(log));
  }

  @PostMapping("/rerun-latest-failed")
  public Result<Map<String, Long>> rerunLatestFailed() {
    Long orgId = AuthContext.getOrgId();
    LogisticsMaintenanceTodoJobLog failedLog = findLatestFailedLog(orgId);
    return Result.ok(executeRerun(failedLog));
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String triggerType,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(LogisticsMaintenanceTodoJobLog.class)
        .eq(LogisticsMaintenanceTodoJobLog::getIsDeleted, 0)
        .eq(orgId != null, LogisticsMaintenanceTodoJobLog::getOrgId, orgId)
        .eq(triggerType != null && !triggerType.isBlank(), LogisticsMaintenanceTodoJobLog::getTriggerType, triggerType)
        .eq(status != null && !status.isBlank(), LogisticsMaintenanceTodoJobLog::getStatus, status)
        .orderByDesc(LogisticsMaintenanceTodoJobLog::getExecutedAt)
        .orderByDesc(LogisticsMaintenanceTodoJobLog::getCreateTime);
    List<LogisticsMaintenanceTodoJobLog> rows = jobLogMapper.selectList(wrapper);
    List<String> headers = List.of("ID", "执行时间", "触发方式", "计划触发", "状态", "扫描天数", "命中设备", "新建待办", "跳过重复", "记录创建时间", "记录更新时间", "错误信息");
    List<List<String>> csvRows = rows.stream()
        .map(item -> List.of(
            safe(item.getId()),
            formatDateTime(item.getExecutedAt()),
            triggerTypeLabel(item.getTriggerType()),
            triggerPlanLabel(item.getTriggerType()),
            safe(item.getStatus()),
            safe(item.getDays()),
            safe(item.getTotalMatched()),
            safe(item.getCreatedCount()),
            safe(item.getSkippedCount()),
            formatDateTime(item.getCreateTime()),
            formatDateTime(item.getUpdateTime()),
            safe(item.getErrorMessage())))
        .toList();
    return csvResponse("logistics-maintenance-todo-job-log", headers, csvRows);
  }

  private LogisticsMaintenanceTodoJobLog findAccessibleLog(Long id) {
    Long orgId = AuthContext.getOrgId();
    return jobLogMapper.selectOne(Wrappers.lambdaQuery(LogisticsMaintenanceTodoJobLog.class)
        .eq(LogisticsMaintenanceTodoJobLog::getId, id)
        .eq(LogisticsMaintenanceTodoJobLog::getIsDeleted, 0)
        .eq(orgId != null, LogisticsMaintenanceTodoJobLog::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private LogisticsMaintenanceTodoJobLog findLatestLog(Long orgId) {
    return jobLogMapper.selectOne(Wrappers.lambdaQuery(LogisticsMaintenanceTodoJobLog.class)
        .eq(LogisticsMaintenanceTodoJobLog::getIsDeleted, 0)
        .eq(orgId != null, LogisticsMaintenanceTodoJobLog::getOrgId, orgId)
        .orderByDesc(LogisticsMaintenanceTodoJobLog::getExecutedAt)
        .orderByDesc(LogisticsMaintenanceTodoJobLog::getCreateTime)
        .last("LIMIT 1"));
  }

  private LogisticsMaintenanceTodoJobLog findLatestFailedLog(Long orgId) {
    return jobLogMapper.selectOne(Wrappers.lambdaQuery(LogisticsMaintenanceTodoJobLog.class)
        .eq(LogisticsMaintenanceTodoJobLog::getIsDeleted, 0)
        .eq(orgId != null, LogisticsMaintenanceTodoJobLog::getOrgId, orgId)
        .eq(LogisticsMaintenanceTodoJobLog::getStatus, "FAILED")
        .orderByDesc(LogisticsMaintenanceTodoJobLog::getExecutedAt)
        .orderByDesc(LogisticsMaintenanceTodoJobLog::getCreateTime)
        .last("LIMIT 1"));
  }

  private Map<String, Long> executeRerun(LogisticsMaintenanceTodoJobLog log) {
    LocalDateTime now = LocalDateTime.now();
    Map<String, Long> result = new HashMap<>();
    result.put("createdCount", 0L);
    result.put("skippedCount", 0L);
    result.put("totalMatched", 0L);
    result.put("rerunTriggered", 0L);
    result.put("blockedByWindow", 0L);
    result.put("blockedByCooldown", 0L);
    if (log == null) {
      return result;
    }
    if (log.getExecutedAt() != null && log.getExecutedAt().isBefore(now.minusDays(RERUN_MAX_LOOKBACK_DAYS))) {
      result.put("blockedByWindow", 1L);
      return result;
    }
    Long authOrgId = AuthContext.getOrgId();
    Long targetOrgId = authOrgId != null ? authOrgId : log.getOrgId();
    LogisticsMaintenanceTodoJobLog latestRetryLog = jobLogMapper.selectOne(
        Wrappers.lambdaQuery(LogisticsMaintenanceTodoJobLog.class)
            .eq(LogisticsMaintenanceTodoJobLog::getIsDeleted, 0)
            .eq(targetOrgId != null, LogisticsMaintenanceTodoJobLog::getOrgId, targetOrgId)
            .eq(LogisticsMaintenanceTodoJobLog::getTriggerType, "RETRY")
            .orderByDesc(LogisticsMaintenanceTodoJobLog::getExecutedAt)
            .orderByDesc(LogisticsMaintenanceTodoJobLog::getCreateTime)
            .last("LIMIT 1"));
    if (latestRetryLog != null
        && latestRetryLog.getExecutedAt() != null
        && !latestRetryLog.getExecutedAt().isBefore(now.minusSeconds(RERUN_COOLDOWN_SECONDS))) {
      result.put("blockedByCooldown", 1L);
      return result;
    }
    int days = log.getDays() == null ? 30 : Math.min(Math.max(log.getDays(), 1), 90);
    Long operator = AuthContext.getStaffId();
    try {
      LogisticsMaintenanceTodoGenerateResult generateResult =
          equipmentTodoService.generateMaintenanceTodos(targetOrgId, days, operator);
      equipmentTodoService.saveJobLog(targetOrgId, "RETRY", days, generateResult, "SUCCESS", null, operator);
      result.put("createdCount", generateResult.getCreatedCount());
      result.put("skippedCount", generateResult.getSkippedCount());
      result.put("totalMatched", generateResult.getTotalMatched());
      result.put("rerunTriggered", 1L);
      return result;
    } catch (RuntimeException ex) {
      equipmentTodoService.saveJobLog(targetOrgId, "RETRY", days, null, "FAILED", truncateMessage(ex.getMessage()), operator);
      throw ex;
    }
  }

  private ResponseEntity<byte[]> csvResponse(String filenameBase, List<String> headers, List<List<String>> rows) {
    StringBuilder sb = new StringBuilder("\uFEFF");
    sb.append(toCsvLine(headers));
    for (List<String> row : rows) {
      sb.append(toCsvLine(row));
    }
    byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    String filename = filenameBase + ".csv";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + urlEncode(filename))
        .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
        .body(bytes);
  }

  private String toCsvLine(List<String> values) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(escapeCsv(values.get(i)));
    }
    sb.append("\n");
    return sb.toString();
  }

  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    String escaped = value.replace("\"", "\"\"");
    return "\"" + escaped + "\"";
  }

  private String urlEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
  }

  private String formatDateTime(java.time.LocalDateTime value) {
    if (value == null) {
      return "";
    }
    return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  private String safe(Object value) {
    return value == null ? "" : String.valueOf(value);
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

  private String triggerTypeLabel(String triggerType) {
    if ("SCHEDULED".equalsIgnoreCase(triggerType)) {
      return "自动";
    }
    if ("RETRY".equalsIgnoreCase(triggerType)) {
      return "重跑";
    }
    return "手动";
  }

  private String triggerPlanLabel(String triggerType) {
    if ("SCHEDULED".equalsIgnoreCase(triggerType)) {
      return "定时触发(CRON=" + safe(maintenanceTodoCron) + ")";
    }
    if ("RETRY".equalsIgnoreCase(triggerType)) {
      return "失败后手动触发重跑";
    }
    return "人工手动触发";
  }
}
