package com.zhiyangyun.care.hr.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.Department;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.DepartmentMapper;
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
  private final DepartmentMapper departmentMapper;

  public HrPerformanceServiceImpl(CareTaskExecuteLogMapper executeLogMapper,
      CareTaskReviewMapper reviewMapper,
      StaffPointsLogMapper pointsLogMapper,
      StaffPointsAccountMapper pointsAccountMapper,
      StaffMapper staffMapper,
      DepartmentMapper departmentMapper) {
    this.executeLogMapper = executeLogMapper;
    this.reviewMapper = reviewMapper;
    this.pointsLogMapper = pointsLogMapper;
    this.pointsAccountMapper = pointsAccountMapper;
    this.staffMapper = staffMapper;
    this.departmentMapper = departmentMapper;
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

    Double avgScore = reviews.isEmpty() ? 0D : reviews.stream()
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
    response.setRedeemableCash((pointsBalance / 300) * 10);
    return response;
  }

  @Override
  public List<StaffPerformanceRankItem> ranking(Long orgId, String dateFrom, String dateTo, String sortBy, String staffCategory) {
    LocalDateTime start = parseStart(dateFrom);
    LocalDateTime end = parseEnd(dateTo);
    String normalizedCategory = normalizeCategory(staffCategory);

    List<StaffAccount> staffList = staffMapper.selectList(
        Wrappers.lambdaQuery(StaffAccount.class)
            .eq(StaffAccount::getIsDeleted, 0)
            .eq(orgId != null, StaffAccount::getOrgId, orgId));
    Map<Long, Department> departmentMap = departmentMapper.selectList(
            Wrappers.lambdaQuery(Department.class)
                .eq(Department::getIsDeleted, 0)
                .eq(orgId != null, Department::getOrgId, orgId))
        .stream()
        .collect(Collectors.toMap(Department::getId, item -> item, (a, b) -> a));

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
      } else {
        item.setAvgScore(0D);
      }
      item.setPeriodPoints(stats.periodPoints);
      StaffPointsAccount account = accountMap.get(staff.getId());
      int pointsBalance = account == null ? 0 : account.getPointsBalance();
      item.setPointsBalance(pointsBalance);
      item.setRedeemableCash((pointsBalance / 300) * 10);
      item.setStaffCategory(resolveStaffCategory(staff, departmentMap));
      items.add(item);
    }
    if (normalizedCategory != null) {
      items = new ArrayList<>(items.stream()
          .filter(item -> normalizedCategory.equals(item.getStaffCategory()))
          .toList());
    }

    Comparator<StaffPerformanceRankItem> comparator;
    if ("tasks".equalsIgnoreCase(sortBy)) {
      comparator = Comparator.comparing(StaffPerformanceRankItem::getTaskCount, Comparator.nullsLast(Integer::compareTo))
          .reversed()
          .thenComparing(StaffPerformanceRankItem::getPeriodPoints, Comparator.nullsLast(Integer::compareTo).reversed())
          .thenComparing(StaffPerformanceRankItem::getStaffId, Comparator.nullsLast(Long::compareTo));
    } else {
      comparator = Comparator.comparing(StaffPerformanceRankItem::getPeriodPoints, Comparator.nullsLast(Integer::compareTo))
          .reversed()
          .thenComparing(StaffPerformanceRankItem::getTaskCount, Comparator.nullsLast(Integer::compareTo).reversed())
          .thenComparing(StaffPerformanceRankItem::getStaffId, Comparator.nullsLast(Long::compareTo));
    }

    items.sort(comparator);
    for (int i = 0; i < items.size(); i++) {
      items.get(i).setRankNo(i + 1);
    }
    Map<String, Integer> categoryCounter = new HashMap<>();
    for (StaffPerformanceRankItem item : items) {
      String category = item.getStaffCategory() == null ? "OTHER" : item.getStaffCategory();
      int categoryRank = categoryCounter.getOrDefault(category, 0) + 1;
      categoryCounter.put(category, categoryRank);
      item.setCategoryRankNo(categoryRank);
      item.setMedalLevel(resolveMedalLevel(categoryRank));
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

  private String normalizeCategory(String staffCategory) {
    if (staffCategory == null || staffCategory.isBlank()) {
      return null;
    }
    String normalized = staffCategory.trim().toUpperCase();
    if (!"NURSING".equals(normalized) && !"ADMINISTRATION".equals(normalized) && !"OPERATION".equals(normalized)) {
      return null;
    }
    return normalized;
  }

  private String resolveStaffCategory(StaffAccount staff, Map<Long, Department> departmentMap) {
    if (staff == null || departmentMap == null) {
      return "ADMINISTRATION";
    }
    Department department = staff.getDepartmentId() == null ? null : departmentMap.get(staff.getDepartmentId());
    String deptName = department == null ? "" : String.valueOf(department.getDeptName());
    if (deptName.contains("护理") || deptName.contains("照护")) return "NURSING";
    if (deptName.contains("运营") || deptName.contains("营销") || deptName.contains("市场")) return "OPERATION";
    return "ADMINISTRATION";
  }

  private String resolveMedalLevel(int rankNo) {
    if (rankNo == 1) return "GOLD";
    if (rankNo == 2) return "SILVER";
    if (rankNo == 3) return "BRONZE";
    return "NONE";
  }
}
