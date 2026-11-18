package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaWorkReport;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.schedule.entity.AttendanceRecord;
import com.zhiyangyun.care.schedule.mapper.AttendanceRecordMapper;
import com.zhiyangyun.care.oa.mapper.OaWorkReportMapper;
import com.zhiyangyun.care.oa.model.OaWorkReportRequest;
import com.zhiyangyun.care.oa.model.OaWorkReportSummaryResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
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
  private final StaffMapper staffMapper;
  private final OaTaskMapper taskMapper;
  private final OaTodoMapper todoMapper;
  private final AttendanceRecordMapper attendanceMapper;

  public OaWorkReportController(OaWorkReportMapper reportMapper, StaffMapper staffMapper, OaTaskMapper taskMapper,
      OaTodoMapper todoMapper, AttendanceRecordMapper attendanceMapper) {
    this.reportMapper = reportMapper;
    this.staffMapper = staffMapper;
    this.taskMapper = taskMapper;
    this.todoMapper = todoMapper;
    this.attendanceMapper = attendanceMapper;
  }

  public record ReviewRequest(String comment, Integer score) {}

  /** 主管点评：只能点评本部门已提交的总结（院长、管理员不限部门），不能点评自己的 */
  @PutMapping("/{id}/review")
  public Result<OaWorkReport> review(@PathVariable Long id, @RequestBody ReviewRequest request) {
    if (!AuthContext.isMinisterOrHigher()) {
      throw new org.springframework.security.access.AccessDeniedException("仅部门负责人及以上可点评");
    }
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    OaWorkReport report = reportMapper.selectOne(applyReportScope(Wrappers.lambdaQuery(OaWorkReport.class)
        .eq(OaWorkReport::getId, id)
        .eq(OaWorkReport::getIsDeleted, 0)
        .eq(orgId != null, OaWorkReport::getOrgId, orgId)
        .last("LIMIT 1"), staffId, false));
    if (report == null) {
      throw new IllegalArgumentException("总结不存在或不在您的点评范围内");
    }
    if (java.util.Objects.equals(report.getReporterId(), staffId)) {
      throw new IllegalArgumentException("不能点评自己的总结");
    }
    if (!"SUBMITTED".equals(report.getStatus())) {
      throw new IllegalArgumentException("只能点评已提交的总结");
    }
    if (request.comment() == null || request.comment().isBlank()) {
      throw new IllegalArgumentException("请填写点评意见");
    }
    if (request.score() != null && (request.score() < 1 || request.score() > 5)) {
      throw new IllegalArgumentException("评分为 1-5 分");
    }
    report.setReviewerId(staffId);
    report.setReviewerName(AuthContext.getUsername());
    report.setReviewComment(request.comment().trim());
    report.setReviewScore(request.score());
    report.setReviewedAt(java.time.LocalDateTime.now());
    reportMapper.updateById(report);
    return Result.ok(report);
  }

  /** 撰写草稿用的本人工作数据（只统计本人；只填入草稿，不会自动提交） */
  @GetMapping("/draft-stats")
  public Result<java.util.Map<String, Object>> draftStats(
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate startDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate endDate) {
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("开始日期不能晚于结束日期");
    }
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    java.time.LocalDateTime from = startDate.atStartOfDay();
    java.time.LocalDateTime to = endDate.plusDays(1).atStartOfDay();
    java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
    stats.put("tasksDone", taskMapper.selectCount(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0).eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(OaTask::getAssigneeId, staffId).eq(OaTask::getStatus, "DONE")
        .ge(OaTask::getUpdateTime, from).lt(OaTask::getUpdateTime, to)));
    stats.put("todosDone", todoMapper.selectCount(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0).eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(OaTodo::getAssigneeId, staffId).eq(OaTodo::getStatus, "DONE")
        .ge(OaTodo::getUpdateTime, from).lt(OaTodo::getUpdateTime, to)));
    stats.put("attendanceDays", attendanceMapper.selectCount(Wrappers.lambdaQuery(AttendanceRecord.class)
        .eq(AttendanceRecord::getIsDeleted, 0).eq(orgId != null, AttendanceRecord::getOrgId, orgId)
        .eq(AttendanceRecord::getStaffId, staffId).isNotNull(AttendanceRecord::getCheckInTime)
        .ge(AttendanceRecord::getWorkDate, startDate).le(AttendanceRecord::getWorkDate, endDate)));
    return Result.ok(stats);
  }

  @GetMapping("/page")
  public Result<IPage<OaWorkReport>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String reportType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "true") boolean mineOnly,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate startDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate endDate) {
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("开始日期不能晚于结束日期");
    }
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    String normalizedType = normalizeReportType(reportType);
    String normalizedStatus = normalizeStatus(status);
    var wrapper = applyReportScope(Wrappers.lambdaQuery(OaWorkReport.class)
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
        .orderByDesc(OaWorkReport::getCreateTime), staffId, mineOnly);
    return Result.ok(reportMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/summary")
  public Result<OaWorkReportSummaryResponse> summary(
      @RequestParam(required = false) String reportType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "true") boolean mineOnly,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate startDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate endDate) {
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("开始日期不能晚于结束日期");
    }
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    String normalizedType = normalizeReportType(reportType);
    String normalizedStatus = normalizeStatus(status);

    var baseWrapper = buildSummaryWrapper(orgId, staffId, normalizedType, normalizedStatus, keyword, startDate, endDate, mineOnly);

    LocalDate today = LocalDate.now();
    LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
    LocalDate monthStart = today.withDayOfMonth(1);

    OaWorkReportSummaryResponse response = new OaWorkReportSummaryResponse();
    response.setTotalCount(count(reportMapper.selectCount(baseWrapper)));
    response.setDraftCount(count(reportMapper.selectCount(
        buildSummaryWrapper(orgId, staffId, normalizedType, "DRAFT", keyword, startDate, endDate, mineOnly))));
    response.setSubmittedCount(count(reportMapper.selectCount(
        buildSummaryWrapper(orgId, staffId, normalizedType, "SUBMITTED", keyword, startDate, endDate, mineOnly))));
    response.setTodaySubmittedCount(count(reportMapper.selectCount(applyReportScope(Wrappers.lambdaQuery(OaWorkReport.class)
        .eq(OaWorkReport::getIsDeleted, 0)
        .eq(orgId != null, OaWorkReport::getOrgId, orgId)
        .eq(normalizedType != null, OaWorkReport::getReportType, normalizedType)
        .eq(OaWorkReport::getStatus, "SUBMITTED")
        .eq(OaWorkReport::getReportDate, today), staffId, mineOnly))));
    response.setWeekSubmittedCount(count(reportMapper.selectCount(applyReportScope(Wrappers.lambdaQuery(OaWorkReport.class)
        .eq(OaWorkReport::getIsDeleted, 0)
        .eq(orgId != null, OaWorkReport::getOrgId, orgId)
        .eq(normalizedType != null, OaWorkReport::getReportType, normalizedType)
        .eq(OaWorkReport::getStatus, "SUBMITTED")
        .ge(OaWorkReport::getReportDate, weekStart), staffId, mineOnly))));
    response.setMonthSubmittedCount(count(reportMapper.selectCount(applyReportScope(Wrappers.lambdaQuery(OaWorkReport.class)
        .eq(OaWorkReport::getIsDeleted, 0)
        .eq(orgId != null, OaWorkReport::getOrgId, orgId)
        .eq(normalizedType != null, OaWorkReport::getReportType, normalizedType)
        .eq(OaWorkReport::getStatus, "SUBMITTED")
        .ge(OaWorkReport::getReportDate, monthStart), staffId, mineOnly))));
    response.setDayTypeCount(count(reportMapper.selectCount(
        buildSummaryWrapper(orgId, staffId, "DAY", normalizedStatus, keyword, startDate, endDate, mineOnly))));
    response.setWeekTypeCount(count(reportMapper.selectCount(
        buildSummaryWrapper(orgId, staffId, "WEEK", normalizedStatus, keyword, startDate, endDate, mineOnly))));
    response.setMonthTypeCount(count(reportMapper.selectCount(
        buildSummaryWrapper(orgId, staffId, "MONTH", normalizedStatus, keyword, startDate, endDate, mineOnly))));
    response.setYearTypeCount(count(reportMapper.selectCount(
        buildSummaryWrapper(orgId, staffId, "YEAR", normalizedStatus, keyword, startDate, endDate, mineOnly))));
    response.setMissingSummaryCount(count(reportMapper.selectCount(
        buildSummaryWrapper(orgId, staffId, normalizedType, normalizedStatus, keyword, startDate, endDate, mineOnly)
            .and(w -> w.isNull(OaWorkReport::getContentSummary).or().eq(OaWorkReport::getContentSummary, "")))));
    return Result.ok(response);
  }

  @GetMapping("/daily/page")
  public Result<IPage<OaWorkReport>> dailyPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "true") boolean mineOnly,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate startDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate endDate) {
    return page(pageNo, pageSize, "DAY", status, keyword, mineOnly, startDate, endDate);
  }

  @GetMapping("/weekly/page")
  public Result<IPage<OaWorkReport>> weeklyPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "true") boolean mineOnly,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate startDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate endDate) {
    return page(pageNo, pageSize, "WEEK", status, keyword, mineOnly, startDate, endDate);
  }

  @GetMapping("/monthly/page")
  public Result<IPage<OaWorkReport>> monthlyPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "true") boolean mineOnly,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate startDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate endDate) {
    return page(pageNo, pageSize, "MONTH", status, keyword, mineOnly, startDate, endDate);
  }

  @GetMapping("/yearly/page")
  public Result<IPage<OaWorkReport>> yearlyPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "true") boolean mineOnly,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate startDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate endDate) {
    return page(pageNo, pageSize, "YEAR", status, keyword, mineOnly, startDate, endDate);
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
    report.setReporterId(AuthContext.getStaffId());
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
    requireNotReviewed(report);
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
    report.setReporterId(AuthContext.getStaffId());
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
      requireNotReviewed(report);
      report.setIsDeleted(1);
      reportMapper.updateById(report);
    }
    return Result.ok(null);
  }

  private OaWorkReport findAccessibleReport(Long id) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    return reportMapper.selectOne(applyReportScope(Wrappers.lambdaQuery(OaWorkReport.class)
        .eq(OaWorkReport::getId, id)
        .eq(OaWorkReport::getIsDeleted, 0)
        .eq(orgId != null, OaWorkReport::getOrgId, orgId)
        .last("LIMIT 1"), staffId, true));
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

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OaWorkReport> buildSummaryWrapper(
      Long orgId, Long staffId, String reportType, String status, String keyword, LocalDate startDate, LocalDate endDate, boolean mineOnly) {
    return applyReportScope(Wrappers.lambdaQuery(OaWorkReport.class)
        .eq(OaWorkReport::getIsDeleted, 0)
        .eq(orgId != null, OaWorkReport::getOrgId, orgId)
        .eq(reportType != null, OaWorkReport::getReportType, reportType)
        .eq(status != null, OaWorkReport::getStatus, status)
        .and(keyword != null && !keyword.isBlank(), q -> q
            .like(OaWorkReport::getTitle, keyword)
            .or().like(OaWorkReport::getContentSummary, keyword)
            .or().like(OaWorkReport::getCompletedWork, keyword)
            .or().like(OaWorkReport::getRiskIssue, keyword)
            .or().like(OaWorkReport::getNextPlan, keyword)
            .or().like(OaWorkReport::getReporterName, keyword))
        .ge(startDate != null, OaWorkReport::getReportDate, startDate)
        .le(endDate != null, OaWorkReport::getReportDate, endDate), staffId, mineOnly);
  }

  private void requireNotReviewed(OaWorkReport report) {
    if (report.getReviewedAt() != null) {
      throw new IllegalArgumentException("总结已被点评，不能再修改或删除");
    }
  }

  /**
   * 数据范围由服务端身份决定：普通员工无论 mineOnly 传什么都只看本人；
   * 部门负责人可看本部门；院长与管理员可看全院。
   */
  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OaWorkReport> applyReportScope(
      com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OaWorkReport> wrapper,
      Long staffId,
      boolean mineOnly) {
    if (!mineOnly && AuthContext.isMinisterOrHigher()) {
      if (AuthContext.isAdmin() || AuthContext.hasRole("DIRECTOR")) {
        return wrapper;
      }
      StaffAccount me = staffId == null ? null : staffMapper.selectById(staffId);
      if (me != null && me.getDepartmentId() != null) {
        List<Long> team = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
                .eq(StaffAccount::getIsDeleted, 0).eq(StaffAccount::getDepartmentId, me.getDepartmentId()))
            .stream().map(StaffAccount::getId).toList();
        return wrapper.in(OaWorkReport::getReporterId, team);
      }
    }
    if (staffId == null) {
      return wrapper.eq(OaWorkReport::getId, -1L);
    }
    return wrapper.and(w -> w.eq(OaWorkReport::getReporterId, staffId).or().eq(OaWorkReport::getCreatedBy, staffId));
  }

  private long count(Long value) {
    return value == null ? 0L : value;
  }
}
