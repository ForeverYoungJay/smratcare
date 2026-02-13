package com.zhiyangyun.care.elder.controller.adm;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionRequest;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionResponse;
import com.zhiyangyun.care.elder.model.lifecycle.ChangeLogResponse;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeResponse;
import com.zhiyangyun.care.elder.service.lifecycle.ElderLifecycleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/elder/lifecycle")
public class ElderLifecycleController {
  private final ElderLifecycleService lifecycleService;
  private final AuditLogService auditLogService;

  public ElderLifecycleController(ElderLifecycleService lifecycleService, AuditLogService auditLogService) {
    this.lifecycleService = lifecycleService;
    this.auditLogService = auditLogService;
  }

  @PostMapping("/admit")
  public Result<AdmissionResponse> admit(@Valid @RequestBody AdmissionRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    AdmissionResponse response = lifecycleService.admit(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "ADMIT", "ELDER", request.getElderId(), "入院办理");
    return Result.ok(response);
  }

  @PostMapping("/discharge")
  public Result<DischargeResponse> discharge(@Valid @RequestBody DischargeRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    DischargeResponse response = lifecycleService.discharge(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DISCHARGE", "ELDER", request.getElderId(), "退院办理");
    return Result.ok(response);
  }

  @GetMapping("/changes")
  public Result<IPage<ChangeLogResponse>> changes(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(lifecycleService.changeLogs(tenantId, pageNo, pageSize, elderId));
  }
}
