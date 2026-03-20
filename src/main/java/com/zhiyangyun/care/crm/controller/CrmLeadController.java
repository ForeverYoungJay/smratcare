package com.zhiyangyun.care.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.crm.model.CrmLeadRequest;
import com.zhiyangyun.care.crm.model.CrmLeadResponse;
import com.zhiyangyun.care.crm.service.CrmLeadService;
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
@RequestMapping("/api/crm/leads")
public class CrmLeadController {
  private static final String CRM_LEAD_READ =
      "hasAnyRole('STAFF','HR_EMPLOYEE','HR_MINISTER','MEDICAL_EMPLOYEE','MEDICAL_MINISTER',"
          + "'NURSING_EMPLOYEE','NURSING_MINISTER','FINANCE_EMPLOYEE','FINANCE_MINISTER',"
          + "'LOGISTICS_EMPLOYEE','LOGISTICS_MINISTER','MARKETING_EMPLOYEE','MARKETING_MINISTER',"
          + "'DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String CRM_LEAD_WRITE =
      "hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private final CrmLeadService leadService;
  private final AuditLogService auditLogService;

  public CrmLeadController(CrmLeadService leadService, AuditLogService auditLogService) {
    this.leadService = leadService;
    this.auditLogService = auditLogService;
  }

  @PreAuthorize(CRM_LEAD_WRITE)
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

  @PreAuthorize(CRM_LEAD_WRITE)
  @PutMapping("/{id}")
  public Result<CrmLeadResponse> update(@PathVariable Long id, @Valid @RequestBody CrmLeadRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    Long currentStaffId = AuthContext.getStaffId();
    boolean adminView = AuthContext.isMinisterOrHigher();
    CrmLeadResponse response = leadService.update(id, request, currentStaffId, adminView);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "CRM_LEAD", id, "更新CRM线索");
    return Result.ok(response);
  }

  @PreAuthorize(CRM_LEAD_READ)
  @GetMapping("/{id}")
  public Result<CrmLeadResponse> get(@PathVariable Long id) {
    Long currentStaffId = AuthContext.getStaffId();
    boolean adminView = AuthContext.isMinisterOrHigher();
    return Result.ok(leadService.get(id, AuthContext.getOrgId(), currentStaffId, adminView));
  }

  @PreAuthorize(CRM_LEAD_READ)
  @GetMapping("/page")
  public Result<IPage<CrmLeadResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String source,
      @RequestParam(required = false) String customerTag,
      @RequestParam(required = false) String consultantName,
      @RequestParam(required = false) String consultantPhone,
      @RequestParam(required = false) String elderName,
      @RequestParam(required = false) String elderPhone,
      @RequestParam(required = false) String consultDateFrom,
      @RequestParam(required = false) String consultDateTo,
      @RequestParam(required = false) String consultType,
      @RequestParam(required = false) String mediaChannel,
      @RequestParam(required = false) String infoSource,
      @RequestParam(required = false) String marketerName,
      @RequestParam(required = false) String followupStatus,
      @RequestParam(required = false) String reservationChannel,
      @RequestParam(required = false) String contractNo,
      @RequestParam(required = false) String contractStatus,
      @RequestParam(required = false) String flowStage,
      @RequestParam(required = false) String currentOwnerDept,
      @RequestParam(required = false) String callbackType,
      @RequestParam(required = false) String followupDateFrom,
      @RequestParam(required = false) String followupDateTo,
      @RequestParam(required = false) Boolean followupDueOnly) {
    Long tenantId = AuthContext.getOrgId();
    Long currentStaffId = AuthContext.getStaffId();
    boolean adminView = AuthContext.isMinisterOrHigher();
    return Result.ok(leadService.page(
        tenantId, currentStaffId, adminView, pageNo, pageSize, keyword, status, source, customerTag,
        consultantName, consultantPhone, elderName, elderPhone,
        consultDateFrom, consultDateTo, consultType, mediaChannel,
        infoSource, marketerName, followupStatus, reservationChannel,
        contractNo, contractStatus, flowStage, currentOwnerDept, callbackType,
        followupDateFrom, followupDateTo, followupDueOnly));
  }

  @PreAuthorize(CRM_LEAD_WRITE)
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    Long currentStaffId = AuthContext.getStaffId();
    boolean adminView = AuthContext.isMinisterOrHigher();
    leadService.delete(id, tenantId, currentStaffId, adminView);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "CRM_LEAD", id, "删除CRM线索");
    return Result.ok(null);
  }
}
