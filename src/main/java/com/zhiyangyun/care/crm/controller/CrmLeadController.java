package com.zhiyangyun.care.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.crm.model.CrmLeadRequest;
import com.zhiyangyun.care.crm.model.CrmLeadResponse;
import com.zhiyangyun.care.crm.service.CrmLeadService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/crm/leads")
public class CrmLeadController {
  private final CrmLeadService leadService;
  private final AuditLogService auditLogService;

  public CrmLeadController(CrmLeadService leadService, AuditLogService auditLogService) {
    this.leadService = leadService;
    this.auditLogService = auditLogService;
  }

  @PostMapping
  public Result<CrmLeadResponse> create(@Valid @RequestBody CrmLeadRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    CrmLeadResponse response = leadService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "CRM_LEAD", response == null ? null : response.getId(), "创建CRM线索");
    return Result.ok(response);
  }

  @PutMapping("/{id}")
  public Result<CrmLeadResponse> update(@PathVariable Long id, @Valid @RequestBody CrmLeadRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    CrmLeadResponse response = leadService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "CRM_LEAD", id, "更新CRM线索");
    return Result.ok(response);
  }

  @GetMapping("/{id}")
  public Result<CrmLeadResponse> get(@PathVariable Long id) {
    return Result.ok(leadService.get(id, AuthContext.getOrgId()));
  }

  @GetMapping("/page")
  public Result<IPage<CrmLeadResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(leadService.page(tenantId, pageNo, pageSize, keyword, status));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    leadService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "CRM_LEAD", id, "删除CRM线索");
    return Result.ok(null);
  }
}
