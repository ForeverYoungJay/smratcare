package com.zhiyangyun.care.schedule.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.schedule.model.AttendanceDashboardOverviewResponse;
import com.zhiyangyun.care.schedule.model.AttendanceReviewRequest;
import com.zhiyangyun.care.schedule.model.AttendanceResponse;
import com.zhiyangyun.care.schedule.model.AttendanceSeasonRuleRequest;
import com.zhiyangyun.care.schedule.model.AttendanceSeasonRuleResponse;
import com.zhiyangyun.care.schedule.model.AttendanceStaffAvailabilityResponse;
import com.zhiyangyun.care.schedule.service.AttendanceService;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
  private final AttendanceService attendanceService;

  public AttendanceController(AttendanceService attendanceService) {
    this.attendanceService = attendanceService;
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/page")
  public Result<IPage<AttendanceResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortOrder) {
    LocalDate from = dateFrom == null || dateFrom.isBlank() ? null : LocalDate.parse(dateFrom);
    LocalDate to = dateTo == null || dateTo.isBlank() ? null : LocalDate.parse(dateTo);
    return Result.ok(attendanceService.page(AuthContext.getOrgId(), pageNo, pageSize, staffId, from, to, status, sortBy, sortOrder));
  }

  @GetMapping("/overview")
  public Result<AttendanceDashboardOverviewResponse> overview(
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) String month) {
    YearMonth targetMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
    return Result.ok(attendanceService.overview(AuthContext.getOrgId(), AuthContext.getStaffId(), staffId, targetMonth));
  }

  @PostMapping("/punch")
  public Result<AttendanceResponse> punch(@RequestParam String action) {
    return Result.ok(attendanceService.punch(AuthContext.getOrgId(), AuthContext.getStaffId(), action));
  }

  @GetMapping("/season-rule")
  public Result<AttendanceSeasonRuleResponse> seasonRule() {
    return Result.ok(attendanceService.getSeasonRule(AuthContext.getOrgId()));
  }

  @PostMapping("/season-rule")
  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<AttendanceSeasonRuleResponse> saveSeasonRule(@RequestBody AttendanceSeasonRuleRequest request) {
    return Result.ok(attendanceService.saveSeasonRule(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @GetMapping("/staff-availability")
  public Result<AttendanceStaffAvailabilityResponse> staffAvailability(@RequestParam Long staffId) {
    return Result.ok(attendanceService.staffAvailability(AuthContext.getOrgId(), staffId));
  }

  @PutMapping("/{id}/review")
  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<AttendanceResponse> review(
      @PathVariable Long id,
      @RequestBody(required = false) AttendanceReviewRequest request) {
    Integer reviewed = request == null ? 1 : request.getReviewed();
    String reviewRemark = request == null ? null : request.getReviewRemark();
    return Result.ok(attendanceService.reviewRecord(AuthContext.getOrgId(), AuthContext.getStaffId(), id, reviewed, reviewRemark));
  }
}
