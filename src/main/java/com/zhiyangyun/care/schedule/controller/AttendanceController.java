package com.zhiyangyun.care.schedule.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.schedule.model.AttendanceResponse;
import com.zhiyangyun.care.schedule.service.AttendanceService;
import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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

  @PreAuthorize("hasRole('ADMIN')")
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
}
