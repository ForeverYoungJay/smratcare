package com.zhiyangyun.care.nursing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.nursing.model.ShiftHandoverRequest;
import com.zhiyangyun.care.nursing.model.ShiftHandoverResponse;
import com.zhiyangyun.care.nursing.service.ShiftHandoverService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
@RequestMapping("/api/nursing/handovers")
public class ShiftHandoverController {
  private final ShiftHandoverService shiftHandoverService;
  private final AuditLogService auditLogService;

  public ShiftHandoverController(ShiftHandoverService shiftHandoverService, AuditLogService auditLogService) {
    this.shiftHandoverService = shiftHandoverService;
    this.auditLogService = auditLogService;
  }

  @PreAuthorize("hasAnyRole('NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping
  public Result<ShiftHandoverResponse> create(@Valid @RequestBody ShiftHandoverRequest request) {
    Result<ShiftHandoverResponse> validation = validateRequest(request);
    if (validation != null) {
      return validation;
    }
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    ShiftHandoverResponse response = shiftHandoverService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "SHIFT_HANDOVER", response == null ? null : response.getId(), "创建交接班");
    return Result.ok(response);
  }

  @PreAuthorize("hasAnyRole('NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PutMapping("/{id}")
  public Result<ShiftHandoverResponse> update(@PathVariable Long id, @Valid @RequestBody ShiftHandoverRequest request) {
    Result<ShiftHandoverResponse> validation = validateRequest(request);
    if (validation != null) {
      return validation;
    }
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    ShiftHandoverResponse response = shiftHandoverService.update(id, request);
    if (response == null) {
      return Result.error(404, "交接班记录不存在或无权限访问");
    }
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "SHIFT_HANDOVER", id, "更新交接班");
    return Result.ok(response);
  }

  @PreAuthorize("hasAnyRole('NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/{id}")
  public Result<ShiftHandoverResponse> get(@PathVariable Long id) {
    ShiftHandoverResponse response = shiftHandoverService.get(id, AuthContext.getOrgId());
    if (response == null) {
      return Result.error(404, "交接班记录不存在或无权限访问");
    }
    return Result.ok(response);
  }

  @PreAuthorize("hasAnyRole('NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/page")
  public Result<IPage<ShiftHandoverResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String shiftCode,
      @RequestParam(required = false) String status) {
    LocalDate from = parseDate(dateFrom);
    LocalDate to = parseDate(dateTo);
    return Result.ok(shiftHandoverService.page(AuthContext.getOrgId(), pageNo, pageSize, from, to, shiftCode, status));
  }

  @PreAuthorize("hasAnyRole('NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    shiftHandoverService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "SHIFT_HANDOVER", id, "删除交接班");
    return Result.ok(null);
  }

  private Result<ShiftHandoverResponse> validateRequest(ShiftHandoverRequest request) {
    if (request == null) {
      return Result.error(400, "请求体不能为空");
    }
    String status = request.getStatus() == null ? "DRAFT" : request.getStatus().trim().toUpperCase();
    boolean needStaff = "HANDED_OVER".equals(status) || "CONFIRMED".equals(status);
    if (needStaff && (request.getFromStaffId() == null || request.getToStaffId() == null)) {
      return Result.error(400, "已交接/已确认状态必须选择交班人与接班人");
    }
    return null;
  }

  private LocalDate parseDate(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return LocalDate.parse(value);
    } catch (DateTimeParseException ignore) {
      return null;
    }
  }
}
