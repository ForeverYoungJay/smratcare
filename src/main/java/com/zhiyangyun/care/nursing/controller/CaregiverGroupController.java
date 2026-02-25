package com.zhiyangyun.care.nursing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.nursing.model.CaregiverGroupRequest;
import com.zhiyangyun.care.nursing.model.CaregiverGroupResponse;
import com.zhiyangyun.care.nursing.service.CaregiverGroupService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/nursing/groups")
public class CaregiverGroupController {
  private final CaregiverGroupService caregiverGroupService;
  private final AuditLogService auditLogService;

  public CaregiverGroupController(CaregiverGroupService caregiverGroupService, AuditLogService auditLogService) {
    this.caregiverGroupService = caregiverGroupService;
    this.auditLogService = auditLogService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<CaregiverGroupResponse> create(@Valid @RequestBody CaregiverGroupRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    CaregiverGroupResponse response = caregiverGroupService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "CAREGIVER_GROUP", response == null ? null : response.getId(), "创建护工小组");
    return Result.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<CaregiverGroupResponse> update(@PathVariable Long id, @Valid @RequestBody CaregiverGroupRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    CaregiverGroupResponse response = caregiverGroupService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "CAREGIVER_GROUP", id, "更新护工小组");
    return Result.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public Result<CaregiverGroupResponse> get(@PathVariable Long id) {
    return Result.ok(caregiverGroupService.get(id, AuthContext.getOrgId()));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/page")
  public Result<IPage<CaregiverGroupResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer enabled) {
    return Result.ok(caregiverGroupService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword, enabled));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/list")
  public Result<List<CaregiverGroupResponse>> list(@RequestParam(required = false) Integer enabled) {
    return Result.ok(caregiverGroupService.list(AuthContext.getOrgId(), enabled));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    caregiverGroupService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "CAREGIVER_GROUP", id, "删除护工小组");
    return Result.ok(null);
  }
}
