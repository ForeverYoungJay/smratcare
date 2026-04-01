package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.Department;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.DepartmentMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.hr.entity.StaffMonthlyFeeBill;
import com.zhiyangyun.care.hr.entity.StaffProfile;
import com.zhiyangyun.care.hr.entity.StaffServicePlan;
import com.zhiyangyun.care.hr.mapper.StaffMonthlyFeeBillMapper;
import com.zhiyangyun.care.hr.mapper.StaffProfileMapper;
import com.zhiyangyun.care.hr.mapper.StaffServicePlanMapper;
import com.zhiyangyun.care.hr.model.StaffPerformanceSummaryResponse;
import com.zhiyangyun.care.hr.service.HrPerformanceService;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.model.OaMyInfoItemResponse;
import com.zhiyangyun.care.oa.model.OaMyInfoSummaryResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oa/my-info")
public class OaMyInfoController {
  private final StaffMapper staffMapper;
  private final DepartmentMapper departmentMapper;
  private final StaffProfileMapper staffProfileMapper;
  private final StaffServicePlanMapper staffServicePlanMapper;
  private final StaffMonthlyFeeBillMapper staffMonthlyFeeBillMapper;
  private final OaTaskMapper oaTaskMapper;
  private final OaApprovalMapper oaApprovalMapper;
  private final HrPerformanceService hrPerformanceService;
  private final ObjectMapper objectMapper;

