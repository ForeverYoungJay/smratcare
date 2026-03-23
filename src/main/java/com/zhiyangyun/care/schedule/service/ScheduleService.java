package com.zhiyangyun.care.schedule.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.schedule.model.ScheduleRequest;
import com.zhiyangyun.care.schedule.model.ScheduleResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ScheduleService {
  IPage<ScheduleResponse> page(Long orgId, Long requesterStaffId, boolean adminView, long pageNo, long pageSize, Long staffId,
      LocalDate dateFrom, LocalDate dateTo, Integer status, String sortBy, String sortOrder);

  IPage<ScheduleResponse> swapCandidatePage(Long orgId, Long requesterStaffId, long pageNo, long pageSize, Long targetStaffId,
      LocalDate dutyDate);

  ScheduleResponse create(Long orgId, Long staffId, ScheduleRequest request);

  ScheduleResponse update(Long orgId, Long staffId, ScheduleRequest request);

  void delete(Long orgId, Long id);

  ScheduleResponse getOne(Long orgId, Long requesterStaffId, Long id, boolean adminView);

  boolean applySwapByApproval(Long orgId, Long swapRequestId, Long operatorId, LocalDateTime approvedAt);
}
