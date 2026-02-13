package com.zhiyangyun.care.asset.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.asset.model.FloorRequest;
import com.zhiyangyun.care.asset.model.FloorResponse;
import com.zhiyangyun.care.asset.service.FloorService;
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
@RequestMapping("/api/asset/floors")
public class FloorController {
  private final FloorService floorService;
  private final AuditLogService auditLogService;

  public FloorController(FloorService floorService, AuditLogService auditLogService) {
    this.floorService = floorService;
    this.auditLogService = auditLogService;
  }

  @PostMapping
  public Result<FloorResponse> create(@Valid @RequestBody FloorRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    FloorResponse response = floorService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "FLOOR", response == null ? null : response.getId(), "创建楼层");
    return Result.ok(response);
  }

  @PutMapping("/{id}")
  public Result<FloorResponse> update(@PathVariable Long id, @Valid @RequestBody FloorRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    FloorResponse response = floorService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "FLOOR", id, "更新楼层");
    return Result.ok(response);
  }

  @GetMapping("/{id}")
  public Result<FloorResponse> get(@PathVariable Long id) {
    return Result.ok(floorService.get(id));
  }

  @GetMapping("/page")
  public Result<IPage<FloorResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long buildingId,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(floorService.page(tenantId, pageNo, pageSize, buildingId, keyword, status));
  }

  @GetMapping("/list")
  public Result<java.util.List<FloorResponse>> list(@RequestParam(required = false) Long buildingId) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(floorService.list(tenantId, buildingId));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    floorService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "FLOOR", id, "删除楼层");
    return Result.ok(null);
  }
}
