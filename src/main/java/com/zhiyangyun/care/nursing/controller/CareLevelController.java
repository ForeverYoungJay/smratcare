package com.zhiyangyun.care.nursing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.nursing.model.CareLevelRequest;
import com.zhiyangyun.care.nursing.model.CareLevelResponse;
import com.zhiyangyun.care.nursing.service.CareLevelService;
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
@RequestMapping("/api/nursing/care-levels")
public class CareLevelController {
  private final CareLevelService careLevelService;
  private final AuditLogService auditLogService;

  public CareLevelController(CareLevelService careLevelService, AuditLogService auditLogService) {
    this.careLevelService = careLevelService;
    this.auditLogService = auditLogService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<CareLevelResponse> create(@Valid @RequestBody CareLevelRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    CareLevelResponse response = careLevelService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "CARE_LEVEL", response == null ? null : response.getId(), "创建护理等级");
    return Result.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<CareLevelResponse> update(@PathVariable Long id, @Valid @RequestBody CareLevelRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    CareLevelResponse response = careLevelService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "CARE_LEVEL", id, "更新护理等级");
    return Result.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public Result<CareLevelResponse> get(@PathVariable Long id) {
    return Result.ok(careLevelService.get(id, AuthContext.getOrgId()));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/page")
  public Result<IPage<CareLevelResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer enabled) {
    return Result.ok(careLevelService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword, enabled));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/list")
  public Result<List<CareLevelResponse>> list(@RequestParam(required = false) Integer enabled) {
    return Result.ok(careLevelService.list(AuthContext.getOrgId(), enabled));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    careLevelService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "CARE_LEVEL", id, "删除护理等级");
    return Result.ok(null);
  }
}
