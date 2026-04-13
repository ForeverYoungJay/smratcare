package com.zhiyangyun.care.schedule.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.schedule.model.AttendanceDashboardOverviewResponse;
import com.zhiyangyun.care.schedule.model.AttendanceResponse;
import com.zhiyangyun.care.schedule.model.AttendanceSeasonRuleRequest;
import com.zhiyangyun.care.schedule.model.AttendanceSeasonRuleResponse;
import com.zhiyangyun.care.schedule.model.AttendanceStaffAvailabilityResponse;
import java.time.LocalDate;
import java.time.YearMonth;

public interface AttendanceService {
  IPage<AttendanceResponse> page(Long orgId, long pageNo, long pageSize, Long staffId,
      LocalDate dateFrom, LocalDate dateTo, String status, String sortBy, String sortOrder);

  AttendanceDashboardOverviewResponse overview(Long orgId, Long currentStaffId, Long queryStaffId, YearMonth month);

  AttendanceResponse punch(Long orgId, Long currentStaffId, String action);

  AttendanceSeasonRuleResponse getSeasonRule(Long orgId);

  AttendanceSeasonRuleResponse saveSeasonRule(Long orgId, Long currentStaffId, AttendanceSeasonRuleRequest request);

  AttendanceStaffAvailabilityResponse staffAvailability(Long orgId, Long staffId);

  AttendanceResponse reviewRecord(Long orgId, Long currentStaffId, Long id, Integer reviewed, String reviewRemark);
}
