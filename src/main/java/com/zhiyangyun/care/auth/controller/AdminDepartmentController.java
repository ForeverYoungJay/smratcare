package com.zhiyangyun.care.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.Department;
import com.zhiyangyun.care.auth.mapper.DepartmentMapper;
import com.zhiyangyun.care.auth.model.DepartmentOptionResponse;
import com.zhiyangyun.care.auth.model.DepartmentRequest;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
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
@RequestMapping("/api/admin/departments")
public class AdminDepartmentController {
  private final DepartmentMapper departmentMapper;

  public AdminDepartmentController(DepartmentMapper departmentMapper) {
    this.departmentMapper = departmentMapper;
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping
  public Result<Department> create(@Valid @RequestBody DepartmentRequest request) {
    Department department = new Department();
    department.setOrgId(AuthContext.getOrgId());
    department.setParentId(null);
    department.setDeptName(request.getDeptName());
    department.setDeptCode(null);
    department.setSortNo(request.getSortNo());
    department.setStatus(request.getStatus());
    departmentMapper.insert(department);
    return Result.ok(department);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PutMapping("/{id}")
  public Result<Department> update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
    Department department = requireDepartment(id);
    department.setParentId(null);
    department.setDeptName(request.getDeptName());
    department.setDeptCode(null);
    department.setSortNo(request.getSortNo());
    department.setStatus(request.getStatus());
    departmentMapper.updateById(department);
    return Result.ok(department);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Department department = requireDepartment(id);
    department.setIsDeleted(1);
    departmentMapper.updateById(department);
    return Result.ok(null);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/{id}")
  public Result<Department> get(@PathVariable Long id) {
    return Result.ok(requireDepartment(id));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping
  public Result<IPage<Department>> list(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String sortBy,
      @RequestParam(defaultValue = "desc") String order) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(Department.class)
        .eq(Department::getIsDeleted, 0)
        .eq(orgId != null, Department::getOrgId, orgId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.like(Department::getDeptName, keyword);
    }
    if (sortBy != null && !sortBy.isBlank()) {
      boolean asc = "asc".equalsIgnoreCase(order);
      if ("deptName".equals(sortBy)) {
        wrapper.orderBy(true, asc, Department::getDeptName);
      } else if ("createTime".equals(sortBy)) {
        wrapper.orderBy(true, asc, Department::getCreateTime);
      }
    }
    return Result.ok(departmentMapper.selectPage(new Page<>(page, size), wrapper));
  }

  @PreAuthorize("isAuthenticated() and !hasRole('FAMILY')")
  @GetMapping("/options")
  public Result<IPage<DepartmentOptionResponse>> listOptions(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "200") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(Department.class)
        .eq(Department::getIsDeleted, 0)
        .eq(orgId != null, Department::getOrgId, orgId)
        .eq(activeOnly, Department::getStatus, 1);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.like(Department::getDeptName, keyword);
    }
    wrapper.orderByAsc(Department::getSortNo).orderByAsc(Department::getId);
    IPage<Department> sourcePage = departmentMapper.selectPage(new Page<>(page, size), wrapper);
    Page<DepartmentOptionResponse> responsePage = new Page<>(sourcePage.getCurrent(), sourcePage.getSize(), sourcePage.getTotal());
    responsePage.setRecords(sourcePage.getRecords().stream().map(this::toDepartmentOptionResponse).toList());
    return Result.ok(responsePage);
  }

  private DepartmentOptionResponse toDepartmentOptionResponse(Department department) {
    DepartmentOptionResponse row = new DepartmentOptionResponse();
    row.setId(department.getId());
    row.setDeptName(department.getDeptName());
    row.setSortNo(department.getSortNo());
    row.setOrgId(department.getOrgId());
    row.setStatus(department.getStatus());
    return row;
  }

  private Department requireDepartment(Long id) {
    Department department = departmentMapper.selectById(id);
    if (department == null || department.getIsDeleted() != null && department.getIsDeleted() == 1) {
      throw new IllegalArgumentException("Department not found");
    }
    AuthContext.requireOrgAccess(department.getOrgId());
    return department;
  }
}
