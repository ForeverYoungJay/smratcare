package com.zhiyangyun.care.nursing.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.nursing.model.NursingQualityMetricsResponse;
import com.zhiyangyun.care.nursing.service.NursingReportService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nursing/quality")
public class NursingQualityController {
  private final NursingReportService nursingReportService;

  public NursingQualityController(NursingReportService nursingReportService) {
    this.nursingReportService = nursingReportService;
  }

  @PreAuthorize("hasAnyRole('NURSING_EMPLOYEE','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/metrics")
  public Result<NursingQualityMetricsResponse> metrics(
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    try {
      LocalDate from = dateFrom == null || dateFrom.isBlank() ? null : LocalDate.parse(dateFrom);
      LocalDate to = dateTo == null || dateTo.isBlank() ? null : LocalDate.parse(dateTo);
      return Result.ok(nursingReportService.qualityMetrics(AuthContext.getOrgId(), from, to));
    } catch (DateTimeParseException ex) {
      return Result.error(400, "日期格式应为 yyyy-MM-dd");
    }
  }
}
