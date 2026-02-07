package com.zhiyangyun.care.auth.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffRole;
import com.zhiyangyun.care.auth.mapper.StaffRoleMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.model.StaffRoleAssignRequest;
import com.zhiyangyun.care.auth.model.StaffRoleChangeRequest;
import com.zhiyangyun.care.auth.security.AuthContext;
import jakarta.validation.Valid;
import java.util.List;
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
  private final StaffRoleMapper staffRoleMapper;

  public AdminStaffRoleController(StaffRoleMapper staffRoleMapper) {
    this.staffRoleMapper = staffRoleMapper;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/assign")
  @Transactional
  public Result<Void> assignRoles(@Valid @RequestBody StaffRoleAssignRequest request) {
    request.setOrgId(AuthContext.getOrgId());
    staffRoleMapper.delete(Wrappers.lambdaQuery(StaffRole.class)
        .eq(StaffRole::getOrgId, request.getOrgId())
        .eq(StaffRole::getStaffId, request.getStaffId()));

    for (Long roleId : request.getRoleIds()) {
      StaffRole staffRole = new StaffRole();
      staffRole.setOrgId(request.getOrgId());
      staffRole.setStaffId(request.getStaffId());
      staffRole.setRoleId(roleId);
      staffRoleMapper.insert(staffRole);
    }
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public Result<List<StaffRole>> listByStaff(@RequestParam Long staffId, @RequestParam Long orgId) {
    orgId = AuthContext.getOrgId();
    return Result.ok(staffRoleMapper.selectList(Wrappers.lambdaQuery(StaffRole.class)
        .eq(StaffRole::getOrgId, orgId)
        .eq(StaffRole::getStaffId, staffId)
        .eq(StaffRole::getIsDeleted, 0)));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/add")
  public Result<Void> addRole(@Valid @RequestBody StaffRoleChangeRequest request) {
    request.setOrgId(AuthContext.getOrgId());
    StaffRole staffRole = new StaffRole();
    staffRole.setOrgId(request.getOrgId());
    staffRole.setStaffId(request.getStaffId());
    staffRole.setRoleId(request.getRoleId());
    staffRoleMapper.insert(staffRole);
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/remove")
  public Result<Void> removeRole(@Valid @RequestBody StaffRoleChangeRequest request) {
    request.setOrgId(AuthContext.getOrgId());
    staffRoleMapper.delete(Wrappers.lambdaQuery(StaffRole.class)
        .eq(StaffRole::getOrgId, request.getOrgId())
        .eq(StaffRole::getStaffId, request.getStaffId())
        .eq(StaffRole::getRoleId, request.getRoleId()));
    return Result.ok(null);
  }
}
