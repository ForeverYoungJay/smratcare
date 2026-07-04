package com.zhiyangyun.care.smart.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.smart.entity.SmartAlertDispatch;
import com.zhiyangyun.care.smart.model.SmartDispatchActionRequest;
import com.zhiyangyun.care.smart.service.SmartDispatchService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 告警派单闭环接口。 */
@RestController
@RequestMapping("/api/smart/dispatch")
public class SmartDispatchController {

  private static final String OPERATOR =
      "hasAnyRole('NURSING_EMPLOYEE','NURSING_MINISTER','MEDICAL_EMPLOYEE','MEDICAL_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";

  private final SmartDispatchService dispatchService;

  public SmartDispatchController(SmartDispatchService dispatchService) {
    this.dispatchService = dispatchService;
  }

  @GetMapping("/page")
  @PreAuthorize(OPERATOR)
  public Result<IPage<SmartAlertDispatch>> page(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String level,
      @RequestParam(required = false) Long assigneeId) {
    return Result.ok(dispatchService.page(pageNo, pageSize, status, level, assigneeId));
  }

  @PostMapping("/auto")
  @PreAuthorize(OPERATOR)
  public Result<Integer> auto() {
    return Result.ok(dispatchService.autoDispatchOpenAlerts());
  }

  @PostMapping("/assign")
  @PreAuthorize(OPERATOR)
  public Result<SmartAlertDispatch> assign(@Valid @RequestBody SmartDispatchActionRequest request) {
    return Result.ok(dispatchService.assign(request));
  }

  @PostMapping("/{id}/respond")
  @PreAuthorize(OPERATOR)
  public Result<SmartAlertDispatch> respond(@PathVariable("id") Long id) {
    return Result.ok(dispatchService.respond(id));
  }

  @PostMapping("/{id}/onsite")
  @PreAuthorize(OPERATOR)
  public Result<SmartAlertDispatch> onsite(@PathVariable("id") Long id) {
    return Result.ok(dispatchService.onsite(id));
  }

  @PostMapping("/handle")
  @PreAuthorize(OPERATOR)
  public Result<SmartAlertDispatch> handle(@Valid @RequestBody SmartDispatchActionRequest request) {
    return Result.ok(dispatchService.handle(request));
  }

  @PostMapping("/review")
  @PreAuthorize(OPERATOR)
  public Result<SmartAlertDispatch> review(@Valid @RequestBody SmartDispatchActionRequest request) {
    return Result.ok(dispatchService.review(request));
  }
}
