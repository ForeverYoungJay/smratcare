package com.zhiyangyun.care.hr.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.Department;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.DepartmentMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.hr.entity.StaffMonthlyFeeBill;
import com.zhiyangyun.care.hr.entity.StaffProfile;
import com.zhiyangyun.care.hr.mapper.StaffMonthlyFeeBillMapper;
import com.zhiyangyun.care.hr.mapper.StaffProfileMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StaffSocialSecurityMonthlyBillScheduler {
  private final StaffProfileMapper staffProfileMapper;
  private final StaffMonthlyFeeBillMapper staffMonthlyFeeBillMapper;
  private final StaffMapper staffMapper;
  private final DepartmentMapper departmentMapper;
  private final ObjectMapper objectMapper;

  public StaffSocialSecurityMonthlyBillScheduler(
      StaffProfileMapper staffProfileMapper,
      StaffMonthlyFeeBillMapper staffMonthlyFeeBillMapper,
      StaffMapper staffMapper,
      DepartmentMapper departmentMapper,
      ObjectMapper objectMapper) {
    this.staffProfileMapper = staffProfileMapper;
    this.staffMonthlyFeeBillMapper = staffMonthlyFeeBillMapper;
    this.staffMapper = staffMapper;
    this.departmentMapper = departmentMapper;
    this.objectMapper = objectMapper;
  }

  @Scheduled(cron = "${app.hr.social-security-monthly-bill.cron:0 15 1 1 * ?}")
  public void generateMonthlyBills() {
    YearMonth month = YearMonth.now();
    List<StaffProfile> profiles = staffProfileMapper.selectList(Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getIsDeleted, 0)
        .eq(StaffProfile::getSocialSecurityCompanyApply, 1));
    if (profiles.isEmpty()) {
      return;
    }
    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIdsSafe(
            profiles.stream().map(StaffProfile::getStaffId).distinct().toList())
        .stream()
        .filter(item -> item.getId() != null)
        .collect(Collectors.toMap(StaffAccount::getId, item -> item, (a, b) -> a));
    for (StaffProfile profile : profiles) {
      StaffAccount staff = staffMap.get(profile.getStaffId());
      if (!canGenerate(profile, staff, month)) {
        continue;
      }
      Department department = staff.getDepartmentId() == null ? null : departmentMapper.selectById(staff.getDepartmentId());
      StaffMonthlyFeeBill bill = staffMonthlyFeeBillMapper.selectOne(Wrappers.lambdaQuery(StaffMonthlyFeeBill.class)
          .eq(StaffMonthlyFeeBill::getOrgId, profile.getOrgId())
          .eq(StaffMonthlyFeeBill::getStaffId, profile.getStaffId())
          .eq(StaffMonthlyFeeBill::getFeeMonth, month.toString())
          .eq(StaffMonthlyFeeBill::getFeeType, "SOCIAL_SECURITY")
          .eq(StaffMonthlyFeeBill::getIsDeleted, 0)
          .last("LIMIT 1"));
      if (bill == null) {
        bill = new StaffMonthlyFeeBill();
        bill.setOrgId(profile.getOrgId());
        bill.setStaffId(profile.getStaffId());
        bill.setFeeMonth(month.toString());
        bill.setFeeType("SOCIAL_SECURITY");
        bill.setCreatedBy(0L);
      }
      BigDecimal amount = normalizeMoney(profile.getSocialSecurityMonthlyAmount());
      bill.setStaffNo(normalizeBlank(staff.getStaffNo()));
      bill.setStaffName(normalizeBlank(staff.getRealName()));
      bill.setDepartmentId(staff.getDepartmentId());
      bill.setDepartmentName(department == null ? null : normalizeBlank(department.getDeptName()));
      bill.setTitle("员工社保 " + month + " " + defaultText(staff.getRealName(), defaultText(staff.getUsername(), "员工")));
      bill.setQuantity(BigDecimal.ONE);
      bill.setUnitPrice(amount);
      bill.setAmount(amount);
      bill.setStatus("GENERATED");
      bill.setFinanceSyncStatus("PENDING");
      bill.setFinanceSyncId(null);
      bill.setFinanceSyncAt(null);
      bill.setFinanceSyncBy(null);
      bill.setDetailJson(writeJson(detailPayload(profile, amount)));
      bill.setRemark(defaultText(profile.getSocialSecurityRemark(), "社保自动记账"));
      if (bill.getId() == null) {
        staffMonthlyFeeBillMapper.insert(bill);
      } else {
        staffMonthlyFeeBillMapper.updateById(bill);
      }
      profile.setSocialSecurityLastBilledMonth(month.toString());
      staffProfileMapper.updateById(profile);
    }
  }

  private boolean canGenerate(StaffProfile profile, StaffAccount staff, YearMonth month) {
    if (profile == null || staff == null || staff.getId() == null) {
      return false;
    }
    if (!Integer.valueOf(1).equals(staff.getStatus())) {
      return false;
    }
    if (normalizeMoney(profile.getSocialSecurityMonthlyAmount()).signum() <= 0) {
      return false;
    }
    String workflowStatus = defaultText(profile.getSocialSecurityWorkflowStatus(), "DRAFT").toUpperCase(Locale.ROOT);
    String socialSecurityStatus = defaultText(profile.getSocialSecurityStatus(), "PENDING").toUpperCase(Locale.ROOT);
    if (!List.of("ACTIVE").contains(workflowStatus)
        && !List.of("COMPLETED", "PAID").contains(socialSecurityStatus)) {
      return false;
    }
    return !month.toString().equals(normalizeBlank(profile.getSocialSecurityLastBilledMonth()));
  }

  private Map<String, Object> detailPayload(StaffProfile profile, BigDecimal amount) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("detailSummary", "社保月度费用 " + amount.setScale(2, RoundingMode.HALF_UP) + "元/人");
    payload.put("workflowStatus", profile.getSocialSecurityWorkflowStatus());
    payload.put("socialSecurityStartDate", profile.getSocialSecurityStartDate() == null ? null : profile.getSocialSecurityStartDate().toString());
    payload.put("completedAt", profile.getSocialSecurityCompletedAt() == null ? null : profile.getSocialSecurityCompletedAt().toString());
    return payload;
  }

  private String writeJson(Map<String, Object> payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (Exception ex) {
      return "{}";
    }
  }

  private BigDecimal normalizeMoney(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
  }

  private String normalizeBlank(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String defaultText(String value, String fallback) {
    return normalizeBlank(value) == null ? fallback : value.trim();
  }
}
