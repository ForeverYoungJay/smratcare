package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthInspection;
import com.zhiyangyun.care.health.entity.HealthNursingLog;
import com.zhiyangyun.care.health.mapper.HealthInspectionMapper;
import com.zhiyangyun.care.health.mapper.HealthNursingLogMapper;
import com.zhiyangyun.care.health.model.HealthInspectionRequest;
import com.zhiyangyun.care.health.model.HealthInspectionSummaryResponse;
import com.zhiyangyun.care.health.model.HealthNameCountStatItem;
import com.zhiyangyun.care.health.service.HealthInspectionClosureService;
import com.zhiyangyun.care.health.support.ElderResolveSupport;
import com.zhiyangyun.care.medicalcare.entity.MedicalAlertRuleConfig;
import com.zhiyangyun.care.medicalcare.mapper.MedicalAlertRuleConfigMapper;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
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
@RequestMapping("/api/health/inspection")
public class HealthInspectionController {
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final HealthInspectionMapper mapper;
  private final HealthNursingLogMapper nursingLogMapper;
  private final ElderResolveSupport elderResolveSupport;
  private final HealthInspectionClosureService inspectionClosureService;
  private final MedicalAlertRuleConfigMapper alertRuleConfigMapper;

  public HealthInspectionController(
      HealthInspectionMapper mapper,
      HealthNursingLogMapper nursingLogMapper,
      ElderResolveSupport elderResolveSupport,
      HealthInspectionClosureService inspectionClosureService,
      MedicalAlertRuleConfigMapper alertRuleConfigMapper) {
    this.mapper = mapper;
    this.nursingLogMapper = nursingLogMapper;
    this.elderResolveSupport = elderResolveSupport;
    this.inspectionClosureService = inspectionClosureService;
    this.alertRuleConfigMapper = alertRuleConfigMapper;
  }

