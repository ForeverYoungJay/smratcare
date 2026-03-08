package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import com.zhiyangyun.care.oa.model.OaTodoSummaryResponse;
import com.zhiyangyun.care.oa.model.OaTodoRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
@RequestMapping("/api/oa/todo")
public class OaTodoController {
  private final OaTodoMapper todoMapper;
  private final OaTaskMapper taskMapper;

  public OaTodoController(OaTodoMapper todoMapper, OaTaskMapper taskMapper) {
    this.todoMapper = todoMapper;
    this.taskMapper = taskMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaTodo>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String sourceType,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "false") boolean mineOnly) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    boolean adminView = AuthContext.isAdmin();
    String normalizedStatus = normalizeStatus(status);
    String normalizedSourceType = normalizeSourceType(sourceType);
    var wrapper = buildQuery(orgId, staffId, adminView, mineOnly, normalizedStatus, normalizedSourceType, keyword)
        .orderByDesc(OaTodo::getCreateTime);
    return Result.ok(todoMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/summary")
  public Result<OaTodoSummaryResponse> summary(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String sourceType,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "false") boolean mineOnly) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    boolean adminView = AuthContext.isAdmin();
    String normalizedStatus = normalizeStatus(status);
    String normalizedSourceType = normalizeSourceType(sourceType);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfToday = startOfToday.plusDays(1);

    OaTodoSummaryResponse response = new OaTodoSummaryResponse();
    response.setTotalCount(count(todoMapper.selectCount(buildQuery(orgId, staffId, adminView, mineOnly, normalizedStatus, normalizedSourceType, keyword))));
    response.setOpenCount(count(todoMapper.selectCount(buildQuery(orgId, staffId, adminView, mineOnly, "OPEN", normalizedSourceType, keyword))));
    response.setDoneCount(count(todoMapper.selectCount(buildQuery(orgId, staffId, adminView, mineOnly, "DONE", normalizedSourceType, keyword))));
    response.setDueTodayCount(count(todoMapper.selectCount(buildQuery(orgId, staffId, adminView, mineOnly, normalizedStatus, normalizedSourceType, keyword)
        .eq(OaTodo::getStatus, "OPEN")
        .isNotNull(OaTodo::getDueTime)
        .ge(OaTodo::getDueTime, startOfToday)
        .lt(OaTodo::getDueTime, endOfToday))));
    response.setOverdueCount(count(todoMapper.selectCount(buildQuery(orgId, staffId, adminView, mineOnly, normalizedStatus, normalizedSourceType, keyword)
        .eq(OaTodo::getStatus, "OPEN")
        .isNotNull(OaTodo::getDueTime)
        .lt(OaTodo::getDueTime, now))));
    response.setUnassignedCount(count(todoMapper.selectCount(buildQuery(orgId, staffId, adminView, mineOnly, normalizedStatus, normalizedSourceType, keyword)
        .and(w -> w.isNull(OaTodo::getAssigneeName).or().eq(OaTodo::getAssigneeName, "")))));
    response.setBirthdayOpenCount(count(todoMapper.selectCount(buildQuery(orgId, staffId, adminView, mineOnly, "OPEN", "BIRTHDAY", keyword))));
    response.setApprovalOpenCount(count(todoMapper.selectCount(buildQuery(orgId, staffId, adminView, mineOnly, "OPEN", "APPROVAL", keyword))));
    response.setNormalOpenCount(count(todoMapper.selectCount(buildQuery(orgId, staffId, adminView, mineOnly, "OPEN", "NORMAL", keyword))));
    return Result.ok(response);
  }

  @PostMapping
  public Result<OaTodo> create(@Valid @RequestBody OaTodoRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long currentStaffId = AuthContext.getStaffId();
    boolean adminView = AuthContext.isAdmin();
    OaTodo todo = new OaTodo();
    todo.setTenantId(orgId);
    todo.setOrgId(orgId);
    todo.setTitle(request.getTitle());
    todo.setContent(request.getContent());
    todo.setDueTime(request.getDueTime());
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null && !"OPEN".equals(normalizedStatus)) {
      throw new IllegalArgumentException("创建待办仅支持 OPEN 状态");
    }
    todo.setStatus(normalizedStatus == null ? "OPEN" : normalizedStatus);
    if (!adminView) {
      todo.setAssigneeId(currentStaffId);
      todo.setAssigneeName(AuthContext.getUsername());
    } else {
      todo.setAssigneeId(request.getAssigneeId());
      todo.setAssigneeName(request.getAssigneeName());
    }
    todo.setCreatedBy(currentStaffId);
    todoMapper.insert(todo);
    syncTodoTask(todo);
    return Result.ok(todo);
  }

  @PutMapping("/{id}")
  public Result<OaTodo> update(@PathVariable Long id, @Valid @RequestBody OaTodoRequest request) {
    OaTodo todo = findAccessibleTodo(id);
    boolean adminView = AuthContext.isAdmin();
    if (todo == null) {
      return Result.ok(null);
    }
    if (!"OPEN".equals(todo.getStatus())) {
      throw new IllegalArgumentException("仅 OPEN 状态待办可编辑");
    }
    todo.setTitle(request.getTitle());
    todo.setContent(request.getContent());
    todo.setDueTime(request.getDueTime());
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null && !"OPEN".equals(normalizedStatus)) {
      throw new IllegalArgumentException("编辑待办仅支持保持 OPEN 状态");
    }
    todo.setStatus("OPEN");
    if (adminView) {
      todo.setAssigneeId(request.getAssigneeId());
      todo.setAssigneeName(request.getAssigneeName());
    } else {
      todo.setAssigneeId(AuthContext.getStaffId());
      todo.setAssigneeName(AuthContext.getUsername());
    }
    todoMapper.updateById(todo);
    syncTodoTask(todo);
    return Result.ok(todo);
  }

  @PutMapping("/{id}/done")
  public Result<OaTodo> done(@PathVariable Long id) {
    OaTodo todo = findAccessibleTodo(id);
    if (todo == null) {
      return Result.ok(null);
    }
    if (!"OPEN".equals(todo.getStatus())) {
      throw new IllegalArgumentException("仅 OPEN 状态待办可完成");
    }
    todo.setStatus("DONE");
    todoMapper.updateById(todo);
    syncTodoTask(todo);
    return Result.ok(todo);
  }

  @PutMapping("/{id}/snooze")
  public Result<OaTodo> snooze(
      @PathVariable Long id,
      @RequestParam(required = false, defaultValue = "1") Integer days) {
    OaTodo todo = findAccessibleTodo(id);
    if (todo == null) {
      return Result.ok(null);
    }
    if (!"OPEN".equals(todo.getStatus())) {
      throw new IllegalArgumentException("仅 OPEN 状态待办可延后");
    }
    if (!isBirthdayReminder(todo)) {
      throw new IllegalArgumentException("仅生日提醒待办支持延后");
    }
    int shift = normalizeSnoozeDays(days);
    shiftBirthdayReminder(todo, shift);
    todoMapper.updateById(todo);
    syncTodoTask(todo);
    return Result.ok(todo);
  }

  @PutMapping("/{id}/ignore-birthday")
  public Result<OaTodo> ignoreBirthday(@PathVariable Long id) {
    OaTodo todo = findAccessibleTodo(id);
    if (todo == null) {
      return Result.ok(null);
    }
    if (!"OPEN".equals(todo.getStatus())) {
      throw new IllegalArgumentException("仅 OPEN 状态待办可忽略");
    }
    if (!isBirthdayReminder(todo)) {
      throw new IllegalArgumentException("仅生日提醒待办支持忽略");
    }
    ignoreBirthdayReminder(todo);
    todoMapper.updateById(todo);
    syncTodoTask(todo);
    return Result.ok(todo);
  }

  @PutMapping("/batch/snooze")
  public Result<Integer> batchSnooze(
      @RequestBody OaBatchIdsRequest request,
      @RequestParam(required = false, defaultValue = "1") Integer days) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    boolean adminView = AuthContext.isAdmin();
    if (!adminView && staffId == null) {
      return Result.ok(0);
    }
    int shift = normalizeSnoozeDays(days);
    List<OaTodo> todos = todoMapper.selectList(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .and(!adminView, w -> w.eq(OaTodo::getAssigneeId, staffId).or().eq(OaTodo::getCreatedBy, staffId))
        .in(OaTodo::getId, ids));
    int count = 0;
    for (OaTodo todo : todos) {
      if (!"OPEN".equals(todo.getStatus()) || !isBirthdayReminder(todo)) {
        continue;
      }
      shiftBirthdayReminder(todo, shift);
      todoMapper.updateById(todo);
      syncTodoTask(todo);
      count++;
    }
    return Result.ok(count);
  }

  @PutMapping("/batch/ignore-birthday")
  public Result<Integer> batchIgnoreBirthday(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    boolean adminView = AuthContext.isAdmin();
    if (!adminView && staffId == null) {
      return Result.ok(0);
    }
    List<OaTodo> todos = todoMapper.selectList(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .and(!adminView, w -> w.eq(OaTodo::getAssigneeId, staffId).or().eq(OaTodo::getCreatedBy, staffId))
        .in(OaTodo::getId, ids));
    int count = 0;
    for (OaTodo todo : todos) {
      if (!"OPEN".equals(todo.getStatus()) || !isBirthdayReminder(todo)) {
        continue;
      }
      ignoreBirthdayReminder(todo);
      todoMapper.updateById(todo);
      syncTodoTask(todo);
      count++;
    }
    return Result.ok(count);
  }

  @PutMapping("/batch/done")
  public Result<Integer> batchDone(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    boolean adminView = AuthContext.isAdmin();
    if (!adminView && staffId == null) {
      return Result.ok(0);
    }
    List<OaTodo> todos = todoMapper.selectList(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .and(!adminView, w -> w.eq(OaTodo::getAssigneeId, staffId).or().eq(OaTodo::getCreatedBy, staffId))
        .in(OaTodo::getId, ids));
    int count = 0;
    for (OaTodo todo : todos) {
      if (!"OPEN".equals(todo.getStatus())) {
        continue;
      }
      todo.setStatus("DONE");
      todoMapper.updateById(todo);
      syncTodoTask(todo);
      count++;
    }
    return Result.ok(count);
  }

  @DeleteMapping("/batch")
  public Result<Integer> batchDelete(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    boolean adminView = AuthContext.isAdmin();
    if (!adminView && staffId == null) {
      return Result.ok(0);
    }
    List<OaTodo> todos = todoMapper.selectList(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .and(!adminView, w -> w.eq(OaTodo::getAssigneeId, staffId).or().eq(OaTodo::getCreatedBy, staffId))
        .in(OaTodo::getId, ids));
    for (OaTodo todo : todos) {
      todo.setIsDeleted(1);
      todoMapper.updateById(todo);
      deleteTodoTask(todo.getId(), orgId);
    }
    return Result.ok(todos.size());
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String sourceType,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "false") boolean mineOnly) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    boolean adminView = AuthContext.isAdmin();
    String normalizedStatus = normalizeStatus(status);
    String normalizedSourceType = normalizeSourceType(sourceType);
    var wrapper = buildQuery(orgId, staffId, adminView, mineOnly, normalizedStatus, normalizedSourceType, keyword)
        .orderByDesc(OaTodo::getCreateTime);
    List<OaTodo> todos = todoMapper.selectList(wrapper);
    List<String> headers = List.of("ID", "来源", "标题", "内容", "负责人", "截止时间", "状态");
    List<List<String>> rows = todos.stream()
        .map(item -> List.of(
            safe(item.getId()),
            resolveTodoSourceType(item),
            safe(item.getTitle()),
            safe(item.getContent()),
            safe(item.getAssigneeName()),
            formatDateTime(item.getDueTime()),
            safe(item.getStatus())))
        .toList();
    return csvResponse("oa-todo", headers, rows);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaTodo todo = findAccessibleTodo(id);
    if (todo != null) {
      todo.setIsDeleted(1);
      todoMapper.updateById(todo);
      deleteTodoTask(todo.getId(), todo.getOrgId());
    }
    return Result.ok(null);
  }

  private void syncTodoTask(OaTodo todo) {
    if (todo == null || todo.getId() == null) {
      return;
    }
    Long orgId = todo.getOrgId();
    OaTask task = taskMapper.selectOne(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(OaTask::getSourceTodoId, todo.getId())
        .last("LIMIT 1"));
    if (task == null) {
      task = new OaTask();
      task.setTenantId(todo.getTenantId());
      task.setOrgId(todo.getOrgId());
      task.setCreatedBy(todo.getCreatedBy());
      task.setSourceTodoId(todo.getId());
    }
    task.setTitle("【代办】" + safe(todo.getTitle()));
    task.setDescription(todo.getContent());
    task.setStartTime(todo.getDueTime());
    task.setEndTime(todo.getDueTime());
    task.setPriority("NORMAL");
    task.setStatus("DONE".equalsIgnoreCase(todo.getStatus()) ? "DONE" : "OPEN");
    task.setAssigneeId(todo.getAssigneeId());
    task.setAssigneeName(todo.getAssigneeName());
    task.setCalendarType("DAILY");
    task.setPlanCategory("代办事项");
    task.setUrgency("NORMAL");
    task.setEventColor("#1677ff");
    task.setIsRecurring(0);
    if (task.getId() == null) {
      taskMapper.insert(task);
    } else {
      taskMapper.updateById(task);
    }
  }

  private void deleteTodoTask(Long todoId, Long orgId) {
    if (todoId == null) {
      return;
    }
    OaTask task = taskMapper.selectOne(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(OaTask::getSourceTodoId, todoId)
        .last("LIMIT 1"));
    if (task == null) {
      return;
    }
    task.setIsDeleted(1);
    taskMapper.updateById(task);
  }

  private OaTodo findAccessibleTodo(Long id) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    boolean adminView = AuthContext.isAdmin();
    if (!adminView && staffId == null) {
      return null;
    }
    return todoMapper.selectOne(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getId, id)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .and(!adminView, w -> w.eq(OaTodo::getAssigneeId, staffId)
            .or()
            .eq(OaTodo::getCreatedBy, staffId))
        .last("LIMIT 1"));
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

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OaTodo> buildQuery(
      Long orgId, Long staffId, boolean adminView, boolean mineOnly, String normalizedStatus, String normalizedSourceType, String keyword) {
    var wrapper = Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(normalizedStatus != null, OaTodo::getStatus, normalizedStatus);
    if (mineOnly || !adminView) {
      if (staffId == null) {
        wrapper.eq(OaTodo::getId, -1L);
        return wrapper;
      }
      wrapper.and(w -> w.eq(staffId != null, OaTodo::getAssigneeId, staffId)
          .or()
          .eq(staffId != null, OaTodo::getCreatedBy, staffId));
    }
    if ("BIRTHDAY".equals(normalizedSourceType)) {
      wrapper.like(OaTodo::getContent, "[BIRTHDAY_REMINDER:");
    } else if ("APPROVAL".equals(normalizedSourceType)) {
      wrapper.like(OaTodo::getContent, "[APPROVAL_FLOW:");
    } else if ("NORMAL".equals(normalizedSourceType)) {
      wrapper.and(w -> w.and(n -> n.notLike(OaTodo::getContent, "[BIRTHDAY_REMINDER:")
              .notLike(OaTodo::getContent, "[APPROVAL_FLOW:"))
          .or().isNull(OaTodo::getContent));
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaTodo::getTitle, keyword)
          .or().like(OaTodo::getContent, keyword)
          .or().like(OaTodo::getAssigneeName, keyword));
    }
    return wrapper;
  }

  private long count(Long value) {
    return value == null ? 0L : value;
  }

  private boolean isBirthdayReminder(OaTodo todo) {
    if (todo == null) {
      return false;
    }
    String content = safe(todo.getContent());
    return content.contains("[BIRTHDAY_REMINDER:");
  }

  private String normalizeSourceType(String sourceType) {
    if (sourceType == null || sourceType.isBlank()) {
      return null;
    }
    String normalized = sourceType.trim().toUpperCase();
    if (!"BIRTHDAY".equals(normalized)
        && !"APPROVAL".equals(normalized)
        && !"NORMAL".equals(normalized)) {
      throw new IllegalArgumentException("sourceType 仅支持 BIRTHDAY/APPROVAL/NORMAL");
    }
    return normalized;
  }

  private String resolveTodoSourceType(OaTodo todo) {
    if (todo == null) {
      return "普通待办";
    }
    String content = safe(todo.getContent());
    if (content.contains("[APPROVAL_FLOW:")) {
      return "审批流";
    }
    if (content.contains("[BIRTHDAY_REMINDER:")) {
      return "生日提醒";
    }
    return "普通待办";
  }

  private int normalizeSnoozeDays(Integer days) {
    return days == null ? 1 : Math.max(1, Math.min(days, 30));
  }

  private void shiftBirthdayReminder(OaTodo todo, int shiftDays) {
    if (todo == null) {
      return;
    }
    LocalDate baseDate = (todo.getDueTime() == null ? LocalDate.now() : todo.getDueTime().toLocalDate());
    LocalDate nextDate = baseDate.plusDays(Math.max(1, shiftDays));
    todo.setDueTime(LocalDateTime.of(nextDate, LocalTime.of(9, 0)));
    String operator = safe(AuthContext.getUsername());
    String content = safe(todo.getContent());
    content = content + " [BIRTHDAY_SNOOZED:" + Math.max(1, shiftDays) + "d"
        + ":" + (operator.isBlank() ? "system" : operator)
        + ":" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        + "]";
    todo.setContent(content);
  }

  private void ignoreBirthdayReminder(OaTodo todo) {
    if (todo == null) {
      return;
    }
    String operator = safe(AuthContext.getUsername());
    String content = safe(todo.getContent());
    if (!content.contains("[BIRTHDAY_IGNORED]")) {
      content = content + " [BIRTHDAY_IGNORED]";
    }
    content = content + " [BIRTHDAY_IGNORED_BY:"
        + (operator.isBlank() ? "system" : operator)
        + ":" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        + "]";
    todo.setContent(content);
    todo.setStatus("DONE");
    todo.setDueTime(LocalDateTime.now());
  }
}
