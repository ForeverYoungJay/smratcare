package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaActivityPlan;
import com.zhiyangyun.care.oa.mapper.OaActivityPlanMapper;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import com.zhiyangyun.care.oa.model.OaActivityPlanRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/oa/activity-plan")
public class OaActivityPlanController {
  private final OaActivityPlanMapper activityPlanMapper;

  public OaActivityPlanController(OaActivityPlanMapper activityPlanMapper) {
    this.activityPlanMapper = activityPlanMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaActivityPlan>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(required = false) String keyword) {
    if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
      throw new IllegalArgumentException("开始日期不能晚于结束日期");
    }
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaActivityPlan.class)
        .eq(OaActivityPlan::getIsDeleted, 0)
        .eq(orgId != null, OaActivityPlan::getOrgId, orgId)
        .eq(normalizedStatus != null, OaActivityPlan::getStatus, normalizedStatus)
        .ge(dateFrom != null, OaActivityPlan::getPlanDate, dateFrom)
        .le(dateTo != null, OaActivityPlan::getPlanDate, dateTo);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaActivityPlan::getTitle, keyword)
          .or().like(OaActivityPlan::getOrganizer, keyword)
          .or().like(OaActivityPlan::getLocation, keyword));
    }
    wrapper.orderByDesc(OaActivityPlan::getPlanDate).orderByDesc(OaActivityPlan::getCreateTime);
    return Result.ok(activityPlanMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<OaActivityPlan> create(@Valid @RequestBody OaActivityPlanRequest request) {
    Long orgId = AuthContext.getOrgId();
    validateDateRange(request.getStartTime(), request.getEndTime());
    OaActivityPlan plan = new OaActivityPlan();
    plan.setTenantId(orgId);
    plan.setOrgId(orgId);
    plan.setTitle(request.getTitle());
    plan.setPlanDate(request.getPlanDate());
    plan.setStartTime(request.getStartTime());
    plan.setEndTime(request.getEndTime());
    plan.setLocation(request.getLocation());
    plan.setOrganizer(request.getOrganizer());
    plan.setParticipantTarget(request.getParticipantTarget());
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null && !"PLANNED".equals(normalizedStatus)) {
      throw new IllegalArgumentException("创建活动计划仅支持 PLANNED 状态");
    }
    plan.setStatus(normalizedStatus == null ? "PLANNED" : normalizedStatus);
    plan.setContent(request.getContent());
    plan.setRemark(request.getRemark());
    plan.setCreatedBy(AuthContext.getStaffId());
    activityPlanMapper.insert(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}")
  public Result<OaActivityPlan> update(@PathVariable Long id, @Valid @RequestBody OaActivityPlanRequest request) {
    OaActivityPlan plan = findAccessiblePlan(id);
    if (plan == null) {
      return Result.ok(null);
    }
    if ("DONE".equals(plan.getStatus()) || "CANCELLED".equals(plan.getStatus())) {
      throw new IllegalArgumentException("已完成/已取消计划不可编辑");
    }
    validateDateRange(request.getStartTime(), request.getEndTime());
    plan.setTitle(request.getTitle());
    plan.setPlanDate(request.getPlanDate());
    plan.setStartTime(request.getStartTime());
    plan.setEndTime(request.getEndTime());
    plan.setLocation(request.getLocation());
    plan.setOrganizer(request.getOrganizer());
    plan.setParticipantTarget(request.getParticipantTarget());
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null) {
      ensureStatusTransition(plan.getStatus(), normalizedStatus);
      plan.setStatus(normalizedStatus);
    }
    plan.setContent(request.getContent());
    plan.setRemark(request.getRemark());
    activityPlanMapper.updateById(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}/start")
  public Result<OaActivityPlan> start(@PathVariable Long id) {
    OaActivityPlan plan = findAccessiblePlan(id);
    if (plan == null) {
      return Result.ok(null);
    }
    ensureStatusTransition(plan.getStatus(), "IN_PROGRESS");
    plan.setStatus("IN_PROGRESS");
    activityPlanMapper.updateById(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}/done")
  public Result<OaActivityPlan> done(@PathVariable Long id) {
    OaActivityPlan plan = findAccessiblePlan(id);
    if (plan == null) {
      return Result.ok(null);
    }
    ensureStatusTransition(plan.getStatus(), "DONE");
    plan.setStatus("DONE");
    activityPlanMapper.updateById(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}/cancel")
  public Result<OaActivityPlan> cancel(@PathVariable Long id) {
    OaActivityPlan plan = findAccessiblePlan(id);
    if (plan == null) {
      return Result.ok(null);
    }
    ensureStatusTransition(plan.getStatus(), "CANCELLED");
    plan.setStatus("CANCELLED");
    activityPlanMapper.updateById(plan);
    return Result.ok(plan);
  }

  @PutMapping("/batch/start")
  public Result<Integer> batchStart(@RequestBody OaBatchIdsRequest request) {
    return batchStatusUpdate(request, "IN_PROGRESS");
  }

  @PutMapping("/batch/done")
  public Result<Integer> batchDone(@RequestBody OaBatchIdsRequest request) {
    return batchStatusUpdate(request, "DONE");
  }

  @PutMapping("/batch/cancel")
  public Result<Integer> batchCancel(@RequestBody OaBatchIdsRequest request) {
    return batchStatusUpdate(request, "CANCELLED");
  }

  @DeleteMapping("/batch")
  public Result<Integer> batchDelete(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaActivityPlan> list = activityPlanMapper.selectList(Wrappers.lambdaQuery(OaActivityPlan.class)
        .eq(OaActivityPlan::getIsDeleted, 0)
        .eq(orgId != null, OaActivityPlan::getOrgId, orgId)
        .in(OaActivityPlan::getId, ids));
    for (OaActivityPlan item : list) {
      item.setIsDeleted(1);
      activityPlanMapper.updateById(item);
    }
    return Result.ok(list.size());
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(required = false) String keyword) {
    if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
      throw new IllegalArgumentException("开始日期不能晚于结束日期");
    }
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaActivityPlan.class)
        .eq(OaActivityPlan::getIsDeleted, 0)
        .eq(orgId != null, OaActivityPlan::getOrgId, orgId)
        .eq(normalizedStatus != null, OaActivityPlan::getStatus, normalizedStatus)
        .ge(dateFrom != null, OaActivityPlan::getPlanDate, dateFrom)
        .le(dateTo != null, OaActivityPlan::getPlanDate, dateTo)
        .orderByDesc(OaActivityPlan::getPlanDate)
        .orderByDesc(OaActivityPlan::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaActivityPlan::getTitle, keyword)
          .or().like(OaActivityPlan::getOrganizer, keyword)
          .or().like(OaActivityPlan::getLocation, keyword));
    }
    List<OaActivityPlan> list = activityPlanMapper.selectList(wrapper);
    List<String> headers = List.of("ID", "计划标题", "计划日期", "开始时间", "结束时间", "地点", "组织人", "参与对象", "状态", "备注");
    List<List<String>> rows = list.stream()
        .map(item -> List.of(
            safe(item.getId()),
            safe(item.getTitle()),
            safe(item.getPlanDate()),
            formatDateTime(item.getStartTime()),
            formatDateTime(item.getEndTime()),
            safe(item.getLocation()),
            safe(item.getOrganizer()),
            safe(item.getParticipantTarget()),
            safe(item.getStatus()),
            safe(item.getRemark())))
        .toList();
    return csvResponse("oa-activity-plan", headers, rows);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaActivityPlan plan = findAccessiblePlan(id);
    if (plan != null) {
      plan.setIsDeleted(1);
      activityPlanMapper.updateById(plan);
    }
    return Result.ok(null);
  }

  private Result<Integer> batchStatusUpdate(OaBatchIdsRequest request, String targetStatus) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaActivityPlan> list = activityPlanMapper.selectList(Wrappers.lambdaQuery(OaActivityPlan.class)
        .eq(OaActivityPlan::getIsDeleted, 0)
        .eq(orgId != null, OaActivityPlan::getOrgId, orgId)
        .in(OaActivityPlan::getId, ids));
    int count = 0;
    for (OaActivityPlan item : list) {
      try {
        ensureStatusTransition(item.getStatus(), targetStatus);
      } catch (IllegalArgumentException ignored) {
        continue;
      }
      item.setStatus(targetStatus);
      activityPlanMapper.updateById(item);
      count++;
    }
    return Result.ok(count);
  }

  private OaActivityPlan findAccessiblePlan(Long id) {
    Long orgId = AuthContext.getOrgId();
    return activityPlanMapper.selectOne(Wrappers.lambdaQuery(OaActivityPlan.class)
        .eq(OaActivityPlan::getId, id)
        .eq(OaActivityPlan::getIsDeleted, 0)
        .eq(orgId != null, OaActivityPlan::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private void validateDateRange(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
      throw new IllegalArgumentException("开始时间不能晚于结束时间");
    }
  }

  private void ensureStatusTransition(String fromStatus, String toStatus) {
    if (toStatus == null || fromStatus == null || toStatus.equals(fromStatus)) {
      return;
    }
    if ("PLANNED".equals(fromStatus) && ("IN_PROGRESS".equals(toStatus) || "CANCELLED".equals(toStatus))) {
      return;
    }
    if ("IN_PROGRESS".equals(fromStatus) && ("DONE".equals(toStatus) || "CANCELLED".equals(toStatus))) {
      return;
    }
    throw new IllegalArgumentException("活动计划状态不允许从 " + fromStatus + " 变更为 " + toStatus);
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase();
    if (!"PLANNED".equals(normalized)
        && !"IN_PROGRESS".equals(normalized)
        && !"DONE".equals(normalized)
        && !"CANCELLED".equals(normalized)) {
      throw new IllegalArgumentException("status 仅支持 PLANNED/IN_PROGRESS/DONE/CANCELLED");
    }
    return normalized;
  }

  private List<Long> sanitizeIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return ids.stream().filter(id -> id != null && id > 0).distinct().collect(Collectors.toList());
  }

  private String safe(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private String formatDateTime(LocalDateTime value) {
    if (value == null) {
      return "";
    }
    return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  private ResponseEntity<byte[]> csvResponse(String filenameBase, List<String> headers, List<List<String>> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append('\uFEFF');
    sb.append(toCsvLine(headers)).append('\n');
    for (List<String> row : rows) {
      sb.append(toCsvLine(row)).append('\n');
    }
    byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    String filename = filenameBase + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    HttpHeaders headersObj = new HttpHeaders();
    headersObj.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename + ".csv");
    headersObj.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
    headersObj.setContentLength(bytes.length);
    return ResponseEntity.ok().headers(headersObj).body(bytes);
  }

  private String toCsvLine(List<String> fields) {
    List<String> escaped = new ArrayList<>();
    for (String field : fields) {
      String value = field == null ? "" : field;
      value = value.replace("\"", "\"\"");
      if (value.contains(",") || value.contains("\n") || value.contains("\r") || value.contains("\"")) {
        value = "\"" + value + "\"";
      }
      escaped.add(value);
    }
    return String.join(",", escaped);
  }
}