  @GetMapping("/page")
  public Result<IPage<HealthInspection>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long inspectionId,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String inspectionFrom,
      @RequestParam(required = false) String inspectionTo,
      @RequestParam(required = false) String keyword) {
    var wrapper = buildQuery(
        AuthContext.getOrgId(),
        inspectionId,
        elderId,
        normalizeStatusFilter(status),
        inspectionFrom,
        inspectionTo,
        normalizeText(keyword));
    wrapper.orderByDesc(HealthInspection::getInspectionDate);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/summary")
  public Result<HealthInspectionSummaryResponse> summary(
      @RequestParam(required = false) Long inspectionId,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String inspectionFrom,
      @RequestParam(required = false) String inspectionTo,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    status = normalizeStatusFilter(status);
    keyword = normalizeText(keyword);
    LocalDate from = parseDate(inspectionFrom);
    LocalDate to = parseDate(inspectionTo);
    LocalDateTime fromStart = from == null ? null : from.atStartOfDay();
    LocalDateTime toEndExclusive = to == null ? null : to.plusDays(1).atStartOfDay();
    var wrapper = buildQuery(orgId, inspectionId, elderId, status, inspectionFrom, inspectionTo, keyword);
    long totalCount = mapper.selectCount(wrapper);
    long abnormalCount = mapper.selectCount(buildQuery(orgId, inspectionId, elderId, "ABNORMAL", inspectionFrom, inspectionTo, keyword));
    long followingCount = mapper.selectCount(buildQuery(orgId, inspectionId, elderId, "FOLLOWING", inspectionFrom, inspectionTo, keyword));
    long closedCount = mapper.selectCount(buildQuery(orgId, inspectionId, elderId, "CLOSED", inspectionFrom, inspectionTo, keyword));

    long linkedLogCount = nursingLogMapper.selectCount(Wrappers.lambdaQuery(HealthNursingLog.class)
        .eq(HealthNursingLog::getIsDeleted, 0)
        .eq(orgId != null, HealthNursingLog::getOrgId, orgId)
        .eq(inspectionId != null, HealthNursingLog::getSourceInspectionId, inspectionId)
        .eq(elderId != null, HealthNursingLog::getElderId, elderId)
        .isNotNull(HealthNursingLog::getSourceInspectionId)
        .ge(fromStart != null, HealthNursingLog::getLogTime, fromStart)
        .lt(toEndExclusive != null, HealthNursingLog::getLogTime, toEndExclusive));

    var statusStatWrapper = Wrappers.query(HealthInspection.class)
        .select("status AS name", "COUNT(1) AS totalCount")
        .eq("is_deleted", 0)
        .eq(orgId != null, "org_id", orgId)
        .eq(inspectionId != null, "id", inspectionId)
        .eq(elderId != null, "elder_id", elderId)
        .eq(status != null && !status.isBlank(), "status", status)
        .ge(from != null, "inspection_date", from)
        .le(to != null, "inspection_date", to)
        .groupBy("status")
        .orderByDesc("totalCount");
    final String keywordFilter = keyword;
    if (keywordFilter != null && !keywordFilter.isBlank()) {
      statusStatWrapper.and(w -> w.like("elder_name", keywordFilter)
          .or().like("inspection_item", keywordFilter)
          .or().like("inspector_name", keywordFilter));
    }
    List<HealthNameCountStatItem> statusStats = mapper.selectMaps(statusStatWrapper).stream().map(map -> {
      HealthNameCountStatItem item = new HealthNameCountStatItem();
      item.setName(normalizeStatus(String.valueOf(map.getOrDefault("name", "UNKNOWN"))));
      item.setTotalCount(toLong(map.get("totalCount")));
      return item;
    }).toList();

    HealthInspectionSummaryResponse response = new HealthInspectionSummaryResponse();
    response.setTotalCount(totalCount);
    response.setAbnormalCount(abnormalCount);
    response.setFollowingCount(followingCount);
    response.setClosedCount(closedCount);
    response.setLinkedLogCount(linkedLogCount);
    response.setStatusStats(statusStats);
    return Result.ok(response);
  }

  @PostMapping
  public Result<HealthInspection> create(@Valid @RequestBody HealthInspectionRequest request) {
    String resolvedStatus = resolveInspectionStatus(request.getStatus(), request.getResult());
    String followUpAction = normalizeText(request.getFollowUpAction());
    if ("ABNORMAL".equals(resolvedStatus) && (followUpAction == null || followUpAction.isBlank())) {
      return Result.error(400, "异常巡检必须填写跟进措施");
    }
    Long orgId = AuthContext.getOrgId();
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    HealthInspection item = new HealthInspection();
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, normalizeText(request.getElderName())));
    item.setInspectionDate(request.getInspectionDate());
    item.setInspectionItem(normalizeText(request.getInspectionItem()));
    item.setResult(normalizeText(request.getResult()));
    item.setStatus(resolvedStatus);
    item.setInspectorName(normalizeText(request.getInspectorName()));
    item.setFollowUpAction(followUpAction);
    item.setAttachmentUrls(normalizeText(request.getAttachmentUrls()));
    item.setOtherNote(normalizeText(request.getOtherNote()));
    item.setRemark(normalizeText(request.getRemark()));
    item.setCreatedBy(AuthContext.getStaffId());
    mapper.insert(item);
    syncAbnormalInspectionLog(item);
    inspectionClosureService.syncFromInspection(item, AuthContext.getStaffId());
    return Result.ok(item);
  }

  @PutMapping("/{id}")
  public Result<HealthInspection> update(@PathVariable Long id, @Valid @RequestBody HealthInspectionRequest request) {
    HealthInspection item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item == null || item.getIsDeleted() != null && item.getIsDeleted() == 1
        || orgId != null && !orgId.equals(item.getOrgId())) {
      return Result.ok(null);
    }
    String resolvedStatus = resolveInspectionStatus(request.getStatus(), request.getResult());
    String followUpAction = normalizeText(request.getFollowUpAction());
    if ("ABNORMAL".equals(resolvedStatus) && (followUpAction == null || followUpAction.isBlank())) {
      return Result.error(400, "异常巡检必须填写跟进措施");
    }
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, normalizeText(request.getElderName())));
    item.setInspectionDate(request.getInspectionDate());
    item.setInspectionItem(normalizeText(request.getInspectionItem()));
    item.setResult(normalizeText(request.getResult()));
    item.setStatus(resolvedStatus);
    item.setInspectorName(normalizeText(request.getInspectorName()));
    item.setFollowUpAction(followUpAction);
    item.setAttachmentUrls(normalizeText(request.getAttachmentUrls()));
    item.setOtherNote(normalizeText(request.getOtherNote()));
    item.setRemark(normalizeText(request.getRemark()));
    mapper.updateById(item);
    syncAbnormalInspectionLog(item);
    inspectionClosureService.syncFromInspection(item, AuthContext.getStaffId());
    return Result.ok(item);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    HealthInspection item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item != null && (item.getIsDeleted() == null || item.getIsDeleted() == 0)
        && (orgId == null || orgId.equals(item.getOrgId()))) {
      item.setIsDeleted(1);
      mapper.updateById(item);
    }
    return Result.ok(null);
  }

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HealthInspection> buildQuery(
      Long orgId,
      Long inspectionId,
      Long elderId,
      String status,
      String inspectionFrom,
      String inspectionTo,
      String keyword) {
    LocalDate from = parseDate(inspectionFrom);
    LocalDate to = parseDate(inspectionTo);
    var wrapper = Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(inspectionId != null, HealthInspection::getId, inspectionId)
        .eq(elderId != null, HealthInspection::getElderId, elderId)
        .eq(status != null && !status.isBlank(), HealthInspection::getStatus, status)
        .ge(from != null, HealthInspection::getInspectionDate, from)
        .le(to != null, HealthInspection::getInspectionDate, to);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(HealthInspection::getElderName, keyword)
          .or().like(HealthInspection::getInspectionItem, keyword)
          .or().like(HealthInspection::getInspectorName, keyword)
          .or().like(HealthInspection::getOtherNote, keyword)
          .or().like(HealthInspection::getFollowUpAction, keyword));
    }
    return wrapper;
  }

  private LocalDate parseDate(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return LocalDate.parse(value, DATE_FORMATTER);
    } catch (DateTimeParseException ignore) {
      try {
        return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
      } catch (DateTimeParseException e) {
        return null;
      }
    }
  }

  private String resolveInspectionStatus(String status, String result) {
    String normalizedStatus = normalizeStatusFilter(status);
    if (normalizedStatus != null) {
      return normalizedStatus;
    }
    if (result != null && result.contains("异常")) {
      return "ABNORMAL";
    }
    return "NORMAL";
  }

  private String normalizeStatusFilter(String status) {
    String normalized = normalizeText(status);
    if (normalized == null) {
      return null;
    }
    String upper = normalized.toUpperCase(Locale.ROOT);
    if ("NORMAL".equals(upper) || "ABNORMAL".equals(upper) || "FOLLOWING".equals(upper) || "CLOSED".equals(upper)) {
      return upper;
    }
    return null;
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return "UNKNOWN";
    }
    return status.toUpperCase(Locale.ROOT);
  }

  private String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
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

  private void syncAbnormalInspectionLog(HealthInspection inspection) {
    if (inspection == null || inspection.getId() == null || !"ABNORMAL".equals(inspection.getStatus())) {
      return;
    }
    Long orgId = inspection.getOrgId();
    MedicalAlertRuleConfig config = alertRuleConfigMapper.selectOne(Wrappers.lambdaQuery(MedicalAlertRuleConfig.class)
        .eq(MedicalAlertRuleConfig::getIsDeleted, 0)
        .eq(orgId != null, MedicalAlertRuleConfig::getOrgId, orgId)
        .last("LIMIT 1"));
    if (config != null && Integer.valueOf(0).equals(config.getAutoCreateNursingLogFromInspection())) {
      return;
    }

    long linkedCount = nursingLogMapper.selectCount(Wrappers.lambdaQuery(HealthNursingLog.class)
        .eq(HealthNursingLog::getIsDeleted, 0)
        .eq(orgId != null, HealthNursingLog::getOrgId, orgId)
        .eq(HealthNursingLog::getSourceInspectionId, inspection.getId()));
    if (linkedCount > 0) {
      return;
    }

    HealthNursingLog nursingLog = new HealthNursingLog();
    nursingLog.setTenantId(inspection.getTenantId());
    nursingLog.setOrgId(inspection.getOrgId());
    nursingLog.setElderId(inspection.getElderId());
    nursingLog.setElderName(inspection.getElderName());
    nursingLog.setSourceInspectionId(inspection.getId());
    nursingLog.setLogTime(LocalDateTime.now());
    boolean raiseTask = config == null || !Integer.valueOf(0).equals(config.getAutoRaiseTaskFromAbnormal());
    nursingLog.setLogType(raiseTask ? "INCIDENT" : "INSPECTION_FOLLOW_UP");
    nursingLog.setContent(buildAutoNursingLogContent(inspection));
    nursingLog.setStaffName(inspection.getInspectorName());
    nursingLog.setStatus("PENDING");
    nursingLog.setRemark("系统根据异常巡检自动生成");
    nursingLog.setCreatedBy(AuthContext.getStaffId());
    nursingLogMapper.insert(nursingLog);
    inspectionClosureService.syncFromNursingLog(nursingLog);
  }

  private String buildAutoNursingLogContent(HealthInspection inspection) {
    String inspectionItem = normalizeText(inspection.getInspectionItem());
    String result = normalizeText(inspection.getResult());
    String action = normalizeText(inspection.getFollowUpAction());
    StringBuilder builder = new StringBuilder();
    builder.append("异常巡检自动联动");
    if (inspectionItem != null) {
      builder.append("：").append(inspectionItem);
    }
    if (result != null) {
      builder.append("；结果=").append(result);
    }
    if (action != null) {
      builder.append("；处理=").append(action);
    }
    return builder.toString();
  }
}
