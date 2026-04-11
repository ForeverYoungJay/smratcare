package com.zhiyangyun.care.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.Department;
import com.zhiyangyun.care.auth.entity.Role;
import com.zhiyangyun.care.auth.mapper.DepartmentMapper;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.auth.security.PagePermissionPathHelper;
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
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Locale;

@RestController
@RequestMapping("/api/admin/roles")
public class AdminRoleController {
  private final RoleMapper roleMapper;
  private final DepartmentMapper departmentMapper;
  private final ObjectMapper objectMapper;

  public AdminRoleController(RoleMapper roleMapper, DepartmentMapper departmentMapper, ObjectMapper objectMapper) {
    this.roleMapper = roleMapper;
    this.departmentMapper = departmentMapper;
    this.objectMapper = objectMapper;
  }

  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN','HR_MINISTER')")
  @PostMapping
  public Result<Role> create(@Valid @RequestBody RoleRequest request) {
    if (!StringUtils.hasText(request.getRoleName())) {
      return Result.error(400, "Role name is required");
    }
    Long orgId = AuthContext.getOrgId();
    Department department = requireDepartmentInOrg(request.getDepartmentId(), orgId, "所属部门");
    Role superiorRole = requireRoleInOrg(request.getSuperiorRoleId(), orgId, null, "上级角色");
    Role role = new Role();
    role.setOrgId(orgId);
    role.setDepartmentId(department == null ? null : department.getId());
    role.setSuperiorRoleId(superiorRole == null ? null : superiorRole.getId());
    role.setRoleName(request.getRoleName());
    role.setRoleCode(resolveRoleCode(request.getRoleCode(), request.getRoleName(), request.getDepartmentId()));
    role.setRoleDesc(null);
    role.setRoutePermissionsJson(normalizeRoutePermissionsJson(request.getRoutePermissionsJson()));
    role.setStatus(request.getStatus());
    roleMapper.insert(role);
    return Result.ok(role);
  }

  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN','HR_MINISTER')")
  @PutMapping("/{id}")
  public Result<Role> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
    if (!StringUtils.hasText(request.getRoleName())) {
      return Result.error(400, "Role name is required");
    }
    Role role = requireRole(id);
    Department department = requireDepartmentInOrg(request.getDepartmentId(), role.getOrgId(), "所属部门");
    Role superiorRole = requireRoleInOrg(request.getSuperiorRoleId(), role.getOrgId(), id, "上级角色");
    role.setDepartmentId(department == null ? null : department.getId());
    role.setSuperiorRoleId(superiorRole == null ? null : superiorRole.getId());
    role.setRoleName(request.getRoleName());
    role.setRoleCode(resolveRoleCodeForUpdate(role, request));
    role.setRoleDesc(null);
    role.setRoutePermissionsJson(normalizeRoutePermissionsJson(request.getRoutePermissionsJson()));
    role.setStatus(request.getStatus());
    roleMapper.updateById(role);
    return Result.ok(role);
  }

  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN','HR_MINISTER')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Role role = requireRole(id);
    role.setIsDeleted(1);
    roleMapper.updateById(role);
    return Result.ok(null);
  }

  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN','HR_MINISTER')")
  @GetMapping("/{id}")
  public Result<Role> get(@PathVariable Long id) {
    return Result.ok(requireRole(id));
  }

  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN','HR_MINISTER')")
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

  private String resolveRoleCode(String requested, String roleName, Long departmentId) {
    if (StringUtils.hasText(requested)) {
      return requested.trim().toUpperCase(Locale.ROOT);
    }
    String inferred = inferRoleCode(roleName, departmentId);
    if (StringUtils.hasText(inferred)) {
      return inferred;
    }
    String normalized = Normalizer.normalize(roleName == null ? "" : roleName, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .replaceAll("[^A-Za-z0-9]+", "_")
        .replaceAll("^_+|_+$", "")
        .toUpperCase(Locale.ROOT);
    if (normalized.isBlank()) {
      normalized = "ROLE_" + System.currentTimeMillis();
    }
    if (!normalized.startsWith("ROLE_")) {
      normalized = "ROLE_" + normalized;
    }
    return normalized;
  }

  private String resolveRoleCodeForUpdate(Role role, RoleRequest request) {
    if (StringUtils.hasText(request.getRoleCode())) {
      return resolveRoleCode(request.getRoleCode(), request.getRoleName(), request.getDepartmentId());
    }
    if (StringUtils.hasText(role.getRoleCode())) {
      return role.getRoleCode().trim().toUpperCase(Locale.ROOT);
    }
    return resolveRoleCode(null, request.getRoleName(), request.getDepartmentId());
  }

  private String inferRoleCode(String roleName, Long departmentId) {
    String name = roleName == null ? "" : roleName.trim();
    if (name.contains("系统管理员")) return "SYS_ADMIN";
    if (name.contains("院长")) return "DIRECTOR";
    if (name.equals("管理员") || name.contains("超级管理员")) return "ADMIN";

    Department department = departmentId == null ? null : departmentMapper.selectById(departmentId);
    String deptName = department == null ? "" : String.valueOf(department.getDeptName());
    String deptPrefix = inferDepartmentPrefix(deptName, name);
    if (!StringUtils.hasText(deptPrefix)) {
      return null;
    }
    if ("GUARD".equals(deptPrefix)) {
      return "GUARD";
    }
    String suffix = inferRoleLevelSuffix(name, deptPrefix);
    return suffix == null ? null : deptPrefix + "_" + suffix;
  }

  private String inferDepartmentPrefix(String deptName, String roleName) {
    String text = (deptName + " " + roleName).trim();
    if (text.contains("行政人事") || text.contains("人事")) return "HR";
    if (text.contains("护理")) return "NURSING";
    if (text.contains("医务") || text.contains("医疗")) return "MEDICAL";
    if (text.contains("财务")) return "FINANCE";
    if (text.contains("后勤")) return "LOGISTICS";
    if (text.contains("营销")) return "MARKETING";
    if (text.contains("消防")) return "GUARD";
    return null;
  }

  private String inferRoleLevelSuffix(String roleName, String deptPrefix) {
    String text = roleName == null ? "" : roleName.trim();
    if (text.contains("部长") || text.contains("主管") || text.contains("主任")) {
      return "MINISTER";
    }
    return "EMPLOYEE";
  }

  private Role requireRole(Long id) {
    Role role = roleMapper.selectById(id);
    if (role == null || role.getIsDeleted() != null && role.getIsDeleted() == 1) {
      throw new IllegalArgumentException("Role not found");
    }
    AuthContext.requireOrgAccess(role.getOrgId());
    return role;
  }

  private Department requireDepartmentInOrg(Long departmentId, Long orgId, String label) {
    if (departmentId == null) {
      return null;
    }
    Department department = departmentMapper.selectById(departmentId);
    if (department == null || department.getIsDeleted() != null && department.getIsDeleted() == 1) {
      throw new IllegalArgumentException(label + "不存在");
    }
    if (orgId == null || !orgId.equals(department.getOrgId())) {
      throw new IllegalArgumentException(label + "不属于当前机构");
    }
    return department;
  }

  private Role requireRoleInOrg(Long roleId, Long orgId, Long currentRoleId, String label) {
    if (roleId == null) {
      return null;
    }
    if (currentRoleId != null && currentRoleId.equals(roleId)) {
      throw new IllegalArgumentException(label + "不能设置为自身");
    }
    Role role = roleMapper.selectById(roleId);
    if (role == null || role.getIsDeleted() != null && role.getIsDeleted() == 1) {
      throw new IllegalArgumentException(label + "不存在");
    }
    if (orgId == null || !orgId.equals(role.getOrgId())) {
      throw new IllegalArgumentException(label + "不属于当前机构");
    }
    return role;
  }

  private String normalizeRoutePermissionsJson(String value) {
    return PagePermissionPathHelper.normalizeJson(objectMapper, value);
  }
}