  public OaMyInfoController(
      StaffMapper staffMapper,
      DepartmentMapper departmentMapper,
      StaffProfileMapper staffProfileMapper,
      StaffServicePlanMapper staffServicePlanMapper,
      StaffMonthlyFeeBillMapper staffMonthlyFeeBillMapper,
      OaTaskMapper oaTaskMapper,
      OaApprovalMapper oaApprovalMapper,
      HrPerformanceService hrPerformanceService,
      ObjectMapper objectMapper) {
    this.staffMapper = staffMapper;
    this.departmentMapper = departmentMapper;
    this.staffProfileMapper = staffProfileMapper;
    this.staffServicePlanMapper = staffServicePlanMapper;
    this.staffMonthlyFeeBillMapper = staffMonthlyFeeBillMapper;
    this.oaTaskMapper = oaTaskMapper;
    this.oaApprovalMapper = oaApprovalMapper;
    this.hrPerformanceService = hrPerformanceService;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/summary")
  public Result<OaMyInfoSummaryResponse> summary(@RequestParam(required = false) List<Long> staffIds) {
    Long orgId = AuthContext.getOrgId();
    Long currentStaffId = AuthContext.getStaffId();
    List<Long> resolvedStaffIds = resolveVisibleStaffIds(currentStaffId, staffIds);
    if (resolvedStaffIds.isEmpty()) {
      return Result.ok(new OaMyInfoSummaryResponse());
    }

    List<StaffAccount> staffList = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .in(StaffAccount::getId, resolvedStaffIds));
    Map<Long, StaffAccount> staffMap = staffList.stream()
        .filter(item -> item.getId() != null)
        .collect(Collectors.toMap(StaffAccount::getId, item -> item, (a, b) -> a));
    List<Long> effectiveStaffIds = resolvedStaffIds.stream()
        .filter(staffMap::containsKey)
        .toList();
    Map<Long, StaffProfile> profileMap = loadProfileMap(orgId, effectiveStaffIds);
    Map<Long, Department> departmentMap = loadDepartmentMap(staffList);
    Map<Long, StaffServicePlan> planMap = loadServicePlanMap(orgId, effectiveStaffIds);

    YearMonth currentMonth = YearMonth.now();
    YearMonth previousMonth = currentMonth.minusMonths(1);
    YearMonth nextMonth = currentMonth.plusMonths(1);
    Map<Long, StaffMonthlyFeeBill> electricityMap = loadElectricityMap(orgId, effectiveStaffIds, currentMonth.toString());
    Map<Long, List<OaTask>> openTaskMap = loadOpenTaskMap(orgId, effectiveStaffIds);
    Map<Long, Integer> completedTaskCountMap = loadCompletedTaskCountMap(orgId, effectiveStaffIds, currentMonth);
    Map<Long, List<OaApproval>> reimbursementMap = loadReimbursements(orgId, effectiveStaffIds, currentMonth);

    List<OaMyInfoItemResponse> items = new ArrayList<>();
    for (Long staffId : effectiveStaffIds) {
      StaffAccount staff = staffMap.get(staffId);
      StaffProfile profile = profileMap.get(staffId);
      Department department = departmentMap.get(staff == null ? null : staff.getDepartmentId());
      StaffServicePlan plan = planMap.get(staffId);
      StaffMonthlyFeeBill electricity = electricityMap.get(staffId);
      List<OaTask> dutyTasks = openTaskMap.getOrDefault(staffId, List.of());
      List<OaApproval> reimbursements = reimbursementMap.getOrDefault(staffId, List.of());
      StaffPerformanceSummaryResponse performance = hrPerformanceService.summary(
          orgId,
          staffId,
          currentMonth.atDay(1).toString(),
          currentMonth.atEndOfMonth().toString());

      OaMyInfoItemResponse item = new OaMyInfoItemResponse();
      item.setStaffId(staffId);
      item.setStaffNo(staff == null ? null : staff.getStaffNo());
      item.setStaffName(displayName(staff));
      item.setUsername(staff == null ? null : staff.getUsername());
      item.setPhone(staff == null ? null : staff.getPhone());
      item.setDepartmentId(staff == null ? null : staff.getDepartmentId());
      item.setDepartmentName(department == null ? null : department.getDeptName());
      item.setJobTitle(profile == null ? null : profile.getJobTitle());
      item.setHireDate(profile == null ? null : profile.getHireDate());
      item.setProbationEndDate(resolveProbationEndDate(profile));
      item.setProbationRemainingDays(resolveRemainingDays(item.getProbationEndDate()));
      item.setContractSignDueDate(resolveContractSignDueDate(profile));
      item.setContractSignRemainingDays(resolveRemainingDays(item.getContractSignDueDate()));

      BigDecimal salaryBaseline = resolveSalaryBaseline(orgId, staff, profile);
      item.setSalarySource(salaryBaseline == null ? "待录入" : "按最近入职薪资基准展示");
      item.setCurrentMonthSalary(salaryBaseline);
      item.setPreviousMonthSalary(salaryBaseline);
      item.setNextMonthSalary(salaryBaseline);

      item.setTaskOpenCount(dutyTasks.size());
      item.setTaskOverdueCount((int) dutyTasks.stream()
          .filter(task -> task.getEndTime() != null && task.getEndTime().isBefore(LocalDateTime.now()))
          .count());
      item.setTaskCompletedThisMonth(completedTaskCountMap.getOrDefault(staffId, 0));
      item.setDutyTasks(dutyTasks.stream().limit(4).map(OaTask::getTitle).filter(Objects::nonNull).toList());

      item.setPerformanceAvgScore(performance == null ? null : performance.getAvgScore());
      item.setPerformancePointsBalance(performance == null ? null : performance.getPointsBalance());
      item.setPerformanceRedeemableCash(performance == null ? null : performance.getRedeemableCash());
      item.setPerformanceSuccessCount(performance == null ? null : performance.getSuccessCount());

      item.setReimbursePendingCount((int) reimbursements.stream().filter(approval -> "PENDING".equalsIgnoreCase(approval.getStatus())).count());
      item.setReimburseMonthCount(reimbursements.size());
      item.setReimburseMonthAmount(reimbursements.stream()
          .map(OaApproval::getAmount)
          .filter(Objects::nonNull)
          .reduce(BigDecimal.ZERO, BigDecimal::add));

      item.setElectricityMonth(currentMonth.toString());
      item.setElectricityAmount(electricity == null ? null : electricity.getAmount());
      item.setMeterNo(firstNonBlank(electricity == null ? null : electricity.getMeterNo(), plan == null ? null : plan.getMeterNo()));
      item.setDormitoryLabel(buildDormitoryLabel(electricity, plan));
      items.add(item);
    }

    OaMyInfoSummaryResponse response = new OaMyInfoSummaryResponse();
    response.setCurrentMonth(currentMonth.toString());
    response.setPreviousMonth(previousMonth.toString());
    response.setNextMonth(nextMonth.toString());
    response.setCompareEnabled(AuthContext.isAdmin());
    response.setItems(items);
    return Result.ok(response);
  }

