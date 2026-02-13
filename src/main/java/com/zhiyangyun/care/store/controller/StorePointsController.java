package com.zhiyangyun.care.store.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.store.model.PointsAccountResponse;
import com.zhiyangyun.care.store.model.PointsAdjustRequest;
import com.zhiyangyun.care.store.model.PointsLogResponse;
import com.zhiyangyun.care.store.service.StorePointsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store/points")
public class StorePointsController {
  private final StorePointsService pointsService;
  private final AuditLogService auditLogService;

  public StorePointsController(StorePointsService pointsService, AuditLogService auditLogService) {
    this.pointsService = pointsService;
    this.auditLogService = auditLogService;
  }

  @GetMapping("/page")
  public Result<IPage<PointsAccountResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(pointsService.page(orgId, pageNo, pageSize, keyword, status));
  }

  @GetMapping("/log/page")
  public Result<IPage<PointsLogResponse>> logPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(pointsService.logPage(orgId, pageNo, pageSize, elderId, keyword));
  }

  @PostMapping("/adjust")
  public Result<Void> adjust(@Valid @RequestBody PointsAdjustRequest request) {
    Long orgId = AuthContext.getOrgId();
    request.setOrgId(orgId);
    request.setOperatorId(AuthContext.getStaffId());
    pointsService.adjust(request);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "POINTS_ADJUST", "ELDER", request.getElderId(), request.getRemark());
    return Result.ok(null);
  }
}
