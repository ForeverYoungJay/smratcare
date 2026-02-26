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

  public HealthInspectionController(
      HealthInspectionMapper mapper,
      HealthNursingLogMapper nursingLogMapper,
      ElderResolveSupport elderResolveSupport,
      HealthInspectionClosureService inspectionClosureService) {
    this.mapper = mapper;
    this.nursingLogMapper = nursingLogMapper;
    this.elderResolveSupport = elderResolveSupport;
    this.inspectionClosureService = inspectionClosureService;
  }

  @GetMapping("/page")
  public Result<IPage<HealthInspection>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String inspectionFrom,
      @RequestParam(required = false) String inspectionTo,
      @RequestParam(required = false) String keyword) {
    var wrapper = buildQuery(AuthContext.getOrgId(), elderId, status, inspectionFrom, inspectionTo, keyword);
    wrapper.orderByDesc(HealthInspection::getInspectionDate);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/summary")
  public Result<HealthInspectionSummaryResponse> summary(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String inspectionFrom,
      @RequestParam(required = false) String inspectionTo,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LocalDate from = parseDate(inspectionFrom);
    LocalDate to = parseDate(inspectionTo);
    LocalDateTime fromStart = from == null ? null : from.atStartOfDay();
    LocalDateTime toEndExclusive = to == null ? null : to.plusDays(1).atStartOfDay();
    var wrapper = buildQuery(orgId, elderId, status, inspectionFrom, inspectionTo, keyword);
    long totalCount = mapper.selectCount(wrapper);
    long abnormalCount = mapper.selectCount(buildQuery(orgId, elderId, "ABNORMAL", inspectionFrom, inspectionTo, keyword));
    long followingCount = mapper.selectCount(buildQuery(orgId, elderId, "FOLLOWING", inspectionFrom, inspectionTo, keyword));
    long closedCount = mapper.selectCount(buildQuery(orgId, elderId, "CLOSED", inspectionFrom, inspectionTo, keyword));

    long linkedLogCount = nursingLogMapper.selectCount(Wrappers.lambdaQuery(HealthNursingLog.class)
        .eq(HealthNursingLog::getIsDeleted, 0)
        .eq(orgId != null, HealthNursingLog::getOrgId, orgId)
        .eq(elderId != null, HealthNursingLog::getElderId, elderId)
        .isNotNull(HealthNursingLog::getSourceInspectionId)
        .ge(fromStart != null, HealthNursingLog::getLogTime, fromStart)
        .le(toEndExclusive != null, HealthNursingLog::getLogTime, toEndExclusive));

    var statusStatWrapper = Wrappers.query(HealthInspection.class)
        .select("status AS name", "COUNT(1) AS totalCount")
        .eq("is_deleted", 0)
        .eq(orgId != null, "org_id", orgId)
        .eq(elderId != null, "elder_id", elderId)
        .eq(status != null && !status.isBlank(), "status", status)
        .ge(from != null, "inspection_date", from)
        .le(to != null, "inspection_date", to)
        .groupBy("status")
        .orderByDesc("totalCount");
    if (keyword != null && !keyword.isBlank()) {
      statusStatWrapper.and(w -> w.like("elder_name", keyword)
          .or().like("inspection_item", keyword)
          .or().like("inspector_name", keyword));
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
    Long orgId = AuthContext.getOrgId();
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    HealthInspection item = new HealthInspection();
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setInspectionDate(request.getInspectionDate());
    item.setInspectionItem(request.getInspectionItem());
    item.setResult(request.getResult());
    item.setStatus(resolveInspectionStatus(request.getStatus(), request.getResult()));
    item.setInspectorName(request.getInspectorName());
    item.setFollowUpAction(request.getFollowUpAction());
    item.setRemark(request.getRemark());
    item.setCreatedBy(AuthContext.getStaffId());
    mapper.insert(item);
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
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setInspectionDate(request.getInspectionDate());
    item.setInspectionItem(request.getInspectionItem());
    item.setResult(request.getResult());
    item.setStatus(resolveInspectionStatus(request.getStatus(), request.getResult()));
    item.setInspectorName(request.getInspectorName());
    item.setFollowUpAction(request.getFollowUpAction());
    item.setRemark(request.getRemark());
    mapper.updateById(item);
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
        .eq(elderId != null, HealthInspection::getElderId, elderId)
        .eq(status != null && !status.isBlank(), HealthInspection::getStatus, status)
        .ge(from != null, HealthInspection::getInspectionDate, from)
        .le(to != null, HealthInspection::getInspectionDate, to);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(HealthInspection::getElderName, keyword)
          .or().like(HealthInspection::getInspectionItem, keyword)
          .or().like(HealthInspection::getInspectorName, keyword));
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
    if (status != null && !status.isBlank()) {
      return status;
    }
    if (result != null && result.contains("异常")) {
      return "ABNORMAL";
    }
    return "NORMAL";
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return "UNKNOWN";
    }
    return status.toUpperCase(Locale.ROOT);
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
