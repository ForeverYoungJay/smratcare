package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.audit.entity.AuditLog;
import com.zhiyangyun.care.audit.mapper.AuditLogMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.model.FinanceHomeSummary;
import com.zhiyangyun.care.finance.service.DepositService;
import com.zhiyangyun.care.finance.service.ElectricityFeeService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 首页财务卡片：在住老人数、代养费结清率、押金预警、电费未缴房间，外加最近操作记录。 */
@RestController
@RequestMapping("/api/finance/home")
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class FinanceHomeController {
  private final BillMonthlyMapper billMonthlyMapper;
  private final ElderMapper elderMapper;
  private final BedMapper bedMapper;
  private final AuditLogMapper auditLogMapper;
  private final DepositService depositService;
  private final ElectricityFeeService electricityFeeService;

  public FinanceHomeController(BillMonthlyMapper billMonthlyMapper,
      ElderMapper elderMapper,
      BedMapper bedMapper,
      AuditLogMapper auditLogMapper,
      DepositService depositService,
      ElectricityFeeService electricityFeeService) {
    this.billMonthlyMapper = billMonthlyMapper;
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.auditLogMapper = auditLogMapper;
    this.depositService = depositService;
    this.electricityFeeService = electricityFeeService;
  }

  @GetMapping("/summary")
  public Result<FinanceHomeSummary> summary(@RequestParam(required = false) String month) {
    Long orgId = AuthContext.getOrgId();
    YearMonth targetMonth = parseMonth(month);
    FinanceHomeSummary summary = new FinanceHomeSummary();
    summary.setBillMonth(targetMonth.toString());

    // 在住老人数与床位占用
    summary.setResidentCount(intOf(elderMapper.selectCount(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(orgId != null, ElderProfile::getOrgId, orgId)
            .eq(ElderProfile::getStatus, 1))));
    summary.setBedTotal(intOf(bedMapper.selectCount(
        Wrappers.lambdaQuery(Bed.class)
            .eq(Bed::getIsDeleted, 0)
            .eq(orgId != null, Bed::getOrgId, orgId))));
    summary.setOccupiedBedCount(intOf(bedMapper.selectCount(
        Wrappers.lambdaQuery(Bed.class)
            .eq(Bed::getIsDeleted, 0)
            .eq(orgId != null, Bed::getOrgId, orgId)
            .isNotNull(Bed::getElderId))));

    // 代养费结清率
    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getBillMonth, targetMonth.toString())
            .ne(BillMonthly::getStatus, 9));
    int settled = 0;
    BigDecimal total = BigDecimal.ZERO;
    BigDecimal outstanding = BigDecimal.ZERO;
    for (BillMonthly bill : bills) {
      BigDecimal billOutstanding = scale2(bill.getOutstandingAmount());
      if (billOutstanding.compareTo(BigDecimal.ZERO) <= 0) {
        settled += 1;
      }
      total = total.add(scale2(bill.getTotalAmount()));
      outstanding = outstanding.add(billOutstanding.max(BigDecimal.ZERO));
    }
    summary.setBillCount(bills.size());
    summary.setSettledBillCount(settled);
    summary.setBillTotalAmount(total);
    summary.setBillOutstandingAmount(outstanding);
    summary.setCareFeeSettleRate(rate(BigDecimal.valueOf(settled), BigDecimal.valueOf(bills.size())));
    summary.setCareFeeAmountRate(rate(total.subtract(outstanding), total));

    // 押金预警
    var depositSummary = depositService.summary();
    summary.setDepositShortfallCount(depositSummary.getShortfallCount());
    summary.setDepositShortfallAmount(depositSummary.getShortfallAmount());
    summary.setDepositTotalBalance(depositSummary.getTotalBalance());

    // 电费未缴房间
    var electricitySummary = electricityFeeService.summary(targetMonth.toString());
    summary.setElectricityUnpaidRoomCount(electricitySummary.getUnpaidRoomCount());
    summary.setElectricityUnpaidFee(electricitySummary.getUnpaidFee());
    summary.setElectricityUnrecordedRoomCount(electricitySummary.getUnrecordedRoomCount());

    // 最近操作记录
    List<AuditLog> logs = auditLogMapper.selectList(
        Wrappers.lambdaQuery(AuditLog.class)
            .eq(orgId != null, AuditLog::getOrgId, orgId)
            .orderByDesc(AuditLog::getCreateTime)
            .orderByDesc(AuditLog::getId)
            .last("LIMIT 12"));
    for (AuditLog log : logs) {
      FinanceHomeSummary.RecentOperation item = new FinanceHomeSummary.RecentOperation();
      item.setId(log.getId());
      item.setActorName(log.getActorName());
      item.setActionType(log.getActionType());
      item.setActionTypeText(actionText(log.getActionType()));
      item.setEntityType(log.getEntityType());
      item.setDetail(log.getDetail());
      item.setCreateTime(log.getCreateTime());
      summary.getRecentOperations().add(item);
    }
    return Result.ok(summary);
  }

  /** MyBatis-Plus 的 selectCount 在不同版本上返回 Long/long，这里统一收口。 */
  private static int intOf(Number count) {
    return count == null ? 0 : count.intValue();
  }

  private static String actionText(String actionType) {
    if (actionType == null || actionType.isBlank()) {
      return "操作";
    }
    return switch (actionType) {
      case "FIN_BILL_PAY" -> "账单收款";
      case "FIN_BILL_INVALIDATE" -> "作废账单";
      case "FIN_VOUCHER_ISSUE" -> "发放消费券";
      case "FIN_VOUCHER_REVOKE" -> "作废消费券";
      case "FIN_ELECTRICITY_READING_SAVE" -> "登记电表读数";
      case "FIN_ELECTRICITY_PAY_STATUS" -> "电费缴费状态";
      case "FIN_ELECTRICITY_PRICE_SAVE" -> "设置电价";
      case "FIN_DEPOSIT_PAY" -> "押金缴纳";
      case "FIN_DEPOSIT_DEDUCT" -> "押金扣款";
      case "FIN_DEPOSIT_REFUND" -> "押金退还";
      case "FIN_DEPOSIT_STANDARD_SAVE" -> "保存押金标准";
      case "FIN_DEPOSIT_STANDARD_DELETE" -> "删除押金标准";
      case "FIN_REMINDER_HANDLE" -> "处理提醒";
      case "FIN_REMINDER_HANDLE_ALL" -> "一键处理提醒";
      default -> actionType;
    };
  }

  private static BigDecimal rate(BigDecimal numerator, BigDecimal denominator) {
    if (denominator == null || denominator.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return numerator.multiply(BigDecimal.valueOf(100))
        .divide(denominator, 1, RoundingMode.HALF_UP);
  }

  private static YearMonth parseMonth(String month) {
    if (month == null || month.isBlank()) {
      return YearMonth.now();
    }
    try {
      return YearMonth.parse(month.trim());
    } catch (Exception ignored) {
      return YearMonth.now();
    }
  }

  private static BigDecimal scale2(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
  }
}
