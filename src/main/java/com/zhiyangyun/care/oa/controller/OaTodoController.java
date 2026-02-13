package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.oa.model.OaTodoRequest;
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
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), OaTodo::getStatus, status)
        .orderByDesc(OaTodo::getCreateTime);
    return Result.ok(todoMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
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
    todo.setStatus(request.getStatus() == null ? "OPEN" : request.getStatus());
    todo.setAssigneeId(request.getAssigneeId());
    todo.setAssigneeName(request.getAssigneeName());
    todo.setCreatedBy(AuthContext.getStaffId());
    todoMapper.insert(todo);
    return Result.ok(todo);
  }

  @PutMapping("/{id}")
  public Result<OaTodo> update(@PathVariable Long id, @Valid @RequestBody OaTodoRequest request) {
    OaTodo todo = todoMapper.selectById(id);
    if (todo == null) {
      return Result.ok(null);
    }
    todo.setTitle(request.getTitle());
    todo.setContent(request.getContent());
    todo.setDueTime(request.getDueTime());
    todo.setStatus(request.getStatus());
    todo.setAssigneeId(request.getAssigneeId());
    todo.setAssigneeName(request.getAssigneeName());
    todoMapper.updateById(todo);
    return Result.ok(todo);
  }

  @PutMapping("/{id}/done")
  public Result<OaTodo> done(@PathVariable Long id) {
    OaTodo todo = todoMapper.selectById(id);
    if (todo == null) {
      return Result.ok(null);
    }
    todo.setStatus("DONE");
    todoMapper.updateById(todo);
    return Result.ok(todo);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaTodo todo = todoMapper.selectById(id);
    if (todo != null) {
      todo.setIsDeleted(1);
      todoMapper.updateById(todo);
    }
    return Result.ok(null);
  }
}
