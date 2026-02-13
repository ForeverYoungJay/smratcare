package com.zhiyangyun.care.standard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.standard.model.ElderPackageRequest;
import com.zhiyangyun.care.standard.model.ElderPackageResponse;
import com.zhiyangyun.care.standard.service.ElderPackageService;
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
@RequestMapping("/api/standard/elder-packages")
public class ElderPackageController {
  private final ElderPackageService elderPackageService;
  private final AuditLogService auditLogService;

  public ElderPackageController(ElderPackageService elderPackageService, AuditLogService auditLogService) {
    this.elderPackageService = elderPackageService;
    this.auditLogService = auditLogService;
  }

  @PostMapping
  public Result<ElderPackageResponse> create(@Valid @RequestBody ElderPackageRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    ElderPackageResponse response = elderPackageService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "ELDER_PACKAGE", response == null ? null : response.getId(), "创建老人套餐");
    return Result.ok(response);
  }

  @PutMapping("/{id}")
  public Result<ElderPackageResponse> update(@PathVariable Long id, @Valid @RequestBody ElderPackageRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    ElderPackageResponse response = elderPackageService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "ELDER_PACKAGE", id, "更新老人套餐");
    return Result.ok(response);
  }

  @GetMapping("/page")
  public Result<IPage<ElderPackageResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) Integer status) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(elderPackageService.page(tenantId, pageNo, pageSize, elderId, status));
  }

  @GetMapping("/list")
  public Result<List<ElderPackageResponse>> list(@RequestParam(required = false) Long elderId) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(elderPackageService.list(tenantId, elderId));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    elderPackageService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "ELDER_PACKAGE", id, "删除老人套餐");
    return Result.ok(null);
  }
}
