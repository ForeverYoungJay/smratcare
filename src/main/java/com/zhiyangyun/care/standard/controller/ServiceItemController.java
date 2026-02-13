package com.zhiyangyun.care.standard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.standard.model.ServiceItemRequest;
import com.zhiyangyun.care.standard.model.ServiceItemResponse;
import com.zhiyangyun.care.standard.service.ServiceItemService;
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
@RequestMapping("/api/standard/items")
public class ServiceItemController {
  private final ServiceItemService serviceItemService;
  private final AuditLogService auditLogService;

  public ServiceItemController(ServiceItemService serviceItemService, AuditLogService auditLogService) {
    this.serviceItemService = serviceItemService;
    this.auditLogService = auditLogService;
  }

  @PostMapping
  public Result<ServiceItemResponse> create(@Valid @RequestBody ServiceItemRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    ServiceItemResponse response = serviceItemService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "SERVICE_ITEM", response == null ? null : response.getId(), "创建服务项目");
    return Result.ok(response);
  }

  @PutMapping("/{id}")
  public Result<ServiceItemResponse> update(@PathVariable Long id, @Valid @RequestBody ServiceItemRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    ServiceItemResponse response = serviceItemService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "SERVICE_ITEM", id, "更新服务项目");
    return Result.ok(response);
  }

  @GetMapping("/{id}")
  public Result<ServiceItemResponse> get(@PathVariable Long id) {
    return Result.ok(serviceItemService.get(id, AuthContext.getOrgId()));
  }

  @GetMapping("/page")
  public Result<IPage<ServiceItemResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer enabled) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(serviceItemService.page(tenantId, pageNo, pageSize, keyword, enabled));
  }

  @GetMapping("/list")
  public Result<List<ServiceItemResponse>> list(@RequestParam(required = false) Integer enabled) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(serviceItemService.list(tenantId, enabled));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    serviceItemService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "SERVICE_ITEM", id, "删除服务项目");
    return Result.ok(null);
  }
}
