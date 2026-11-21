package com.zhiyangyun.care.nursing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.nursing.entity.NursingRectification;
import com.zhiyangyun.care.nursing.model.NursingRectificationRequest;
import com.zhiyangyun.care.nursing.model.NursingRectificationReviewRequest;
import com.zhiyangyun.care.nursing.service.NursingRectificationService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 护理整改台账：管理者创建/复核，负责人补充证据；不提供删除。 */
@RestController
@RequestMapping("/api/nursing/rectifications")
public class NursingRectificationController {
  private static final String READ_ACCESS = "hasAnyRole('NURSING_EMPLOYEE','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String MANAGER_ACCESS = "hasAnyRole('NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";

  private final NursingRectificationService rectificationService;
  private final AuditLogService auditLogService;

  public NursingRectificationController(NursingRectificationService rectificationService,
      AuditLogService auditLogService) {
    this.rectificationService = rectificationService;
    this.auditLogService = auditLogService;
  }

  @PreAuthorize(READ_ACCESS)
  @GetMapping("/page")
  public Result<IPage<NursingRectification>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "false") boolean overdue) {
    return Result.ok(rectificationService.page(AuthContext.getOrgId(), pageNo, pageSize, status, keyword, overdue));
  }

  @PreAuthorize(MANAGER_ACCESS)
  @PostMapping
  public Result<NursingRectification> create(@Valid @RequestBody NursingRectificationRequest request) {
    if (request.getTitle() == null || request.getTitle().isBlank() || request.getOwnerId() == null
        || request.getDueDate() == null) {
      return Result.error(400, "整改事项、负责人和完成期限必填");
    }
    Long tenantId = AuthContext.getOrgId();
    NursingRectification row = rectificationService.create(tenantId, request, AuthContext.getStaffId());
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "NURSING_RECTIFICATION", row.getId(), "创建整改事项");
    return Result.ok(row);
  }

  @PreAuthorize(READ_ACCESS)
  @PutMapping("/{id}")
  public Result<NursingRectification> update(@PathVariable Long id,
      @Valid @RequestBody NursingRectificationRequest request) {
    Long tenantId = AuthContext.getOrgId();
    boolean manager = AuthContext.isAdmin() || AuthContext.hasRole("NURSING_MINISTER");
    NursingRectification row;
    try {
      row = rectificationService.update(id, tenantId, request, AuthContext.getStaffId(), manager);
    } catch (IllegalStateException ex) {
      return Result.error(400, ex.getMessage());
    } catch (AccessDeniedException ex) {
      return Result.error(403, ex.getMessage());
    }
    if (row == null) {
      return Result.error(404, "整改事项不存在或无权限访问");
    }
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "NURSING_RECTIFICATION", id, "更新整改事项");
    return Result.ok(row);
  }

  @PreAuthorize(MANAGER_ACCESS)
  @PostMapping("/{id}/review")
  public Result<NursingRectification> review(@PathVariable Long id,
      @Valid @RequestBody NursingRectificationReviewRequest request) {
    Long tenantId = AuthContext.getOrgId();
    NursingRectification row;
    try {
      row = rectificationService.review(id, tenantId, request, AuthContext.getStaffId(), AuthContext.getUsername());
    } catch (IllegalStateException ex) {
      return Result.error(400, ex.getMessage());
    }
    if (row == null) {
      return Result.error(404, "整改事项不存在或无权限访问");
    }
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "REVIEW", "NURSING_RECTIFICATION", id, Boolean.TRUE.equals(request.getPassed()) ? "整改复核通过" : "整改复核退回");
    return Result.ok(row);
  }
}
