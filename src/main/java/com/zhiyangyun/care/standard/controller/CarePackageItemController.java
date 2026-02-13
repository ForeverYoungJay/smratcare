package com.zhiyangyun.care.standard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.standard.model.CarePackageItemRequest;
import com.zhiyangyun.care.standard.model.CarePackageItemResponse;
import com.zhiyangyun.care.standard.service.CarePackageItemService;
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
@RequestMapping("/api/standard/package-items")
public class CarePackageItemController {
  private final CarePackageItemService packageItemService;
  private final AuditLogService auditLogService;

  public CarePackageItemController(CarePackageItemService packageItemService, AuditLogService auditLogService) {
    this.packageItemService = packageItemService;
    this.auditLogService = auditLogService;
  }

  @PostMapping
  public Result<CarePackageItemResponse> create(@Valid @RequestBody CarePackageItemRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    CarePackageItemResponse response = packageItemService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "CARE_PACKAGE_ITEM", response == null ? null : response.getId(), "创建护理套餐明细");
    return Result.ok(response);
  }

  @PutMapping("/{id}")
  public Result<CarePackageItemResponse> update(@PathVariable Long id, @Valid @RequestBody CarePackageItemRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    CarePackageItemResponse response = packageItemService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "CARE_PACKAGE_ITEM", id, "更新护理套餐明细");
    return Result.ok(response);
  }

  @GetMapping("/page")
  public Result<IPage<CarePackageItemResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long packageId) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(packageItemService.page(tenantId, pageNo, pageSize, packageId));
  }

  @GetMapping("/list")
  public Result<List<CarePackageItemResponse>> list(@RequestParam(required = false) Long packageId) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(packageItemService.list(tenantId, packageId));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    packageItemService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "CARE_PACKAGE_ITEM", id, "删除护理套餐明细");
    return Result.ok(null);
  }
}
