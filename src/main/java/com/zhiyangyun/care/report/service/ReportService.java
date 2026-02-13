package com.zhiyangyun.care.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.report.model.ReportResponse;

public interface ReportService {
  IPage<ReportResponse> page(Long orgId, long pageNo, long pageSize, String keyword, String sortBy, String sortOrder);
}
