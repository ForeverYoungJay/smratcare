package com.zhiyangyun.care.schedule.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.schedule.model.ScheduleRequest;
import com.zhiyangyun.care.schedule.model.ScheduleResponse;
import java.time.LocalDate;

public interface ScheduleService {
  IPage<ScheduleResponse> page(Long orgId, long pageNo, long pageSize, Long staffId,
      LocalDate dateFrom, LocalDate dateTo, Integer status, String sortBy, String sortOrder);

  ScheduleResponse create(Long orgId, Long staffId, ScheduleRequest request);

  ScheduleResponse update(Long orgId, Long staffId, ScheduleRequest request);

  void delete(Long orgId, Long id);
}
