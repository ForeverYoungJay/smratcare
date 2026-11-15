package com.zhiyangyun.care.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.crm.model.visit.CrmVisitAppointmentCancelRequest;
import com.zhiyangyun.care.crm.model.visit.CrmVisitAppointmentCompleteRequest;
import com.zhiyangyun.care.crm.model.visit.CrmVisitAppointmentRequest;
import com.zhiyangyun.care.crm.model.visit.CrmVisitAppointmentResponse;
import com.zhiyangyun.care.crm.service.CrmVisitAppointmentService;
import com.zhiyangyun.care.common.web.ResourceNotFoundException;
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
@RequestMapping("/api/crm/visit-appointments")
@PreAuthorize("hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class CrmVisitAppointmentController {
  private final CrmVisitAppointmentService service;

  public CrmVisitAppointmentController(CrmVisitAppointmentService service) {
    this.service = service;
  }

  @GetMapping("/page")
  public Result<IPage<CrmVisitAppointmentResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    return Result.ok(service.page(AuthContext.getOrgId(), AuthContext.getStaffId(), AuthContext.isMinisterOrHigher(),
        pageNo, pageSize, keyword, status, dateFrom, dateTo));
  }

  @PostMapping
  public Result<CrmVisitAppointmentResponse> create(@Valid @RequestBody CrmVisitAppointmentRequest request) {
    return Result.ok(service.create(AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(),
        AuthContext.isMinisterOrHigher(), request));
  }

  @PutMapping("/{id}")
  public Result<CrmVisitAppointmentResponse> update(
      @PathVariable Long id, @Valid @RequestBody CrmVisitAppointmentRequest request) {
    return Result.ok(requireAppointment(service.update(AuthContext.getOrgId(), AuthContext.getStaffId(), AuthContext.isMinisterOrHigher(), id, request)));
  }

  @PostMapping("/{id}/arrive")
  public Result<CrmVisitAppointmentResponse> arrive(@PathVariable Long id) {
    return Result.ok(requireAppointment(service.arrive(AuthContext.getOrgId(), AuthContext.getStaffId(), AuthContext.isMinisterOrHigher(), id)));
  }

  @PostMapping("/{id}/complete")
  public Result<CrmVisitAppointmentResponse> complete(
      @PathVariable Long id, @Valid @RequestBody CrmVisitAppointmentCompleteRequest request) {
    return Result.ok(requireAppointment(service.complete(AuthContext.getOrgId(), AuthContext.getStaffId(), AuthContext.isMinisterOrHigher(), id, request)));
  }

  @PostMapping("/{id}/cancel")
  public Result<CrmVisitAppointmentResponse> cancel(
      @PathVariable Long id, @Valid @RequestBody CrmVisitAppointmentCancelRequest request) {
    return Result.ok(requireAppointment(service.cancel(AuthContext.getOrgId(), AuthContext.getStaffId(), AuthContext.isMinisterOrHigher(), id, request)));
  }

  private CrmVisitAppointmentResponse requireAppointment(CrmVisitAppointmentResponse response) {
    if (response == null) {
      throw new ResourceNotFoundException("参观预约不存在或无权访问");
    }
    return response;
  }
}
