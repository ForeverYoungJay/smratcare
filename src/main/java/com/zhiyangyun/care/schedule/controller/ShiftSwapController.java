package com.zhiyangyun.care.schedule.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.schedule.model.ShiftSwapDecisionRequest;
import com.zhiyangyun.care.schedule.model.ShiftSwapRequestCreateRequest;
import com.zhiyangyun.care.schedule.model.ShiftSwapRequestResponse;
import com.zhiyangyun.care.schedule.service.ShiftSwapService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule/shift-swap")
public class ShiftSwapController {
  private final ShiftSwapService shiftSwapService;

  public ShiftSwapController(ShiftSwapService shiftSwapService) {
    this.shiftSwapService = shiftSwapService;
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/page")
  public Result<IPage<ShiftSwapRequestResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false, defaultValue = "true") boolean mineOnly) {
    return Result.ok(shiftSwapService.page(
        AuthContext.getOrgId(), AuthContext.getStaffId(), AuthContext.isAdmin(), pageNo, pageSize, status, mineOnly));
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping
  public Result<ShiftSwapRequestResponse> create(@Valid @RequestBody ShiftSwapRequestCreateRequest request) {
    return Result.ok(shiftSwapService.create(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @PreAuthorize("isAuthenticated()")
  @PutMapping("/{id}/target-agree")
  public Result<ShiftSwapRequestResponse> targetAgree(
      @PathVariable Long id,
      @RequestBody(required = false) ShiftSwapDecisionRequest request) {
    return Result.ok(shiftSwapService.targetConfirm(
        AuthContext.getOrgId(), AuthContext.getStaffId(), id, true, request == null ? null : request.getRemark()));
  }

  @PreAuthorize("isAuthenticated()")
  @PutMapping("/{id}/target-reject")
  public Result<ShiftSwapRequestResponse> targetReject(
      @PathVariable Long id,
      @RequestBody(required = false) ShiftSwapDecisionRequest request) {
    return Result.ok(shiftSwapService.targetConfirm(
        AuthContext.getOrgId(), AuthContext.getStaffId(), id, false, request == null ? null : request.getRemark()));
  }
}
