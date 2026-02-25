package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.model.OaTaskRequest;
import com.zhiyangyun.care.oa.model.OaTaskTrendItem;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), OaTask::getStatus, status)
        .orderByDesc(OaTask::getStartTime);
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
    var wrapper = Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), OaTask::getStatus, status)
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
    OaTask task = new OaTask();
    task.setTenantId(orgId);
    task.setOrgId(orgId);
    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setStartTime(request.getStartTime());
    task.setEndTime(request.getEndTime());
    task.setPriority(request.getPriority() == null ? "NORMAL" : request.getPriority());
    task.setStatus(request.getStatus() == null ? "OPEN" : request.getStatus());
    task.setAssigneeId(request.getAssigneeId());
    task.setAssigneeName(request.getAssigneeName());
    task.setCreatedBy(AuthContext.getStaffId());
    taskMapper.insert(task);
    return Result.ok(task);
  }

  @PutMapping("/{id}")
  @PreAuthorize("@perm.has('oa.calendar.manage')")
  public Result<OaTask> update(@PathVariable Long id, @Valid @RequestBody OaTaskRequest request) {
    OaTask task = taskMapper.selectById(id);
    if (task == null) {
      return Result.ok(null);
    }
    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setStartTime(request.getStartTime());
    task.setEndTime(request.getEndTime());
    task.setPriority(request.getPriority());
    task.setStatus(request.getStatus());
    task.setAssigneeId(request.getAssigneeId());
    task.setAssigneeName(request.getAssigneeName());
    taskMapper.updateById(task);
    return Result.ok(task);
  }

  @PutMapping("/{id}/done")
  @PreAuthorize("@perm.has('oa.calendar.manage')")
  public Result<OaTask> done(@PathVariable Long id) {
    OaTask task = taskMapper.selectById(id);
    if (task == null) {
      return Result.ok(null);
    }
    task.setStatus("DONE");
    taskMapper.updateById(task);
    return Result.ok(task);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("@perm.has('oa.calendar.manage')")
  public Result<Void> delete(@PathVariable Long id) {
    OaTask task = taskMapper.selectById(id);
    if (task != null) {
      task.setIsDeleted(1);
      taskMapper.updateById(task);
    }
    return Result.ok(null);
  }

  private String toPeriod(LocalDateTime time, String dimension) {
    if ("MONTH".equalsIgnoreCase(dimension)) {
      return time.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
    return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
  }
}
