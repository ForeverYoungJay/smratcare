package com.zhiyangyun.care.elder.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.elder.model.AssignBedRequest;
import com.zhiyangyun.care.elder.model.ElderCreateRequest;
import com.zhiyangyun.care.elder.model.ElderResponse;
import com.zhiyangyun.care.elder.model.ElderUpdateRequest;
import com.zhiyangyun.care.elder.service.ElderService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/elder")
public class ElderController {
  private final ElderService elderService;
  private final AuditLogService auditLogService;

  public ElderController(ElderService elderService, AuditLogService auditLogService) {
    this.elderService = elderService;
    this.auditLogService = auditLogService;
  }

  @PostMapping
  public Result<ElderResponse> create(@Valid @RequestBody ElderCreateRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    ElderResponse response = elderService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "ELDER", response == null ? null : response.getId(), "新增老人");
    return Result.ok(response);
  }

  @PutMapping("/{id}")
  public Result<ElderResponse> update(@PathVariable Long id, @Valid @RequestBody ElderUpdateRequest request) {
    request.setTenantId(AuthContext.getOrgId());
    request.setUpdatedBy(AuthContext.getStaffId());
    ElderResponse response = elderService.update(id, request);
    Long tenantId = AuthContext.getOrgId();
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "ELDER", id, "更新老人档案");
    return Result.ok(response);
  }

  @GetMapping("/{id}")
  public Result<ElderResponse> get(@PathVariable Long id) {
    return Result.ok(elderService.get(id, AuthContext.getOrgId()));
  }

  @GetMapping("/page")
  public Result<IPage<ElderResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(elderService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword));
  }

  @PostMapping("/{id}/assignBed")
  public Result<ElderResponse> assignBed(@PathVariable Long id, @Valid @RequestBody AssignBedRequest request) {
    request.setTenantId(AuthContext.getOrgId());
    request.setCreatedBy(AuthContext.getStaffId());
    ElderResponse response = elderService.assignBed(id, request);
    Long tenantId = AuthContext.getOrgId();
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "BED_ASSIGN", "ELDER", id, "床位分配");
    return Result.ok(response);
  }

  @PostMapping("/{id}/unbindBed")
  public Result<ElderResponse> unbindBed(@PathVariable Long id,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(required = false) String reason) {
    Long tenantId = AuthContext.getOrgId();
    ElderResponse response = elderService.unbindBed(id, endDate, reason, tenantId, AuthContext.getStaffId());
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "BED_UNBIND", "ELDER", id, "床位解绑");
    return Result.ok(response);
  }
}
