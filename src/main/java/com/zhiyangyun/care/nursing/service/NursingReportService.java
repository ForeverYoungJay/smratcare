package com.zhiyangyun.care.nursing.service;

import com.zhiyangyun.care.nursing.model.NursingReportSummaryResponse;
import java.time.LocalDateTime;

public interface NursingReportService {
  NursingReportSummaryResponse summary(Long tenantId, LocalDateTime timeFrom, LocalDateTime timeTo);
}
