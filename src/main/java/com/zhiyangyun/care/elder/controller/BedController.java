package com.zhiyangyun.care.elder.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.elder.model.BedRequest;
import com.zhiyangyun.care.elder.model.BedResponse;
import com.zhiyangyun.care.elder.service.BedService;
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
@RequestMapping("/api/bed")
public class BedController {
  private final BedService bedService;
  private final AuditLogService auditLogService;

  public BedController(BedService bedService, AuditLogService auditLogService) {
    this.bedService = bedService;
    this.auditLogService = auditLogService;
  }

  @PostMapping
  public Result<BedResponse> create(@Valid @RequestBody BedRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    BedResponse response = bedService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "BED", response == null ? null : response.getId(), "创建床位");
    return Result.ok(response);
  }

  @PutMapping("/{id}")
  public Result<BedResponse> update(@PathVariable Long id, @Valid @RequestBody BedRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    BedResponse before = bedService.get(id, tenantId);
    BedResponse response = bedService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "BED", id, "更新床位");
    if (before != null && response != null && before.getStatus() != null
        && !before.getStatus().equals(response.getStatus())) {
      auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
          "STATUS_CHANGE", "BED", id, "床位状态变更");
    }
    return Result.ok(response);
  }

  @GetMapping("/map")
  public Result<java.util.List<BedResponse>> map() {
    return Result.ok(bedService.map(AuthContext.getOrgId()));
  }

  @GetMapping("/page")
  public Result<IPage<BedResponse>> pageExplicit(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String bedNo,
      @RequestParam(required = false) String roomNo,
      @RequestParam(required = false) String elderName) {
    return Result.ok(bedService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword, status, bedNo, roomNo, elderName));
  }

  @GetMapping("/{id}")
  public Result<BedResponse> get(@PathVariable Long id) {
    return Result.ok(bedService.get(id, AuthContext.getOrgId()));
  }

  @GetMapping
  public Result<IPage<BedResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String bedNo,
      @RequestParam(required = false) String roomNo,
      @RequestParam(required = false) String elderName) {
    return Result.ok(bedService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword, status, bedNo, roomNo, elderName));
  }

  @GetMapping("/list")
  public Result<java.util.List<BedResponse>> list() {
    return Result.ok(bedService.list(AuthContext.getOrgId()));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    bedService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "BED", id, "删除床位");
    return Result.ok(null);
  }
}
