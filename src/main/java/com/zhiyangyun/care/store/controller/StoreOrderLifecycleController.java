package com.zhiyangyun.care.store.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.store.model.OrderCancelRequest;
import com.zhiyangyun.care.store.model.OrderRefundRequest;
import com.zhiyangyun.care.store.model.OrderFulfillRequest;
import com.zhiyangyun.care.store.service.StoreOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store/order")
public class StoreOrderLifecycleController {
  private final StoreOrderService storeOrderService;
  private final AuditLogService auditLogService;

  public StoreOrderLifecycleController(StoreOrderService storeOrderService, AuditLogService auditLogService) {
    this.storeOrderService = storeOrderService;
    this.auditLogService = auditLogService;
  }

  @PostMapping("/cancel")
  public Result<Void> cancel(@Valid @RequestBody OrderCancelRequest request) {
    storeOrderService.cancel(request);
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "ORDER_CANCEL", "ORDER", request.getOrderId(), "商城订单取消");
    return Result.ok(null);
  }

  @PostMapping("/refund")
  public Result<Void> refund(@Valid @RequestBody OrderRefundRequest request) {
    storeOrderService.refund(request);
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "ORDER_REFUND", "ORDER", request.getOrderId(), request.getReason());
    return Result.ok(null);
  }

  @PostMapping("/fulfill")
  public Result<Void> fulfill(@Valid @RequestBody OrderFulfillRequest request) {
    storeOrderService.fulfill(request);
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "ORDER_FULFILL", "ORDER", request.getOrderId(), "商城订单出库");
    return Result.ok(null);
  }
}
