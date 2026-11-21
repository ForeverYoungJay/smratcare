package com.zhiyangyun.care.nursing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.nursing.entity.CareIncident;
import com.zhiyangyun.care.nursing.model.CareIncidentActionRequest;
import com.zhiyangyun.care.nursing.model.CareIncidentDetailResponse;
import com.zhiyangyun.care.nursing.model.CareIncidentRequest;
import com.zhiyangyun.care.nursing.model.CareIncidentSummaryResponse;
import com.zhiyangyun.care.nursing.service.CareIncidentService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 护理异常事件台账：只追加处置记录，不提供删除。 */
@RestController
@RequestMapping("/api/nursing/incidents")
public class CareIncidentController {
  private static final String INCIDENT_ACCESS =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";

  private final CareIncidentService careIncidentService;
  private final AuditLogService auditLogService;

  public CareIncidentController(CareIncidentService careIncidentService, AuditLogService auditLogService) {
    this.careIncidentService = careIncidentService;
    this.auditLogService = auditLogService;
  }

  @PreAuthorize(INCIDENT_ACCESS)
  @GetMapping("/page")
  public Result<IPage<CareIncident>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String incidentType,
      @RequestParam(required = false) String severity,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) Long relatedTaskId) {
    return Result.ok(careIncidentService.page(AuthContext.getOrgId(), pageNo, pageSize, status, incidentType,
        severity, keyword, parseDate(dateFrom), parseDate(dateTo), elderId, relatedTaskId));
  }

  @PreAuthorize(INCIDENT_ACCESS)
  @GetMapping("/summary")
  public Result<CareIncidentSummaryResponse> summary(@RequestParam(required = false) String month) {
    YearMonth target;
    try {
      target = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
    } catch (DateTimeParseException ex) {
      return Result.error(400, "月份格式应为 yyyy-MM");
    }
    return Result.ok(careIncidentService.summary(AuthContext.getOrgId(), target));
  }

  @PreAuthorize(INCIDENT_ACCESS)
  @GetMapping("/{id}")
  public Result<CareIncidentDetailResponse> get(@PathVariable Long id) {
    CareIncidentDetailResponse response = careIncidentService.get(id, AuthContext.getOrgId());
    if (response == null) {
      return Result.error(404, "异常事件不存在或无权限访问");
    }
    return Result.ok(response);
  }

  @PreAuthorize(INCIDENT_ACCESS)
  @PostMapping
  public Result<CareIncidentDetailResponse> create(@Valid @RequestBody CareIncidentRequest request) {
    Long tenantId = AuthContext.getOrgId();
    CareIncidentDetailResponse response = careIncidentService.create(tenantId, request,
        AuthContext.getStaffId(), AuthContext.getUsername());
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "CARE_INCIDENT", response.getIncident().getId(), "上报异常事件 " + response.getIncident().getIncidentNo());
    return Result.ok(response);
  }

  @PreAuthorize(INCIDENT_ACCESS)
  @PostMapping("/{id}/actions")
  public Result<CareIncidentDetailResponse> act(@PathVariable Long id,
      @Valid @RequestBody CareIncidentActionRequest request) {
    Long tenantId = AuthContext.getOrgId();
    boolean manager = AuthContext.isAdmin() || AuthContext.hasRole("NURSING_MINISTER");
    CareIncidentDetailResponse response;
    try {
      response = careIncidentService.act(id, tenantId, request, AuthContext.getStaffId(), AuthContext.getUsername(),
          manager);
    } catch (IllegalStateException ex) {
      return Result.error(400, ex.getMessage());
    } catch (AccessDeniedException ex) {
      return Result.error(403, ex.getMessage());
    }
    if (response == null) {
      return Result.error(404, "异常事件不存在或无权限访问");
    }
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        request.getAction().trim().toUpperCase(), "CARE_INCIDENT", id, "异常事件处置");
    return Result.ok(response);
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
