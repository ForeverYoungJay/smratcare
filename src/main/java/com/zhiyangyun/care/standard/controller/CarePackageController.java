package com.zhiyangyun.care.standard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.standard.model.CarePackageRequest;
import com.zhiyangyun.care.standard.model.CarePackageResponse;
import com.zhiyangyun.care.standard.service.CarePackageService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/standard/packages")
public class CarePackageController {
  private final CarePackageService carePackageService;
  private final AuditLogService auditLogService;

  public CarePackageController(CarePackageService carePackageService, AuditLogService auditLogService) {
    this.carePackageService = carePackageService;
    this.auditLogService = auditLogService;
  }

  @PostMapping
  public Result<CarePackageResponse> create(@Valid @RequestBody CarePackageRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    CarePackageResponse response = carePackageService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "CARE_PACKAGE", response == null ? null : response.getId(), "创建护理套餐");
    return Result.ok(response);
  }

  @PutMapping("/{id}")
  public Result<CarePackageResponse> update(@PathVariable Long id, @Valid @RequestBody CarePackageRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    CarePackageResponse response = carePackageService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "CARE_PACKAGE", id, "更新护理套餐");
    return Result.ok(response);
  }

  @GetMapping("/{id}")
  public Result<CarePackageResponse> get(@PathVariable Long id) {
    return Result.ok(carePackageService.get(id, AuthContext.getOrgId()));
  }

  @GetMapping("/page")
  public Result<IPage<CarePackageResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer enabled) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(carePackageService.page(tenantId, pageNo, pageSize, keyword, enabled));
  }

  @GetMapping("/list")
  public Result<List<CarePackageResponse>> list(@RequestParam(required = false) Integer enabled) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(carePackageService.list(tenantId, enabled));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    carePackageService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "CARE_PACKAGE", id, "删除护理套餐");
    return Result.ok(null);
  }
}
