package com.zhiyangyun.care.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.Department;
import com.zhiyangyun.care.auth.mapper.DepartmentMapper;
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

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<Department> create(@Valid @RequestBody DepartmentRequest request) {
    Department department = new Department();
    department.setOrgId(AuthContext.getOrgId());
    department.setParentId(request.getParentId());
    department.setDeptName(request.getDeptName());
    department.setDeptCode(request.getDeptCode());
    department.setSortNo(request.getSortNo());
    department.setStatus(request.getStatus());
    departmentMapper.insert(department);
    return Result.ok(department);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<Department> update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
    Department department = departmentMapper.selectById(id);
    if (department == null) {
      return Result.error(404, "Department not found");
    }
    department.setOrgId(AuthContext.getOrgId());
    department.setParentId(request.getParentId());
    department.setDeptName(request.getDeptName());
    department.setDeptCode(request.getDeptCode());
    department.setSortNo(request.getSortNo());
    department.setStatus(request.getStatus());
    departmentMapper.updateById(department);
    return Result.ok(department);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Department department = departmentMapper.selectById(id);
    if (department == null) {
      return Result.error(404, "Department not found");
    }
    department.setIsDeleted(1);
    departmentMapper.updateById(department);
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public Result<Department> get(@PathVariable Long id) {
    return Result.ok(departmentMapper.selectById(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
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
      wrapper.and(w -> w.like(Department::getDeptName, keyword)
          .or().like(Department::getDeptCode, keyword));
    }
    if (sortBy != null && !sortBy.isBlank()) {
      boolean asc = "asc".equalsIgnoreCase(order);
      if ("deptName".equals(sortBy)) {
        wrapper.orderBy(true, asc, Department::getDeptName);
      } else if ("deptCode".equals(sortBy)) {
        wrapper.orderBy(true, asc, Department::getDeptCode);
      } else if ("createTime".equals(sortBy)) {
        wrapper.orderBy(true, asc, Department::getCreateTime);
      }
    }
    return Result.ok(departmentMapper.selectPage(new Page<>(page, size), wrapper));
  }
}
