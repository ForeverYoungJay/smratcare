package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthNursingLog;
import com.zhiyangyun.care.health.mapper.HealthNursingLogMapper;
import com.zhiyangyun.care.health.model.HealthNursingLogRequest;
import com.zhiyangyun.care.health.model.HealthNursingLogSummaryResponse;
import com.zhiyangyun.care.health.model.HealthNameCountStatItem;
import com.zhiyangyun.care.health.service.HealthInspectionClosureService;
import com.zhiyangyun.care.health.support.ElderResolveSupport;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health/nursing-log")
public class HealthNursingLogController {
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final HealthNursingLogMapper mapper;
  private final ElderResolveSupport elderResolveSupport;
  private final HealthInspectionClosureService inspectionClosureService;

  public HealthNursingLogController(
      HealthNursingLogMapper mapper,
      ElderResolveSupport elderResolveSupport,
      HealthInspectionClosureService inspectionClosureService) {
    this.mapper = mapper;
    this.elderResolveSupport = elderResolveSupport;
    this.inspectionClosureService = inspectionClosureService;
  }

  @GetMapping("/page")
  public Result<IPage<HealthNursingLog>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) Long sourceInspectionId,
      @RequestParam(required = false) String logType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String logFrom,
      @RequestParam(required = false) String logTo,
      @RequestParam(required = false) String keyword) {
    var wrapper = buildQuery(AuthContext.getOrgId(), elderId, sourceInspectionId, logType, status, logFrom, logTo, keyword);
    wrapper.orderByDesc(HealthNursingLog::getLogTime);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/summary")
  public Result<HealthNursingLogSummaryResponse> summary(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) Long sourceInspectionId,
      @RequestParam(required = false) String logType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String logFrom,
      @RequestParam(required = false) String logTo,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = buildQuery(orgId, elderId, sourceInspectionId, logType, status, logFrom, logTo, keyword);
    LocalDateTime from = parseDateTime(logFrom);
    LocalDateTime to = parseDateTime(logTo);
    long totalCount = mapper.selectCount(wrapper);
    long pendingCount = mapper.selectCount(buildQuery(orgId, elderId, sourceInspectionId, logType, "PENDING", logFrom, logTo, keyword));
    long doneCount = mapper.selectCount(buildQuery(orgId, elderId, sourceInspectionId, logType, "DONE", logFrom, logTo, keyword));
    long closedCount = mapper.selectCount(buildQuery(orgId, elderId, sourceInspectionId, logType, "CLOSED", logFrom, logTo, keyword));
    long linkedInspectionCount = mapper.selectCount(Wrappers.lambdaQuery(HealthNursingLog.class)
        .eq(HealthNursingLog::getIsDeleted, 0)
        .eq(orgId != null, HealthNursingLog::getOrgId, orgId)
        .eq(elderId != null, HealthNursingLog::getElderId, elderId)
        .eq(sourceInspectionId != null, HealthNursingLog::getSourceInspectionId, sourceInspectionId)
        .eq(logType != null && !logType.isBlank(), HealthNursingLog::getLogType, logType)
        .eq(status != null && !status.isBlank(), HealthNursingLog::getStatus, status)
        .isNotNull(HealthNursingLog::getSourceInspectionId)
        .ge(from != null, HealthNursingLog::getLogTime, from)
        .le(to != null, HealthNursingLog::getLogTime, to));

    var logTypeStatWrapper = Wrappers.query(HealthNursingLog.class)
        .select("log_type AS name", "COUNT(1) AS totalCount")
        .eq("is_deleted", 0)
        .eq(orgId != null, "org_id", orgId)
        .eq(elderId != null, "elder_id", elderId)
        .eq(sourceInspectionId != null, "source_inspection_id", sourceInspectionId)
        .eq(logType != null && !logType.isBlank(), "log_type", logType)
        .eq(status != null && !status.isBlank(), "status", status)
        .ge(from != null, "log_time", from)
        .le(to != null, "log_time", to)
        .groupBy("log_type")
        .orderByDesc("totalCount");
    if (keyword != null && !keyword.isBlank()) {
      logTypeStatWrapper.and(w -> w.like("elder_name", keyword)
          .or().like("content", keyword)
          .or().like("staff_name", keyword));
    }
    List<HealthNameCountStatItem> logTypeStats = mapper.selectMaps(logTypeStatWrapper).stream().map(map -> {
      HealthNameCountStatItem item = new HealthNameCountStatItem();
      item.setName(String.valueOf(map.getOrDefault("name", "UNKNOWN")));
      item.setTotalCount(toLong(map.get("totalCount")));
      return item;
    }).toList();

    HealthNursingLogSummaryResponse response = new HealthNursingLogSummaryResponse();
    response.setTotalCount(totalCount);
    response.setPendingCount(pendingCount);
    response.setDoneCount(doneCount);
    response.setClosedCount(closedCount);
    response.setLinkedInspectionCount(linkedInspectionCount);
    response.setLogTypeStats(logTypeStats);
    return Result.ok(response);
  }

  @PostMapping
  public Result<HealthNursingLog> create(@Valid @RequestBody HealthNursingLogRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    HealthNursingLog item = new HealthNursingLog();
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setSourceInspectionId(request.getSourceInspectionId());
    item.setLogTime(request.getLogTime());
    item.setLogType(request.getLogType());
    item.setContent(request.getContent());
    item.setStaffName(request.getStaffName());
    item.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "PENDING" : request.getStatus());
    item.setRemark(request.getRemark());
    item.setCreatedBy(AuthContext.getStaffId());
    mapper.insert(item);
    inspectionClosureService.syncFromNursingLog(item);
    return Result.ok(item);
  }

  @PutMapping("/{id}")
  public Result<HealthNursingLog> update(@PathVariable Long id, @Valid @RequestBody HealthNursingLogRequest request) {
    HealthNursingLog item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item == null || item.getIsDeleted() != null && item.getIsDeleted() == 1
        || orgId != null && !orgId.equals(item.getOrgId())) {
      return Result.ok(null);
    }
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setSourceInspectionId(request.getSourceInspectionId());
    item.setLogTime(request.getLogTime());
    item.setLogType(request.getLogType());
    item.setContent(request.getContent());
    item.setStaffName(request.getStaffName());
    item.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "PENDING" : request.getStatus());
    item.setRemark(request.getRemark());
    mapper.updateById(item);
    inspectionClosureService.syncFromNursingLog(item);
    return Result.ok(item);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    HealthNursingLog item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item != null && (item.getIsDeleted() == null || item.getIsDeleted() == 0)
        && (orgId == null || orgId.equals(item.getOrgId()))) {
      item.setIsDeleted(1);
      mapper.updateById(item);
      inspectionClosureService.syncAfterNursingLogDeleted(item.getOrgId(), item.getSourceInspectionId());
    }
    return Result.ok(null);
  }

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HealthNursingLog> buildQuery(
      Long orgId,
      Long elderId,
      Long sourceInspectionId,
      String logType,
      String status,
      String logFrom,
      String logTo,
      String keyword) {
    LocalDateTime from = parseDateTime(logFrom);
    LocalDateTime to = parseDateTime(logTo);
    var wrapper = Wrappers.lambdaQuery(HealthNursingLog.class)
        .eq(HealthNursingLog::getIsDeleted, 0)
        .eq(orgId != null, HealthNursingLog::getOrgId, orgId)
        .eq(elderId != null, HealthNursingLog::getElderId, elderId)
        .eq(sourceInspectionId != null, HealthNursingLog::getSourceInspectionId, sourceInspectionId)
        .eq(logType != null && !logType.isBlank(), HealthNursingLog::getLogType, logType)
        .eq(status != null && !status.isBlank(), HealthNursingLog::getStatus, status)
        .ge(from != null, HealthNursingLog::getLogTime, from)
        .le(to != null, HealthNursingLog::getLogTime, to);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(HealthNursingLog::getElderName, keyword)
          .or().like(HealthNursingLog::getContent, keyword)
          .or().like(HealthNursingLog::getStaffName, keyword));
    }
    return wrapper;
  }

  private LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    } catch (DateTimeParseException ignore) {
      try {
        return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      } catch (DateTimeParseException e) {
        return null;
      }
    }
  }

  private Long toLong(Object value) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    if (value == null) {
      return 0L;
    }
    try {
      return Long.parseLong(String.valueOf(value));
    } catch (NumberFormatException e) {
      return 0L;
    }
  }
}
