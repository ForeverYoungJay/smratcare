package com.zhiyangyun.care.nursing.service;

import com.zhiyangyun.care.nursing.model.NursingReportSummaryResponse;
import com.zhiyangyun.care.nursing.model.NursingQualityMetricsResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface NursingReportService {
  NursingReportSummaryResponse summary(Long tenantId, LocalDateTime timeFrom, LocalDateTime timeTo);
  NursingQualityMetricsResponse qualityMetrics(Long tenantId, LocalDate dateFrom, LocalDate dateTo);
}
