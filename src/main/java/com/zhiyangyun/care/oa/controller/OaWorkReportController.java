package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaWorkReport;
import com.zhiyangyun.care.oa.mapper.OaWorkReportMapper;
import com.zhiyangyun.care.oa.model.OaWorkReportRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/oa/report")
public class OaWorkReportController {
  private final OaWorkReportMapper reportMapper;

  public OaWorkReportController(OaWorkReportMapper reportMapper) {
    this.reportMapper = reportMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaWorkReport>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String reportType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate startDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate endDate) {
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("开始日期不能晚于结束日期");
    }
    Long orgId = AuthContext.getOrgId();
    String normalizedType = normalizeReportType(reportType);
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaWorkReport.class)
        .eq(OaWorkReport::getIsDeleted, 0)
        .eq(orgId != null, OaWorkReport::getOrgId, orgId)
        .eq(normalizedType != null, OaWorkReport::getReportType, normalizedType)
        .eq(normalizedStatus != null, OaWorkReport::getStatus, normalizedStatus)
        .and(keyword != null && !keyword.isBlank(), q -> q
            .like(OaWorkReport::getTitle, keyword)
            .or()
            .like(OaWorkReport::getContentSummary, keyword)
            .or()
            .like(OaWorkReport::getCompletedWork, keyword)
            .or()
            .like(OaWorkReport::getRiskIssue, keyword)
            .or()
            .like(OaWorkReport::getNextPlan, keyword)
            .or()
            .like(OaWorkReport::getReporterName, keyword))
        .ge(startDate != null, OaWorkReport::getReportDate, startDate)
        .le(endDate != null, OaWorkReport::getReportDate, endDate)
        .orderByDesc(OaWorkReport::getReportDate)
        .orderByDesc(OaWorkReport::getCreateTime);
    return Result.ok(reportMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/daily/page")
  public Result<IPage<OaWorkReport>> dailyPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate startDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate endDate) {
    return page(pageNo, pageSize, "DAY", status, keyword, startDate, endDate);
  }

  @GetMapping("/weekly/page")
  public Result<IPage<OaWorkReport>> weeklyPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate startDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate endDate) {
    return page(pageNo, pageSize, "WEEK", status, keyword, startDate, endDate);
  }

  @GetMapping("/monthly/page")
  public Result<IPage<OaWorkReport>> monthlyPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate startDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate endDate) {
    return page(pageNo, pageSize, "MONTH", status, keyword, startDate, endDate);
  }

  @GetMapping("/yearly/page")
  public Result<IPage<OaWorkReport>> yearlyPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate startDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate endDate) {
    return page(pageNo, pageSize, "YEAR", status, keyword, startDate, endDate);
  }

  @PostMapping
  public Result<OaWorkReport> create(@Valid @RequestBody OaWorkReportRequest request) {
    Long orgId = AuthContext.getOrgId();
    OaWorkReport report = new OaWorkReport();
    report.setTenantId(orgId);
    report.setOrgId(orgId);
    report.setTitle(request.getTitle());
    report.setReportType(normalizeReportType(request.getReportType()));
    report.setReportDate(request.getReportDate() == null ? LocalDate.now() : request.getReportDate());
    report.setPeriodStartDate(request.getPeriodStartDate());
    report.setPeriodEndDate(request.getPeriodEndDate());
    report.setContentSummary(request.getContentSummary());
    report.setCompletedWork(request.getCompletedWork());
    report.setRiskIssue(request.getRiskIssue());
    report.setNextPlan(request.getNextPlan());
    String normalizedStatus = normalizeStatus(request.getStatus());
    report.setStatus(normalizedStatus == null ? "DRAFT" : normalizedStatus);
    report.setReporterId(request.getReporterId() == null ? AuthContext.getStaffId() : request.getReporterId());
    report.setReporterName(
        request.getReporterName() == null || request.getReporterName().isBlank()
            ? AuthContext.getUsername()
            : request.getReporterName());
    report.setCreatedBy(AuthContext.getStaffId());
    reportMapper.insert(report);
    return Result.ok(report);
  }

  @PutMapping("/{id}")
  public Result<OaWorkReport> update(@PathVariable Long id, @Valid @RequestBody OaWorkReportRequest request) {
    OaWorkReport report = findAccessibleReport(id);
    if (report == null) {
      return Result.ok(null);
    }
    report.setTitle(request.getTitle());
    report.setReportType(normalizeReportType(request.getReportType()));
    report.setReportDate(request.getReportDate() == null ? report.getReportDate() : request.getReportDate());
    report.setPeriodStartDate(request.getPeriodStartDate());
    report.setPeriodEndDate(request.getPeriodEndDate());
    report.setContentSummary(request.getContentSummary());
    report.setCompletedWork(request.getCompletedWork());
    report.setRiskIssue(request.getRiskIssue());
    report.setNextPlan(request.getNextPlan());
    String normalizedStatus = normalizeStatus(request.getStatus());
    report.setStatus(normalizedStatus == null ? report.getStatus() : normalizedStatus);
    report.setReporterId(request.getReporterId());
    report.setReporterName(request.getReporterName());
    reportMapper.updateById(report);
    return Result.ok(report);
  }

  @PutMapping("/{id}/submit")
  public Result<OaWorkReport> submit(@PathVariable Long id) {
    OaWorkReport report = findAccessibleReport(id);
    if (report == null) {
      return Result.ok(null);
    }
    report.setStatus("SUBMITTED");
    reportMapper.updateById(report);
    return Result.ok(report);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaWorkReport report = findAccessibleReport(id);
    if (report != null) {
      report.setIsDeleted(1);
      reportMapper.updateById(report);
    }
    return Result.ok(null);
  }

  private OaWorkReport findAccessibleReport(Long id) {
    Long orgId = AuthContext.getOrgId();
    return reportMapper.selectOne(Wrappers.lambdaQuery(OaWorkReport.class)
        .eq(OaWorkReport::getId, id)
        .eq(OaWorkReport::getIsDeleted, 0)
        .eq(orgId != null, OaWorkReport::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private String normalizeReportType(String reportType) {
    if (reportType == null || reportType.isBlank()) {
      return null;
    }
    String normalized = reportType.trim().toUpperCase();
    if (!"DAY".equals(normalized)
        && !"WEEK".equals(normalized)
        && !"MONTH".equals(normalized)
        && !"YEAR".equals(normalized)) {
      throw new IllegalArgumentException("reportType 仅支持 DAY/WEEK/MONTH/YEAR");
    }
    return normalized;
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase();
    if (!"DRAFT".equals(normalized) && !"SUBMITTED".equals(normalized)) {
      throw new IllegalArgumentException("status 仅支持 DRAFT/SUBMITTED");
    }
    return normalized;
  }
}
