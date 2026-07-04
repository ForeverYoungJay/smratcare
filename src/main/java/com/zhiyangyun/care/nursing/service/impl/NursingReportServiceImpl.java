package com.zhiyangyun.care.nursing.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.entity.CareTaskExecuteLog;
import com.zhiyangyun.care.entity.CareTaskReview;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.mapper.CareTaskExecuteLogMapper;
import com.zhiyangyun.care.mapper.CareTaskReviewMapper;
import com.zhiyangyun.care.nursing.entity.ServiceBooking;
import com.zhiyangyun.care.nursing.entity.ShiftHandover;
import com.zhiyangyun.care.nursing.mapper.ServiceBookingMapper;
import com.zhiyangyun.care.nursing.mapper.ShiftHandoverMapper;
import com.zhiyangyun.care.nursing.model.NursingReportSummaryResponse;
import com.zhiyangyun.care.nursing.model.NursingStaffWorkloadItem;
import com.zhiyangyun.care.nursing.service.NursingReportService;
import java.time.LocalDate;
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
  private final CareTaskDailyMapper dailyMapper;
  private final CareTaskReviewMapper reviewMapper;
  private final ShiftHandoverMapper handoverMapper;
  private final StaffMapper staffMapper;

  public NursingReportServiceImpl(ServiceBookingMapper bookingMapper, CareTaskExecuteLogMapper executeLogMapper,
      CareTaskDailyMapper dailyMapper, CareTaskReviewMapper reviewMapper, ShiftHandoverMapper handoverMapper,
      StaffMapper staffMapper) {
    this.bookingMapper = bookingMapper;
    this.executeLogMapper = executeLogMapper;
    this.dailyMapper = dailyMapper;
    this.reviewMapper = reviewMapper;
    this.handoverMapper = handoverMapper;
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

    List<CareTaskDaily> dailyTasks = dailyMapper.selectList(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(tenantId != null, CareTaskDaily::getTenantId, tenantId)
        .ge(CareTaskDaily::getPlanTime, from)
        .le(CareTaskDaily::getPlanTime, to));

    List<CareTaskReview> reviews = reviewMapper.selectList(Wrappers.lambdaQuery(CareTaskReview.class)
        .eq(CareTaskReview::getIsDeleted, 0)
        .eq(tenantId != null, CareTaskReview::getTenantId, tenantId)
        .ge(CareTaskReview::getReviewTime, from)
        .le(CareTaskReview::getReviewTime, to));

    LocalDate fromDate = from.toLocalDate();
    LocalDate toDate = to.toLocalDate();
    List<ShiftHandover> handovers = handoverMapper.selectList(Wrappers.lambdaQuery(ShiftHandover.class)
        .eq(ShiftHandover::getIsDeleted, 0)
        .eq(tenantId != null, ShiftHandover::getTenantId, tenantId)
        .ge(ShiftHandover::getDutyDate, fromDate)
        .le(ShiftHandover::getDutyDate, toDate));

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
    int exceptionTaskCount = (int) dailyTasks.stream()
        .filter(task -> "EXCEPTION".equalsIgnoreCase(task.getStatus()))
        .count();
    int exceptionResolvedCount = (int) reviews.stream()
        .filter(review -> "EXCEPTION_RESOLVE".equalsIgnoreCase(review.getReviewerType()))
        .count();
    int suspiciousExecutionCount = (int) logs.stream()
        .filter(log -> log.getSuspiciousFlag() != null && log.getSuspiciousFlag() == 1)
        .count();
    int qualityReviewCount = (int) reviews.stream()
        .filter(review -> !"EXCEPTION_RESOLVE".equalsIgnoreCase(review.getReviewerType()))
        .count();
    double averageReviewScore = reviews.stream()
        .filter(review -> review.getScore() != null)
        .mapToInt(CareTaskReview::getScore)
        .average()
        .orElse(0D);
    int handoverCount = handovers.size();
    int handoverConfirmedCount = (int) handovers.stream()
        .filter(item -> "CONFIRMED".equalsIgnoreCase(item.getStatus()) || item.getConfirmTime() != null)
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
        : staffMapper.selectBatchIdsSafe(staffIds).stream()
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
    response.setQualityReviewCount(qualityReviewCount);
    response.setAverageReviewScore(round(averageReviewScore));
    response.setExceptionTaskCount(exceptionTaskCount);
    response.setExceptionResolvedCount(exceptionResolvedCount);
    response.setExceptionClosureRate(ratio(exceptionResolvedCount, exceptionTaskCount + exceptionResolvedCount));
    response.setSuspiciousExecutionCount(suspiciousExecutionCount);
    response.setHandoverCount(handoverCount);
    response.setHandoverConfirmedCount(handoverConfirmedCount);
    response.setHandoverConfirmRate(ratio(handoverConfirmedCount, handoverCount));
    response.setMonthlyReportSummary(buildMonthlyReportSummary(response));
    response.setReviewActionItems(buildReviewActionItems(response));
    response.setCareHighlights(buildCareHighlights(response));
    response.setRiskSignals(buildRiskSignals(response));
    response.setNextMonthSuggestions(buildNextMonthSuggestions(response));
    response.setAiCareSummary(buildAiCareSummary(response));
    response.setFamilyReadableSummary(buildFamilyReadableSummary(response));
    response.setStaffWorkloads(workloads);
    return response;
  }

  private double ratio(int numerator, int denominator) {
    if (denominator <= 0) {
      return 0D;
    }
    return Math.round((numerator * 10000D) / denominator) / 10000D;
  }

  private double round(double value) {
    return Math.round(value * 100D) / 100D;
  }

  private String buildMonthlyReportSummary(NursingReportSummaryResponse response) {
    return "护理执行 " + response.getCompletedServices() + "/" + response.getTotalServices()
        + "，计划达成率 " + percent(response.getPlanAchievementRate())
        + "，异常闭环率 " + percent(response.getExceptionClosureRate())
        + "，交接班确认率 " + percent(response.getHandoverConfirmRate()) + "。";
  }

  private List<String> buildReviewActionItems(NursingReportSummaryResponse response) {
    List<String> items = new ArrayList<>();
    if (response.getOverdueCount() != null && response.getOverdueCount() > 0) {
      items.add("复盘超时护理任务，核对排班、长者护理等级和任务派发规则。");
    }
    if (response.getExceptionTaskCount() != null && response.getExceptionTaskCount() > 0) {
      items.add("优先关闭未处理护理异常，补齐处理说明和质检评分。");
    }
    if (response.getSuspiciousExecutionCount() != null && response.getSuspiciousExecutionCount() > 0) {
      items.add("抽查可疑执行记录，核对打卡定位、照片和备注真实性。");
    }
    if (response.getHandoverCount() != null && response.getHandoverCount() > 0
        && response.getHandoverConfirmedCount() < response.getHandoverCount()) {
      items.add("跟进未确认交接班，确保风险事项和待办交接到人。");
    }
    if (items.isEmpty()) {
      items.add("本周期护理执行平稳，可抽样复盘高负荷护理员和重点风险长者。");
    }
    return items;
  }

  private List<String> buildCareHighlights(NursingReportSummaryResponse response) {
    List<String> items = new ArrayList<>();
    if (response.getCompletionRate() != null && response.getCompletionRate() >= 0.95D) {
      items.add("护理执行完成率保持在 " + percent(response.getCompletionRate()) + "，基础照护稳定。");
    }
    if (response.getPlanAchievementRate() != null && response.getPlanAchievementRate() >= 0.9D) {
      items.add("护理计划达成率 " + percent(response.getPlanAchievementRate()) + "，计划内服务落实较好。");
    }
    if (response.getHandoverConfirmRate() != null && response.getHandoverConfirmRate() >= 0.95D && response.getHandoverCount() != null
        && response.getHandoverCount() > 0) {
      items.add("交接班确认率 " + percent(response.getHandoverConfirmRate()) + "，风险事项交接较完整。");
    }
    if (items.isEmpty()) {
      items.add("本周期已形成护理执行、质检、异常和交接班数据，可作为月度复盘底稿。");
    }
    return items;
  }

  private List<String> buildRiskSignals(NursingReportSummaryResponse response) {
    List<String> items = new ArrayList<>();
    if (response.getOverdueCount() != null && response.getOverdueCount() > 0) {
      items.add("存在 " + response.getOverdueCount() + " 项超时护理服务，需复核排班与任务派发。");
    }
    if (response.getExceptionTaskCount() != null && response.getExceptionTaskCount() > 0) {
      items.add("仍有 " + response.getExceptionTaskCount() + " 项异常任务待关注，建议纳入护理主管日复盘。");
    }
    if (response.getSuspiciousExecutionCount() != null && response.getSuspiciousExecutionCount() > 0) {
      items.add("发现 " + response.getSuspiciousExecutionCount() + " 条可疑执行记录，需抽查照片、定位和备注。");
    }
    if (response.getHandoverCount() != null && response.getHandoverCount() > 0
        && response.getHandoverConfirmedCount() < response.getHandoverCount()) {
      items.add("交接班仍有未确认记录，重点风险长者和待办事项可能交接不到位。");
    }
    if (items.isEmpty()) {
      items.add("暂无明显护理执行风险，建议继续抽样查看重点风险长者。");
    }
    return items;
  }

  private List<String> buildNextMonthSuggestions(NursingReportSummaryResponse response) {
    List<String> items = new ArrayList<>();
    if (response.getCompletionRate() != null && response.getCompletionRate() < 0.9D) {
      items.add("优先提升护理任务完成率，按护理等级重新核对任务频次和责任人。");
    }
    if (response.getExceptionClosureRate() != null && response.getExceptionClosureRate() < 0.8D) {
      items.add("建立异常任务 24 小时闭环要求，处置说明、照片和质检意见需同步归档。");
    }
    if (response.getAverageReviewScore() != null && response.getAverageReviewScore() > 0D && response.getAverageReviewScore() < 85D) {
      items.add("针对低分质检项开展专项培训，复盘服务话术、执行规范和记录完整性。");
    }
    if (items.isEmpty()) {
      items.add("下月可聚焦高负荷护理员、重点风险长者和家属反馈，形成专项改进清单。");
    }
    return items;
  }

  private String buildAiCareSummary(NursingReportSummaryResponse response) {
    return "AI护理摘要：本周期共完成护理服务 " + response.getCompletedServices() + "/" + response.getTotalServices()
        + "，完成率 " + percent(response.getCompletionRate())
        + "；质检 " + response.getQualityReviewCount() + " 次，平均评分 " + response.getAverageReviewScore()
        + "；异常闭环率 " + percent(response.getExceptionClosureRate())
        + "。重点关注：" + String.join("；", response.getRiskSignals());
  }

  private String buildFamilyReadableSummary(NursingReportSummaryResponse response) {
    return "本周期照护服务整体"
        + (response.getCompletionRate() != null && response.getCompletionRate() >= 0.9D ? "平稳" : "仍需加强")
        + "，已完成 " + response.getCompletedServices() + " 项服务。机构将持续关注护理执行、异常处理和交接班情况，"
        + "并根据老人状态调整后续照护安排。";
  }

  private String percent(Double value) {
    if (value == null) {
      return "0.0%";
    }
    return String.format("%.1f%%", value * 100D);
  }
}
