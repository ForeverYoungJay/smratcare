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
import com.zhiyangyun.care.crm.model.report.MarketingLeadEntrySummaryResponse;
import com.zhiyangyun.care.crm.model.report.MarketingWorkbenchSummaryResponse;
import com.zhiyangyun.care.crm.service.MarketingReportService;
import com.zhiyangyun.care.crm.model.trace.CrmSalesReportSnapshotResponse;
import com.zhiyangyun.care.crm.service.CrmTraceService;
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
  private final CrmTraceService crmTraceService;
  private final AuditLogService auditLogService;

  public MarketingReportController(
      MarketingReportService marketingReportService,
      CrmTraceService crmTraceService,
      AuditLogService auditLogService) {
    this.marketingReportService = marketingReportService;
    this.crmTraceService = crmTraceService;
    this.auditLogService = auditLogService;
  }

  @PreAuthorize("hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/lead-entry-summary")
  public Result<MarketingLeadEntrySummaryResponse> leadEntrySummary(
      @RequestParam String mode,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String consultantName,
      @RequestParam(required = false) String consultantPhone,
      @RequestParam(required = false) String elderName,
      @RequestParam(required = false) String elderPhone,
      @RequestParam(required = false) String consultDateFrom,
      @RequestParam(required = false) String consultDateTo,
      @RequestParam(required = false) String consultType,
      @RequestParam(required = false) String mediaChannel,
      @RequestParam(required = false) String infoSource,
      @RequestParam(required = false) String customerTag,
      @RequestParam(required = false) String marketerName) {
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MARKETING_REPORT_QUERY", "MARKETING_LEAD_ENTRY_SUMMARY", null, "lead entry summary");
    return Result.ok(marketingReportService.leadEntrySummary(
        orgId, mode, keyword, consultantName, consultantPhone, elderName, elderPhone,
        consultDateFrom, consultDateTo, consultType, mediaChannel, infoSource, customerTag, marketerName));
  }

  @PreAuthorize("hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/callback")
  public Result<MarketingCallbackReportResponse> callback(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String source,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) String type) {
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MARKETING_REPORT_QUERY", "MARKETING_CALLBACK", null, "callback report");
    return Result.ok(marketingReportService.callback(orgId, pageNo, pageSize, dateFrom, dateTo, source, staffId, type));
  }

  @PreAuthorize("hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/data-quality")
  public Result<MarketingDataQualityResponse> dataQuality() {
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MARKETING_REPORT_QUERY", "MARKETING_DATA_QUALITY", null, "data quality monitor");
    return Result.ok(marketingReportService.dataQuality(orgId));
  }

  @PreAuthorize("hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/workbench-summary")
  public Result<MarketingWorkbenchSummaryResponse> workbenchSummary(
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MARKETING_REPORT_QUERY", "MARKETING_WORKBENCH_SUMMARY", null, "workbench summary");
    return Result.ok(marketingReportService.workbenchSummary(orgId, dateFrom, dateTo));
  }

  @PreAuthorize("hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/snapshots")
  public Result<List<CrmSalesReportSnapshotResponse>> snapshots(
      @RequestParam(required = false) String snapshotType,
      @RequestParam(defaultValue = "10") int limit) {
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MARKETING_REPORT_QUERY", "MARKETING_REPORT_SNAPSHOTS", null, "report snapshots");
    return Result.ok(crmTraceService.listSnapshots(orgId, snapshotType, limit));
  }

  @PreAuthorize("hasAnyRole('MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/data-quality/normalize-source")
  public Result<Integer> normalizeSource() {
    Long orgId = AuthContext.getOrgId();
    int updated = marketingReportService.normalizeSources(orgId);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MARKETING_DATA_FIX", "MARKETING_SOURCE", null, "normalize source: " + updated);
    return Result.ok(updated);
  }
}
