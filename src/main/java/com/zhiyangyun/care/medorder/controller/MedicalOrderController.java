package com.zhiyangyun.care.medorder.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.medorder.entity.MedicalOrder;
import com.zhiyangyun.care.medorder.entity.MedicalOrderExecution;
import com.zhiyangyun.care.medorder.model.MedicalOrderExecRequest;
import com.zhiyangyun.care.medorder.model.MedicalOrderRequest;
import com.zhiyangyun.care.medorder.service.MedicalOrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 医嘱接口。 */
@RestController
@RequestMapping("/api/medical/order")
public class MedicalOrderController {

  private static final String DOCTOR =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String EXECUTOR =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String VIEWER =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN','STAFF')";

  private final MedicalOrderService orderService;

  public MedicalOrderController(MedicalOrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping("/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<MedicalOrder>> pageOrders(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String category) {
    return Result.ok(orderService.pageOrders(pageNo, pageSize, elderId, status, category));
  }

  @PostMapping
  @PreAuthorize(DOCTOR)
  public Result<Long> create(@Valid @RequestBody MedicalOrderRequest request) {
    return Result.ok(orderService.createOrder(request));
  }

  @PostMapping("/{id}/stop")
  @PreAuthorize(DOCTOR)
  public Result<Void> stop(@PathVariable("id") Long id) {
    orderService.stopOrder(id);
    return Result.ok(null);
  }

  @GetMapping("/executions/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<MedicalOrderExecution>> pageExecutions(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) Long orderId,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status) {
    return Result.ok(orderService.pageExecutions(pageNo, pageSize, orderId, elderId, status));
  }

  @PostMapping("/executions/sign")
  @PreAuthorize(EXECUTOR)
  public Result<MedicalOrderExecution> sign(@Valid @RequestBody MedicalOrderExecRequest request) {
    return Result.ok(orderService.signExecution(request));
  }
}
