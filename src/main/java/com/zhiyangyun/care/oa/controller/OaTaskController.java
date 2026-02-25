package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import com.zhiyangyun.care.oa.model.OaTaskRequest;
import com.zhiyangyun.care.oa.model.OaTaskTrendItem;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/oa/task")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class OaTaskController {
  private final OaTaskMapper taskMapper;

  public OaTaskController(OaTaskMapper taskMapper) {
    this.taskMapper = taskMapper;
  }

  @GetMapping("/page")
  @PreAuthorize("@perm.has('oa.calendar.view')")
  public Result<IPage<OaTask>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(normalizedStatus != null, OaTask::getStatus, normalizedStatus)
        .orderByDesc(OaTask::getStartTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaTask::getTitle, keyword)
          .or().like(OaTask::getDescription, keyword)
          .or().like(OaTask::getAssigneeName, keyword));
    }
    return Result.ok(taskMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/calendar")
  @PreAuthorize("@perm.has('oa.calendar.view')")
  public Result<List<OaTask>> calendar(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String assigneeName,
      @RequestParam(required = false) String keyword,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate startDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate endDate) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(normalizedStatus != null, OaTask::getStatus, normalizedStatus)
        .like(assigneeName != null && !assigneeName.isBlank(), OaTask::getAssigneeName, assigneeName)
        .ge(startDate != null, OaTask::getStartTime, startDate == null ? null : startDate.atStartOfDay())
        .lt(endDate != null, OaTask::getStartTime, endDate == null ? null : endDate.plusDays(1).atStartOfDay())
        .orderByAsc(OaTask::getStartTime)
        .last("LIMIT 1000");
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaTask::getTitle, keyword)
          .or().like(OaTask::getDescription, keyword));
    }
    return Result.ok(taskMapper.selectList(wrapper));
  }

  @GetMapping("/stats/trend")
  @PreAuthorize("@perm.has('oa.calendar.stats')")
  public Result<List<OaTaskTrendItem>> statsTrend(
      @RequestParam(defaultValue = "DAY") String dimension,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateFrom,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateTo) {
    Long orgId = AuthContext.getOrgId();
    LocalDate from = dateFrom == null ? LocalDate.now().minusDays(29) : dateFrom;
    LocalDate to = dateTo == null ? LocalDate.now() : dateTo;
    List<OaTask> tasks = taskMapper.selectList(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .isNotNull(OaTask::getStartTime)
        .ge(OaTask::getStartTime, from.atStartOfDay())
        .lt(OaTask::getStartTime, to.plusDays(1).atStartOfDay()));

    Map<String, OaTaskTrendItem> map = new LinkedHashMap<>();
    for (OaTask task : tasks) {
      if (task.getStartTime() == null) {
        continue;
      }
      String period = toPeriod(task.getStartTime(), dimension);
      OaTaskTrendItem item = map.computeIfAbsent(period, k -> {
        OaTaskTrendItem created = new OaTaskTrendItem();
        created.setPeriod(k);
        created.setTotalCount(0L);
        created.setDoneCount(0L);
        created.setOpenCount(0L);
        created.setHighPriorityCount(0L);
        return created;
      });
      item.setTotalCount(item.getTotalCount() + 1);
      if ("DONE".equalsIgnoreCase(task.getStatus())) {
        item.setDoneCount(item.getDoneCount() + 1);
      } else {
        item.setOpenCount(item.getOpenCount() + 1);
      }
      if ("HIGH".equalsIgnoreCase(task.getPriority())) {
        item.setHighPriorityCount(item.getHighPriorityCount() + 1);
      }
    }
    return Result.ok(map.values().stream().toList());
  }

  @PostMapping
  @PreAuthorize("@perm.has('oa.calendar.manage')")
  public Result<OaTask> create(@Valid @RequestBody OaTaskRequest request) {
    Long orgId = AuthContext.getOrgId();
    validateDateRange(request.getStartTime(), request.getEndTime());
    OaTask task = new OaTask();
    task.setTenantId(orgId);
    task.setOrgId(orgId);
    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setStartTime(request.getStartTime());
    task.setEndTime(request.getEndTime());
    String normalizedPriority = normalizePriority(request.getPriority());
    task.setPriority(normalizedPriority == null ? "NORMAL" : normalizedPriority);
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null && !"OPEN".equals(normalizedStatus)) {
      throw new IllegalArgumentException("创建任务仅支持 OPEN 状态");
    }
    task.setStatus(normalizedStatus == null ? "OPEN" : normalizedStatus);
    task.setAssigneeId(request.getAssigneeId());
    task.setAssigneeName(request.getAssigneeName());
    task.setCreatedBy(AuthContext.getStaffId());
    taskMapper.insert(task);
    return Result.ok(task);
  }

  @PutMapping("/{id}")
  @PreAuthorize("@perm.has('oa.calendar.manage')")
  public Result<OaTask> update(@PathVariable Long id, @Valid @RequestBody OaTaskRequest request) {
    OaTask task = findAccessibleTask(id);
    if (task == null) {
      return Result.ok(null);
    }
    if (!"OPEN".equals(task.getStatus())) {
      throw new IllegalArgumentException("仅 OPEN 状态任务可编辑");
    }
    validateDateRange(request.getStartTime(), request.getEndTime());
    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setStartTime(request.getStartTime());
    task.setEndTime(request.getEndTime());
    String normalizedPriority = normalizePriority(request.getPriority());
    task.setPriority(normalizedPriority == null ? task.getPriority() : normalizedPriority);
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null && !"OPEN".equals(normalizedStatus)) {
      throw new IllegalArgumentException("编辑任务仅支持保持 OPEN 状态");
    }
    task.setStatus("OPEN");
    task.setAssigneeId(request.getAssigneeId());
    task.setAssigneeName(request.getAssigneeName());
    taskMapper.updateById(task);
    return Result.ok(task);
  }

  @PutMapping("/{id}/done")
  @PreAuthorize("@perm.has('oa.calendar.manage')")
  public Result<OaTask> done(@PathVariable Long id) {
    OaTask task = findAccessibleTask(id);
    if (task == null) {
      return Result.ok(null);
    }
    if (!"OPEN".equals(task.getStatus())) {
      throw new IllegalArgumentException("仅 OPEN 状态任务可完成");
    }
    task.setStatus("DONE");
    taskMapper.updateById(task);
    return Result.ok(task);
  }

  @PutMapping("/batch/done")
  @PreAuthorize("@perm.has('oa.calendar.manage')")
  public Result<Integer> batchDone(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaTask> tasks = taskMapper.selectList(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .in(OaTask::getId, ids));
    int count = 0;
    for (OaTask task : tasks) {
      if (!"OPEN".equals(task.getStatus())) {
        continue;
      }
      task.setStatus("DONE");
      taskMapper.updateById(task);
      count++;
    }
    return Result.ok(count);
  }

  @DeleteMapping("/batch")
  @PreAuthorize("@perm.has('oa.calendar.manage')")
  public Result<Integer> batchDelete(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaTask> tasks = taskMapper.selectList(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .in(OaTask::getId, ids));
    for (OaTask task : tasks) {
      task.setIsDeleted(1);
      taskMapper.updateById(task);
    }
    return Result.ok(tasks.size());
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  @PreAuthorize("@perm.has('oa.calendar.view')")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(normalizedStatus != null, OaTask::getStatus, normalizedStatus)
        .orderByDesc(OaTask::getStartTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaTask::getTitle, keyword)
          .or().like(OaTask::getDescription, keyword)
          .or().like(OaTask::getAssigneeName, keyword));
    }
    List<OaTask> tasks = taskMapper.selectList(wrapper);
    List<String> headers = List.of("ID", "标题", "负责人", "开始时间", "结束时间", "优先级", "状态", "描述");
    List<List<String>> rows = tasks.stream()
        .map(item -> List.of(
            safe(item.getId()),
            safe(item.getTitle()),
            safe(item.getAssigneeName()),
            formatDateTime(item.getStartTime()),
            formatDateTime(item.getEndTime()),
            safe(item.getPriority()),
            safe(item.getStatus()),
            safe(item.getDescription())))
        .toList();
    return csvResponse("oa-task", headers, rows);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("@perm.has('oa.calendar.manage')")
  public Result<Void> delete(@PathVariable Long id) {
    OaTask task = findAccessibleTask(id);
    if (task != null) {
      task.setIsDeleted(1);
      taskMapper.updateById(task);
    }
    return Result.ok(null);
  }

  private OaTask findAccessibleTask(Long id) {
    Long orgId = AuthContext.getOrgId();
    return taskMapper.selectOne(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getId, id)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private void validateDateRange(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
      throw new IllegalArgumentException("开始时间不能晚于结束时间");
    }
  }

  private String normalizePriority(String priority) {
    if (priority == null || priority.isBlank()) {
      return null;
    }
    String normalized = priority.trim().toUpperCase();
    if (!"LOW".equals(normalized) && !"NORMAL".equals(normalized) && !"HIGH".equals(normalized)) {
      throw new IllegalArgumentException("priority 仅支持 LOW/NORMAL/HIGH");
    }
    return normalized;
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase();
    if (!"OPEN".equals(normalized) && !"DONE".equals(normalized)) {
      throw new IllegalArgumentException("status 仅支持 OPEN/DONE");
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

  private String toPeriod(LocalDateTime time, String dimension) {
    if ("MONTH".equalsIgnoreCase(dimension)) {
      return time.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
    return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
  }
}
