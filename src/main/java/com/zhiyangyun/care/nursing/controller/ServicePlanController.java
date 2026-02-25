package com.zhiyangyun.care.nursing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.nursing.model.ServicePlanRequest;
import com.zhiyangyun.care.nursing.model.ServicePlanResponse;
import com.zhiyangyun.care.nursing.service.ServicePlanService;
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
@RequestMapping("/api/nursing/service-plans")
public class ServicePlanController {
  private final ServicePlanService servicePlanService;
  private final AuditLogService auditLogService;

  public ServicePlanController(ServicePlanService servicePlanService, AuditLogService auditLogService) {
    this.servicePlanService = servicePlanService;
    this.auditLogService = auditLogService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<ServicePlanResponse> create(@Valid @RequestBody ServicePlanRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    ServicePlanResponse response = servicePlanService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "SERVICE_PLAN", response == null ? null : response.getId(), "创建服务计划");
    return Result.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<ServicePlanResponse> update(@PathVariable Long id, @Valid @RequestBody ServicePlanRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    ServicePlanResponse response = servicePlanService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "SERVICE_PLAN", id, "更新服务计划");
    return Result.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public Result<ServicePlanResponse> get(@PathVariable Long id) {
    return Result.ok(servicePlanService.get(id, AuthContext.getOrgId()));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/page")
  public Result<IPage<ServicePlanResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    return Result.ok(servicePlanService.page(AuthContext.getOrgId(), pageNo, pageSize, elderId, status, keyword));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/list")
  public Result<List<ServicePlanResponse>> list(@RequestParam(required = false) String status) {
    return Result.ok(servicePlanService.list(AuthContext.getOrgId(), status));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    servicePlanService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "SERVICE_PLAN", id, "删除服务计划");
    return Result.ok(null);
  }
}
