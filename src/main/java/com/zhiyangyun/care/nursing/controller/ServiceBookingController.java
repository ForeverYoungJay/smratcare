package com.zhiyangyun.care.nursing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.nursing.model.ServiceBookingRequest;
import com.zhiyangyun.care.nursing.model.ServiceBookingResponse;
import com.zhiyangyun.care.nursing.service.ServiceBookingService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@RequestMapping("/api/nursing/service-bookings")
public class ServiceBookingController {
  private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final ServiceBookingService serviceBookingService;
  private final AuditLogService auditLogService;

  public ServiceBookingController(ServiceBookingService serviceBookingService, AuditLogService auditLogService) {
    this.serviceBookingService = serviceBookingService;
    this.auditLogService = auditLogService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<ServiceBookingResponse> create(@Valid @RequestBody ServiceBookingRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    ServiceBookingResponse response = serviceBookingService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "SERVICE_BOOKING", response == null ? null : response.getId(), "创建服务预定");
    return Result.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<ServiceBookingResponse> update(@PathVariable Long id, @Valid @RequestBody ServiceBookingRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    ServiceBookingResponse response = serviceBookingService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "SERVICE_BOOKING", id, "更新服务预定");
    return Result.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public Result<ServiceBookingResponse> get(@PathVariable Long id) {
    return Result.ok(serviceBookingService.get(id, AuthContext.getOrgId()));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/page")
  public Result<IPage<ServiceBookingResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String timeFrom,
      @RequestParam(required = false) String timeTo,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long elderId) {
    LocalDateTime from = parseDateTime(timeFrom);
    LocalDateTime to = parseDateTime(timeTo);
    return Result.ok(serviceBookingService.page(AuthContext.getOrgId(), pageNo, pageSize, from, to, status, elderId));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/generate")
  public Result<Integer> generate(@RequestParam(required = false) String date,
      @RequestParam(defaultValue = "false") boolean force) {
    LocalDate targetDate = date == null || date.isBlank() ? LocalDate.now() : LocalDate.parse(date);
    int created = serviceBookingService.generateFromPlans(AuthContext.getOrgId(), targetDate, force);
    return Result.ok(created);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    serviceBookingService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "SERVICE_BOOKING", id, "删除服务预定");
    return Result.ok(null);
  }

  private LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    if (value.contains("T")) {
      return LocalDateTime.parse(value);
    }
    return LocalDateTime.parse(value, DATETIME_FORMATTER);
  }
}