  private List<Long> resolveVisibleStaffIds(Long currentStaffId, List<Long> requestedStaffIds) {
    List<Long> sanitized = sanitizeIds(requestedStaffIds);
    if (AuthContext.isAdmin()) {
      if (sanitized.isEmpty() && currentStaffId != null) {
        return List.of(currentStaffId);
      }
      return sanitized.stream().limit(6).toList();
    }
    return currentStaffId == null ? List.of() : List.of(currentStaffId);
  }

  private Map<Long, StaffProfile> loadProfileMap(Long orgId, List<Long> staffIds) {
    if (staffIds.isEmpty()) return Map.of();
    return staffProfileMapper.selectList(Wrappers.lambdaQuery(StaffProfile.class)
            .eq(StaffProfile::getIsDeleted, 0)
            .eq(orgId != null, StaffProfile::getOrgId, orgId)
            .in(StaffProfile::getStaffId, staffIds))
        .stream()
        .collect(Collectors.toMap(StaffProfile::getStaffId, item -> item, (a, b) -> a));
  }

  private Map<Long, Department> loadDepartmentMap(List<StaffAccount> staffList) {
    List<Long> departmentIds = staffList.stream()
        .map(StaffAccount::getDepartmentId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    if (departmentIds.isEmpty()) return Map.of();
    return departmentMapper.selectBatchIds(departmentIds).stream()
        .filter(item -> item.getId() != null)
        .collect(Collectors.toMap(Department::getId, item -> item, (a, b) -> a));
  }

  private Map<Long, StaffServicePlan> loadServicePlanMap(Long orgId, List<Long> staffIds) {
    if (staffIds.isEmpty()) return Map.of();
    return staffServicePlanMapper.selectList(Wrappers.lambdaQuery(StaffServicePlan.class)
            .eq(StaffServicePlan::getIsDeleted, 0)
            .eq(orgId != null, StaffServicePlan::getOrgId, orgId)
            .in(StaffServicePlan::getStaffId, staffIds))
        .stream()
        .collect(Collectors.toMap(StaffServicePlan::getStaffId, item -> item, (a, b) -> a));
  }

  private Map<Long, StaffMonthlyFeeBill> loadElectricityMap(Long orgId, List<Long> staffIds, String month) {
    if (staffIds.isEmpty()) return Map.of();
    return staffMonthlyFeeBillMapper.selectList(Wrappers.lambdaQuery(StaffMonthlyFeeBill.class)
            .eq(StaffMonthlyFeeBill::getIsDeleted, 0)
            .eq(orgId != null, StaffMonthlyFeeBill::getOrgId, orgId)
            .eq(StaffMonthlyFeeBill::getFeeType, "ELECTRICITY")
            .eq(StaffMonthlyFeeBill::getFeeMonth, month)
            .in(StaffMonthlyFeeBill::getStaffId, staffIds)
            .orderByDesc(StaffMonthlyFeeBill::getUpdateTime))
        .stream()
        .collect(Collectors.toMap(StaffMonthlyFeeBill::getStaffId, item -> item, (a, b) -> a));
  }

  private Map<Long, List<OaTask>> loadOpenTaskMap(Long orgId, List<Long> staffIds) {
    if (staffIds.isEmpty()) return Map.of();
    List<OaTask> tasks = oaTaskMapper.selectList(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(OaTask::getStatus, "OPEN")
        .in(OaTask::getAssigneeId, staffIds)
        .orderByAsc(OaTask::getEndTime)
        .orderByDesc(OaTask::getCreateTime));
    return tasks.stream().collect(Collectors.groupingBy(OaTask::getAssigneeId, LinkedHashMap::new, Collectors.toList()));
  }

  private Map<Long, Integer> loadCompletedTaskCountMap(Long orgId, List<Long> staffIds, YearMonth month) {
    if (staffIds.isEmpty()) return Map.of();
    List<OaTask> tasks = oaTaskMapper.selectList(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(OaTask::getStatus, "DONE")
        .in(OaTask::getAssigneeId, staffIds)
        .ge(OaTask::getUpdateTime, month.atDay(1).atStartOfDay())
        .le(OaTask::getUpdateTime, month.atEndOfMonth().atTime(23, 59, 59)));
    Map<Long, Integer> result = new HashMap<>();
    for (OaTask task : tasks) {
      result.put(task.getAssigneeId(), result.getOrDefault(task.getAssigneeId(), 0) + 1);
    }
    return result;
  }

  private Map<Long, List<OaApproval>> loadReimbursements(Long orgId, List<Long> staffIds, YearMonth month) {
    if (staffIds.isEmpty()) return Map.of();
    List<OaApproval> approvals = oaApprovalMapper.selectList(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getApprovalType, "REIMBURSE")
        .in(OaApproval::getApplicantId, staffIds)
        .ge(OaApproval::getCreateTime, month.atDay(1).atStartOfDay())
        .le(OaApproval::getCreateTime, month.atEndOfMonth().atTime(23, 59, 59))
        .orderByDesc(OaApproval::getCreateTime));
    return approvals.stream().collect(Collectors.groupingBy(OaApproval::getApplicantId, LinkedHashMap::new, Collectors.toList()));
  }

