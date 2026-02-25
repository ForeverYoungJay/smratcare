package com.zhiyangyun.care.nursing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.nursing.model.ShiftTemplateRequest;
import com.zhiyangyun.care.nursing.model.ShiftTemplateResponse;
import com.zhiyangyun.care.nursing.service.ShiftTemplateService;
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
@RequestMapping("/api/nursing/shift-templates")
public class ShiftTemplateController {
  private final ShiftTemplateService shiftTemplateService;
  private final AuditLogService auditLogService;

  public ShiftTemplateController(ShiftTemplateService shiftTemplateService, AuditLogService auditLogService) {
    this.shiftTemplateService = shiftTemplateService;
    this.auditLogService = auditLogService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<ShiftTemplateResponse> create(@Valid @RequestBody ShiftTemplateRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    ShiftTemplateResponse response = shiftTemplateService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "SHIFT_TEMPLATE", response == null ? null : response.getId(), "创建排班方案");
    return Result.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<ShiftTemplateResponse> update(@PathVariable Long id, @Valid @RequestBody ShiftTemplateRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    ShiftTemplateResponse response = shiftTemplateService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "SHIFT_TEMPLATE", id, "更新排班方案");
    return Result.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public Result<ShiftTemplateResponse> get(@PathVariable Long id) {
    return Result.ok(shiftTemplateService.get(id, AuthContext.getOrgId()));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/page")
  public Result<IPage<ShiftTemplateResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer enabled) {
    return Result.ok(shiftTemplateService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword, enabled));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/list")
  public Result<List<ShiftTemplateResponse>> list(@RequestParam(required = false) Integer enabled) {
    return Result.ok(shiftTemplateService.list(AuthContext.getOrgId(), enabled));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    shiftTemplateService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "SHIFT_TEMPLATE", id, "删除排班方案");
    return Result.ok(null);
  }
}
