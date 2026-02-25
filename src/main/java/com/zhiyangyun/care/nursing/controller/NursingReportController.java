package com.zhiyangyun.care.nursing.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.nursing.model.NursingReportSummaryResponse;
import com.zhiyangyun.care.nursing.service.NursingReportService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nursing/reports")
public class NursingReportController {
  private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final NursingReportService nursingReportService;

  public NursingReportController(NursingReportService nursingReportService) {
    this.nursingReportService = nursingReportService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/summary")
  public Result<NursingReportSummaryResponse> summary(
      @RequestParam(required = false) String timeFrom,
      @RequestParam(required = false) String timeTo) {
    return Result.ok(nursingReportService.summary(AuthContext.getOrgId(), parseDateTime(timeFrom), parseDateTime(timeTo)));
  }

  private LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    if (value.contains("T")) {
      return LocalDateTime.parse(value);
    }
    return LocalDateTime.parse(value, DATETIME_FORMATTER);
  }
}
