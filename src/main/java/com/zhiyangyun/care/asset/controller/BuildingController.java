package com.zhiyangyun.care.asset.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.asset.model.BuildingRequest;
import com.zhiyangyun.care.asset.model.BuildingResponse;
import com.zhiyangyun.care.asset.service.BuildingService;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
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
@RequestMapping("/api/asset/buildings")
public class BuildingController {
  private final BuildingService buildingService;
  private final AuditLogService auditLogService;

  public BuildingController(BuildingService buildingService, AuditLogService auditLogService) {
    this.buildingService = buildingService;
    this.auditLogService = auditLogService;
  }

  @PostMapping
  public Result<BuildingResponse> create(@Valid @RequestBody BuildingRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    BuildingResponse response = buildingService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "BUILDING", response == null ? null : response.getId(), "创建楼栋");
    return Result.ok(response);
  }

  @PutMapping("/{id}")
  public Result<BuildingResponse> update(@PathVariable Long id, @Valid @RequestBody BuildingRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    BuildingResponse response = buildingService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "BUILDING", id, "更新楼栋");
    return Result.ok(response);
  }

  @GetMapping("/{id}")
  public Result<BuildingResponse> get(@PathVariable Long id) {
    return Result.ok(buildingService.get(id));
  }

  @GetMapping("/page")
  public Result<IPage<BuildingResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(buildingService.page(tenantId, pageNo, pageSize, keyword, status));
  }

  @GetMapping("/list")
  public Result<java.util.List<BuildingResponse>> list() {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(buildingService.list(tenantId));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    buildingService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "BUILDING", id, "删除楼栋");
    return Result.ok(null);
  }
}
