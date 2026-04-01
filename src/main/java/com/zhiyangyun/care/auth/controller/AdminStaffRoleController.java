package com.zhiyangyun.care.auth.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.Role;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.entity.StaffRole;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.mapper.StaffRoleMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.model.StaffRoleAssignRequest;
import com.zhiyangyun.care.auth.model.StaffRoleChangeRequest;
import com.zhiyangyun.care.auth.security.AuthContext;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/staff-roles")
public class AdminStaffRoleController {
  private final StaffMapper staffMapper;
  private final RoleMapper roleMapper;
  private final StaffRoleMapper staffRoleMapper;

  public AdminStaffRoleController(
      StaffMapper staffMapper,
      RoleMapper roleMapper,
      StaffRoleMapper staffRoleMapper) {
    this.staffMapper = staffMapper;
    this.roleMapper = roleMapper;
    this.staffRoleMapper = staffRoleMapper;
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/assign")
  @Transactional
  public Result<Void> assignRoles(@Valid @RequestBody StaffRoleAssignRequest request) {
    StaffAccount staff = requireStaff(request.getStaffId());
    Long targetOrgId = staff.getOrgId();
    List<Long> roleIds = request.getRoleIds().stream()
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    validateRolesBelongToOrg(roleIds, targetOrgId);
    request.setOrgId(targetOrgId);
    staffRoleMapper.delete(Wrappers.lambdaQuery(StaffRole.class)
        .eq(StaffRole::getOrgId, request.getOrgId())
        .eq(StaffRole::getStaffId, request.getStaffId()));

    for (Long roleId : roleIds) {
      StaffRole staffRole = new StaffRole();
      staffRole.setOrgId(request.getOrgId());
      staffRole.setStaffId(request.getStaffId());
      staffRole.setRoleId(roleId);
      staffRoleMapper.insert(staffRole);
    }
    return Result.ok(null);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping
  public Result<List<StaffRole>> listByStaff(@RequestParam Long staffId, @RequestParam Long orgId) {
    StaffAccount staff = requireStaff(staffId);
    Long targetOrgId = staff.getOrgId();
    return Result.ok(staffRoleMapper.selectList(Wrappers.lambdaQuery(StaffRole.class)
        .eq(StaffRole::getOrgId, targetOrgId)
        .eq(StaffRole::getStaffId, staffId)
        .eq(StaffRole::getIsDeleted, 0)));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/add")
  public Result<Void> addRole(@Valid @RequestBody StaffRoleChangeRequest request) {
    StaffAccount staff = requireStaff(request.getStaffId());
    Role role = requireRole(request.getRoleId());
    if (!staff.getOrgId().equals(role.getOrgId())) {
      throw new IllegalArgumentException("角色不属于当前机构");
    }
    request.setOrgId(staff.getOrgId());
    StaffRole staffRole = new StaffRole();
    staffRole.setOrgId(request.getOrgId());
    staffRole.setStaffId(request.getStaffId());
    staffRole.setRoleId(request.getRoleId());
    staffRoleMapper.insert(staffRole);
    return Result.ok(null);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @DeleteMapping("/remove")
  public Result<Void> removeRole(@Valid @RequestBody StaffRoleChangeRequest request) {
    StaffAccount staff = requireStaff(request.getStaffId());
    Role role = requireRole(request.getRoleId());
    if (!staff.getOrgId().equals(role.getOrgId())) {
      throw new IllegalArgumentException("角色不属于当前机构");
    }
    request.setOrgId(staff.getOrgId());
    staffRoleMapper.delete(Wrappers.lambdaQuery(StaffRole.class)
        .eq(StaffRole::getOrgId, request.getOrgId())
        .eq(StaffRole::getStaffId, request.getStaffId())
        .eq(StaffRole::getRoleId, request.getRoleId()));
    return Result.ok(null);
  }

  private StaffAccount requireStaff(Long staffId) {
    StaffAccount staff = staffMapper.selectById(staffId);
    if (staff == null || staff.getIsDeleted() != null && staff.getIsDeleted() == 1) {
      throw new IllegalArgumentException("员工不存在");
    }
    AuthContext.requireOrgAccess(staff.getOrgId());
    return staff;
  }

  private Role requireRole(Long roleId) {
    Role role = roleMapper.selectById(roleId);
    if (role == null || role.getIsDeleted() != null && role.getIsDeleted() == 1) {
      throw new IllegalArgumentException("角色不存在");
    }
    AuthContext.requireOrgAccess(role.getOrgId());
    return role;
  }

  private void validateRolesBelongToOrg(List<Long> roleIds, Long orgId) {
    for (Long roleId : roleIds) {
      Role role = requireRole(roleId);
      if (!orgId.equals(role.getOrgId())) {
        throw new IllegalArgumentException("角色不属于当前机构");
      }
    }
  }
}
