package com.zhiyangyun.care.hr.service;

import java.util.List;
import com.zhiyangyun.care.hr.model.StaffPerformanceRankItem;
import com.zhiyangyun.care.hr.model.StaffPerformanceSummaryResponse;

public interface HrPerformanceService {
  StaffPerformanceSummaryResponse summary(Long orgId, Long staffId, String dateFrom, String dateTo);

  List<StaffPerformanceRankItem> ranking(Long orgId, String dateFrom, String dateTo, String sortBy);
}
