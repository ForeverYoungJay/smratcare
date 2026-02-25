package com.zhiyangyun.care.crm.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.crm.model.report.MarketingCallbackReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingChannelReportItem;
import com.zhiyangyun.care.crm.model.report.MarketingConsultationTrendItem;
import com.zhiyangyun.care.crm.model.report.MarketingConversionReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingDataQualityResponse;
import com.zhiyangyun.care.crm.model.report.MarketingFollowupReportResponse;
import com.zhiyangyun.care.crm.service.MarketingReportService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marketing/report")
public class MarketingReportController {
  private final MarketingReportService marketingReportService;
  private final AuditLogService auditLogService;

  public MarketingReportController(MarketingReportService marketingReportService, AuditLogService auditLogService) {
    this.marketingReportService = marketingReportService;
    this.auditLogService = auditLogService;
  }

  @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
  @GetMapping("/conversion")
  public Result<MarketingConversionReportResponse> conversion(
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String source,
      @RequestParam(required = false) Long staffId) {
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MARKETING_REPORT_QUERY", "MARKETING_CONVERSION", null, "conversion report");
    return Result.ok(marketingReportService.conversion(orgId, dateFrom, dateTo, source, staffId));
  }

  @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
  @GetMapping("/consultation")
  public Result<List<MarketingConsultationTrendItem>> consultation(
      @RequestParam(defaultValue = "14") int days,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String source,
      @RequestParam(required = false) Long staffId) {
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MARKETING_REPORT_QUERY", "MARKETING_CONSULTATION", null, "consultation report");
    return Result.ok(marketingReportService.consultationTrend(orgId, days, dateFrom, dateTo, source, staffId));
  }

  @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
  @GetMapping("/channel")
  public Result<List<MarketingChannelReportItem>> channel(
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) Long staffId) {
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MARKETING_REPORT_QUERY", "MARKETING_CHANNEL", null, "channel report");
    return Result.ok(marketingReportService.channel(orgId, dateFrom, dateTo, staffId));
  }

  @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
  @GetMapping("/followup")
  public Result<MarketingFollowupReportResponse> followup(
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String source,
      @RequestParam(required = false) Long staffId) {
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MARKETING_REPORT_QUERY", "MARKETING_FOLLOWUP", null, "followup report");
    return Result.ok(marketingReportService.followup(orgId, dateFrom, dateTo, source, staffId));
  }

  @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
  @GetMapping("/callback")
  public Result<MarketingCallbackReportResponse> callback(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String source,
      @RequestParam(required = false) Long staffId) {
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MARKETING_REPORT_QUERY", "MARKETING_CALLBACK", null, "callback report");
    return Result.ok(marketingReportService.callback(orgId, pageNo, pageSize, dateFrom, dateTo, source, staffId));
  }

  @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
  @GetMapping("/data-quality")
  public Result<MarketingDataQualityResponse> dataQuality() {
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MARKETING_REPORT_QUERY", "MARKETING_DATA_QUALITY", null, "data quality monitor");
    return Result.ok(marketingReportService.dataQuality(orgId));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/data-quality/normalize-source")
  public Result<Integer> normalizeSource() {
    Long orgId = AuthContext.getOrgId();
    int updated = marketingReportService.normalizeSources(orgId);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MARKETING_DATA_FIX", "MARKETING_SOURCE", null, "normalize source: " + updated);
    return Result.ok(updated);
  }
}
