package com.zhiyangyun.care.compliance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.compliance.entity.ExportLog;
import com.zhiyangyun.care.compliance.model.ExportCompleteRequest;
import com.zhiyangyun.care.compliance.model.ExportConfirmRequest;
import com.zhiyangyun.care.compliance.model.ExportTicketResponse;
import com.zhiyangyun.care.compliance.service.ExportAuditService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 导出二次确认与留痕：敏感导出需先 confirm 拿一次性 exportTicket，导出完成后 complete 核销并回填行数。
 * 留痕查询仅安全/管理岗可见。
 */
@RestController
@RequestMapping("/api/compliance/export")
public class ComplianceExportController {

  private final ExportAuditService exportAuditService;

  public ComplianceExportController(ExportAuditService exportAuditService) {
    this.exportAuditService = exportAuditService;
  }

  /** 导出二次确认：签发一次性导出凭证（任何登录员工均需先确认再导出）。 */
  @PostMapping("/confirm")
  public Result<ExportTicketResponse> confirm(@Valid @RequestBody ExportConfirmRequest request) {
    return Result.ok(exportAuditService.confirm(request));
  }

  /** 导出完成回执：核销凭证并回填实际导出行数。 */
  @PostMapping("/complete")
  public Result<Void> complete(@Valid @RequestBody ExportCompleteRequest request) {
    exportAuditService.complete(request.getExportTicket(), request.getRowCount());
    return Result.ok(null);
  }

  @GetMapping("/page")
  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<IPage<ExportLog>> page(
      @RequestParam(required = false) String module,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize) {
    Long orgId = AuthContext.getOrgId();
    LocalDateTime start = startDate == null || startDate.isBlank()
        ? null : LocalDate.parse(startDate).atStartOfDay();
    LocalDateTime end = endDate == null || endDate.isBlank()
        ? null : LocalDate.parse(endDate).plusDays(1).atStartOfDay();
    return Result.ok(exportAuditService.page(orgId, module, status, start, end, pageNo, pageSize));
  }
}
