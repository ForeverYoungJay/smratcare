package com.zhiyangyun.care.store.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.store.model.OrderCancelRequest;
import com.zhiyangyun.care.store.model.OrderRefundRequest;
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

  public StoreOrderLifecycleController(StoreOrderService storeOrderService) {
    this.storeOrderService = storeOrderService;
  }

  @PostMapping("/cancel")
  public Result<Void> cancel(@Valid @RequestBody OrderCancelRequest request) {
    storeOrderService.cancel(request);
    return Result.ok(null);
  }

  @PostMapping("/refund")
  public Result<Void> refund(@Valid @RequestBody OrderRefundRequest request) {
    storeOrderService.refund(request);
    return Result.ok(null);
  }
}
