package com.zhiyangyun.care.nursing.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.entity.CareTaskExecuteLog;
import com.zhiyangyun.care.mapper.CareTaskExecuteLogMapper;
import com.zhiyangyun.care.nursing.entity.ServiceBooking;
import com.zhiyangyun.care.nursing.mapper.ServiceBookingMapper;
import com.zhiyangyun.care.nursing.model.NursingReportSummaryResponse;
import com.zhiyangyun.care.nursing.model.NursingStaffWorkloadItem;
import com.zhiyangyun.care.nursing.service.NursingReportService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class NursingReportServiceImpl implements NursingReportService {
  private final ServiceBookingMapper bookingMapper;
  private final CareTaskExecuteLogMapper executeLogMapper;
  private final StaffMapper staffMapper;

  public NursingReportServiceImpl(ServiceBookingMapper bookingMapper, CareTaskExecuteLogMapper executeLogMapper,
      StaffMapper staffMapper) {
    this.bookingMapper = bookingMapper;
    this.executeLogMapper = executeLogMapper;
    this.staffMapper = staffMapper;
  }

  @Override
  public NursingReportSummaryResponse summary(Long tenantId, LocalDateTime timeFrom, LocalDateTime timeTo) {
    LocalDateTime from = timeFrom == null ? LocalDateTime.now().minusDays(7) : timeFrom;
    LocalDateTime to = timeTo == null ? LocalDateTime.now() : timeTo;

    List<ServiceBooking> bookings = bookingMapper.selectList(Wrappers.lambdaQuery(ServiceBooking.class)
        .eq(ServiceBooking::getIsDeleted, 0)
        .eq(tenantId != null, ServiceBooking::getTenantId, tenantId)
        .ge(ServiceBooking::getBookingTime, from)
        .le(ServiceBooking::getBookingTime, to));

    List<CareTaskExecuteLog> logs = executeLogMapper.selectList(Wrappers.lambdaQuery(CareTaskExecuteLog.class)
        .eq(CareTaskExecuteLog::getIsDeleted, 0)
        .eq(tenantId != null, CareTaskExecuteLog::getTenantId, tenantId)
        .ge(CareTaskExecuteLog::getExecuteTime, from)
        .le(CareTaskExecuteLog::getExecuteTime, to));

    int totalServices = bookings.size() + logs.size();
    int completedBookings = (int) bookings.stream().filter(b -> "DONE".equalsIgnoreCase(b.getStatus())).count();
    int completedLogs = (int) logs.stream().filter(l -> l.getResultStatus() != null && l.getResultStatus() == 1).count();
    int completedServices = completedBookings + completedLogs;

    int overdueCount = (int) bookings.stream()
        .filter(b -> b.getBookingTime() != null && b.getBookingTime().isBefore(LocalDateTime.now()))
        .filter(b -> !"DONE".equalsIgnoreCase(b.getStatus()) && !"CANCELLED".equalsIgnoreCase(b.getStatus()))
        .count();

    int planBookingCount = (int) bookings.stream().filter(b -> b.getPlanId() != null).count();
    int planCompletedCount = (int) bookings.stream()
        .filter(b -> b.getPlanId() != null && "DONE".equalsIgnoreCase(b.getStatus()))
        .count();

    Map<Long, NursingStaffWorkloadItem> loadMap = new HashMap<>();

    for (ServiceBooking booking : bookings) {
      if (booking.getAssignedStaffId() == null) {
        continue;
      }
      NursingStaffWorkloadItem item = loadMap.computeIfAbsent(booking.getAssignedStaffId(), id -> {
        NursingStaffWorkloadItem x = new NursingStaffWorkloadItem();
        x.setStaffId(id);
        x.setBookingCount(0);
        x.setCompletedBookingCount(0);
        x.setExecuteCount(0);
        x.setCompletedExecuteCount(0);
        x.setTotalLoad(0);
        return x;
      });
      item.setBookingCount(item.getBookingCount() + 1);
      if ("DONE".equalsIgnoreCase(booking.getStatus())) {
        item.setCompletedBookingCount(item.getCompletedBookingCount() + 1);
      }
      item.setTotalLoad(item.getTotalLoad() + 1);
    }

    for (CareTaskExecuteLog log : logs) {
      if (log.getStaffId() == null) {
        continue;
      }
      NursingStaffWorkloadItem item = loadMap.computeIfAbsent(log.getStaffId(), id -> {
        NursingStaffWorkloadItem x = new NursingStaffWorkloadItem();
        x.setStaffId(id);
        x.setBookingCount(0);
        x.setCompletedBookingCount(0);
        x.setExecuteCount(0);
        x.setCompletedExecuteCount(0);
        x.setTotalLoad(0);
        return x;
      });
      item.setExecuteCount(item.getExecuteCount() + 1);
      if (log.getResultStatus() != null && log.getResultStatus() == 1) {
        item.setCompletedExecuteCount(item.getCompletedExecuteCount() + 1);
      }
      item.setTotalLoad(item.getTotalLoad() + 1);
    }

    Set<Long> staffIds = loadMap.keySet();
    Map<Long, StaffAccount> staffMap = staffIds.isEmpty() ? Collections.emptyMap()
        : staffMapper.selectBatchIds(staffIds).stream()
            .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));

    List<NursingStaffWorkloadItem> workloads = new ArrayList<>(loadMap.values());
    for (NursingStaffWorkloadItem item : workloads) {
      StaffAccount staff = staffMap.get(item.getStaffId());
      item.setStaffName(staff == null ? null : staff.getRealName());
    }
    workloads.sort((a, b) -> Integer.compare(b.getTotalLoad(), a.getTotalLoad()));

    NursingReportSummaryResponse response = new NursingReportSummaryResponse();
    response.setTotalServices(totalServices);
    response.setCompletedServices(completedServices);
    response.setCompletionRate(ratio(completedServices, totalServices));
    response.setOverdueCount(overdueCount);
    response.setOverdueRate(ratio(overdueCount, bookings.size()));
    response.setPlanBookingCount(planBookingCount);
    response.setPlanCompletedCount(planCompletedCount);
    response.setPlanAchievementRate(ratio(planCompletedCount, planBookingCount));
    response.setStaffWorkloads(workloads);
    return response;
  }

  private double ratio(int numerator, int denominator) {
    if (denominator <= 0) {
      return 0D;
    }
    return Math.round((numerator * 10000D) / denominator) / 100D;
  }
}
