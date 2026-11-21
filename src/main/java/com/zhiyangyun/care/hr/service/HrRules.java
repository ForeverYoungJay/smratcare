package com.zhiyangyun.care.hr.service;

import com.zhiyangyun.care.hr.entity.HrPayrollLine;
import com.zhiyangyun.care.schedule.entity.AttendanceRecord;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Stream;

/** 人事规则：工资批次状态、工资单金额、考勤锁定期间、跨日夜班 */
public final class HrRules {
  /** 工资批次状态：核算草稿 → 提交审核 → 审核通过 → 财务提交银行 → 付款成功（凭回执） */
  private static final Map<String, Map<String, String>> PAYROLL = Map.of(
      "SUBMIT", Map.of("DRAFT", "SUBMITTED"),
      "APPROVE", Map.of("SUBMITTED", "APPROVED"),
      "REJECT", Map.of("SUBMITTED", "DRAFT"),
      "BANK_SUBMIT", Map.of("APPROVED", "BANK_SUBMITTED"),
      "MARK_PAID", Map.of("BANK_SUBMITTED", "PAID"));

  /** 夜班下班最晚可匹配到上班后多少小时（23:00–07:00 只有 8 小时，留足加班余量） */
  public static final int OVERNIGHT_HOURS = 16;

  private HrRules() {}

  public static String nextPayrollStatus(String status, String action) {
    String next = PAYROLL.getOrDefault(action, Map.of()).get(status);
    if (next == null) {
      throw new IllegalArgumentException("当前工资批次状态不能执行该操作");
    }
    return next;
  }

  /** 应发 = 基本 + 绩效 + 津补贴 + 加班；实发 = 应发 − 扣款 − 个人负担；金额两位小数，不允许负数 */
  public static void computeLine(HrPayrollLine line) {
    BigDecimal gross = Stream.of(line.getBaseAmount(), line.getPerformanceAmount(), line.getAllowanceAmount(), line.getOvertimeAmount())
        .map(HrRules::money).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal net = gross.subtract(money(line.getDeductionAmount())).subtract(money(line.getPersonalAmount()));
    if (net.signum() < 0) {
      throw new IllegalArgumentException("实发金额不能为负数，请核对扣款");
    }
    line.setGrossAmount(gross);
    line.setNetAmount(net);
  }

  public static BigDecimal money(BigDecimal value) {
    if (value == null) {
      return BigDecimal.ZERO.setScale(2);
    }
    if (value.signum() < 0) {
      throw new IllegalArgumentException("金额不能为负数");
    }
    return value.setScale(2, RoundingMode.HALF_UP);
  }

  /** 只能锁定已结束的月份，当月还在打卡 */
  public static YearMonth lockablePeriod(String period, YearMonth current) {
    YearMonth month;
    try {
      month = YearMonth.parse(period);
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("考勤期间格式应为 YYYY-MM");
    }
    if (!month.isBefore(current)) {
      throw new IllegalArgumentException("只能锁定已结束的月份");
    }
    return month;
  }

  /** 前一天上班、尚未下班且在合理时长内：凌晨的下班等打卡归到这条夜班记录 */
  public static boolean isOpenOvernight(AttendanceRecord record, LocalDateTime now) {
    return record != null && record.getCheckInTime() != null && record.getCheckOutTime() == null
        && record.getCheckInTime().isAfter(now.minusHours(OVERNIGHT_HOURS));
  }
}
