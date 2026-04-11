package com.zhiyangyun.care.elder.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.elder.model.BedRequest;
import com.zhiyangyun.care.elder.model.BedResponse;
import com.zhiyangyun.care.elder.model.OccupancyHealthIssueResponse;
import com.zhiyangyun.care.elder.model.OccupancyRepairRequest;
import com.zhiyangyun.care.elder.model.OccupancyRepairResponse;
import com.zhiyangyun.care.elder.service.BedService;
import com.zhiyangyun.care.elder.service.OccupancyConsistencyService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
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
@RequestMapping("/api/bed")
public class BedController {
  private final BedService bedService;
  private final OccupancyConsistencyService occupancyConsistencyService;
  private final AuditLogService auditLogService;

  public BedController(
      BedService bedService,
      OccupancyConsistencyService occupancyConsistencyService,
      AuditLogService auditLogService) {
    this.bedService = bedService;
    this.occupancyConsistencyService = occupancyConsistencyService;
    this.auditLogService = auditLogService;
  }

  @PreAuthorize("@elderAuthz.canWriteBed()")
  @PostMapping
  public Result<BedResponse> create(@Valid @RequestBody BedRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    BedResponse response = bedService.create(request);
    auditLogService.recordStructured(
        tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "BED", response == null ? null : response.getId(), "创建床位",
        null, response, request);
    return Result.ok(response);
  }

  @PreAuthorize("@elderAuthz.canWriteBed()")
  @PutMapping("/{id}")
  public Result<BedResponse> update(@PathVariable Long id, @Valid @RequestBody BedRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    BedResponse before = bedService.get(id, tenantId);
    BedResponse response = bedService.update(id, request);
    auditLogService.recordStructured(
        tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "BED", id, "更新床位",
        before, response, request);
    if (before != null && response != null && before.getStatus() != null
        && !before.getStatus().equals(response.getStatus())) {
      Map<String, Object> context = new LinkedHashMap<>();
      context.put("beforeStatus", before.getStatus());
      context.put("afterStatus", response.getStatus());
      context.put("bedNo", response.getBedNo());
      auditLogService.recordStructured(
          tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
          "STATUS_CHANGE", "BED", id, "床位状态变更",
          before, response, context);
    }
    return Result.ok(response);
  }

  @PreAuthorize("@elderAuthz.canReadBed()")
  @GetMapping("/map")
  public Result<java.util.List<BedResponse>> map(
      @RequestParam(defaultValue = "true") boolean includeRisk) {
    return Result.ok(bedService.map(AuthContext.getOrgId(), includeRisk));
  }

  @PreAuthorize("@elderAuthz.canReadBed()")
  @GetMapping("/page")
  public Result<IPage<BedResponse>> pageExplicit(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String bedNo,
      @RequestParam(required = false) String roomNo,
      @RequestParam(required = false) String elderName,
      @RequestParam(required = false) String roomType,
      @RequestParam(required = false) String bedType) {
    return Result.ok(bedService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword, status, bedNo, roomNo, elderName, roomType, bedType));
  }

  @PreAuthorize("@elderAuthz.canReadBed()")
  @GetMapping("/{id}")
  public Result<BedResponse> get(@PathVariable Long id) {
    return Result.ok(bedService.get(id, AuthContext.getOrgId()));
  }

  @PreAuthorize("@elderAuthz.canReadBed()")
  @GetMapping
  public Result<IPage<BedResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String bedNo,
      @RequestParam(required = false) String roomNo,
      @RequestParam(required = false) String elderName,
      @RequestParam(required = false) String roomType,
      @RequestParam(required = false) String bedType) {
    return Result.ok(bedService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword, status, bedNo, roomNo, elderName, roomType, bedType));
  }

  @PreAuthorize("@elderAuthz.canReadBed()")
  @GetMapping("/list")
  public Result<java.util.List<BedResponse>> list() {
    return Result.ok(bedService.list(AuthContext.getOrgId()));
  }

  @PreAuthorize("@elderAuthz.canReadBed()")
  @GetMapping("/occupancy/health")
  public Result<List<OccupancyHealthIssueResponse>> occupancyHealth(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) Long bedId) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(occupancyConsistencyService.inspect(orgId, orgId, elderId, bedId));
  }

  @PreAuthorize("@elderAuthz.canWriteBed()")
  @PostMapping("/occupancy/repair")
  public Result<OccupancyRepairResponse> repairOccupancy(@RequestBody(required = false) OccupancyRepairRequest request) {
    Long orgId = AuthContext.getOrgId();
    OccupancyRepairRequest actual = request == null ? new OccupancyRepairRequest() : request;
    OccupancyRepairResponse response = occupancyConsistencyService.repair(
        orgId, orgId, actual.getElderId(), actual.getBedId(), actual.getReason(), AuthContext.getStaffId());
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("elderId", actual.getElderId());
    context.put("bedId", actual.getBedId());
    context.put("reason", actual.getReason());
    context.put("actions", response.getActions());
    auditLogService.recordStructured(
        orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "OCCUPANCY_REPAIR", "BED", actual.getBedId(), "床位占用一致性修复",
        null, response, context);
    return Result.ok(response);
  }

  @PreAuthorize("@elderAuthz.canWriteBed()")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    BedResponse beforeSnapshot = bedService.get(id, tenantId);
    bedService.delete(id, tenantId);
    auditLogService.recordStructured(
        tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "BED", id, "删除床位",
        beforeSnapshot, null, null);
    return Result.ok(null);
  }
}
