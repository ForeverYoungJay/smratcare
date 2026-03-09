package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import com.zhiyangyun.care.oa.model.OaTaskConflictCheckRequest;
import com.zhiyangyun.care.oa.model.OaTaskConflictItem;
import com.zhiyangyun.care.oa.model.OaTaskRequest;
import com.zhiyangyun.care.oa.model.OaTaskSummaryResponse;
import com.zhiyangyun.care.oa.model.OaTaskTrendItem;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
@PreAuthorize("hasAnyRole('STAFF','HR_EMPLOYEE','HR_MINISTER','MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','FINANCE_EMPLOYEE','FINANCE_MINISTER','LOGISTICS_EMPLOYEE','LOGISTICS_MINISTER','MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "false") boolean mineOnly) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = buildQuery(orgId, normalizedStatus, keyword, mineOnly).orderByDesc(OaTask::getStartTime);
    return Result.ok(taskMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/summary")
  @PreAuthorize("@perm.has('oa.calendar.view')")
  public Result<OaTaskSummaryResponse> summary(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "false") boolean mineOnly) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfToday = startOfToday.plusDays(1);

    OaTaskSummaryResponse response = new OaTaskSummaryResponse();
    response.setTotalCount(count(taskMapper.selectCount(buildQuery(orgId, normalizedStatus, keyword, mineOnly))));
    response.setOpenCount(count(taskMapper.selectCount(buildQuery(orgId, "OPEN", keyword, mineOnly))));
    response.setDoneCount(count(taskMapper.selectCount(buildQuery(orgId, "DONE", keyword, mineOnly))));
    response.setHighPriorityCount(count(taskMapper.selectCount(buildQuery(orgId, normalizedStatus, keyword, mineOnly)
        .eq(OaTask::getPriority, "HIGH"))));
    response.setDueTodayCount(count(taskMapper.selectCount(buildQuery(orgId, normalizedStatus, keyword, mineOnly)
        .eq(OaTask::getStatus, "OPEN")
        .isNotNull(OaTask::getEndTime)
        .ge(OaTask::getEndTime, startOfToday)
        .lt(OaTask::getEndTime, endOfToday))));
    response.setOverdueCount(count(taskMapper.selectCount(buildQuery(orgId, normalizedStatus, keyword, mineOnly)
        .eq(OaTask::getStatus, "OPEN")
        .isNotNull(OaTask::getEndTime)
        .lt(OaTask::getEndTime, now))));
    response.setUnassignedCount(count(taskMapper.selectCount(buildQuery(orgId, normalizedStatus, keyword, mineOnly)
        .and(w -> w.isNull(OaTask::getAssigneeName).or().eq(OaTask::getAssigneeName, "")))));
    return Result.ok(response);
  }

  @GetMapping("/calendar")
  @PreAuthorize("@perm.has('oa.calendar.view')")
  public Result<List<OaTask>> calendar(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String calendarType,
      @RequestParam(required = false) String urgency,
      @RequestParam(required = false) String assigneeName,
      @RequestParam(required = false) String keyword,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate startDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate endDate) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    String normalizedStatus = normalizeStatus(status);
    String normalizedCalendarType = normalizeCalendarType(calendarType);
    String normalizedUrgency = normalizeUrgency(urgency);
    var wrapper = Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(normalizedStatus != null, OaTask::getStatus, normalizedStatus)
        .eq(normalizedCalendarType != null, OaTask::getCalendarType, normalizedCalendarType)
        .eq(normalizedUrgency != null, OaTask::getUrgency, normalizedUrgency)
        .like(assigneeName != null && !assigneeName.isBlank(), OaTask::getAssigneeName, assigneeName)
        .ge(startDate != null, OaTask::getStartTime, startDate == null ? null : startDate.atStartOfDay())
        .lt(endDate != null, OaTask::getStartTime, endDate == null ? null : endDate.plusDays(1).atStartOfDay())
        .orderByDesc(OaTask::getStartTime)
        .orderByDesc(OaTask::getId);
    if (startDate == null && endDate == null) {
      wrapper.last("LIMIT 1000");
    } else {
      wrapper.last("LIMIT 5000");
    }
    applyMyCalendarScope(wrapper, staffId, false);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaTask::getTitle, keyword)
          .or().like(OaTask::getDescription, keyword));
    }
    return Result.ok(taskMapper.selectList(wrapper));
  }

  @PostMapping("/conflicts/check")
  @PreAuthorize("@perm.has('oa.calendar.view')")
  public Result<List<OaTaskConflictItem>> checkConflicts(@RequestBody OaTaskConflictCheckRequest request) {
    if (request == null || request.getStartTime() == null) {
      return Result.ok(List.of());
    }
    LocalDateTime rangeStart = request.getStartTime();
    LocalDateTime rangeEnd = request.getEndTime() == null ? request.getStartTime() : request.getEndTime();
    if (rangeStart.isAfter(rangeEnd)) {
      LocalDateTime tmp = rangeStart;
      rangeStart = rangeEnd;
      rangeEnd = tmp;
    }
    final LocalDateTime overlapRangeStart = rangeStart;

    String normalizedAssigneeName = trimToNull(request.getAssigneeName());
    List<Long> collaboratorIds = sanitizeIds(request.getCollaboratorIds());
    if (normalizedAssigneeName == null && collaboratorIds.isEmpty()) {
      return Result.ok(List.of());
    }

    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(OaTask::getStatus, "OPEN")
        .ne(request.getTaskId() != null, OaTask::getId, request.getTaskId())
        .isNotNull(OaTask::getStartTime)
        .le(OaTask::getStartTime, rangeEnd)
        .and(w -> w.isNull(OaTask::getEndTime).or().ge(OaTask::getEndTime, overlapRangeStart))
        .last("LIMIT 2000");

    if (normalizedAssigneeName != null || !collaboratorIds.isEmpty()) {
      wrapper.and(w -> {
        if (normalizedAssigneeName != null) {
          w.eq(OaTask::getAssigneeName, normalizedAssigneeName);
        }
        if (!collaboratorIds.isEmpty()) {
          if (normalizedAssigneeName != null) {
            w.or();
          }
          for (int i = 0; i < collaboratorIds.size(); i++) {
            Long collaboratorId = collaboratorIds.get(i);
            if (i > 0) {
              w.or();
            }
            w.apply("FIND_IN_SET({0}, collaborator_ids)", collaboratorId);
          }
        }
      });
    }

    List<OaTask> tasks = taskMapper.selectList(wrapper);
    List<OaTaskConflictItem> result = new ArrayList<>();
    for (OaTask task : tasks) {
      List<String> reasons = new ArrayList<>();
      if (normalizedAssigneeName != null && normalizedAssigneeName.equals(trimToNull(task.getAssigneeName()))) {
        reasons.add("负责人冲突");
      }
      if (!collaboratorIds.isEmpty() && hasCollaboratorOverlap(task.getCollaboratorIds(), collaboratorIds)) {
        reasons.add("协同成员冲突");
      }
      if (reasons.isEmpty()) {
        continue;
      }
      OaTaskConflictItem item = new OaTaskConflictItem();
      item.setTaskId(task.getId());
      item.setTitle(task.getTitle());
      item.setAssigneeName(task.getAssigneeName());
      item.setStartTime(task.getStartTime());
      item.setEndTime(task.getEndTime());
      item.setReason(String.join(" / ", reasons));
      result.add(item);
    }
    return Result.ok(result);
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
    task.setCalendarType(defaultIfBlank(normalizeCalendarType(request.getCalendarType()), "WORK"));
    task.setPlanCategory(trimToNull(request.getPlanCategory()));
    task.setUrgency(defaultIfBlank(normalizeUrgency(request.getUrgency()), "NORMAL"));
    task.setEventColor(defaultTaskColor(task.getUrgency(), request.getEventColor()));
    task.setCollaboratorIds(joinLongList(request.getCollaboratorIds()));
    task.setCollaboratorNames(joinStringList(request.getCollaboratorNames()));
    task.setIsRecurring(Boolean.TRUE.equals(request.getRecurring()) ? 1 : 0);
    task.setRecurrenceRule(normalizeRecurrenceRule(request.getRecurrenceRule()));
    task.setRecurrenceInterval(normalizeRecurrenceInterval(request.getRecurrenceInterval()));
    task.setRecurrenceCount(normalizeRecurrenceCount(request.getRecurrenceCount()));
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
    task.setCalendarType(defaultIfBlank(normalizeCalendarType(request.getCalendarType()), defaultIfBlank(task.getCalendarType(), "WORK")));
    task.setPlanCategory(trimToNull(request.getPlanCategory()));
    task.setUrgency(defaultIfBlank(normalizeUrgency(request.getUrgency()), defaultIfBlank(task.getUrgency(), "NORMAL")));
    task.setEventColor(defaultTaskColor(task.getUrgency(), request.getEventColor()));
    task.setCollaboratorIds(joinLongList(request.getCollaboratorIds()));
    task.setCollaboratorNames(joinStringList(request.getCollaboratorNames()));
    task.setIsRecurring(Boolean.TRUE.equals(request.getRecurring()) ? 1 : 0);
    task.setRecurrenceRule(normalizeRecurrenceRule(request.getRecurrenceRule()));
    task.setRecurrenceInterval(normalizeRecurrenceInterval(request.getRecurrenceInterval()));
    task.setRecurrenceCount(normalizeRecurrenceCount(request.getRecurrenceCount()));
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
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "false") boolean mineOnly) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = buildQuery(orgId, normalizedStatus, keyword, mineOnly)
        .orderByDesc(OaTask::getStartTime);
    List<OaTask> tasks = taskMapper.selectList(wrapper);
    List<String> headers = List.of("ID", "标题", "负责人", "日历种类", "计划分类", "紧急程度", "协同成员", "开始时间", "结束时间", "优先级", "状态", "描述");
    List<List<String>> rows = tasks.stream()
        .map(item -> List.of(
            safe(item.getId()),
            safe(item.getTitle()),
            safe(item.getAssigneeName()),
            safe(item.getCalendarType()),
            safe(item.getPlanCategory()),
            safe(item.getUrgency()),
            safe(item.getCollaboratorNames()),
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

  private String normalizeCalendarType(String calendarType) {
    if (calendarType == null || calendarType.isBlank()) {
      return null;
    }
    String normalized = calendarType.trim().toUpperCase();
    if (!"PERSONAL".equals(normalized)
        && !"WORK".equals(normalized)
        && !"DAILY".equals(normalized)
        && !"COLLAB".equals(normalized)) {
      throw new IllegalArgumentException("calendarType 仅支持 PERSONAL/WORK/DAILY/COLLAB");
    }
    return normalized;
  }

  private String normalizeUrgency(String urgency) {
    if (urgency == null || urgency.isBlank()) {
      return null;
    }
    String normalized = urgency.trim().toUpperCase();
    if (!"NORMAL".equals(normalized) && !"EMERGENCY".equals(normalized)) {
      throw new IllegalArgumentException("urgency 仅支持 NORMAL/EMERGENCY");
    }
    return normalized;
  }

  private String normalizeRecurrenceRule(String recurrenceRule) {
    if (recurrenceRule == null || recurrenceRule.isBlank()) {
      return null;
    }
    String normalized = recurrenceRule.trim().toUpperCase();
    if (!"DAILY".equals(normalized) && !"WEEKLY".equals(normalized) && !"MONTHLY".equals(normalized)) {
      throw new IllegalArgumentException("recurrenceRule 仅支持 DAILY/WEEKLY/MONTHLY");
    }
    return normalized;
  }

  private Integer normalizeRecurrenceInterval(Integer interval) {
    if (interval == null) {
      return null;
    }
    if (interval <= 0 || interval > 365) {
      throw new IllegalArgumentException("recurrenceInterval 取值范围 1-365");
    }
    return interval;
  }

  private Integer normalizeRecurrenceCount(Integer count) {
    if (count == null) {
      return null;
    }
    if (count <= 0 || count > 365) {
      throw new IllegalArgumentException("recurrenceCount 取值范围 1-365");
    }
    return count;
  }

  private String joinLongList(List<Long> values) {
    if (values == null || values.isEmpty()) {
      return null;
    }
    return values.stream()
        .filter(v -> v != null && v > 0)
        .distinct()
        .map(String::valueOf)
        .collect(Collectors.joining(","));
  }

  private String joinStringList(List<String> values) {
    if (values == null || values.isEmpty()) {
      return null;
    }
    return values.stream()
        .map(this::trimToNull)
        .filter(v -> v != null && !v.isBlank())
        .distinct()
        .collect(Collectors.joining(","));
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String defaultIfBlank(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return value;
  }

  private String defaultTaskColor(String urgency, String requestedColor) {
    String normalizedColor = normalizeColor(requestedColor);
    if (normalizedColor != null) {
      return normalizedColor;
    }
    if ("EMERGENCY".equalsIgnoreCase(urgency)) {
      return "#ff4d4f";
    }
    return "#1677ff";
  }

  private String normalizeColor(String color) {
    if (color == null || color.isBlank()) {
      return null;
    }
    String value = color.trim();
    if (value.matches("^#[0-9a-fA-F]{6}$")) {
      return value.toLowerCase();
    }
    List<String> preset = Arrays.asList("#1677ff", "#52c41a", "#faad14", "#ff4d4f", "#722ed1");
    for (String item : preset) {
      if (item.equalsIgnoreCase(value)) {
        return item;
      }
    }
    throw new IllegalArgumentException("eventColor 必须是 #RRGGBB 格式");
  }

  private List<Long> sanitizeIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return ids.stream().filter(id -> id != null && id > 0).distinct().collect(Collectors.toList());
  }

  private boolean hasCollaboratorOverlap(String collaboratorIdsText, List<Long> collaboratorIds) {
    if (collaboratorIdsText == null || collaboratorIdsText.isBlank() || collaboratorIds == null || collaboratorIds.isEmpty()) {
      return false;
    }
    List<String> existed = Arrays.stream(collaboratorIdsText.split(","))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .toList();
    for (Long collaboratorId : collaboratorIds) {
      if (existed.contains(String.valueOf(collaboratorId))) {
        return true;
      }
    }
    return false;
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

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OaTask> buildQuery(
      Long orgId, String normalizedStatus, String keyword, boolean mineOnly) {
    Long staffId = AuthContext.getStaffId();
    var wrapper = Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(normalizedStatus != null, OaTask::getStatus, normalizedStatus);
    applyMyCalendarScope(wrapper, staffId, mineOnly);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaTask::getTitle, keyword)
          .or().like(OaTask::getDescription, keyword)
          .or().like(OaTask::getAssigneeName, keyword));
    }
    return wrapper;
  }

  private void applyMyCalendarScope(
      com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OaTask> wrapper,
      Long staffId,
      boolean mineOnly) {
    if (AuthContext.isAdmin() && !mineOnly) {
      return;
    }
    String username = trimToNull(AuthContext.getUsername());
    if (staffId == null) {
      if (username == null) {
        wrapper.eq(OaTask::getId, -1L);
      } else {
        wrapper.eq(OaTask::getAssigneeName, username);
      }
      return;
    }
    wrapper.and(w -> w.eq(OaTask::getCreatedBy, staffId)
        .or().eq(OaTask::getAssigneeId, staffId)
        .or().eq(username != null, OaTask::getAssigneeName, username)
        .or().apply("FIND_IN_SET({0}, collaborator_ids)", staffId));
  }

  private long count(Long value) {
    return value == null ? 0L : value;
  }

  private String toPeriod(LocalDateTime time, String dimension) {
    if ("MONTH".equalsIgnoreCase(dimension)) {
      return time.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
    return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
  }
}