  private BigDecimal resolveSalaryBaseline(Long orgId, StaffAccount staff, StaffProfile profile) {
    if (staff == null) {
      return null;
    }
    String candidateName = normalizeBlank(staff.getRealName());
    if (candidateName == null) {
      return null;
    }
    OaApproval latestRecruitment = oaApprovalMapper.selectOne(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getApprovalType, "HR_RECRUITMENT")
        .like(OaApproval::getFormData, "\"candidateName\":\"" + candidateName + "\"")
        .orderByDesc(OaApproval::getCreateTime)
        .last("LIMIT 1"));
    Map<String, Object> ext = parseJsonMap(latestRecruitment == null ? null : latestRecruitment.getFormData());
    BigDecimal parsed = parseMoney(ext.get("salary"));
    if (parsed != null) {
      return parsed;
    }
    return profile != null && profile.getSocialSecurityMonthlyAmount() != null
        ? profile.getSocialSecurityMonthlyAmount().setScale(2, RoundingMode.HALF_UP)
        : null;
  }

  private Map<String, Object> parseJsonMap(String json) {
    if (json == null || json.isBlank()) return Map.of();
    try {
      return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    } catch (Exception ex) {
      return Map.of();
    }
  }

  private BigDecimal parseMoney(Object value) {
    if (value == null) return null;
    String text = String.valueOf(value).trim();
    if (text.isEmpty()) return null;
    String normalized = text.replaceAll("[^0-9.\\-]", "");
    if (normalized.isEmpty()) return null;
    try {
      return new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private LocalDate resolveProbationEndDate(StaffProfile profile) {
    if (profile == null || profile.getHireDate() == null) {
      return null;
    }
    return profile.getHireDate().plusDays(90);
  }

  private LocalDate resolveContractSignDueDate(StaffProfile profile) {
    if (profile == null) {
      return null;
    }
    if (profile.getContractStartDate() != null) {
      return profile.getContractStartDate();
    }
    if (profile.getHireDate() != null) {
      return profile.getHireDate().plusDays(30);
    }
    return null;
  }

  private Long resolveRemainingDays(LocalDate targetDate) {
    if (targetDate == null) {
      return null;
    }
    return ChronoUnit.DAYS.between(LocalDate.now(), targetDate);
  }

  private String buildDormitoryLabel(StaffMonthlyFeeBill electricity, StaffServicePlan plan) {
    String building = firstNonBlank(electricity == null ? null : electricity.getDormitoryBuilding(), plan == null ? null : plan.getDormitoryBuilding());
    String room = firstNonBlank(electricity == null ? null : electricity.getDormitoryRoomNo(), plan == null ? null : plan.getDormitoryRoomNo());
    String bed = firstNonBlank(electricity == null ? null : electricity.getDormitoryBedNo(), plan == null ? null : plan.getDormitoryBedNo());
    StringBuilder builder = new StringBuilder();
    if (building != null) builder.append(building);
    if (room != null) {
      if (builder.length() > 0) builder.append(' ');
      builder.append(room);
    }
    if (bed != null) {
      if (builder.length() > 0) builder.append(" / ");
      builder.append("床位 ").append(bed);
    }
    return builder.length() == 0 ? null : builder.toString();
  }

  private String displayName(StaffAccount staff) {
    if (staff == null) return null;
    return firstNonBlank(normalizeBlank(staff.getRealName()), normalizeBlank(staff.getUsername()), "员工");
  }

  private String normalizeBlank(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String firstNonBlank(String... values) {
    if (values == null) return null;
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value.trim();
      }
    }
    return null;
  }

  private List<Long> sanitizeIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    return ids.stream().filter(Objects::nonNull).filter(id -> id > 0).distinct().sorted(Comparator.naturalOrder()).toList();
  }
}
