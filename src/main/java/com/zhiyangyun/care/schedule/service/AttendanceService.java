package com.zhiyangyun.care.schedule.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.schedule.model.AttendanceResponse;
import java.time.LocalDate;

public interface AttendanceService {
  IPage<AttendanceResponse> page(Long orgId, long pageNo, long pageSize, Long staffId,
      LocalDate dateFrom, LocalDate dateTo, String status, String sortBy, String sortOrder);
}
