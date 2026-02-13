package com.zhiyangyun.care.elder.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.elder.model.RoomRequest;
import com.zhiyangyun.care.elder.model.RoomResponse;
import com.zhiyangyun.care.elder.service.RoomService;
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
@RequestMapping("/api/room")
public class RoomController {
  private final RoomService roomService;
  private final AuditLogService auditLogService;

  public RoomController(RoomService roomService, AuditLogService auditLogService) {
    this.roomService = roomService;
    this.auditLogService = auditLogService;
  }

  @PostMapping
  public Result<RoomResponse> create(@Valid @RequestBody RoomRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    RoomResponse response = roomService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "ROOM", response == null ? null : response.getId(), "创建房间");
    return Result.ok(response);
  }

  @PutMapping("/{id}")
  public Result<RoomResponse> update(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    RoomResponse before = roomService.get(id, tenantId);
    RoomResponse response = roomService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "ROOM", id, "更新房间");
    if (before != null && response != null && before.getStatus() != null
        && !before.getStatus().equals(response.getStatus())) {
      auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
          "STATUS_CHANGE", "ROOM", id, "房间状态变更");
    }
    return Result.ok(response);
  }

  @GetMapping("/{id}")
  public Result<RoomResponse> get(@PathVariable Long id) {
    return Result.ok(roomService.get(id, AuthContext.getOrgId()));
  }

  @GetMapping("/page")
  public Result<IPage<RoomResponse>> pageExplicit(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String roomNo,
      @RequestParam(required = false) String building,
      @RequestParam(required = false) String floorNo,
      @RequestParam(required = false) Long buildingId,
      @RequestParam(required = false) Long floorId,
      @RequestParam(required = false) Integer status) {
    return Result.ok(roomService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword, roomNo, building, floorNo, buildingId, floorId, status));
  }

  @GetMapping
  public Result<IPage<RoomResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String roomNo,
      @RequestParam(required = false) String building,
      @RequestParam(required = false) String floorNo,
      @RequestParam(required = false) Long buildingId,
      @RequestParam(required = false) Long floorId,
      @RequestParam(required = false) Integer status) {
    return Result.ok(roomService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword, roomNo, building, floorNo, buildingId, floorId, status));
  }

  @GetMapping("/list")
  public Result<java.util.List<RoomResponse>> list() {
    return Result.ok(roomService.list(AuthContext.getOrgId()));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    roomService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "ROOM", id, "删除房间");
    return Result.ok(null);
  }
}
