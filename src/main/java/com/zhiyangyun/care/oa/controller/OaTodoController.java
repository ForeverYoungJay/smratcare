package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import com.zhiyangyun.care.oa.model.OaTodoSummaryResponse;
import com.zhiyangyun.care.oa.model.OaTodoRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
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
@RequestMapping("/api/oa/todo")
public class OaTodoController {
  private final OaTodoMapper todoMapper;

  public OaTodoController(OaTodoMapper todoMapper) {
    this.todoMapper = todoMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaTodo>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = buildQuery(orgId, normalizedStatus, keyword).orderByDesc(OaTodo::getCreateTime);
    return Result.ok(todoMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/summary")
  public Result<OaTodoSummaryResponse> summary(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfToday = startOfToday.plusDays(1);

    OaTodoSummaryResponse response = new OaTodoSummaryResponse();
    response.setTotalCount(count(todoMapper.selectCount(buildQuery(orgId, normalizedStatus, keyword))));
    response.setOpenCount(count(todoMapper.selectCount(buildQuery(orgId, "OPEN", keyword))));
    response.setDoneCount(count(todoMapper.selectCount(buildQuery(orgId, "DONE", keyword))));
    response.setDueTodayCount(count(todoMapper.selectCount(buildQuery(orgId, normalizedStatus, keyword)
        .eq(OaTodo::getStatus, "OPEN")
        .isNotNull(OaTodo::getDueTime)
        .ge(OaTodo::getDueTime, startOfToday)
        .lt(OaTodo::getDueTime, endOfToday))));
    response.setOverdueCount(count(todoMapper.selectCount(buildQuery(orgId, normalizedStatus, keyword)
        .eq(OaTodo::getStatus, "OPEN")
        .isNotNull(OaTodo::getDueTime)
        .lt(OaTodo::getDueTime, now))));
    response.setUnassignedCount(count(todoMapper.selectCount(buildQuery(orgId, normalizedStatus, keyword)
        .and(w -> w.isNull(OaTodo::getAssigneeName).or().eq(OaTodo::getAssigneeName, "")))));
    return Result.ok(response);
  }

  @PostMapping
  public Result<OaTodo> create(@Valid @RequestBody OaTodoRequest request) {
    Long orgId = AuthContext.getOrgId();
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
    todo.setAssigneeId(request.getAssigneeId());
    todo.setAssigneeName(request.getAssigneeName());
    todo.setCreatedBy(AuthContext.getStaffId());
    todoMapper.insert(todo);
    return Result.ok(todo);
  }

  @PutMapping("/{id}")
  public Result<OaTodo> update(@PathVariable Long id, @Valid @RequestBody OaTodoRequest request) {
    OaTodo todo = findAccessibleTodo(id);
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
    todo.setAssigneeId(request.getAssigneeId());
    todo.setAssigneeName(request.getAssigneeName());
    todoMapper.updateById(todo);
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
    return Result.ok(todo);
  }

  @PutMapping("/batch/done")
  public Result<Integer> batchDone(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaTodo> todos = todoMapper.selectList(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .in(OaTodo::getId, ids));
    int count = 0;
    for (OaTodo todo : todos) {
      if (!"OPEN".equals(todo.getStatus())) {
        continue;
      }
      todo.setStatus("DONE");
      todoMapper.updateById(todo);
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
    List<OaTodo> todos = todoMapper.selectList(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .in(OaTodo::getId, ids));
    for (OaTodo todo : todos) {
      todo.setIsDeleted(1);
      todoMapper.updateById(todo);
    }
    return Result.ok(todos.size());
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(normalizedStatus != null, OaTodo::getStatus, normalizedStatus)
        .orderByDesc(OaTodo::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaTodo::getTitle, keyword)
          .or().like(OaTodo::getContent, keyword)
          .or().like(OaTodo::getAssigneeName, keyword));
    }
    List<OaTodo> todos = todoMapper.selectList(wrapper);
    List<String> headers = List.of("ID", "标题", "内容", "负责人", "截止时间", "状态");
    List<List<String>> rows = todos.stream()
        .map(item -> List.of(
            safe(item.getId()),
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
    }
    return Result.ok(null);
  }

  private OaTodo findAccessibleTodo(Long id) {
    Long orgId = AuthContext.getOrgId();
    return todoMapper.selectOne(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getId, id)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
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
      Long orgId, String normalizedStatus, String keyword) {
    var wrapper = Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(normalizedStatus != null, OaTodo::getStatus, normalizedStatus);
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
}
