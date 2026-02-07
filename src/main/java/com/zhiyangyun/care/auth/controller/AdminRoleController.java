package com.zhiyangyun.care.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.Role;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.auth.model.RoleRequest;
import jakarta.validation.Valid;
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
@RequestMapping("/api/admin/roles")
public class AdminRoleController {
  private final RoleMapper roleMapper;

  public AdminRoleController(RoleMapper roleMapper) {
    this.roleMapper = roleMapper;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<Role> create(@Valid @RequestBody RoleRequest request) {
    Role role = new Role();
    role.setOrgId(AuthContext.getOrgId());
    role.setRoleName(request.getRoleName());
    role.setRoleCode(request.getRoleCode());
    role.setRoleDesc(request.getRoleDesc());
    role.setStatus(request.getStatus());
    roleMapper.insert(role);
    return Result.ok(role);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<Role> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
    Role role = roleMapper.selectById(id);
    if (role == null) {
      return Result.error(404, "Role not found");
    }
    role.setOrgId(AuthContext.getOrgId());
    role.setRoleName(request.getRoleName());
    role.setRoleCode(request.getRoleCode());
    role.setRoleDesc(request.getRoleDesc());
    role.setStatus(request.getStatus());
    roleMapper.updateById(role);
    return Result.ok(role);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Role role = roleMapper.selectById(id);
    if (role == null) {
      return Result.error(404, "Role not found");
    }
    role.setIsDeleted(1);
    roleMapper.updateById(role);
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public Result<Role> get(@PathVariable Long id) {
    return Result.ok(roleMapper.selectById(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public Result<IPage<Role>> list(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String sortBy,
      @RequestParam(defaultValue = "desc") String order) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(Role.class)
        .eq(Role::getIsDeleted, 0)
        .eq(orgId != null, Role::getOrgId, orgId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(Role::getRoleName, keyword)
          .or().like(Role::getRoleCode, keyword));
    }
    if (sortBy != null && !sortBy.isBlank()) {
      boolean asc = "asc".equalsIgnoreCase(order);
      if ("roleName".equals(sortBy)) {
        wrapper.orderBy(true, asc, Role::getRoleName);
      } else if ("roleCode".equals(sortBy)) {
        wrapper.orderBy(true, asc, Role::getRoleCode);
      } else if ("createTime".equals(sortBy)) {
        wrapper.orderBy(true, asc, Role::getCreateTime);
      }
    }
    return Result.ok(roleMapper.selectPage(new Page<>(page, size), wrapper));
  }
}
