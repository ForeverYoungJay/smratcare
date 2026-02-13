package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.model.OaTaskRequest;
import jakarta.validation.Valid;
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
public class OaTaskController {
  private final OaTaskMapper taskMapper;

  public OaTaskController(OaTaskMapper taskMapper) {
    this.taskMapper = taskMapper;
  }

  @GetMapping("/page")
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

  @PostMapping
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
  public Result<Void> delete(@PathVariable Long id) {
    OaTask task = taskMapper.selectById(id);
    if (task != null) {
      task.setIsDeleted(1);
      taskMapper.updateById(task);
    }
    return Result.ok(null);
  }
}
