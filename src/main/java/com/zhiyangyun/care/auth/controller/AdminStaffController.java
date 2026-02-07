package com.zhiyangyun.care.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.auth.model.StaffCreateRequest;
import com.zhiyangyun.care.auth.model.StaffUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@RequestMapping("/api/admin/staff")
public class AdminStaffController {
  private final StaffMapper staffMapper;
  private final PasswordEncoder passwordEncoder;

  public AdminStaffController(StaffMapper staffMapper, PasswordEncoder passwordEncoder) {
    this.staffMapper = staffMapper;
    this.passwordEncoder = passwordEncoder;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<StaffAccount> create(@Valid @RequestBody StaffCreateRequest request) {
    StaffAccount staff = new StaffAccount();
    staff.setOrgId(AuthContext.getOrgId());
    staff.setDepartmentId(request.getDepartmentId());
    staff.setStaffNo(request.getStaffNo());
    staff.setUsername(request.getUsername());
    staff.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    staff.setRealName(request.getRealName());
    staff.setPhone(request.getPhone());
    staff.setEmail(request.getEmail());
    staff.setGender(request.getGender());
    staff.setStatus(request.getStatus());
    staffMapper.insert(staff);
    return Result.ok(staff);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping
  public Result<StaffAccount> update(@Valid @RequestBody StaffUpdateRequest request) {
    StaffAccount staff = staffMapper.selectById(request.getId());
    if (staff == null) {
      return Result.error(404, "Staff not found");
    }
    if (request.getDepartmentId() != null) {
      staff.setDepartmentId(request.getDepartmentId());
    }
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      staff.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    }
    if (request.getRealName() != null) {
      staff.setRealName(request.getRealName());
    }
    if (request.getPhone() != null) {
      staff.setPhone(request.getPhone());
    }
    if (request.getEmail() != null) {
      staff.setEmail(request.getEmail());
    }
    if (request.getGender() != null) {
      staff.setGender(request.getGender());
    }
    if (request.getStatus() != null) {
      staff.setStatus(request.getStatus());
    }
    staffMapper.updateById(staff);
    return Result.ok(staff);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    StaffAccount staff = staffMapper.selectById(id);
    if (staff == null) {
      return Result.error(404, "Staff not found");
    }
    staff.setIsDeleted(1);
    staffMapper.updateById(staff);
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public Result<StaffAccount> get(@PathVariable Long id) {
    return Result.ok(staffMapper.selectById(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public Result<IPage<StaffAccount>> list(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String sortBy,
      @RequestParam(defaultValue = "desc") String order) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(StaffAccount::getUsername, keyword)
          .or().like(StaffAccount::getRealName, keyword)
          .or().like(StaffAccount::getPhone, keyword));
    }
    if (sortBy != null && !sortBy.isBlank()) {
      boolean asc = "asc".equalsIgnoreCase(order);
      if ("username".equals(sortBy)) {
        wrapper.orderBy(true, asc, StaffAccount::getUsername);
      } else if ("realName".equals(sortBy)) {
        wrapper.orderBy(true, asc, StaffAccount::getRealName);
      } else if ("createTime".equals(sortBy)) {
        wrapper.orderBy(true, asc, StaffAccount::getCreateTime);
      }
    }
    return Result.ok(staffMapper.selectPage(new Page<>(page, size), wrapper));
  }
}
