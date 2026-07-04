package com.zhiyangyun.care.compliance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.compliance.entity.SensitiveAccessLog;
import com.zhiyangyun.care.compliance.model.SensitiveAccessSummary;
import com.zhiyangyun.care.compliance.service.SensitiveAccessService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 合规审计接口：敏感数据访问留痕的查询与统计。仅安全/管理岗可见。
 */
@RestController
@RequestMapping("/api/compliance/sensitive-access")
public class ComplianceAuditController {

  private final SensitiveAccessService sensitiveAccessService;

  public ComplianceAuditController(SensitiveAccessService sensitiveAccessService) {
    this.sensitiveAccessService = sensitiveAccessService;
  }

  @GetMapping("/page")
  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<IPage<SensitiveAccessLog>> page(
      @RequestParam(required = false) Long actorId,
      @RequestParam(required = false) String dataCategory,
      @RequestParam(required = false) String action,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize) {
    Long orgId = AuthContext.getOrgId();
    LocalDateTime start = startDate == null || startDate.isBlank()
        ? null : LocalDate.parse(startDate).atStartOfDay();
    LocalDateTime end = endDate == null || endDate.isBlank()
        ? null : LocalDate.parse(endDate).plusDays(1).atStartOfDay();
    return Result.ok(sensitiveAccessService.page(orgId, actorId, dataCategory, action, start, end, pageNo, pageSize));
  }

  @GetMapping("/summary")
  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<SensitiveAccessSummary> summary(@RequestParam(defaultValue = "30") int days) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(sensitiveAccessService.summary(orgId, days));
  }
}
