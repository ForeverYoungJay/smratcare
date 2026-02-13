package com.zhiyangyun.care.hr.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.entity.CareTaskExecuteLog;
import com.zhiyangyun.care.entity.CareTaskReview;
import com.zhiyangyun.care.hr.entity.StaffPointsAccount;
import com.zhiyangyun.care.hr.entity.StaffPointsLog;
import com.zhiyangyun.care.hr.mapper.StaffPointsAccountMapper;
import com.zhiyangyun.care.hr.mapper.StaffPointsLogMapper;
import com.zhiyangyun.care.hr.model.StaffPerformanceRankItem;
import com.zhiyangyun.care.hr.model.StaffPerformanceSummaryResponse;
import com.zhiyangyun.care.hr.service.HrPerformanceService;
import com.zhiyangyun.care.mapper.CareTaskExecuteLogMapper;
import com.zhiyangyun.care.mapper.CareTaskReviewMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class HrPerformanceServiceImpl implements HrPerformanceService {
  private final CareTaskExecuteLogMapper executeLogMapper;
  private final CareTaskReviewMapper reviewMapper;
  private final StaffPointsLogMapper pointsLogMapper;
  private final StaffPointsAccountMapper pointsAccountMapper;
  private final StaffMapper staffMapper;

  public HrPerformanceServiceImpl(CareTaskExecuteLogMapper executeLogMapper,
      CareTaskReviewMapper reviewMapper,
      StaffPointsLogMapper pointsLogMapper,
      StaffPointsAccountMapper pointsAccountMapper,
      StaffMapper staffMapper) {
    this.executeLogMapper = executeLogMapper;
    this.reviewMapper = reviewMapper;
    this.pointsLogMapper = pointsLogMapper;
    this.pointsAccountMapper = pointsAccountMapper;
    this.staffMapper = staffMapper;
  }

  @Override
  public StaffPerformanceSummaryResponse summary(Long orgId, Long staffId, String dateFrom, String dateTo) {
    LocalDateTime start = parseStart(dateFrom);
    LocalDateTime end = parseEnd(dateTo);

    List<CareTaskExecuteLog> logs = executeLogMapper.selectList(
        Wrappers.lambdaQuery(CareTaskExecuteLog.class)
            .eq(CareTaskExecuteLog::getIsDeleted, 0)
            .eq(orgId != null, CareTaskExecuteLog::getTenantId, orgId)
            .eq(orgId != null, CareTaskExecuteLog::getOrgId, orgId)
            .eq(staffId != null, CareTaskExecuteLog::getStaffId, staffId)
            .ge(start != null, CareTaskExecuteLog::getExecuteTime, start)
            .le(end != null, CareTaskExecuteLog::getExecuteTime, end));

    int taskCount = logs.size();
    int successCount = (int) logs.stream().filter(l -> Objects.equals(l.getResultStatus(), 1)).count();
    int failCount = (int) logs.stream().filter(l -> Objects.equals(l.getResultStatus(), 0)).count();
    int suspiciousCount = (int) logs.stream().filter(l -> Objects.equals(l.getSuspiciousFlag(), 1)).count();

    List<CareTaskReview> reviews = reviewMapper.selectList(
        Wrappers.lambdaQuery(CareTaskReview.class)
            .eq(CareTaskReview::getIsDeleted, 0)
            .eq(orgId != null, CareTaskReview::getOrgId, orgId)
            .eq(staffId != null, CareTaskReview::getStaffId, staffId)
            .ge(start != null, CareTaskReview::getReviewTime, start)
            .le(end != null, CareTaskReview::getReviewTime, end));

    Double avgScore = reviews.isEmpty() ? null : reviews.stream()
        .mapToInt(CareTaskReview::getScore)
        .average()
        .orElse(0);

    List<StaffPointsLog> pointsLogs = pointsLogMapper.selectList(
        Wrappers.lambdaQuery(StaffPointsLog.class)
            .eq(StaffPointsLog::getIsDeleted, 0)
            .eq(orgId != null, StaffPointsLog::getOrgId, orgId)
            .eq(staffId != null, StaffPointsLog::getStaffId, staffId)
            .ge(start != null, StaffPointsLog::getCreateTime, start)
            .le(end != null, StaffPointsLog::getCreateTime, end));

    int pointsEarned = pointsLogs.stream()
        .mapToInt(StaffPointsLog::getChangePoints)
        .filter(v -> v > 0)
        .sum();
    int pointsDeducted = pointsLogs.stream()
        .mapToInt(StaffPointsLog::getChangePoints)
        .filter(v -> v < 0)
        .map(Math::abs)
        .sum();

    StaffPointsAccount account = pointsAccountMapper.selectOne(
        Wrappers.lambdaQuery(StaffPointsAccount.class)
            .eq(StaffPointsAccount::getIsDeleted, 0)
            .eq(orgId != null, StaffPointsAccount::getOrgId, orgId)
            .eq(staffId != null, StaffPointsAccount::getStaffId, staffId));
    int pointsBalance = account == null ? 0 : account.getPointsBalance();

    StaffAccount staff = staffMapper.selectById(staffId);

    StaffPerformanceSummaryResponse response = new StaffPerformanceSummaryResponse();
    response.setStaffId(staffId);
    response.setStaffName(staff == null ? null : staff.getRealName());
    response.setTaskCount(taskCount);
    response.setSuccessCount(successCount);
    response.setFailCount(failCount);
    response.setSuspiciousCount(suspiciousCount);
    response.setAvgScore(avgScore);
    response.setPointsBalance(pointsBalance);
    response.setPointsEarned(pointsEarned);
    response.setPointsDeducted(pointsDeducted);
    return response;
  }

  @Override
  public List<StaffPerformanceRankItem> ranking(Long orgId, String dateFrom, String dateTo, String sortBy) {
    LocalDateTime start = parseStart(dateFrom);
    LocalDateTime end = parseEnd(dateTo);

    List<StaffAccount> staffList = staffMapper.selectList(
        Wrappers.lambdaQuery(StaffAccount.class)
            .eq(StaffAccount::getIsDeleted, 0)
            .eq(orgId != null, StaffAccount::getOrgId, orgId));

    Map<Long, StaffStats> statsMap = new HashMap<>();
    for (StaffAccount staff : staffList) {
      statsMap.put(staff.getId(), new StaffStats());
    }

    List<CareTaskExecuteLog> logs = executeLogMapper.selectList(
        Wrappers.lambdaQuery(CareTaskExecuteLog.class)
            .eq(CareTaskExecuteLog::getIsDeleted, 0)
            .eq(orgId != null, CareTaskExecuteLog::getOrgId, orgId)
            .ge(start != null, CareTaskExecuteLog::getExecuteTime, start)
            .le(end != null, CareTaskExecuteLog::getExecuteTime, end));

    for (CareTaskExecuteLog log : logs) {
      StaffStats stats = statsMap.computeIfAbsent(log.getStaffId(), k -> new StaffStats());
      stats.taskCount++;
      if (Objects.equals(log.getResultStatus(), 1)) {
        stats.successCount++;
      }
    }

    List<CareTaskReview> reviews = reviewMapper.selectList(
        Wrappers.lambdaQuery(CareTaskReview.class)
            .eq(CareTaskReview::getIsDeleted, 0)
            .eq(orgId != null, CareTaskReview::getOrgId, orgId)
            .ge(start != null, CareTaskReview::getReviewTime, start)
            .le(end != null, CareTaskReview::getReviewTime, end));
    Map<Long, List<CareTaskReview>> reviewMap = reviews.stream()
        .collect(Collectors.groupingBy(CareTaskReview::getStaffId));

    List<StaffPointsLog> pointsLogs = pointsLogMapper.selectList(
        Wrappers.lambdaQuery(StaffPointsLog.class)
            .eq(StaffPointsLog::getIsDeleted, 0)
            .eq(orgId != null, StaffPointsLog::getOrgId, orgId)
            .ge(start != null, StaffPointsLog::getCreateTime, start)
            .le(end != null, StaffPointsLog::getCreateTime, end));
    for (StaffPointsLog log : pointsLogs) {
      StaffStats stats = statsMap.computeIfAbsent(log.getStaffId(), k -> new StaffStats());
      stats.periodPoints += log.getChangePoints();
    }

    Map<Long, StaffPointsAccount> accountMap = pointsAccountMapper.selectList(
            Wrappers.lambdaQuery(StaffPointsAccount.class)
                .eq(StaffPointsAccount::getIsDeleted, 0)
                .eq(orgId != null, StaffPointsAccount::getOrgId, orgId))
        .stream()
        .collect(Collectors.toMap(StaffPointsAccount::getStaffId, a -> a));

    List<StaffPerformanceRankItem> items = new ArrayList<>();
    for (StaffAccount staff : staffList) {
      StaffStats stats = statsMap.getOrDefault(staff.getId(), new StaffStats());
      StaffPerformanceRankItem item = new StaffPerformanceRankItem();
      item.setStaffId(staff.getId());
      item.setStaffName(staff.getRealName());
      item.setTaskCount(stats.taskCount);
      item.setSuccessCount(stats.successCount);
      List<CareTaskReview> staffReviews = reviewMap.getOrDefault(staff.getId(), List.of());
      if (!staffReviews.isEmpty()) {
        item.setAvgScore(staffReviews.stream().mapToInt(CareTaskReview::getScore).average().orElse(0));
      }
      item.setPeriodPoints(stats.periodPoints);
      StaffPointsAccount account = accountMap.get(staff.getId());
      item.setPointsBalance(account == null ? 0 : account.getPointsBalance());
      items.add(item);
    }

    Comparator<StaffPerformanceRankItem> comparator;
    if ("tasks".equalsIgnoreCase(sortBy)) {
      comparator = Comparator.comparing(StaffPerformanceRankItem::getTaskCount, Comparator.nullsLast(Integer::compareTo))
          .reversed()
          .thenComparing(StaffPerformanceRankItem::getPeriodPoints, Comparator.nullsLast(Integer::compareTo)).reversed();
    } else {
      comparator = Comparator.comparing(StaffPerformanceRankItem::getPeriodPoints, Comparator.nullsLast(Integer::compareTo))
          .reversed()
          .thenComparing(StaffPerformanceRankItem::getTaskCount, Comparator.nullsLast(Integer::compareTo)).reversed();
    }

    items.sort(comparator);
    for (int i = 0; i < items.size(); i++) {
      items.get(i).setRankNo(i + 1);
    }
    return items;
  }

  private LocalDateTime parseStart(String dateFrom) {
    if (dateFrom == null || dateFrom.isBlank()) {
      return null;
    }
    return LocalDateTime.of(LocalDate.parse(dateFrom), LocalTime.MIN);
  }

  private LocalDateTime parseEnd(String dateTo) {
    if (dateTo == null || dateTo.isBlank()) {
      return null;
    }
    return LocalDateTime.of(LocalDate.parse(dateTo), LocalTime.MAX);
  }

  private static class StaffStats {
    private int taskCount;
    private int successCount;
    private int periodPoints;
  }
}
