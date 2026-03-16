package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.audit.entity.AuditLog;
import com.zhiyangyun.care.audit.mapper.AuditLogMapper;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillingConfigEntry;
import com.zhiyangyun.care.bill.entity.BillItem;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillingConfigMapper;
import com.zhiyangyun.care.bill.mapper.BillItemMapper;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.finance.entity.AdmissionFeeAudit;
import com.zhiyangyun.care.finance.entity.ConsumptionRecord;
import com.zhiyangyun.care.finance.entity.DischargeFeeAudit;
import com.zhiyangyun.care.finance.entity.DischargeSettlement;
import com.zhiyangyun.care.finance.entity.ElderAccount;
import com.zhiyangyun.care.finance.entity.ElderAccountLog;
import com.zhiyangyun.care.finance.entity.MonthlyAllocation;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.entity.ReconciliationDaily;
import com.zhiyangyun.care.finance.mapper.AdmissionFeeAuditMapper;
import com.zhiyangyun.care.finance.mapper.ConsumptionRecordMapper;
import com.zhiyangyun.care.finance.mapper.DischargeFeeAuditMapper;
import com.zhiyangyun.care.finance.mapper.DischargeSettlementMapper;
import com.zhiyangyun.care.finance.mapper.ElderAccountLogMapper;
import com.zhiyangyun.care.finance.mapper.ElderAccountMapper;
import com.zhiyangyun.care.finance.mapper.MonthlyAllocationMapper;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import com.zhiyangyun.care.finance.mapper.ReconciliationDailyMapper;
import com.zhiyangyun.care.finance.model.FeeWorkflowConstants;
import com.zhiyangyun.care.finance.model.FinanceBillingConfigBatchUpsertRequest;
import com.zhiyangyun.care.finance.model.FinanceBillingConfigRollbackRequest;
import com.zhiyangyun.care.finance.model.FinanceBillingConfigSnapshotItem;
import com.zhiyangyun.care.finance.model.FinanceBillingConfigUpsertRequest;
import com.zhiyangyun.care.finance.model.FinanceAllocationRuleItem;
import com.zhiyangyun.care.finance.model.FinanceAllocationResidentSyncResponse;
import com.zhiyangyun.care.finance.model.FinanceAllocationMeterValidateRequest;
import com.zhiyangyun.care.finance.model.FinanceAllocationMeterValidateResponse;
import com.zhiyangyun.care.finance.model.FinanceAllocationTemplateInitResponse;
import com.zhiyangyun.care.finance.model.FinanceAutoDebitExceptionItem;
import com.zhiyangyun.care.finance.model.FinanceCollectionFollowUpItem;
import com.zhiyangyun.care.finance.model.FinanceConfigChangeLogItem;
import com.zhiyangyun.care.finance.model.FinanceCrossPeriodApprovalRequest;
import com.zhiyangyun.care.finance.model.FinanceCrossPeriodApprovalResponse;
import com.zhiyangyun.care.finance.model.FinanceConfigImpactPreviewRequest;
import com.zhiyangyun.care.finance.model.FinanceConfigImpactPreviewResponse;
import com.zhiyangyun.care.finance.model.FinanceDischargeStatusSyncExecuteRequest;
import com.zhiyangyun.care.finance.model.FinanceDischargeStatusSyncExecuteResponse;
import com.zhiyangyun.care.finance.model.FinanceDischargeStatusSyncResponse;
import com.zhiyangyun.care.finance.model.FinanceInvoiceReceiptItem;
import com.zhiyangyun.care.finance.model.FinanceHandleActionRequest;
import com.zhiyangyun.care.finance.model.FinanceHandleLogItem;
import com.zhiyangyun.care.finance.model.FinanceIssueCenterItem;
import com.zhiyangyun.care.finance.model.FinanceLedgerHealthResponse;
import com.zhiyangyun.care.finance.model.FinanceModuleEntrySummaryResponse;
import com.zhiyangyun.care.finance.model.FinanceMasterDataOverviewResponse;
import com.zhiyangyun.care.finance.model.FinanceMonthCloseExecuteRequest;
import com.zhiyangyun.care.finance.model.FinanceMonthCloseExecuteResponse;
import com.zhiyangyun.care.finance.model.FinanceMonthLockStatusResponse;
import com.zhiyangyun.care.finance.model.FinanceMonthCloseSummaryResponse;
import com.zhiyangyun.care.finance.model.FinanceMonthUnlockRequest;
import com.zhiyangyun.care.finance.model.FinanceOpsInsightResponse;
import com.zhiyangyun.care.finance.model.FinancePaymentAdjustmentApprovalRequest;
import com.zhiyangyun.care.finance.model.FinancePaymentAdjustmentItem;
import com.zhiyangyun.care.finance.model.FinanceReconcileExceptionItem;
import com.zhiyangyun.care.finance.model.FinanceRoomOpsDetailResponse;
import com.zhiyangyun.care.finance.model.FinanceSearchResponse;
import com.zhiyangyun.care.finance.model.FinanceWorkbenchOverviewResponse;
import com.zhiyangyun.care.finance.service.FinanceMonthLockService;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/workbench")
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class FinanceWorkbenchController {
  private final PaymentRecordMapper paymentRecordMapper;
  private final BillMonthlyMapper billMonthlyMapper;
  private final BillItemMapper billItemMapper;
  private final BillingConfigMapper billingConfigMapper;
  private final ElderAccountMapper elderAccountMapper;
  private final ElderAccountLogMapper elderAccountLogMapper;
  private final AdmissionFeeAuditMapper admissionFeeAuditMapper;
  private final DischargeFeeAuditMapper dischargeFeeAuditMapper;
  private final DischargeSettlementMapper dischargeSettlementMapper;
  private final ConsumptionRecordMapper consumptionRecordMapper;
  private final MonthlyAllocationMapper monthlyAllocationMapper;
  private final ReconciliationDailyMapper reconciliationDailyMapper;
  private final CrmContractMapper crmContractMapper;
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;
  private final ElderMapper elderMapper;
  private final OaApprovalMapper oaApprovalMapper;
  private final AuditLogMapper auditLogMapper;
  private final AuditLogService auditLogService;
  private final ObjectMapper objectMapper;
  private final FinanceMonthLockService financeMonthLockService;
  private static final Pattern UPDATE_ROLLBACK_PATTERN =
      Pattern.compile("更新配置:\\s*([A-Z0-9_]+)@([0-9]{4}-[0-9]{2})\\s+([0-9\\.-]+)->([0-9\\.-]+)");
  private static final Pattern CREATE_ROLLBACK_PATTERN =
      Pattern.compile("新增配置:\\s*([A-Z0-9_]+)@([0-9]{4}-[0-9]{2})\\s+值=([0-9\\.-]+)");

  public FinanceWorkbenchController(
      PaymentRecordMapper paymentRecordMapper,
      BillMonthlyMapper billMonthlyMapper,
      BillItemMapper billItemMapper,
      BillingConfigMapper billingConfigMapper,
      ElderAccountMapper elderAccountMapper,
      ElderAccountLogMapper elderAccountLogMapper,
      AdmissionFeeAuditMapper admissionFeeAuditMapper,
      DischargeFeeAuditMapper dischargeFeeAuditMapper,
      DischargeSettlementMapper dischargeSettlementMapper,
      ConsumptionRecordMapper consumptionRecordMapper,
      MonthlyAllocationMapper monthlyAllocationMapper,
      ReconciliationDailyMapper reconciliationDailyMapper,
      CrmContractMapper crmContractMapper,
      BedMapper bedMapper,
      RoomMapper roomMapper,
      ElderMapper elderMapper,
      OaApprovalMapper oaApprovalMapper,
      AuditLogMapper auditLogMapper,
      AuditLogService auditLogService,
      ObjectMapper objectMapper,
      FinanceMonthLockService financeMonthLockService) {
    this.paymentRecordMapper = paymentRecordMapper;
    this.billMonthlyMapper = billMonthlyMapper;
    this.billItemMapper = billItemMapper;
    this.billingConfigMapper = billingConfigMapper;
    this.elderAccountMapper = elderAccountMapper;
    this.elderAccountLogMapper = elderAccountLogMapper;
    this.admissionFeeAuditMapper = admissionFeeAuditMapper;
    this.dischargeFeeAuditMapper = dischargeFeeAuditMapper;
    this.dischargeSettlementMapper = dischargeSettlementMapper;
    this.consumptionRecordMapper = consumptionRecordMapper;
    this.monthlyAllocationMapper = monthlyAllocationMapper;
    this.reconciliationDailyMapper = reconciliationDailyMapper;
    this.crmContractMapper = crmContractMapper;
    this.bedMapper = bedMapper;
    this.roomMapper = roomMapper;
    this.elderMapper = elderMapper;
    this.oaApprovalMapper = oaApprovalMapper;
    this.auditLogMapper = auditLogMapper;
    this.auditLogService = auditLogService;
    this.objectMapper = objectMapper;
    this.financeMonthLockService = financeMonthLockService;
  }

  @GetMapping("/overview")
  public Result<FinanceWorkbenchOverviewResponse> overview() {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    LocalDateTime startOfToday = today.atStartOfDay();
    LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();
    String thisMonth = YearMonth.from(today).toString();

    FinanceWorkbenchOverviewResponse response = new FinanceWorkbenchOverviewResponse();
    response.setBizDate(today);

    List<PaymentRecord> todayPayments = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(orgId != null, PaymentRecord::getOrgId, orgId)
            .ge(PaymentRecord::getPaidAt, startOfToday)
            .lt(PaymentRecord::getPaidAt, endOfToday));
    List<BillMonthly> thisMonthBills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getBillMonth, thisMonth));
    List<BillMonthly> outstandingBills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .gt(BillMonthly::getOutstandingAmount, BigDecimal.ZERO));

    response.setCashier(buildCashierCard(orgId, todayPayments, startOfToday, endOfToday));
    response.setRisk(buildRiskCard(orgId, outstandingBills, today));
    response.setPending(buildPendingCard(orgId, today));
    response.setRevenueStructure(buildRevenueStructureCard(orgId, thisMonthBills));
    response.setRoomOps(buildRoomOpsCard(orgId, thisMonthBills));
    response.setAutoDebit(buildAutoDebitCard(orgId, outstandingBills, todayPayments, thisMonth));
    response.setMedicalFlow(buildMedicalFlowCard(orgId, startOfToday, endOfToday));
    response.setAllocation(buildAllocationCard(orgId, thisMonth));
    response.setReconcile(buildReconcileCard(orgId, thisMonth, thisMonthBills, todayPayments));
    response.setQuickEntries(buildQuickEntries());

    return Result.ok(response);
  }

  @GetMapping("/module-entry")
  public Result<FinanceModuleEntrySummaryResponse> moduleEntry(
      @RequestParam String moduleKey) {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    LocalDateTime startOfToday = today.atStartOfDay();
    LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();
    String thisMonth = YearMonth.from(today).toString();
    String normalizedKey = normalizeText(moduleKey, "").trim().toUpperCase(Locale.ROOT);

    List<ConsumptionRecord> monthConsumption = consumptionRecordMapper.selectList(
        Wrappers.lambdaQuery(ConsumptionRecord.class)
            .eq(ConsumptionRecord::getIsDeleted, 0)
            .eq(orgId != null, ConsumptionRecord::getOrgId, orgId)
            .ge(ConsumptionRecord::getConsumeDate, today.withDayOfMonth(1))
            .le(ConsumptionRecord::getConsumeDate, today));
    List<ConsumptionRecord> todayConsumption = monthConsumption.stream()
        .filter(item -> item.getConsumeDate() != null && item.getConsumeDate().isEqual(today))
        .toList();
    List<BillMonthly> thisMonthBills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getBillMonth, thisMonth));
    List<PaymentRecord> todayPayments = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(orgId != null, PaymentRecord::getOrgId, orgId)
            .ge(PaymentRecord::getPaidAt, startOfToday)
            .lt(PaymentRecord::getPaidAt, endOfToday));

    FinanceModuleEntrySummaryResponse response = buildModuleEntrySummary(
        normalizedKey, orgId, today, startOfToday, endOfToday, thisMonth, monthConsumption, todayConsumption, thisMonthBills,
        todayPayments);
    return Result.ok(response);
  }

  @GetMapping("/ops-insights")
  public Result<FinanceOpsInsightResponse> opsInsights() {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    String thisMonth = YearMonth.from(today).toString();

    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getBillMonth, thisMonth));
    long overdueCount = bills.stream()
        .filter(item -> safe(item.getOutstandingAmount()).compareTo(BigDecimal.ZERO) > 0)
        .count();
    BigDecimal overdueAmount = bills.stream()
        .map(item -> safe(item.getOutstandingAmount()))
        .filter(item -> item.compareTo(BigDecimal.ZERO) > 0)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    long highArrearsCount = bills.stream()
        .filter(item -> safe(item.getOutstandingAmount()).compareTo(new BigDecimal("1000")) >= 0)
        .count();

    List<ElderAccount> accounts = elderAccountMapper.selectList(
        Wrappers.lambdaQuery(ElderAccount.class)
            .eq(ElderAccount::getIsDeleted, 0)
            .eq(orgId != null, ElderAccount::getOrgId, orgId));
    long lowBalanceCount = accounts.stream()
        .filter(item -> safe(item.getBalance()).compareTo(safe(item.getWarnThreshold())) < 0)
        .count();

    Long pendingApprovalCountRaw = oaApprovalMapper.selectCount(
        Wrappers.lambdaQuery(OaApproval.class)
            .eq(OaApproval::getIsDeleted, 0)
            .eq(orgId != null, OaApproval::getOrgId, orgId)
            .eq(OaApproval::getStatus, "PENDING")
            .and(q -> q.like(OaApproval::getTitle, "费用")
                .or().like(OaApproval::getTitle, "退款")
                .or().like(OaApproval::getTitle, "冲正")
                .or().like(OaApproval::getTitle, "减免")));
    long pendingApprovalCount = pendingApprovalCountRaw == null ? 0L : pendingApprovalCountRaw;

    List<FinanceReconcileExceptionItem> reconcileIssues = buildReconcileExceptions(
        orgId, today, null);
    long reconcileIssueCount = reconcileIssues.size();

    Long disabledBillingRulesRaw = billingConfigMapper.selectCount(
        Wrappers.lambdaQuery(BillingConfigEntry.class)
            .eq(BillingConfigEntry::getIsDeleted, 0)
            .eq(orgId != null, BillingConfigEntry::getOrgId, orgId)
            .eq(BillingConfigEntry::getEffectiveMonth, thisMonth)
            .eq(BillingConfigEntry::getStatus, 0));
    long disabledBillingRules = disabledBillingRulesRaw == null ? 0L : disabledBillingRulesRaw;

    FinanceOpsInsightResponse response = new FinanceOpsInsightResponse();
    response.setGeneratedAt(LocalDateTime.now());

    List<FinanceOpsInsightResponse.Item> items = new ArrayList<>();
    if (overdueCount > 0) {
      FinanceOpsInsightResponse.Item item = new FinanceOpsInsightResponse.Item();
      item.setLevel(overdueAmount.compareTo(new BigDecimal("10000")) >= 0 ? "HIGH" : "MEDIUM");
      item.setTitle("欠费催缴优先处理");
      item.setDetail("本月欠费 " + overdueCount + " 人，累计 " + overdueAmount.setScale(2, RoundingMode.HALF_UP) + " 元");
      item.setSuggestion("先处理高欠费账单，再触发自动催缴和家属通知");
      item.setActionPath("/finance/bills/in-resident?filter=overdue");
      item.setActionLabel("查看欠费账单");
      item.setAffectedCount(overdueCount);
      items.add(item);
    }
    if (highArrearsCount > 0) {
      FinanceOpsInsightResponse.Item item = new FinanceOpsInsightResponse.Item();
      item.setLevel("HIGH");
      item.setTitle("存在高风险欠费长者");
      item.setDetail("应收超 1000 元的欠费账单 " + highArrearsCount + " 条");
      item.setSuggestion("建议运营与财务联合复核缴费计划");
      item.setActionPath("/finance/bills/auto-deduct-errors?date=today");
      item.setActionLabel("查看高风险异常");
      item.setAffectedCount(highArrearsCount);
      items.add(item);
    }
    if (lowBalanceCount > 0) {
      FinanceOpsInsightResponse.Item item = new FinanceOpsInsightResponse.Item();
      item.setLevel(lowBalanceCount >= 20 ? "HIGH" : "MEDIUM");
      item.setTitle("预存余额预警需触达");
      item.setDetail("低余额长者 " + lowBalanceCount + " 人");
      item.setSuggestion("建议批量发送充值提醒，避免自动扣费失败");
      item.setActionPath("/finance/accounts/list?filter=low_balance");
      item.setActionLabel("查看低余额账户");
      item.setAffectedCount(lowBalanceCount);
      items.add(item);
    }
    if (pendingApprovalCount > 0) {
      FinanceOpsInsightResponse.Item item = new FinanceOpsInsightResponse.Item();
      item.setLevel("MEDIUM");
      item.setTitle("审批积压影响账务闭环");
      item.setDetail("财务相关待审批 " + pendingApprovalCount + " 条");
      item.setSuggestion("优先处理退款/冲正，避免跨期对账异常");
      item.setActionPath("/oa/approval?module=finance&status=pending");
      item.setActionLabel("前往审批中心");
      item.setAffectedCount(pendingApprovalCount);
      items.add(item);
    }
    if (reconcileIssueCount > 0) {
      FinanceOpsInsightResponse.Item item = new FinanceOpsInsightResponse.Item();
      item.setLevel("HIGH");
      item.setTitle("对账异常待处理");
      item.setDetail("今日对账异常 " + reconcileIssueCount + " 条");
      item.setSuggestion("建议先核销收款和账单映射，再处理发票关联");
      item.setActionPath("/finance/reconcile/exception");
      item.setActionLabel("处理对账异常");
      item.setAffectedCount(reconcileIssueCount);
      items.add(item);
    }
    if (disabledBillingRules > 0) {
      FinanceOpsInsightResponse.Item item = new FinanceOpsInsightResponse.Item();
      item.setLevel("LOW");
      item.setTitle("存在停用配置项");
      item.setDetail("本月停用计费配置 " + disabledBillingRules + " 条");
      item.setSuggestion("请确认是否为临时停用，避免影响自动计费");
      item.setActionPath("/finance/bills/rules");
      item.setActionLabel("查看计费规则");
      item.setAffectedCount(disabledBillingRules);
      items.add(item);
    }
    if (items.isEmpty()) {
      FinanceOpsInsightResponse.Item item = new FinanceOpsInsightResponse.Item();
      item.setLevel("LOW");
      item.setTitle("当前财务运行平稳");
      item.setDetail("未识别到高优先级风险");
      item.setSuggestion("建议每日复核收款、对账、分摊三类任务");
      item.setActionPath("/finance/reconcile/center");
      item.setActionLabel("进入对账中心");
      item.setAffectedCount(0L);
      items.add(item);
    }
    items.sort((a, b) -> insightLevelWeight(b.getLevel()) - insightLevelWeight(a.getLevel()));
    response.setItems(items);
    response.setTotalInsights(items.size());
    response.setHighPriorityCount((int) items.stream()
        .filter(item -> "HIGH".equalsIgnoreCase(item.getLevel()))
        .count());
    return Result.ok(response);
  }

  @GetMapping("/invoice/page")
  public Result<IPage<FinanceInvoiceReceiptItem>> invoicePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String date,
      @RequestParam(required = false) String method,
      @RequestParam(required = false) String invoiceStatus,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LocalDate targetDate = (date == null || date.isBlank() || "today".equalsIgnoreCase(date))
        ? LocalDate.now()
        : LocalDate.parse(date);
    List<FinanceInvoiceReceiptItem> allRows = queryInvoiceReceiptItems(orgId, targetDate, method, invoiceStatus, keyword);
    int fromIndex = (int) Math.max((pageNo - 1) * pageSize, 0);
    int toIndex = (int) Math.min(fromIndex + pageSize, allRows.size());
    List<FinanceInvoiceReceiptItem> pageRows = fromIndex >= allRows.size() ? List.of() : allRows.subList(fromIndex, toIndex);
    IPage<FinanceInvoiceReceiptItem> result = new Page<>(pageNo, pageSize, allRows.size());
    result.setRecords(pageRows);
    return Result.ok(result);
  }

  @GetMapping(value = "/invoice/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportInvoice(
      @RequestParam(required = false) String date,
      @RequestParam(required = false) String method,
      @RequestParam(required = false) String invoiceStatus,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LocalDate targetDate = (date == null || date.isBlank() || "today".equalsIgnoreCase(date))
        ? LocalDate.now()
        : LocalDate.parse(date);
    List<FinanceInvoiceReceiptItem> rows = queryInvoiceReceiptItems(orgId, targetDate, method, invoiceStatus, keyword);
    List<String> headers = List.of("收款时间", "长者", "金额", "支付方式", "收据号", "发票关联", "备注");
    List<List<String>> body = rows.stream()
        .map(item -> List.of(
            stringOf(item.getPaidAt()),
            stringOf(item.getElderName()),
            stringOf(item.getAmount()),
            stringOf(item.getPayMethodLabel()),
            stringOf(item.getReceiptNo()),
            stringOf(item.getInvoiceStatusLabel()),
            stringOf(item.getRemark())))
        .toList();
    return csvResponse("finance-invoice-receipt", headers, body);
  }

  @GetMapping("/payment-adjustments/page")
  public Result<IPage<FinancePaymentAdjustmentItem>> paymentAdjustmentsPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String month) {
    Long orgId = AuthContext.getOrgId();
    YearMonth targetMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
    List<FinancePaymentAdjustmentItem> rows = queryPaymentAdjustmentItems(orgId, targetMonth);
    int fromIndex = (int) Math.max((pageNo - 1) * pageSize, 0);
    int toIndex = (int) Math.min(fromIndex + pageSize, rows.size());
    List<FinancePaymentAdjustmentItem> pageRows = fromIndex >= rows.size() ? List.of() : rows.subList(fromIndex, toIndex);
    IPage<FinancePaymentAdjustmentItem> result = new Page<>(pageNo, pageSize, rows.size());
    result.setRecords(pageRows);
    return Result.ok(result);
  }

  @GetMapping(value = "/payment-adjustments/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportPaymentAdjustments(
      @RequestParam(required = false) String month) {
    Long orgId = AuthContext.getOrgId();
    YearMonth targetMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
    List<FinancePaymentAdjustmentItem> rows = queryPaymentAdjustmentItems(orgId, targetMonth);
    List<String> headers = List.of(
        "发生时间", "类型", "长者", "金额", "说明", "备注", "审批状态", "审批单", "账单ID", "收款ID");
    List<List<String>> body = rows.stream()
        .map(item -> List.of(
            stringOf(item.getOccurredAt()),
            stringOf(item.getTypeLabel()),
            stringOf(item.getElderName()),
            stringOf(item.getAmount()),
            stringOf(item.getDetail()),
            stringOf(item.getRemark()),
            stringOf(item.getApprovalStatusLabel()),
            stringOf(item.getApprovalTitle()),
            stringOf(item.getBillId()),
            stringOf(item.getPaymentId())))
        .toList();
    return csvResponse("finance-payment-adjustments", headers, body);
  }

  @GetMapping("/issue-center/page")
  public Result<IPage<FinanceIssueCenterItem>> issueCenterPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String date,
      @RequestParam(required = false) String riskLevel,
      @RequestParam(required = false) String sourceModule,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LocalDate targetDate = (date == null || date.isBlank() || "today".equalsIgnoreCase(date))
        ? LocalDate.now()
        : LocalDate.parse(date);
    List<FinanceIssueCenterItem> rows = buildIssueCenterItems(orgId, targetDate).stream()
        .filter(item -> matchesIssueCenter(item, riskLevel, sourceModule, keyword))
        .sorted(Comparator.comparing(
            FinanceIssueCenterItem::getOccurredAt,
            Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
        .toList();
    int fromIndex = (int) Math.max((pageNo - 1) * pageSize, 0);
    int toIndex = (int) Math.min(fromIndex + pageSize, rows.size());
    List<FinanceIssueCenterItem> pageRows = fromIndex >= rows.size() ? List.of() : rows.subList(fromIndex, toIndex);
    IPage<FinanceIssueCenterItem> result = new Page<>(pageNo, pageSize, rows.size());
    result.setRecords(pageRows);
    return Result.ok(result);
  }

  @GetMapping(value = "/issue-center/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportIssueCenter(
      @RequestParam(required = false) String date,
      @RequestParam(required = false) String riskLevel,
      @RequestParam(required = false) String sourceModule,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LocalDate targetDate = (date == null || date.isBlank() || "today".equalsIgnoreCase(date))
        ? LocalDate.now()
        : LocalDate.parse(date);
    List<FinanceIssueCenterItem> rows = buildIssueCenterItems(orgId, targetDate).stream()
        .filter(item -> matchesIssueCenter(item, riskLevel, sourceModule, keyword))
        .sorted(Comparator.comparing(
            FinanceIssueCenterItem::getOccurredAt,
            Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
        .toList();
    List<String> headers = List.of(
        "发生时间", "来源模块", "异常类型", "风险等级", "长者", "涉及金额", "异常描述", "处理建议",
        "处理状态", "责任人", "处理时限", "复核结论", "最近处理时间", "最近处理备注", "账单ID", "收款ID");
    List<List<String>> body = rows.stream()
        .map(item -> List.of(
            stringOf(item.getOccurredAt()),
            stringOf(item.getSourceModuleLabel()),
            stringOf(item.getIssueTypeLabel()),
            stringOf(item.getRiskLevelLabel()),
            stringOf(item.getElderName()),
            stringOf(item.getAmount()),
            stringOf(item.getDetail()),
            stringOf(item.getSuggestion()),
            stringOf(item.getLatestHandleStatusLabel()),
            stringOf(item.getLatestOwnerName()),
            stringOf(item.getLatestDueDate()),
            stringOf(item.getLatestReviewResult()),
            stringOf(item.getLatestHandleAt()),
            stringOf(item.getLatestHandleRemark()),
            stringOf(item.getBillId()),
            stringOf(item.getPaymentId())))
        .toList();
    return csvResponse("finance-issue-center", headers, body);
  }

  @GetMapping("/collection-follow-up/page")
  public Result<IPage<FinanceCollectionFollowUpItem>> collectionFollowUpPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String month,
      @RequestParam(required = false) String riskLevel,
      @RequestParam(required = false) String stage,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    YearMonth targetMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
    List<FinanceCollectionFollowUpItem> rows = buildCollectionFollowUpItems(orgId, targetMonth).stream()
        .filter(item -> matchesCollectionFollowUp(item, riskLevel, stage, keyword))
        .sorted(Comparator
            .comparing(FinanceCollectionFollowUpItem::getOutstandingAmount, Comparator.nullsLast(BigDecimal::compareTo))
            .reversed()
            .thenComparing(FinanceCollectionFollowUpItem::getOverdueMonths, Comparator.nullsLast(Integer::compareTo).reversed()))
        .toList();
    int fromIndex = (int) Math.max((pageNo - 1) * pageSize, 0);
    int toIndex = (int) Math.min(fromIndex + pageSize, rows.size());
    List<FinanceCollectionFollowUpItem> pageRows = fromIndex >= rows.size() ? List.of() : rows.subList(fromIndex, toIndex);
    IPage<FinanceCollectionFollowUpItem> result = new Page<>(pageNo, pageSize, rows.size());
    result.setRecords(pageRows);
    return Result.ok(result);
  }

  @GetMapping(value = "/collection-follow-up/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportCollectionFollowUp(
      @RequestParam(required = false) String month,
      @RequestParam(required = false) String riskLevel,
      @RequestParam(required = false) String stage,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    YearMonth targetMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
    List<FinanceCollectionFollowUpItem> rows = buildCollectionFollowUpItems(orgId, targetMonth).stream()
        .filter(item -> matchesCollectionFollowUp(item, riskLevel, stage, keyword))
        .sorted(Comparator
            .comparing(FinanceCollectionFollowUpItem::getOutstandingAmount, Comparator.nullsLast(BigDecimal::compareTo))
            .reversed()
            .thenComparing(FinanceCollectionFollowUpItem::getOverdueMonths, Comparator.nullsLast(Integer::compareTo).reversed()))
        .toList();
    List<String> headers = List.of(
        "长者", "起始账期", "最近账期", "欠费金额", "账户余额", "拖欠月数", "风险等级", "跟进阶段",
        "承诺付款日", "责任人", "联系人", "联系渠道", "联系结果", "下次提醒", "最近跟进", "最近跟进时间", "跟进理由", "建议动作", "主账单ID");
    List<List<String>> body = rows.stream()
        .map(item -> List.of(
            stringOf(item.getElderName()),
            stringOf(item.getOldestBillMonth()),
            stringOf(item.getLatestBillMonth()),
            stringOf(item.getOutstandingAmount()),
            stringOf(item.getBalance()),
            stringOf(item.getOverdueMonths()),
            stringOf(item.getRiskLevelLabel()),
            stringOf(item.getStageLabel()),
            stringOf(item.getPromisedDate()),
            stringOf(item.getLatestOwnerName()),
            stringOf(item.getLatestContactName()),
            stringOf(item.getLatestContactChannel()),
            stringOf(item.getLatestContactResult()),
            stringOf(item.getNextReminderAt()),
            stringOf(item.getLatestHandleStatusLabel()),
            stringOf(item.getLatestHandleAt()),
            stringOf(item.getFollowUpReason()),
            stringOf(item.getSuggestion()),
            stringOf(item.getPrimaryBillId())))
        .toList();
    return csvResponse("finance-collection-follow-up", headers, body);
  }

  @GetMapping("/month-close/summary")
  public Result<FinanceMonthCloseSummaryResponse> monthCloseSummary(
      @RequestParam(required = false) String month) {
    Long orgId = AuthContext.getOrgId();
    YearMonth targetMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
    return Result.ok(buildMonthCloseSummary(orgId, targetMonth));
  }

  @GetMapping("/month-close/status")
  public Result<FinanceMonthLockStatusResponse> monthCloseStatus(
      @RequestParam(required = false) String month) {
    Long orgId = AuthContext.getOrgId();
    YearMonth targetMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
    return Result.ok(financeMonthLockService.getMonthLockStatus(orgId, targetMonth));
  }

  @GetMapping(value = "/month-close/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportMonthCloseSummary(
      @RequestParam(required = false) String month) {
    Long orgId = AuthContext.getOrgId();
    YearMonth targetMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
    FinanceMonthCloseSummaryResponse summary = buildMonthCloseSummary(orgId, targetMonth);
    List<String> headers = List.of(
        "月份", "关账状态", "锁账状态", "关账人", "关账时间", "反锁人", "反锁时间", "反锁原因", "步骤", "步骤状态", "当前数量", "说明", "动作路径");
    List<List<String>> body = summary.getSteps().stream()
        .map(step -> List.of(
            stringOf(summary.getMonth()),
            stringOf(summary.getCloseStatusLabel()),
            stringOf(summary.getLockStatusLabel()),
            stringOf(summary.getClosedBy()),
            stringOf(summary.getClosedAt()),
            stringOf(summary.getUnlockedBy()),
            stringOf(summary.getUnlockedAt()),
            stringOf(summary.getUnlockReason()),
            stringOf(step.getLabel()),
            stringOf(step.getStatusLabel()),
            stringOf(step.getCount()),
            stringOf(step.getDetail()),
            stringOf(step.getActionPath())))
        .toList();
    return csvResponse("finance-month-close", headers, body);
  }

  @PostMapping("/issue-center/handle")
  public Result<FinanceHandleLogItem> handleIssueCenterAction(
      @RequestBody FinanceHandleActionRequest request) {
    Long orgId = AuthContext.getOrgId();
    FinanceHandleLogItem item = recordHandleAction(
        orgId,
        "FINANCE_ISSUE_HANDLE",
        request == null ? new FinanceHandleActionRequest() : request,
        resolveAuditEntityId(request == null ? null : request.getBillId(),
            request == null ? null : request.getPaymentId(),
            request == null ? null : request.getElderId()),
        "UPDATED");
    return Result.ok(item);
  }

  @GetMapping("/issue-center/handle-logs")
  public Result<List<FinanceHandleLogItem>> issueCenterHandleLogs(
      @RequestParam(required = false) Long billId,
      @RequestParam(required = false) Long paymentId,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String sourceModule,
      @RequestParam(defaultValue = "20") int limit) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(queryHandleLogs(orgId, "FINANCE_ISSUE_HANDLE", billId, paymentId, elderId, sourceModule, limit));
  }

  @PostMapping("/collection-follow-up/handle")
  public Result<FinanceHandleLogItem> handleCollectionFollowUp(
      @RequestBody FinanceHandleActionRequest request) {
    Long orgId = AuthContext.getOrgId();
    FinanceHandleLogItem item = recordHandleAction(
        orgId,
        "FINANCE_COLLECTION_HANDLE",
        request == null ? new FinanceHandleActionRequest() : request,
        resolveAuditEntityId(null, null, request == null ? null : request.getElderId()),
        "FOLLOWED");
    return Result.ok(item);
  }

  @GetMapping("/collection-follow-up/logs")
  public Result<List<FinanceHandleLogItem>> collectionFollowUpLogs(
      @RequestParam(required = false) Long elderId,
      @RequestParam(defaultValue = "20") int limit) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(queryHandleLogs(orgId, "FINANCE_COLLECTION_HANDLE", null, null, elderId, null, limit));
  }

  @PostMapping("/month-close/execute")
  public Result<FinanceMonthCloseExecuteResponse> executeMonthClose(
      @RequestBody(required = false) FinanceMonthCloseExecuteRequest request) {
    Long orgId = AuthContext.getOrgId();
    FinanceMonthCloseExecuteRequest payload = request == null ? new FinanceMonthCloseExecuteRequest() : request;
    YearMonth targetMonth = payload.getMonth() == null || payload.getMonth().isBlank() ? YearMonth.now() : YearMonth.parse(payload.getMonth());
    FinanceMonthCloseSummaryResponse summary = buildMonthCloseSummary(orgId, targetMonth);
    boolean blocked = (summary.getBlockedSteps() == null ? 0 : summary.getBlockedSteps()) > 0;
    FinanceMonthCloseExecuteResponse response = new FinanceMonthCloseExecuteResponse();
    response.setMonth(targetMonth.toString());
    response.setActorName(AuthContext.getUsername());
    response.setOccurredAt(LocalDateTime.now());
    if (blocked && !Boolean.TRUE.equals(payload.getForceClose())) {
      response.setSuccess(false);
      response.setStatus("BLOCKED");
      response.setStatusLabel("阻塞");
      response.setMessage("当前仍有阻塞步骤，未执行关账。可修复后重试，或使用强制关账。");
      return Result.ok(response);
    }
    Map<String, Object> detail = new LinkedHashMap<>();
    detail.put("month", targetMonth.toString());
    detail.put("status", blocked ? "FORCED_CLOSED" : "CLOSED");
    detail.put("remark", normalizeText(payload.getRemark(), ""));
    detail.put("blockedSteps", summary.getBlockedSteps());
    detail.put("warningSteps", summary.getWarningSteps());
    detail.put("completedSteps", summary.getCompletedSteps());
    detail.put("issueCount", summary.getIssueCount());
    writeAuditLog(orgId, "MONTH_CLOSE_EXECUTE", "FINANCE_MONTH_CLOSE", monthEntityId(targetMonth), detail);
    response.setSuccess(true);
    response.setStatus(blocked ? "FORCED_CLOSED" : "CLOSED");
    response.setStatusLabel(blocked ? "强制关账" : "已关账");
    response.setMessage(blocked ? "已按强制关账执行，并记录了阻塞项。" : "本月已记录为正式关账。");
    return Result.ok(response);
  }

  @PostMapping("/month-close/unlock")
  @PreAuthorize("hasAnyRole('FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<FinanceMonthLockStatusResponse> unlockMonthClose(
      @RequestBody(required = false) FinanceMonthUnlockRequest request) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(financeMonthLockService.unlockMonth(orgId, request == null ? new FinanceMonthUnlockRequest() : request));
  }

  @PostMapping("/month-close/cross-period/request")
  public Result<FinanceCrossPeriodApprovalResponse> requestCrossPeriodApproval(
      @RequestBody FinanceCrossPeriodApprovalRequest request) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(financeMonthLockService.requestCrossPeriodApproval(orgId, request));
  }

  @PostMapping("/payment-adjustments/approval/request")
  public Result<FinancePaymentAdjustmentItem> requestPaymentAdjustmentApproval(
      @RequestBody FinancePaymentAdjustmentApprovalRequest request) {
    Long orgId = AuthContext.getOrgId();
    if (request == null) {
      throw new IllegalArgumentException("审批请求不能为空");
    }
    OaApproval existing = oaApprovalMapper.selectList(
            Wrappers.lambdaQuery(OaApproval.class)
                .eq(OaApproval::getIsDeleted, 0)
                .eq(orgId != null, OaApproval::getOrgId, orgId)
                .eq(OaApproval::getApprovalType, "REIMBURSE")
                .orderByDesc(OaApproval::getCreateTime))
        .stream()
        .filter(item -> paymentAdjustmentApprovalKey(item)
            .equals(paymentAdjustmentApprovalKey(request.getType(), request.getBillId(), request.getPaymentId())))
        .filter(item -> "PENDING".equalsIgnoreCase(item.getStatus()))
        .findFirst()
        .orElse(null);
    if (existing != null) {
      FinancePaymentAdjustmentItem item = new FinancePaymentAdjustmentItem();
      item.setType(normalizeText(request.getType(), "PAYMENT_ADJUSTMENT"));
      item.setBillId(request.getBillId());
      item.setPaymentId(request.getPaymentId());
      item.setElderId(request.getElderId());
      item.setElderName(request.getElderName());
      item.setAmount(safe(request.getAmount()));
      fillPaymentAdjustmentApproval(item, existing);
      item.setOccurredAt(existing.getCreateTime());
      return Result.ok(item);
    }
    OaApproval approval = new OaApproval();
    approval.setTenantId(orgId);
    approval.setOrgId(orgId);
    approval.setApprovalType("REIMBURSE");
    approval.setTitle(buildAdjustmentApprovalTitle(request));
    approval.setApplicantId(AuthContext.getStaffId());
    approval.setApplicantName(AuthContext.getUsername());
    approval.setAmount(safe(request.getAmount()));
    approval.setStatus("PENDING");
    approval.setRemark(request.getRemark());
    Map<String, Object> form = new LinkedHashMap<>();
    form.put("financeSource", "PAYMENT_ADJUSTMENT");
    form.put("type", request.getType());
    form.put("billId", request.getBillId());
    form.put("paymentId", request.getPaymentId());
    form.put("elderId", request.getElderId());
    form.put("elderName", request.getElderName());
    form.put("amount", safe(request.getAmount()));
    form.put("reason", normalizeText(request.getReason(), ""));
    form.put("remark", normalizeText(request.getRemark(), ""));
    approval.setFormData(toJson(form));
    approval.setCreatedBy(AuthContext.getStaffId());
    oaApprovalMapper.insert(approval);

    FinancePaymentAdjustmentItem item = new FinancePaymentAdjustmentItem();
    item.setType(normalizeText(request.getType(), "PAYMENT_ADJUSTMENT"));
    item.setBillId(request.getBillId());
    item.setPaymentId(request.getPaymentId());
    item.setElderId(request.getElderId());
    item.setElderName(request.getElderName());
    item.setAmount(safe(request.getAmount()));
    item.setApprovalId(approval.getId());
    item.setApprovalStatus(approval.getStatus());
    item.setApprovalStatusLabel(approvalStatusLabel(approval.getStatus()));
    item.setApprovalTitle(approval.getTitle());
    item.setOccurredAt(approval.getCreateTime());
    return Result.ok(item);
  }

  @GetMapping("/search")
  public Result<FinanceSearchResponse> financeSearch(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "8") int limit) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(buildFinanceSearchResponse(orgId, keyword, Math.min(Math.max(limit, 1), 20)));
  }

  @GetMapping(value = "/search/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportFinanceSearch(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "20") int limit) {
    Long orgId = AuthContext.getOrgId();
    FinanceSearchResponse response = buildFinanceSearchResponse(orgId, keyword, Math.min(Math.max(limit, 1), 50));
    List<String> headers = List.of("分类", "ID", "标题", "副标题", "补充信息", "打开路径", "更新时间");
    List<List<String>> body = flattenSearchRows(response).stream()
        .map(item -> List.of(
            stringOf(searchTypeLabel(item.getType())),
            stringOf(item.getId()),
            stringOf(item.getTitle()),
            stringOf(item.getSubtitle()),
            stringOf(item.getExtra()),
            stringOf(item.getActionPath()),
            stringOf(item.getTime())))
        .toList();
    return csvResponse("finance-search", headers, body);
  }

  @GetMapping("/auto-deduct/exceptions")
  public Result<List<FinanceAutoDebitExceptionItem>> autoDebitExceptions(
      @RequestParam(required = false) String date) {
    Long orgId = AuthContext.getOrgId();
    LocalDate targetDate = (date == null || date.isBlank() || "today".equalsIgnoreCase(date))
        ? LocalDate.now()
        : LocalDate.parse(date);
    String targetMonth = YearMonth.from(targetDate).toString();

    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .gt(BillMonthly::getOutstandingAmount, BigDecimal.ZERO)
            .le(BillMonthly::getBillMonth, targetMonth)
            .orderByDesc(BillMonthly::getOutstandingAmount));
    if (bills.isEmpty()) {
      return Result.ok(List.of());
    }
    List<Long> elderIds = bills.stream().map(BillMonthly::getElderId).filter(Objects::nonNull).distinct().toList();
    Map<Long, ElderAccount> accountMap = elderAccountMapper.selectList(
        Wrappers.lambdaQuery(ElderAccount.class)
            .eq(ElderAccount::getIsDeleted, 0)
            .eq(orgId != null, ElderAccount::getOrgId, orgId)
            .in(!elderIds.isEmpty(), ElderAccount::getElderId, elderIds))
        .stream()
        .collect(Collectors.toMap(ElderAccount::getElderId, Function.identity(), (a, b) -> a));
    Map<Long, ElderProfile> elderMap = elderMapper.selectList(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(orgId != null, ElderProfile::getOrgId, orgId)
            .in(!elderIds.isEmpty(), ElderProfile::getId, elderIds))
        .stream()
        .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (a, b) -> a));

    List<FinanceAutoDebitExceptionItem> rows = new ArrayList<>();
    for (BillMonthly bill : bills) {
      ElderAccount account = accountMap.get(bill.getElderId());
      ElderProfile elder = elderMap.get(bill.getElderId());
      BigDecimal balance = account == null ? BigDecimal.ZERO : safe(account.getBalance());
      BigDecimal outstanding = safe(bill.getOutstandingAmount());
      FinanceAutoDebitExceptionItem item = new FinanceAutoDebitExceptionItem();
      item.setBillId(bill.getId());
      item.setElderId(bill.getElderId());
      item.setElderName(elder == null ? null : elder.getFullName());
      item.setBillMonth(bill.getBillMonth());
      item.setOutstandingAmount(outstanding);
      item.setBalance(balance);
      if (balance.compareTo(outstanding) < 0) {
        item.setReasonCode("INSUFFICIENT_BALANCE");
        item.setReasonLabel("余额不足");
        item.setSuggestion("请先充值或转人工催缴");
      } else if (elder != null && elder.getStatus() != null && elder.getStatus() != 1) {
        item.setReasonCode("STATUS_RESTRICTED");
        item.setReasonLabel("状态限制");
        item.setSuggestion("长者非在住状态，请确认扣费规则");
      } else {
        item.setReasonCode("RULE_MISSING");
        item.setReasonLabel("规则缺失");
        item.setSuggestion("检查计费规则与自动扣费任务配置");
      }
      rows.add(item);
    }
    return Result.ok(rows);
  }

  @GetMapping(value = "/auto-deduct/exceptions/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportAutoDebitExceptions(
      @RequestParam(required = false) String date) {
    List<FinanceAutoDebitExceptionItem> rows = autoDebitExceptions(date).getData();
    List<String> headers = List.of("账单月", "长者", "应扣金额", "账户余额", "失败原因", "处理建议", "账单ID", "长者ID");
    List<List<String>> body = rows.stream()
        .map(item -> List.of(
            stringOf(item.getBillMonth()),
            stringOf(item.getElderName()),
            stringOf(item.getOutstandingAmount()),
            stringOf(item.getBalance()),
            stringOf(item.getReasonLabel()),
            stringOf(item.getSuggestion()),
            stringOf(item.getBillId()),
            stringOf(item.getElderId())))
        .toList();
    return csvResponse("finance-auto-deduct-exceptions", headers, body);
  }

  @GetMapping("/room-ops/detail")
  public Result<FinanceRoomOpsDetailResponse> roomOpsDetail(
      @RequestParam(required = false) String period,
      @RequestParam(required = false) String building,
      @RequestParam(required = false) String room) {
    Long orgId = AuthContext.getOrgId();
    String month = (period == null || period.isBlank() || "this_month".equalsIgnoreCase(period))
        ? YearMonth.now().toString()
        : period;
    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getBillMonth, month));
    List<Bed> beds = bedMapper.selectList(
        Wrappers.lambdaQuery(Bed.class)
            .eq(Bed::getIsDeleted, 0)
            .eq(orgId != null, Bed::getOrgId, orgId));
    List<Room> rooms = roomMapper.selectList(
        Wrappers.lambdaQuery(Room.class)
            .eq(Room::getIsDeleted, 0)
            .eq(orgId != null, Room::getOrgId, orgId));
    Map<Long, Room> roomMap = rooms.stream()
        .collect(Collectors.toMap(Room::getId, Function.identity(), (a, b) -> a));
    Map<Long, Bed> elderBedMap = beds.stream()
        .filter(item -> item.getElderId() != null)
        .collect(Collectors.toMap(Bed::getElderId, Function.identity(), (a, b) -> a));
    Map<Long, BigDecimal> roomIncomeMap = new HashMap<>();
    for (BillMonthly billMonthly : bills) {
      Bed bed = elderBedMap.get(billMonthly.getElderId());
      if (bed == null || bed.getRoomId() == null) continue;
      roomIncomeMap.merge(bed.getRoomId(), safe(billMonthly.getTotalAmount()), BigDecimal::add);
    }
    Map<Long, Integer> roomOccupied = new HashMap<>();
    Map<Long, Integer> roomTotal = new HashMap<>();
    for (Bed bed : beds) {
      if (bed.getRoomId() == null) continue;
      roomTotal.merge(bed.getRoomId(), 1, Integer::sum);
      if (bed.getElderId() != null) roomOccupied.merge(bed.getRoomId(), 1, Integer::sum);
    }

    FinanceRoomOpsDetailResponse response = new FinanceRoomOpsDetailResponse();
    response.setPeriod(month);
    response.setBuilding(normalizeText(building, ""));
    response.setRoom(normalizeText(room, ""));
    response.setTotalIncome(BigDecimal.ZERO);
    response.setTotalCost(BigDecimal.ZERO);
    response.setTotalNetAmount(BigDecimal.ZERO);
    response.setTotalOccupiedBeds(0);
    response.setTotalEmptyBeds(0);

    List<FinanceRoomOpsDetailResponse.Row> rows = new ArrayList<>();
    for (Room roomEntity : rooms) {
      if (building != null && !building.isBlank() && !Objects.equals(roomEntity.getBuilding(), building)) continue;
      if (room != null && !room.isBlank() && !Objects.equals(roomEntity.getRoomNo(), room)) continue;
      Long roomId = roomEntity.getId();
      BigDecimal income = roomIncomeMap.getOrDefault(roomId, BigDecimal.ZERO);
      int occupied = roomOccupied.getOrDefault(roomId, 0);
      int totalBeds = roomTotal.getOrDefault(roomId, roomEntity.getCapacity() == null ? 0 : roomEntity.getCapacity());
      int empty = Math.max(totalBeds - occupied, 0);

      FinanceRoomOpsDetailResponse.Row rowItem = new FinanceRoomOpsDetailResponse.Row();
      rowItem.setBuilding(normalizeText(roomEntity.getBuilding(), "未标注楼栋"));
      rowItem.setFloorNo(normalizeText(roomEntity.getFloorNo(), "未标注楼层"));
      rowItem.setRoomNo(normalizeText(roomEntity.getRoomNo(), "未知房间"));
      rowItem.setIncome(income);
      rowItem.setCost(BigDecimal.ZERO);
      rowItem.setNetAmount(income);
      rowItem.setOccupiedBeds(occupied);
      rowItem.setEmptyBeds(empty);
      rows.add(rowItem);

      response.setTotalIncome(response.getTotalIncome().add(income));
      response.setTotalNetAmount(response.getTotalNetAmount().add(income));
      response.setTotalOccupiedBeds(response.getTotalOccupiedBeds() + occupied);
      response.setTotalEmptyBeds(response.getTotalEmptyBeds() + empty);
    }
    rows.sort(Comparator.comparing(FinanceRoomOpsDetailResponse.Row::getNetAmount).reversed());
    response.setRows(rows);
    return Result.ok(response);
  }

  @GetMapping("/allocation/rules")
  public Result<List<FinanceAllocationRuleItem>> allocationRules(
      @RequestParam(required = false) String month) {
    Long orgId = AuthContext.getOrgId();
    String targetMonth = (month == null || month.isBlank())
        ? YearMonth.now().toString()
        : month;
    List<BillingConfigEntry> entries = billingConfigMapper.selectList(
        Wrappers.lambdaQuery(BillingConfigEntry.class)
            .eq(BillingConfigEntry::getIsDeleted, 0)
            .eq(orgId != null, BillingConfigEntry::getOrgId, orgId)
            .eq(BillingConfigEntry::getEffectiveMonth, targetMonth)
            .orderByAsc(BillingConfigEntry::getConfigKey));
    List<FinanceAllocationRuleItem> rows = entries.stream()
        .filter(item -> isAllocationOrUtilityRule(item.getConfigKey()))
        .map(this::toAllocationRuleItem)
        .toList();
    return Result.ok(rows);
  }

  @GetMapping("/allocation/resident-options")
  public Result<FinanceAllocationResidentSyncResponse> allocationResidentOptions(
      @RequestParam(required = false) String building,
      @RequestParam(required = false) String floorNo,
      @RequestParam(required = false) String roomNo) {
    Long orgId = AuthContext.getOrgId();
    String selectedBuilding = normalizeText(building, "");
    String selectedFloorNo = normalizeText(floorNo, "");
    String selectedRoomNo = normalizeText(roomNo, "");

    List<Room> allRooms = roomMapper.selectList(
        Wrappers.lambdaQuery(Room.class)
            .eq(Room::getIsDeleted, 0)
            .eq(orgId != null, Room::getOrgId, orgId));
    FinanceAllocationResidentSyncResponse response = new FinanceAllocationResidentSyncResponse();
    response.setSyncTime(LocalDateTime.now());
    response.setSelectedBuilding(selectedBuilding);
    response.setSelectedFloorNo(selectedFloorNo);
    response.setSelectedRoomNo(selectedRoomNo);

    response.setBuildingOptions(allRooms.stream()
        .map(item -> normalizeText(item.getBuilding(), ""))
        .filter(item -> !item.isBlank())
        .distinct()
        .sorted()
        .toList());

    List<Room> buildingRooms = allRooms.stream()
        .filter(item -> selectedBuilding.isBlank() || selectedBuilding.equals(normalizeText(item.getBuilding(), "")))
        .toList();
    response.setFloorOptions(buildingRooms.stream()
        .map(item -> normalizeText(item.getFloorNo(), ""))
        .filter(item -> !item.isBlank())
        .distinct()
        .sorted()
        .toList());

    List<Room> matchedRooms = buildingRooms.stream()
        .filter(item -> selectedFloorNo.isBlank() || selectedFloorNo.equals(normalizeText(item.getFloorNo(), "")))
        .filter(item -> selectedRoomNo.isBlank() || selectedRoomNo.equals(normalizeText(item.getRoomNo(), "")))
        .toList();
    response.setRoomOptions(buildingRooms.stream()
        .filter(item -> selectedFloorNo.isBlank() || selectedFloorNo.equals(normalizeText(item.getFloorNo(), "")))
        .map(item -> normalizeText(item.getRoomNo(), ""))
        .filter(item -> !item.isBlank())
        .distinct()
        .sorted()
        .toList());

    List<Long> roomIds = matchedRooms.stream().map(Room::getId).filter(Objects::nonNull).distinct().toList();
    if (roomIds.isEmpty()) {
      return Result.ok(response);
    }

    Map<Long, Room> roomMap = matchedRooms.stream()
        .collect(Collectors.toMap(Room::getId, Function.identity(), (a, b) -> a));
    List<Bed> beds = bedMapper.selectList(
        Wrappers.lambdaQuery(Bed.class)
            .eq(Bed::getIsDeleted, 0)
            .eq(orgId != null, Bed::getOrgId, orgId)
            .in(Bed::getRoomId, roomIds)
            .isNotNull(Bed::getElderId));
    List<Long> elderIds = beds.stream()
        .map(Bed::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    if (elderIds.isEmpty()) {
      return Result.ok(response);
    }
    Map<Long, ElderProfile> elderMap = elderMapper.selectList(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(orgId != null, ElderProfile::getOrgId, orgId)
            .in(ElderProfile::getId, elderIds)
            .in(ElderProfile::getStatus, List.of(1, 2)))
        .stream()
        .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (a, b) -> a));

    for (Bed bedEntity : beds) {
      ElderProfile elder = elderMap.get(bedEntity.getElderId());
      if (elder == null) {
        continue;
      }
      Room roomEntity = roomMap.get(bedEntity.getRoomId());
      FinanceAllocationResidentSyncResponse.ResidentOption row = new FinanceAllocationResidentSyncResponse.ResidentOption();
      row.setElderId(elder.getId());
      row.setElderName(normalizeText(elder.getFullName(), "未知老人"));
      row.setElderStatus(elder.getStatus());
      row.setBedId(bedEntity.getId());
      row.setBedNo(normalizeText(bedEntity.getBedNo(), ""));
      if (roomEntity != null) {
        row.setRoomId(roomEntity.getId());
        row.setRoomNo(normalizeText(roomEntity.getRoomNo(), ""));
        row.setBuilding(normalizeText(roomEntity.getBuilding(), ""));
        row.setFloorNo(normalizeText(roomEntity.getFloorNo(), ""));
      }
      response.getResidentOptions().add(row);
    }
    response.getResidentOptions().sort(Comparator
        .comparing(FinanceAllocationResidentSyncResponse.ResidentOption::getBuilding, Comparator.nullsLast(String::compareTo))
        .thenComparing(FinanceAllocationResidentSyncResponse.ResidentOption::getFloorNo, Comparator.nullsLast(String::compareTo))
        .thenComparing(FinanceAllocationResidentSyncResponse.ResidentOption::getRoomNo, Comparator.nullsLast(String::compareTo))
        .thenComparing(FinanceAllocationResidentSyncResponse.ResidentOption::getBedNo, Comparator.nullsLast(String::compareTo)));
    return Result.ok(response);
  }

  @PostMapping("/allocation/rules/template/init")
  public Result<FinanceAllocationTemplateInitResponse> initAllocationRuleTemplate(
      @RequestParam(required = false) String month) {
    String targetMonth = (month == null || month.isBlank())
        ? YearMonth.now().toString()
        : month;
    FinanceAllocationTemplateInitResponse response = new FinanceAllocationTemplateInitResponse();
    response.setMonth(targetMonth);
    List<FinanceBillingConfigUpsertRequest> templates = defaultAllocationTemplates(targetMonth);
    for (FinanceBillingConfigUpsertRequest item : templates) {
      BillingConfigEntry existed = billingConfigMapper.selectOne(
          Wrappers.lambdaQuery(BillingConfigEntry.class)
              .eq(BillingConfigEntry::getIsDeleted, 0)
              .eq(BillingConfigEntry::getOrgId, AuthContext.getOrgId())
              .eq(BillingConfigEntry::getConfigKey, item.getConfigKey())
              .eq(BillingConfigEntry::getEffectiveMonth, item.getEffectiveMonth())
              .last("LIMIT 1"));
      upsertBillingConfigInternal(item);
      response.getConfigKeys().add(item.getConfigKey());
      if (existed == null) {
        response.setCreatedCount(response.getCreatedCount() + 1);
      } else {
        response.setUpdatedCount(response.getUpdatedCount() + 1);
      }
    }
    return Result.ok(response);
  }

  @PostMapping("/allocation/meter/validate")
  public Result<FinanceAllocationMeterValidateResponse> validateAllocationMeter(
      @Valid @RequestBody FinanceAllocationMeterValidateRequest request) {
    Long orgId = AuthContext.getOrgId();
    BigDecimal threshold = request.getAbnormalThreshold() == null
        ? BigDecimal.valueOf(500)
        : request.getAbnormalThreshold();
    String targetMonth = normalizeText(request.getMonth(), YearMonth.now().toString());
    List<FinanceAllocationMeterValidateRequest.Row> sourceRows = request.getRows() == null
        ? List.of()
        : request.getRows();
    Map<String, List<Room>> roomMap = roomMapper.selectList(
        Wrappers.lambdaQuery(Room.class)
            .eq(Room::getIsDeleted, 0)
            .eq(orgId != null, Room::getOrgId, orgId))
        .stream()
        .collect(Collectors.groupingBy(item -> normalizeText(item.getRoomNo(), "").toUpperCase(Locale.ROOT)));

    FinanceAllocationMeterValidateResponse response = new FinanceAllocationMeterValidateResponse();
    response.setMonth(targetMonth);
    response.setAbnormalThreshold(threshold);
    response.setTotalRows(sourceRows.size());
    for (int index = 0; index < sourceRows.size(); index += 1) {
      FinanceAllocationMeterValidateRequest.Row row = sourceRows.get(index);
      FinanceAllocationMeterValidateResponse.RowResult result = new FinanceAllocationMeterValidateResponse.RowResult();
      result.setRowNo(index + 1);
      result.setBuilding(normalizeText(row.getBuilding(), ""));
      result.setFloorNo(normalizeText(row.getFloorNo(), ""));
      result.setRoomNo(normalizeText(row.getRoomNo(), ""));
      result.setPreviousReading(row.getPreviousReading());
      result.setCurrentReading(row.getCurrentReading());
      result.setRemark(normalizeText(row.getRemark(), ""));
      result.setLevel("OK");
      result.setCode("OK");
      result.setMessage("校验通过");
      result.setValid(true);

      String normalizedRoomNo = normalizeText(row.getRoomNo(), "").toUpperCase(Locale.ROOT);
      if (normalizedRoomNo.isBlank()) {
        markInvalidMeterRow(result, "ROOM_REQUIRED", "房间号不能为空");
      } else if (roomMap.getOrDefault(normalizedRoomNo, List.of()).isEmpty()) {
        markInvalidMeterRow(result, "ROOM_NOT_FOUND", "房间不存在");
      }

      BigDecimal previous = row.getPreviousReading();
      BigDecimal current = row.getCurrentReading();
      if (current == null || previous == null) {
        markInvalidMeterRow(result, "READING_REQUIRED", "上期/本期读数不能为空");
      } else {
        BigDecimal usage = current.subtract(previous);
        result.setUsage(usage);
        if (previous.compareTo(BigDecimal.ZERO) < 0 || current.compareTo(BigDecimal.ZERO) < 0) {
          markInvalidMeterRow(result, "READING_NEGATIVE", "读数不能为负数");
        } else if (usage.compareTo(BigDecimal.ZERO) < 0) {
          markInvalidMeterRow(result, "READING_ROLLBACK", "本期读数不能小于上期读数");
        } else if (usage.compareTo(threshold) > 0 && result.isValid()) {
          result.setLevel("WARN");
          result.setCode("ABNORMAL_SPIKE");
          result.setMessage("用电波动异常，请人工复核");
        }
      }

      if (result.isValid()) {
        response.setValidRows(response.getValidRows() + 1);
      } else {
        response.setInvalidRows(response.getInvalidRows() + 1);
      }
      if ("WARN".equals(result.getLevel())) {
        response.setWarningRows(response.getWarningRows() + 1);
      }
      response.getRows().add(result);
    }
    return Result.ok(response);
  }

  @GetMapping("/reconcile/exceptions")
  public Result<List<FinanceReconcileExceptionItem>> reconcileExceptions(
      @RequestParam(required = false) String date,
      @RequestParam(required = false) String type) {
    Long orgId = AuthContext.getOrgId();
    LocalDate targetDate = (date == null || date.isBlank() || "today".equalsIgnoreCase(date))
        ? LocalDate.now()
        : LocalDate.parse(date);
    return Result.ok(buildReconcileExceptions(orgId, targetDate, type));
  }

  private List<FinanceReconcileExceptionItem> buildReconcileExceptions(
      Long orgId,
      LocalDate targetDate,
      String type) {
    LocalDateTime start = targetDate.atStartOfDay();
    LocalDateTime end = targetDate.plusDays(1).atStartOfDay();

    List<FinanceReconcileExceptionItem> result = new ArrayList<>();
    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .gt(BillMonthly::getPaidAmount, BigDecimal.ZERO)
            .gt(BillMonthly::getOutstandingAmount, BigDecimal.ZERO));
    if (type == null || type.isBlank() || "BILL_PAID_UNMATCHED".equalsIgnoreCase(type)) {
      for (BillMonthly bill : bills) {
        result.add(reconcileItem(
            "BILL_PAID_UNMATCHED", "账单已收未核销", bill.getId(), null, bill.getElderId(),
            null, safe(bill.getOutstandingAmount()), "账单仍有未核销余额", "核对收款登记与核销状态", bill.getUpdateTime()));
      }
    }

    List<PaymentRecord> payments = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(orgId != null, PaymentRecord::getOrgId, orgId)
            .ge(PaymentRecord::getPaidAt, start)
            .lt(PaymentRecord::getPaidAt, end));
    Map<String, List<PaymentRecord>> txnMap = payments.stream()
        .filter(item -> item.getExternalTxnId() != null && !item.getExternalTxnId().isBlank())
        .collect(Collectors.groupingBy(PaymentRecord::getExternalTxnId));
    if (type == null || type.isBlank() || "DUPLICATED_PAYMENT".equalsIgnoreCase(type)) {
      for (Map.Entry<String, List<PaymentRecord>> entry : txnMap.entrySet()) {
        if (entry.getValue().size() <= 1) continue;
        for (PaymentRecord payment : entry.getValue()) {
          result.add(reconcileItem(
              "DUPLICATED_PAYMENT", "收款重复/冲正未完成", payment.getBillMonthlyId(), payment.getId(), null,
              null, safe(payment.getAmount()), "交易号重复: " + entry.getKey(), "执行冲正或合并核销", payment.getPaidAt()));
        }
      }
    }

    if (type == null || type.isBlank() || "INVOICE_UNLINKED".equalsIgnoreCase(type)) {
      payments.stream()
          .filter(item -> containsAny(item.getRemark(), "发票", "invoice", "收据"))
          .filter(item -> item.getBillMonthlyId() == null || item.getBillMonthlyId() <= 0)
          .forEach(item -> result.add(reconcileItem(
              "INVOICE_UNLINKED", "发票未关联账单", null, item.getId(), null,
              null, safe(item.getAmount()), normalizeText(item.getRemark(), "发票记录缺少账单关联"),
              "补充账单关联关系", item.getPaidAt())));
    }

    return result;
  }

  @GetMapping("/ledger/health")
  public Result<FinanceLedgerHealthResponse> ledgerHealth(
      @RequestParam(defaultValue = "20") int limit) {
    Long orgId = AuthContext.getOrgId();
    int cappedLimit = Math.min(Math.max(limit, 1), 100);
    FinanceLedgerHealthResponse response = new FinanceLedgerHealthResponse();
    response.setCheckedAt(LocalDateTime.now());

    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId));
    response.setBillCount((long) bills.size());
    if (bills.isEmpty()) {
      response.setPaymentCount(0L);
      response.setConsumptionCount(0L);
      response.setMismatchBillItemCount(0L);
      response.setMismatchPaidCount(0L);
      response.setMissingPaymentFlowCount(0L);
      response.setTotalIssueCount(0L);
      return Result.ok(response);
    }

    List<Long> billIds = bills.stream().map(BillMonthly::getId).filter(Objects::nonNull).toList();
    List<BillItem> billItems = billItemMapper.selectList(
        Wrappers.lambdaQuery(BillItem.class)
            .eq(BillItem::getIsDeleted, 0)
            .eq(orgId != null, BillItem::getOrgId, orgId)
            .in(BillItem::getBillMonthlyId, billIds));
    Map<Long, BigDecimal> itemAmountByBill = billItems.stream()
        .filter(item -> item.getBillMonthlyId() != null)
        .collect(Collectors.groupingBy(
            BillItem::getBillMonthlyId,
            Collectors.reducing(BigDecimal.ZERO, item -> safe(item.getAmount()), BigDecimal::add)));

    List<PaymentRecord> payments = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(orgId != null, PaymentRecord::getOrgId, orgId)
            .in(PaymentRecord::getBillMonthlyId, billIds));
    response.setPaymentCount((long) payments.size());
    Map<Long, BigDecimal> paymentAmountByBill = payments.stream()
        .filter(item -> item.getBillMonthlyId() != null)
        .collect(Collectors.groupingBy(
            PaymentRecord::getBillMonthlyId,
            Collectors.reducing(BigDecimal.ZERO, item -> safe(item.getAmount()), BigDecimal::add)));

    List<Long> paymentIds = payments.stream().map(PaymentRecord::getId).filter(Objects::nonNull).toList();
    List<ConsumptionRecord> paymentFlows = paymentIds.isEmpty()
        ? List.of()
        : consumptionRecordMapper.selectList(
            Wrappers.lambdaQuery(ConsumptionRecord.class)
                .eq(ConsumptionRecord::getIsDeleted, 0)
                .eq(orgId != null, ConsumptionRecord::getOrgId, orgId)
                .eq(ConsumptionRecord::getSourceType, "BILL_PAYMENT")
                .in(ConsumptionRecord::getSourceId, paymentIds));
    response.setConsumptionCount((long) paymentFlows.size());
    Set<Long> flowPaymentIdSet = paymentFlows.stream()
        .map(ConsumptionRecord::getSourceId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    List<FinanceLedgerHealthResponse.IssueItem> issues = new ArrayList<>();
    for (BillMonthly bill : bills) {
      BigDecimal expectedTotal = safe(bill.getTotalAmount());
      BigDecimal itemTotal = itemAmountByBill.getOrDefault(bill.getId(), BigDecimal.ZERO);
      if (expectedTotal.compareTo(itemTotal) != 0) {
        issues.add(ledgerIssue(
            "BILL_ITEM_MISMATCH",
            "账单总额与明细不一致",
            bill.getId(),
            null,
            bill.getElderId(),
            null,
            expectedTotal,
            itemTotal,
            "bill.totalAmount != sum(bill_item.amount)"));
      }
      BigDecimal expectedPaid = safe(bill.getPaidAmount());
      BigDecimal paymentTotal = paymentAmountByBill.getOrDefault(bill.getId(), BigDecimal.ZERO);
      if (expectedPaid.compareTo(paymentTotal) != 0) {
        issues.add(ledgerIssue(
            "PAID_AMOUNT_MISMATCH",
            "账单已付与收款不一致",
            bill.getId(),
            null,
            bill.getElderId(),
            null,
            expectedPaid,
            paymentTotal,
            "bill.paidAmount != sum(payment_record.amount)"));
      }
    }

    for (PaymentRecord payment : payments) {
      if (!flowPaymentIdSet.contains(payment.getId())) {
        issues.add(ledgerIssue(
            "MISSING_PAYMENT_FLOW",
            "收款缺少消费流水回写",
            payment.getBillMonthlyId(),
            payment.getId(),
            null,
            null,
            safe(payment.getAmount()),
            BigDecimal.ZERO,
            "consumption_record(sourceType=BILL_PAYMENT) not found"));
      }
    }

    response.setMismatchBillItemCount(issues.stream().filter(item -> "BILL_ITEM_MISMATCH".equals(item.getIssueType())).count());
    response.setMismatchPaidCount(issues.stream().filter(item -> "PAID_AMOUNT_MISMATCH".equals(item.getIssueType())).count());
    response.setMissingPaymentFlowCount(issues.stream().filter(item -> "MISSING_PAYMENT_FLOW".equals(item.getIssueType())).count());
    response.setTotalIssueCount((long) issues.size());
    response.setIssues(issues.stream().limit(cappedLimit).toList());
    return Result.ok(response);
  }

  @GetMapping(value = "/ledger/health/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportLedgerHealth(
      @RequestParam(defaultValue = "200") int limit) {
    FinanceLedgerHealthResponse data = ledgerHealth(limit).getData();
    List<String> headers = List.of("异常类型", "账单ID", "收款ID", "老人ID", "老人姓名", "期望金额", "实际金额", "异常描述");
    List<List<String>> body = (data == null || data.getIssues() == null)
        ? List.of()
        : data.getIssues().stream()
            .map(item -> List.of(
                stringOf(item.getIssueTypeLabel()),
                stringOf(item.getBillId()),
                stringOf(item.getPaymentId()),
                stringOf(item.getElderId()),
                stringOf(item.getElderName()),
                stringOf(item.getExpectedAmount()),
                stringOf(item.getActualAmount()),
                stringOf(item.getDetail())))
            .toList();
    return csvResponse("finance-ledger-health", headers, body);
  }

  @GetMapping("/discharge/status-sync")
  public Result<FinanceDischargeStatusSyncResponse> dischargeStatusSync(
      @RequestParam(defaultValue = "100") int limit) {
    Long orgId = AuthContext.getOrgId();
    int cappedLimit = Math.min(Math.max(limit, 10), 500);
    return Result.ok(buildDischargeStatusSyncResponse(orgId, cappedLimit));
  }

  @PostMapping("/discharge/status-sync/execute")
  @Transactional
  public Result<FinanceDischargeStatusSyncExecuteResponse> executeDischargeStatusSync(
      @RequestBody(required = false) FinanceDischargeStatusSyncExecuteRequest request) {
    Long orgId = AuthContext.getOrgId();
    FinanceDischargeStatusSyncExecuteRequest payload = request == null
        ? new FinanceDischargeStatusSyncExecuteRequest()
        : request;
    FinanceDischargeStatusSyncResponse summary = buildDischargeStatusSyncResponse(orgId, 500);
    List<FinanceDischargeStatusSyncResponse.Row> targetRows = summary.getRows();
    if (payload.getSettlementId() != null) {
      targetRows = targetRows.stream()
          .filter(row -> Objects.equals(row.getSettlementId(), payload.getSettlementId()))
          .toList();
    }
    if (payload.getElderId() != null) {
      targetRows = targetRows.stream()
          .filter(row -> Objects.equals(row.getElderId(), payload.getElderId()))
          .toList();
    }
    if (!Boolean.TRUE.equals(payload.getProcessAll()) && payload.getSettlementId() == null && payload.getElderId() == null) {
      targetRows = targetRows.stream().limit(20).toList();
    }

    FinanceDischargeStatusSyncExecuteResponse response = new FinanceDischargeStatusSyncExecuteResponse();
    response.setProcessedCount(targetRows.size());
    for (FinanceDischargeStatusSyncResponse.Row row : targetRows) {
      FinanceDischargeStatusSyncExecuteResponse.RowResult result = new FinanceDischargeStatusSyncExecuteResponse.RowResult();
      result.setSettlementId(row.getSettlementId());
      result.setElderId(row.getElderId());
      if (row.getElderId() == null) {
        result.setSuccess(false);
        result.setMessage("缺少老人ID，无法回写");
        response.setFailCount(response.getFailCount() + 1);
        response.getResults().add(result);
        continue;
      }
      ElderProfile elder = elderMapper.selectOne(
          Wrappers.lambdaQuery(ElderProfile.class)
              .eq(ElderProfile::getIsDeleted, 0)
              .eq(orgId != null, ElderProfile::getOrgId, orgId)
              .eq(ElderProfile::getId, row.getElderId())
              .last("LIMIT 1"));
      if (elder == null) {
        result.setSuccess(false);
        result.setMessage("老人不存在或不在本机构");
        response.setFailCount(response.getFailCount() + 1);
        response.getResults().add(result);
        continue;
      }
      List<Bed> occupiedBeds = bedMapper.selectList(
          Wrappers.lambdaQuery(Bed.class)
              .eq(Bed::getIsDeleted, 0)
              .eq(orgId != null, Bed::getOrgId, orgId)
              .eq(Bed::getElderId, elder.getId()));
      elder.setStatus(3);
      elder.setBedId(null);
      elderMapper.updateById(elder);
      for (Bed bedEntity : occupiedBeds) {
        bedEntity.setElderId(null);
        bedEntity.setStatus(1);
        bedMapper.updateById(bedEntity);
      }
      result.setSuccess(true);
      result.setMessage("状态回写成功");
      response.setSuccessCount(response.getSuccessCount() + 1);
      response.getResults().add(result);
    }
    response.setFailCount(response.getProcessedCount() - response.getSuccessCount());
    return Result.ok(response);
  }

  @GetMapping(value = "/reconcile/exceptions/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportReconcileExceptions(
      @RequestParam(required = false) String date,
      @RequestParam(required = false) String type) {
    List<FinanceReconcileExceptionItem> rows = reconcileExceptions(date, type).getData();
    List<String> headers = List.of("发生时间", "异常类型", "长者", "涉及金额", "异常描述", "处理建议", "账单ID", "收款ID");
    List<List<String>> body = rows.stream()
        .map(item -> List.of(
            stringOf(item.getOccurredAt()),
            stringOf(item.getExceptionTypeLabel()),
            stringOf(item.getElderName()),
            stringOf(item.getAmount()),
            stringOf(item.getDetail()),
            stringOf(item.getSuggestion()),
            stringOf(item.getBillId()),
            stringOf(item.getPaymentId())))
        .toList();
    return csvResponse("finance-reconcile-exceptions", headers, body);
  }

  @GetMapping("/config/overview")
  public Result<FinanceMasterDataOverviewResponse> configOverview(
      @RequestParam(required = false) String month) {
    Long orgId = AuthContext.getOrgId();
    String targetMonth = (month == null || month.isBlank()) ? YearMonth.now().toString() : month;
    List<BillingConfigEntry> configs = billingConfigMapper.selectList(
        Wrappers.lambdaQuery(BillingConfigEntry.class)
            .eq(BillingConfigEntry::getIsDeleted, 0)
            .eq(orgId != null, BillingConfigEntry::getOrgId, orgId)
            .eq(BillingConfigEntry::getEffectiveMonth, targetMonth)
            .orderByDesc(BillingConfigEntry::getUpdateTime));
    List<PaymentRecord> recentPayments = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(orgId != null, PaymentRecord::getOrgId, orgId)
            .ge(PaymentRecord::getPaidAt, LocalDate.now().minusDays(30).atStartOfDay()));
    Set<String> channelSet = recentPayments.stream()
        .map(PaymentRecord::getPayMethod)
        .filter(item -> item != null && !item.isBlank())
        .map(this::normalizePayMethod)
        .collect(Collectors.toSet());
    Long oaPending = oaApprovalMapper.selectCount(
        Wrappers.lambdaQuery(OaApproval.class)
            .eq(OaApproval::getIsDeleted, 0)
            .eq(orgId != null, OaApproval::getOrgId, orgId)
            .eq(OaApproval::getStatus, "PENDING")
            .and(q -> q.like(OaApproval::getTitle, "费用")
                .or().like(OaApproval::getTitle, "退款")
                .or().like(OaApproval::getTitle, "冲正")
                .or().like(OaApproval::getTitle, "减免")));
    Long pendingDiscount = admissionFeeAuditMapper.selectCount(
        Wrappers.lambdaQuery(AdmissionFeeAudit.class)
            .eq(AdmissionFeeAudit::getIsDeleted, 0)
            .eq(orgId != null, AdmissionFeeAudit::getOrgId, orgId)
            .eq(AdmissionFeeAudit::getStatus, FeeWorkflowConstants.AUDIT_PENDING));
    Long pendingRefund = dischargeFeeAuditMapper.selectCount(
        Wrappers.lambdaQuery(DischargeFeeAudit.class)
            .eq(DischargeFeeAudit::getIsDeleted, 0)
            .eq(orgId != null, DischargeFeeAudit::getOrgId, orgId)
            .eq(DischargeFeeAudit::getStatus, FeeWorkflowConstants.AUDIT_PENDING));

    FinanceMasterDataOverviewResponse response = new FinanceMasterDataOverviewResponse();
    response.setMonth(targetMonth);
    response.setFeeSubjectCount((long) configs.stream().map(BillingConfigEntry::getConfigKey).filter(Objects::nonNull).distinct().count());
    response.setBillingRuleCount((long) configs.size());
    response.setPaymentChannelCount((long) channelSet.size());
    response.setPendingApprovalCount((oaPending == null ? 0 : oaPending) + (pendingDiscount == null ? 0 : pendingDiscount)
        + (pendingRefund == null ? 0 : pendingRefund));
    response.setPaymentChannels(channelSet.stream().map(this::payMethodLabel).sorted().toList());
    response.setRecentConfigs(configs.stream().limit(20).map(item -> {
      FinanceMasterDataOverviewResponse.ConfigEntry entry = new FinanceMasterDataOverviewResponse.ConfigEntry();
      entry.setConfigKey(item.getConfigKey());
      entry.setEffectiveMonth(item.getEffectiveMonth());
      entry.setValueText(safe(item.getConfigValue()).toPlainString());
      entry.setRemark(item.getRemark());
      entry.setStatus(item.getStatus());
      return entry;
    }).toList());
    return Result.ok(response);
  }

  @PostMapping("/config/impact-preview")
  public Result<FinanceConfigImpactPreviewResponse> configImpactPreview(
      @RequestBody(required = false) FinanceConfigImpactPreviewRequest request) {
    Long orgId = AuthContext.getOrgId();
    String targetMonth = request == null || request.getMonth() == null || request.getMonth().isBlank()
        ? YearMonth.now().toString()
        : request.getMonth();
    List<String> configKeys = request == null || request.getConfigKeys() == null
        ? List.of()
        : request.getConfigKeys().stream()
            .filter(Objects::nonNull)
            .map(item -> normalizeText(item, "").trim().toUpperCase(Locale.ROOT))
            .filter(item -> !item.isBlank())
            .distinct()
            .toList();
    List<BillMonthly> monthBills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getBillMonth, targetMonth));
    Long activeBillCount = (long) monthBills.size();
    Long activeElderCount = monthBills.stream()
        .map(BillMonthly::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .count();
    Long highRiskBillCount = monthBills.stream()
        .filter(item -> safe(item.getOutstandingAmount()).compareTo(BigDecimal.ZERO) > 0)
        .count();
    Long recentPaymentCount = paymentRecordMapper.selectCount(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(orgId != null, PaymentRecord::getOrgId, orgId)
            .ge(PaymentRecord::getPaidAt, LocalDate.now().minusDays(7).atStartOfDay()));
    Long allocationTaskCount = monthlyAllocationMapper.selectCount(
        Wrappers.lambdaQuery(MonthlyAllocation.class)
            .eq(MonthlyAllocation::getIsDeleted, 0)
            .eq(orgId != null, MonthlyAllocation::getOrgId, orgId)
            .eq(MonthlyAllocation::getAllocationMonth, targetMonth));
    Long pendingApprovalCount = oaApprovalMapper.selectCount(
        Wrappers.lambdaQuery(OaApproval.class)
            .eq(OaApproval::getIsDeleted, 0)
            .eq(orgId != null, OaApproval::getOrgId, orgId)
            .eq(OaApproval::getStatus, "PENDING")
            .and(q -> q.like(OaApproval::getTitle, "费用")
                .or().like(OaApproval::getTitle, "退款")
                .or().like(OaApproval::getTitle, "冲正")
                .or().like(OaApproval::getTitle, "减免")));
    FinanceConfigImpactPreviewResponse response = new FinanceConfigImpactPreviewResponse();
    response.setMonth(targetMonth);
    response.setActiveBillCount(activeBillCount);
    response.setActiveElderCount(activeElderCount);
    response.setHighRiskBillCount(highRiskBillCount);
    response.setPendingApprovalCount(pendingApprovalCount == null ? 0L : pendingApprovalCount);
    response.setRecentPaymentCount(recentPaymentCount == null ? 0L : recentPaymentCount);
    response.setAllocationTaskCount(allocationTaskCount == null ? 0L : allocationTaskCount);
    response.setConfigKeyCount(configKeys.size());

    List<FinanceConfigImpactPreviewResponse.Item> impactedItems = new ArrayList<>();
    for (String configKey : configKeys) {
      FinanceConfigImpactPreviewResponse.Item item = new FinanceConfigImpactPreviewResponse.Item();
      item.setConfigKey(configKey);
      if (configKey.startsWith("FEE_SUBJECT_")) {
        item.setModuleLabel("消费与费用流水");
        item.setAffectedCount(activeBillCount);
        item.setImpactHint("影响消费科目记账与账单展示口径");
      } else if (configKey.startsWith("PAYMENT_CHANNEL_")) {
        item.setModuleLabel("收费登记与对账中心");
        item.setAffectedCount(response.getRecentPaymentCount());
        item.setImpactHint("影响收款登记渠道、对账与日结");
      } else if (configKey.startsWith("ALLOC_")) {
        item.setModuleLabel("分摊与公共费用");
        item.setAffectedCount(response.getAllocationTaskCount());
        item.setImpactHint("影响月分摊账单生成及人均费用");
      } else if (configKey.startsWith("BILL_")) {
        item.setModuleLabel("账单中心与自动扣费");
        item.setAffectedCount(activeBillCount);
        item.setImpactHint("影响在住账单应收口径与催缴策略");
      } else {
        item.setModuleLabel("财务配置中心");
        item.setAffectedCount(activeElderCount);
        item.setImpactHint("影响财务流程默认策略，请确认联动页面");
      }
      impactedItems.add(item);
    }
    response.setImpactedItems(impactedItems);

    List<String> riskTips = new ArrayList<>();
    if (highRiskBillCount > 0) {
      riskTips.add("当前存在 " + highRiskBillCount + " 条欠费账单，调整规则后请复核催缴结果");
    }
    if (response.getPendingApprovalCount() > 0) {
      riskTips.add("存在 " + response.getPendingApprovalCount() + " 条待审批单据，避免中途变更审批口径");
    }
    if (configKeys.stream().anyMatch(item -> item.startsWith("PAYMENT_CHANNEL_"))
        && response.getRecentPaymentCount() > 0) {
      riskTips.add("近7天收款记录 " + response.getRecentPaymentCount() + " 条，请确认渠道停启不会影响交班");
    }
    if (configKeys.stream().anyMatch(item -> item.startsWith("ALLOC_"))
        && response.getAllocationTaskCount() == 0) {
      riskTips.add("本月还未生成分摊任务，建议先做规则演练再正式生效");
    }
    if (riskTips.isEmpty()) {
      riskTips.add("未发现高风险冲突，可按流程发布配置");
    }
    response.setRiskTips(riskTips);
    return Result.ok(response);
  }

  @GetMapping("/billing-config")
  public Result<List<BillingConfigEntry>> billingConfig(
      @RequestParam(required = false) String month,
      @RequestParam(required = false) String keyPrefix) {
    Long orgId = AuthContext.getOrgId();
    String targetMonth = (month == null || month.isBlank()) ? YearMonth.now().toString() : month;
    var wrapper = Wrappers.lambdaQuery(BillingConfigEntry.class)
        .eq(BillingConfigEntry::getIsDeleted, 0)
        .eq(orgId != null, BillingConfigEntry::getOrgId, orgId)
        .eq(BillingConfigEntry::getEffectiveMonth, targetMonth)
        .orderByAsc(BillingConfigEntry::getConfigKey);
    List<BillingConfigEntry> all = billingConfigMapper.selectList(wrapper);
    if (keyPrefix == null || keyPrefix.isBlank()) {
      return Result.ok(all);
    }
    String prefix = keyPrefix.trim().toLowerCase(Locale.ROOT);
    return Result.ok(all.stream()
        .filter(item -> item.getConfigKey() != null && item.getConfigKey().toLowerCase(Locale.ROOT).startsWith(prefix))
        .toList());
  }

  @PostMapping("/billing-config")
  public Result<BillingConfigEntry> upsertBillingConfig(@Valid @RequestBody FinanceBillingConfigUpsertRequest request) {
    return Result.ok(upsertBillingConfigInternal(request));
  }

  @PostMapping("/billing-config/batch")
  public Result<List<BillingConfigEntry>> batchUpsertBillingConfig(
      @Valid @RequestBody FinanceBillingConfigBatchUpsertRequest request) {
    List<BillingConfigEntry> saved = request.getItems().stream()
        .map(this::upsertBillingConfigInternal)
        .toList();
    return Result.ok(saved);
  }

  private BillingConfigEntry upsertBillingConfigInternal(FinanceBillingConfigUpsertRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    String username = AuthContext.getUsername();
    BillingConfigEntry existing = billingConfigMapper.selectOne(
        Wrappers.lambdaQuery(BillingConfigEntry.class)
            .eq(BillingConfigEntry::getIsDeleted, 0)
            .eq(orgId != null, BillingConfigEntry::getOrgId, orgId)
            .eq(BillingConfigEntry::getConfigKey, request.getConfigKey())
            .eq(BillingConfigEntry::getEffectiveMonth, request.getEffectiveMonth())
            .last("LIMIT 1"));
    if (existing == null) {
      existing = new BillingConfigEntry();
      existing.setOrgId(orgId);
      existing.setConfigKey(request.getConfigKey());
      existing.setConfigValue(request.getConfigValue());
      existing.setEffectiveMonth(request.getEffectiveMonth());
      existing.setStatus(request.getStatus() == null ? 1 : request.getStatus());
      existing.setRemark(request.getRemark());
      billingConfigMapper.insert(existing);
      auditLogService.record(
          orgId, orgId, staffId, username,
          "FIN_BILLING_CONFIG_CREATE", "FINANCE_CONFIG", existing.getId(),
          "新增配置: " + existing.getConfigKey()
              + "@" + existing.getEffectiveMonth()
              + " 值=" + safe(existing.getConfigValue()).toPlainString());
      return existing;
    }
    String before = safe(existing.getConfigValue()).toPlainString();
    existing.setConfigValue(request.getConfigValue());
    existing.setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());
    existing.setRemark(request.getRemark());
    billingConfigMapper.updateById(existing);
    auditLogService.record(
        orgId, orgId, staffId, username,
        "FIN_BILLING_CONFIG_UPDATE", "FINANCE_CONFIG", existing.getId(),
        "更新配置: " + existing.getConfigKey()
            + "@" + existing.getEffectiveMonth()
            + " " + before + "->" + safe(existing.getConfigValue()).toPlainString());
    return existing;
  }

  @GetMapping("/config/change-log/page")
  public Result<IPage<FinanceConfigChangeLogItem>> configChangeLogPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(AuditLog.class)
        .eq(AuditLog::getOrgId, orgId)
        .eq(AuditLog::getEntityType, "FINANCE_CONFIG")
        .orderByDesc(AuditLog::getCreateTime);
    if (from != null && !from.isBlank()) {
      wrapper.ge(AuditLog::getCreateTime, LocalDate.parse(from).atStartOfDay());
    }
    if (to != null && !to.isBlank()) {
      wrapper.lt(AuditLog::getCreateTime, LocalDate.parse(to).plusDays(1).atStartOfDay());
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(q -> q.like(AuditLog::getActorName, keyword)
          .or().like(AuditLog::getActionType, keyword)
          .or().like(AuditLog::getDetail, keyword));
    }
    IPage<AuditLog> page = auditLogMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    IPage<FinanceConfigChangeLogItem> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    result.setRecords(page.getRecords().stream().map(this::toConfigLogItem).toList());
    return Result.ok(result);
  }

  @GetMapping("/billing-config/snapshots")
  public Result<List<FinanceBillingConfigSnapshotItem>> billingConfigSnapshots() {
    Long orgId = AuthContext.getOrgId();
    List<BillingConfigEntry> configs = billingConfigMapper.selectList(
        Wrappers.lambdaQuery(BillingConfigEntry.class)
            .eq(BillingConfigEntry::getIsDeleted, 0)
            .eq(orgId != null, BillingConfigEntry::getOrgId, orgId));
    Map<String, List<BillingConfigEntry>> grouped = configs.stream()
        .filter(item -> item.getEffectiveMonth() != null && !item.getEffectiveMonth().isBlank())
        .collect(Collectors.groupingBy(BillingConfigEntry::getEffectiveMonth));
    List<FinanceBillingConfigSnapshotItem> result = grouped.entrySet().stream()
        .map(entry -> {
          FinanceBillingConfigSnapshotItem item = new FinanceBillingConfigSnapshotItem();
          item.setEffectiveMonth(entry.getKey());
          item.setConfigCount((long) entry.getValue().size());
          item.setLatestUpdateTime(entry.getValue().stream()
              .map(BillingConfigEntry::getUpdateTime)
              .filter(Objects::nonNull)
              .max(Comparator.naturalOrder())
              .orElse(null));
          return item;
        })
        .sorted(Comparator.comparing(FinanceBillingConfigSnapshotItem::getEffectiveMonth).reversed())
        .toList();
    return Result.ok(result);
  }

  @PostMapping("/billing-config/rollback")
  public Result<BillingConfigEntry> rollbackBillingConfig(@Valid @RequestBody FinanceBillingConfigRollbackRequest request) {
    Long orgId = AuthContext.getOrgId();
    AuditLog log = auditLogMapper.selectOne(
        Wrappers.lambdaQuery(AuditLog.class)
            .eq(AuditLog::getId, request.getLogId())
            .eq(AuditLog::getOrgId, orgId)
            .eq(AuditLog::getEntityType, "FINANCE_CONFIG")
            .last("LIMIT 1"));
    if (log == null) {
      throw new IllegalArgumentException("变更记录不存在");
    }
    String detail = normalizeText(log.getDetail(), "");
    Matcher updateMatcher = UPDATE_ROLLBACK_PATTERN.matcher(detail);
    Matcher createMatcher = CREATE_ROLLBACK_PATTERN.matcher(detail);
    FinanceBillingConfigUpsertRequest upsert = new FinanceBillingConfigUpsertRequest();
    if (updateMatcher.find()) {
      upsert.setConfigKey(updateMatcher.group(1));
      upsert.setEffectiveMonth(updateMatcher.group(2));
      upsert.setConfigValue(new BigDecimal(updateMatcher.group(3)));
      upsert.setStatus(1);
      upsert.setRemark("按变更记录回滚#" + log.getId());
    } else if (createMatcher.find()) {
      upsert.setConfigKey(createMatcher.group(1));
      upsert.setEffectiveMonth(createMatcher.group(2));
      upsert.setConfigValue(new BigDecimal(createMatcher.group(3)));
      upsert.setStatus(0);
      upsert.setRemark("回滚创建，停用配置#" + log.getId());
    } else {
      throw new IllegalArgumentException("该记录不支持自动回滚");
    }
    BillingConfigEntry saved = upsertBillingConfigInternal(upsert);
    auditLogService.record(
        orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "FIN_BILLING_CONFIG_ROLLBACK", "FINANCE_CONFIG", saved.getId(),
        "按日志#" + log.getId() + "回滚配置: " + saved.getConfigKey() + "@" + saved.getEffectiveMonth());
    return Result.ok(saved);
  }

  private FinanceWorkbenchOverviewResponse.CashierCard buildCashierCard(
      Long orgId,
      List<PaymentRecord> todayPayments,
      LocalDateTime startOfToday,
      LocalDateTime endOfToday) {
    FinanceWorkbenchOverviewResponse.CashierCard card = new FinanceWorkbenchOverviewResponse.CashierCard();
    BigDecimal todayCollected = todayPayments.stream()
        .map(PaymentRecord::getAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    card.setTodayCollectedTotal(todayCollected);
    Map<String, BigDecimal> amountByMethod = new LinkedHashMap<>();
    for (PaymentRecord payment : todayPayments) {
      String method = normalizePayMethod(payment.getPayMethod());
      amountByMethod.merge(method, safe(payment.getAmount()), BigDecimal::add);
    }
    List<FinanceWorkbenchOverviewResponse.PaymentMethodAmount> methodItems = amountByMethod.entrySet().stream()
        .map(entry -> {
          FinanceWorkbenchOverviewResponse.PaymentMethodAmount item = new FinanceWorkbenchOverviewResponse.PaymentMethodAmount();
          item.setMethod(entry.getKey());
          item.setMethodLabel(payMethodLabel(entry.getKey()));
          item.setAmount(entry.getValue());
          return item;
        })
        .toList();
    card.setPaymentMethods(methodItems);

    BigDecimal todayInvoice = todayPayments.stream()
        .filter(item -> containsAny(item.getRemark(), "发票", "invoice", "收据"))
        .map(PaymentRecord::getAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    card.setTodayInvoiceAmount(todayInvoice);

    BigDecimal refundAmount = dischargeSettlementMapper.selectList(
        Wrappers.lambdaQuery(DischargeSettlement.class)
            .eq(DischargeSettlement::getIsDeleted, 0)
            .eq(orgId != null, DischargeSettlement::getOrgId, orgId)
            .eq(DischargeSettlement::getFinanceRefunded, 1)
            .ge(DischargeSettlement::getFinanceRefundTime, startOfToday)
            .lt(DischargeSettlement::getFinanceRefundTime, endOfToday))
        .stream()
        .map(DischargeSettlement::getRefundAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    card.setTodayRefundAmount(refundAmount);
    return card;
  }

  private int insightLevelWeight(String level) {
    if ("HIGH".equalsIgnoreCase(level)) return 3;
    if ("MEDIUM".equalsIgnoreCase(level)) return 2;
    return 1;
  }

  private FinanceWorkbenchOverviewResponse.RiskCard buildRiskCard(
      Long orgId,
      List<BillMonthly> outstandingBills,
      LocalDate today) {
    FinanceWorkbenchOverviewResponse.RiskCard card = new FinanceWorkbenchOverviewResponse.RiskCard();
    Set<Long> overdueElders = outstandingBills.stream()
        .map(BillMonthly::getElderId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    BigDecimal overdueAmount = outstandingBills.stream()
        .map(BillMonthly::getOutstandingAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    card.setOverdueElderCount((long) overdueElders.size());
    card.setOverdueAmount(overdueAmount);

    long lowBalanceCount = elderAccountMapper.selectList(
        Wrappers.lambdaQuery(ElderAccount.class)
            .eq(ElderAccount::getIsDeleted, 0)
            .eq(orgId != null, ElderAccount::getOrgId, orgId))
        .stream()
        .filter(account -> safe(account.getBalance()).compareTo(safe(account.getWarnThreshold())) < 0)
        .count();
    card.setLowBalanceCount(lowBalanceCount);

    LocalDate warningDate = today.plusDays(30);
    Long expiringCount = crmContractMapper.selectCount(
        Wrappers.lambdaQuery(CrmContract.class)
            .eq(CrmContract::getIsDeleted, 0)
            .eq(orgId != null, CrmContract::getOrgId, orgId)
            .isNotNull(CrmContract::getEffectiveTo)
            .ge(CrmContract::getEffectiveTo, today)
            .le(CrmContract::getEffectiveTo, warningDate));
    card.setExpiringContractCount(expiringCount == null ? 0L : expiringCount);
    return card;
  }

  private FinanceWorkbenchOverviewResponse.PendingCard buildPendingCard(Long orgId, LocalDate today) {
    FinanceWorkbenchOverviewResponse.PendingCard card = new FinanceWorkbenchOverviewResponse.PendingCard();
    Long pendingDiscount = admissionFeeAuditMapper.selectCount(
        Wrappers.lambdaQuery(AdmissionFeeAudit.class)
            .eq(AdmissionFeeAudit::getIsDeleted, 0)
            .eq(orgId != null, AdmissionFeeAudit::getOrgId, orgId)
            .eq(AdmissionFeeAudit::getStatus, FeeWorkflowConstants.AUDIT_PENDING));
    Long pendingRefund = dischargeFeeAuditMapper.selectCount(
        Wrappers.lambdaQuery(DischargeFeeAudit.class)
            .eq(DischargeFeeAudit::getIsDeleted, 0)
            .eq(orgId != null, DischargeFeeAudit::getOrgId, orgId)
            .eq(DischargeFeeAudit::getStatus, FeeWorkflowConstants.AUDIT_PENDING));
    Long pendingSettlement = dischargeSettlementMapper.selectCount(
        Wrappers.lambdaQuery(DischargeSettlement.class)
            .eq(DischargeSettlement::getIsDeleted, 0)
            .eq(orgId != null, DischargeSettlement::getOrgId, orgId)
            .in(DischargeSettlement::getStatus,
                FeeWorkflowConstants.SETTLEMENT_PENDING_CONFIRM,
                FeeWorkflowConstants.SETTLEMENT_PROCESSING));
    card.setPendingDiscountCount(pendingDiscount == null ? 0L : pendingDiscount);
    card.setPendingRefundCount(pendingRefund == null ? 0L : pendingRefund);
    card.setPendingDischargeSettlementCount(pendingSettlement == null ? 0L : pendingSettlement);
    List<FinanceHandleLogItem> issueLogs = queryHandleLogs(orgId, "FINANCE_ISSUE_HANDLE", null, null, null, null, 500);
    long issueTodoCount = latestHandleLogMap(issueLogs).values().stream()
        .filter(item -> !"DONE".equalsIgnoreCase(item.getStatus()))
        .count();
    List<FinanceHandleLogItem> collectionLogs = queryHandleLogs(orgId, "FINANCE_COLLECTION_HANDLE", null, null, null, null, 500);
    long reminderCount = latestCollectionHandleMap(collectionLogs).values().stream()
        .filter(item -> !"DONE".equalsIgnoreCase(item.getStatus()))
        .filter(item -> item.getNextReminderAt() != null && !item.getNextReminderAt().toLocalDate().isAfter(today))
        .count();
    card.setIssueTodoCount(issueTodoCount);
    card.setCollectionReminderCount(reminderCount);
    card.setLockedMonthCount(financeMonthLockService.getMonthLockStatus(orgId, YearMonth.from(today)).isLocked() ? 1L : 0L);
    return card;
  }

  private FinanceWorkbenchOverviewResponse.RevenueStructureCard buildRevenueStructureCard(
      Long orgId,
      List<BillMonthly> thisMonthBills) {
    FinanceWorkbenchOverviewResponse.RevenueStructureCard card = new FinanceWorkbenchOverviewResponse.RevenueStructureCard();
    BigDecimal total = thisMonthBills.stream()
        .map(BillMonthly::getTotalAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    card.setMonthRevenueTotal(total);
    List<Long> billIds = thisMonthBills.stream().map(BillMonthly::getId).filter(Objects::nonNull).toList();
    if (billIds.isEmpty()) {
      card.setCategories(List.of());
      return card;
    }
    List<BillItem> items = billItemMapper.selectList(
        Wrappers.lambdaQuery(BillItem.class)
            .eq(BillItem::getIsDeleted, 0)
            .eq(orgId != null, BillItem::getOrgId, orgId)
            .in(BillItem::getBillMonthlyId, billIds));
    Map<String, BigDecimal> categorySum = new LinkedHashMap<>();
    for (BillItem item : items) {
      String code = normalizeRevenueCategory(item.getItemType(), item.getItemName());
      categorySum.merge(code, safe(item.getAmount()), BigDecimal::add);
    }
    List<FinanceWorkbenchOverviewResponse.RevenueCategory> categories = categorySum.entrySet().stream()
        .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
        .map(entry -> {
          FinanceWorkbenchOverviewResponse.RevenueCategory category = new FinanceWorkbenchOverviewResponse.RevenueCategory();
          category.setCode(entry.getKey());
          category.setName(revenueCategoryLabel(entry.getKey()));
          category.setAmount(entry.getValue());
          category.setRatio(percentage(entry.getValue(), total));
          return category;
        })
        .toList();
    card.setCategories(categories);
    return card;
  }

  private FinanceWorkbenchOverviewResponse.RoomOpsCard buildRoomOpsCard(
      Long orgId,
      List<BillMonthly> thisMonthBills) {
    FinanceWorkbenchOverviewResponse.RoomOpsCard card = new FinanceWorkbenchOverviewResponse.RoomOpsCard();
    if (thisMonthBills.isEmpty()) {
      card.setEmptyBedLossEstimate(BigDecimal.ZERO);
      return card;
    }
    List<Bed> beds = bedMapper.selectList(
        Wrappers.lambdaQuery(Bed.class)
            .eq(Bed::getIsDeleted, 0)
            .eq(orgId != null, Bed::getOrgId, orgId));
    List<Room> rooms = roomMapper.selectList(
        Wrappers.lambdaQuery(Room.class)
            .eq(Room::getIsDeleted, 0)
            .eq(orgId != null, Room::getOrgId, orgId));
    Map<Long, Room> roomMap = rooms.stream()
        .collect(Collectors.toMap(Room::getId, Function.identity(), (a, b) -> a));

    Map<Long, Bed> elderBedMap = beds.stream()
        .filter(item -> item.getElderId() != null)
        .collect(Collectors.toMap(Bed::getElderId, Function.identity(), (a, b) -> a));

    Map<Long, BigDecimal> roomIncomeMap = new HashMap<>();
    for (BillMonthly bill : thisMonthBills) {
      Bed bed = elderBedMap.get(bill.getElderId());
      if (bed == null || bed.getRoomId() == null) {
        continue;
      }
      roomIncomeMap.merge(bed.getRoomId(), safe(bill.getTotalAmount()), BigDecimal::add);
    }

    Map<Long, Integer> roomOccupiedBeds = new HashMap<>();
    Map<Long, Integer> roomTotalBeds = new HashMap<>();
    for (Bed bed : beds) {
      if (bed.getRoomId() == null) {
        continue;
      }
      roomTotalBeds.merge(bed.getRoomId(), 1, Integer::sum);
      if (bed.getElderId() != null) {
        roomOccupiedBeds.merge(bed.getRoomId(), 1, Integer::sum);
      }
    }

    Map<String, BigDecimal> floorIncome = new HashMap<>();
    List<FinanceWorkbenchOverviewResponse.RoomRanking> roomRankings = new ArrayList<>();
    BigDecimal totalRoomIncome = BigDecimal.ZERO;
    int totalOccupied = 0;
    int totalEmpty = 0;
    for (Room room : rooms) {
      Long roomId = room.getId();
      BigDecimal income = roomIncomeMap.getOrDefault(roomId, BigDecimal.ZERO);
      int occupied = roomOccupiedBeds.getOrDefault(roomId, 0);
      int totalBeds = roomTotalBeds.getOrDefault(roomId, room.getCapacity() == null ? 0 : room.getCapacity());
      int empty = Math.max(totalBeds - occupied, 0);
      totalRoomIncome = totalRoomIncome.add(income);
      totalOccupied += occupied;
      totalEmpty += empty;
      String floorNo = normalizeText(room.getFloorNo(), "未标注楼层");
      floorIncome.merge(floorNo, income, BigDecimal::add);

      FinanceWorkbenchOverviewResponse.RoomRanking ranking = new FinanceWorkbenchOverviewResponse.RoomRanking();
      ranking.setBuilding(normalizeText(room.getBuilding(), "未标注楼栋"));
      ranking.setFloorNo(floorNo);
      ranking.setRoomNo(normalizeText(room.getRoomNo(), "未知房间"));
      ranking.setIncome(income);
      ranking.setCost(BigDecimal.ZERO);
      ranking.setNetAmount(income);
      ranking.setOccupiedBeds(occupied);
      ranking.setEmptyBeds(empty);
      roomRankings.add(ranking);
    }

    List<FinanceWorkbenchOverviewResponse.FloorRanking> floorRankings = floorIncome.entrySet().stream()
        .map(entry -> {
          FinanceWorkbenchOverviewResponse.FloorRanking item = new FinanceWorkbenchOverviewResponse.FloorRanking();
          item.setFloorNo(entry.getKey());
          item.setIncome(entry.getValue());
          return item;
        })
        .sorted(Comparator.comparing(FinanceWorkbenchOverviewResponse.FloorRanking::getIncome).reversed())
        .toList();
    card.setFloorTop(floorRankings.stream().limit(3).toList());
    List<FinanceWorkbenchOverviewResponse.FloorRanking> floorBottom = new ArrayList<>(floorRankings);
    floorBottom.sort(Comparator.comparing(FinanceWorkbenchOverviewResponse.FloorRanking::getIncome));
    card.setFloorBottom(floorBottom.stream().limit(3).toList());
    roomRankings.sort(Comparator.comparing(FinanceWorkbenchOverviewResponse.RoomRanking::getIncome).reversed());
    card.setRoomTop10(roomRankings.stream().limit(10).toList());

    BigDecimal avgPerOccupiedBed = totalOccupied <= 0
        ? BigDecimal.ZERO
        : totalRoomIncome.divide(BigDecimal.valueOf(totalOccupied), 2, RoundingMode.HALF_UP);
    card.setEmptyBedLossEstimate(avgPerOccupiedBed.multiply(BigDecimal.valueOf(totalEmpty)));
    return card;
  }

  private FinanceWorkbenchOverviewResponse.AutoDebitCard buildAutoDebitCard(
      Long orgId,
      List<BillMonthly> outstandingBills,
      List<PaymentRecord> todayPayments,
      String thisMonth) {
    FinanceWorkbenchOverviewResponse.AutoDebitCard card = new FinanceWorkbenchOverviewResponse.AutoDebitCard();
    long shouldDeduct = outstandingBills.stream()
        .filter(item -> item.getBillMonth() != null && item.getBillMonth().compareTo(thisMonth) <= 0)
        .count();
    List<ElderAccountLog> todayLogs = elderAccountLogMapper.selectList(
        Wrappers.lambdaQuery(ElderAccountLog.class)
            .eq(ElderAccountLog::getIsDeleted, 0)
            .eq(orgId != null, ElderAccountLog::getOrgId, orgId)
            .ge(ElderAccountLog::getCreateTime, LocalDate.now().atStartOfDay())
            .lt(ElderAccountLog::getCreateTime, LocalDate.now().plusDays(1).atStartOfDay()));
    long successCount = todayLogs.stream()
        .filter(log -> containsAny(log.getSourceType(), "AUTO", "DEBIT") || containsAny(log.getRemark(), "自动扣费", "自动扣款"))
        .count();
    long failed = Math.max(shouldDeduct - successCount, 0);
    card.setShouldDeductCount(shouldDeduct);
    card.setSuccessCount(successCount);
    card.setFailedCount(failed);
    card.setPendingHandleCount(failed);

    Map<Long, BigDecimal> elderOutstanding = new HashMap<>();
    for (BillMonthly bill : outstandingBills) {
      if (bill.getElderId() == null) continue;
      elderOutstanding.merge(bill.getElderId(), safe(bill.getOutstandingAmount()), BigDecimal::add);
    }
    List<ElderAccount> accounts = elderAccountMapper.selectList(
        Wrappers.lambdaQuery(ElderAccount.class)
            .eq(ElderAccount::getIsDeleted, 0)
            .eq(orgId != null, ElderAccount::getOrgId, orgId)
            .in(!elderOutstanding.isEmpty(), ElderAccount::getElderId, elderOutstanding.keySet()));
    long insufficient = accounts.stream()
        .filter(account -> safe(account.getBalance())
            .compareTo(elderOutstanding.getOrDefault(account.getElderId(), BigDecimal.ZERO)) < 0)
        .count();
    long missingRule = Math.max(failed - insufficient, 0);
    List<FinanceWorkbenchOverviewResponse.ReasonCount> reasons = new ArrayList<>();
    reasons.add(reason("余额不足", insufficient));
    reasons.add(reason("规则缺失", missingRule));
    reasons.add(reason("状态限制", countStatusRestricted(orgId, elderOutstanding.keySet())));
    card.setFailureReasons(reasons);
    return card;
  }

  private FinanceWorkbenchOverviewResponse.MedicalFlowCard buildMedicalFlowCard(
      Long orgId,
      LocalDateTime startOfToday,
      LocalDateTime endOfToday) {
    FinanceWorkbenchOverviewResponse.MedicalFlowCard card = new FinanceWorkbenchOverviewResponse.MedicalFlowCard();
    List<ConsumptionRecord> todayRecords = consumptionRecordMapper.selectList(
        Wrappers.lambdaQuery(ConsumptionRecord.class)
            .eq(ConsumptionRecord::getIsDeleted, 0)
            .eq(orgId != null, ConsumptionRecord::getOrgId, orgId)
            .ge(ConsumptionRecord::getConsumeDate, LocalDate.now())
            .le(ConsumptionRecord::getConsumeDate, LocalDate.now()));
    List<ConsumptionRecord> medicalRecords = todayRecords.stream()
        .filter(item -> isMedicalCategory(item.getCategory(), item.getSourceType()))
        .toList();
    card.setTodayFlowCount((long) medicalRecords.size());
    card.setTodayFlowAmount(medicalRecords.stream()
        .map(ConsumptionRecord::getAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add));

    Long pendingReview = dischargeFeeAuditMapper.selectCount(
        Wrappers.lambdaQuery(DischargeFeeAudit.class)
            .eq(DischargeFeeAudit::getIsDeleted, 0)
            .eq(orgId != null, DischargeFeeAudit::getOrgId, orgId)
            .eq(DischargeFeeAudit::getStatus, FeeWorkflowConstants.AUDIT_PENDING)
            .and(q -> q.like(DischargeFeeAudit::getFeeItem, "医")
                .or().like(DischargeFeeAudit::getFeeItem, "护")));
    card.setPendingReviewCount(pendingReview == null ? 0L : pendingReview);

    Set<String> uniq = new HashSet<>();
    long duplicate = 0;
    for (ConsumptionRecord item : medicalRecords) {
      String key = String.format("%s|%s|%s", item.getElderId(), normalizeText(item.getSourceType(), "-"), item.getSourceId());
      if (!uniq.add(key)) {
        duplicate += 1;
      }
    }
    card.setDuplicateBillingCount(duplicate);
    long missingOrder = medicalRecords.stream()
        .filter(item -> containsAny(item.getSourceType(), "ORDER", "医嘱"))
        .filter(item -> item.getSourceId() == null || item.getSourceId() <= 0)
        .count();
    card.setMissingOrderLinkCount(missingOrder);
    return card;
  }

  private FinanceWorkbenchOverviewResponse.AllocationCard buildAllocationCard(Long orgId, String thisMonth) {
    FinanceWorkbenchOverviewResponse.AllocationCard card = new FinanceWorkbenchOverviewResponse.AllocationCard();
    List<MonthlyAllocation> allocations = monthlyAllocationMapper.selectList(
        Wrappers.lambdaQuery(MonthlyAllocation.class)
            .eq(MonthlyAllocation::getIsDeleted, 0)
            .eq(orgId != null, MonthlyAllocation::getOrgId, orgId)
            .eq(MonthlyAllocation::getAllocationMonth, thisMonth));
    long generated = allocations.stream()
        .filter(item -> containsAny(item.getStatus(), "GENERATED", "COMPLETED", "DONE"))
        .count();
    long error = allocations.stream()
        .filter(item -> containsAny(item.getStatus(), "ERROR", "FAILED"))
        .count();
    int allocatedTarget = allocations.stream()
        .map(MonthlyAllocation::getTargetCount)
        .filter(Objects::nonNull)
        .reduce(0, Integer::sum);
    long roomCount = roomMapper.selectCount(
        Wrappers.lambdaQuery(Room.class)
            .eq(Room::getIsDeleted, 0)
            .eq(orgId != null, Room::getOrgId, orgId));
    card.setMonthGeneratedCount(generated);
    card.setErrorCount(error);
    card.setUngeneratedRoomCount(Math.max(roomCount - allocatedTarget, 0));
    return card;
  }

  private FinanceWorkbenchOverviewResponse.ReconcileCard buildReconcileCard(
      Long orgId,
      String thisMonth,
      List<BillMonthly> thisMonthBills,
      List<PaymentRecord> todayPayments) {
    FinanceWorkbenchOverviewResponse.ReconcileCard card = new FinanceWorkbenchOverviewResponse.ReconcileCard();
    long paidUnmatched = thisMonthBills.stream()
        .filter(item -> safe(item.getPaidAmount()).compareTo(BigDecimal.ZERO) > 0)
        .filter(item -> safe(item.getOutstandingAmount()).compareTo(BigDecimal.ZERO) > 0)
        .count();
    card.setBillPaidUnmatchedCount(paidUnmatched);

    Map<String, Long> externalTxnCount = todayPayments.stream()
        .map(PaymentRecord::getExternalTxnId)
        .filter(item -> item != null && !item.isBlank())
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    long duplicated = externalTxnCount.values().stream().filter(count -> count > 1).mapToLong(Long::longValue).sum();
    Long mismatchCount = reconciliationDailyMapper.selectCount(
        Wrappers.lambdaQuery(ReconciliationDaily.class)
            .eq(ReconciliationDaily::getIsDeleted, 0)
            .eq(orgId != null, ReconciliationDaily::getOrgId, orgId)
            .eq(ReconciliationDaily::getReconcileDate, LocalDate.now())
            .eq(ReconciliationDaily::getMismatchFlag, 1));
    card.setDuplicatedOrReversalPendingCount(duplicated + (mismatchCount == null ? 0L : mismatchCount));

    long unlinkedInvoice = todayPayments.stream()
        .filter(item -> containsAny(item.getRemark(), "发票", "invoice"))
        .filter(item -> item.getBillMonthlyId() == null || item.getBillMonthlyId() <= 0)
        .count();
    card.setInvoiceUnlinkedCount(unlinkedInvoice);
    return card;
  }

  private List<FinanceWorkbenchOverviewResponse.QuickEntry> buildQuickEntries() {
    List<FinanceWorkbenchOverviewResponse.QuickEntry> entries = new ArrayList<>();
    entries.add(quick("cashier_desk", "收银台", "/finance/payments/cashier-desk?from=finance_dashboard"));
    entries.add(quick("issue_center", "异常修复中心", "/finance/reconcile/issue-center?from=finance_dashboard"));
    entries.add(quick("collection_follow_up", "欠费催缴跟进", "/finance/bills/follow-up?from=finance_dashboard"));
    entries.add(quick("month_close", "月结进度", "/finance/reconcile/month-close?from=finance_dashboard"));
    entries.add(quick("prepaid_recharge", "预存充值", "/finance/prepaid-recharge?from=finance_dashboard"));
    entries.add(quick("payment_register", "收款登记", "/finance/payments/register?from=finance_dashboard"));
    entries.add(quick("discharge_settlement", "退住结算", "/finance/discharge/settlement?from=finance_dashboard"));
    entries.add(quick("invoice_receipt", "开票/打印收据", "/finance/fees/payment-and-invoice?from=finance_dashboard"));
    entries.add(quick("fee_adjustment", "费用调整单", "/finance/flows/adjustments?from=finance_dashboard"));
    return entries;
  }

  private FinanceWorkbenchOverviewResponse.ReasonCount reason(String reason, long count) {
    FinanceWorkbenchOverviewResponse.ReasonCount item = new FinanceWorkbenchOverviewResponse.ReasonCount();
    item.setReason(reason);
    item.setCount(count);
    return item;
  }

  private FinanceWorkbenchOverviewResponse.QuickEntry quick(String key, String label, String path) {
    FinanceWorkbenchOverviewResponse.QuickEntry entry = new FinanceWorkbenchOverviewResponse.QuickEntry();
    entry.setKey(key);
    entry.setLabel(label);
    entry.setPath(path);
    return entry;
  }

  private long countStatusRestricted(Long orgId, Set<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      return 0;
    }
    return elderMapper.selectList(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(orgId != null, ElderProfile::getOrgId, orgId)
            .in(ElderProfile::getId, elderIds))
        .stream()
        .filter(item -> item.getStatus() != null && item.getStatus() != 1)
        .count();
  }

  private boolean isMedicalCategory(String category, String sourceType) {
    String text = (normalizeText(category, "") + " " + normalizeText(sourceType, "")).toLowerCase(Locale.ROOT);
    return text.contains("med")
        || text.contains("nurs")
        || text.contains("医")
        || text.contains("护")
        || text.contains("treatment");
  }

  private String normalizeRevenueCategory(String itemType, String itemName) {
    String text = (normalizeText(itemType, "") + " " + normalizeText(itemName, "")).toLowerCase(Locale.ROOT);
    if (text.contains("bed") || text.contains("床")) return "ROOM";
    if (text.contains("nurs") || text.contains("护理")) return "NURSING";
    if (text.contains("medical") || text.contains("医") || text.contains("药")) return "MEDICAL";
    if (text.contains("meal") || text.contains("餐")) return "DINING";
    if (text.contains("utility") || text.contains("水") || text.contains("电") || text.contains("电视")
        || text.contains("网络")) return "UTILITY";
    if (text.contains("service") || text.contains("增购")) return "ADDON";
    return "OTHER";
  }

  private String revenueCategoryLabel(String code) {
    if ("ROOM".equals(code)) return "房费";
    if ("NURSING".equals(code)) return "护理服务";
    if ("MEDICAL".equals(code)) return "医护服务";
    if ("DINING".equals(code)) return "餐饮";
    if ("ADDON".equals(code)) return "增购服务";
    if ("UTILITY".equals(code)) return "水电电视网络";
    return "其他";
  }

  private String normalizePayMethod(String payMethod) {
    String method = normalizeText(payMethod, "UNKNOWN").toUpperCase(Locale.ROOT);
    if (method.equals("WECHAT_OFFLINE")) return "WECHAT_OFFLINE";
    if (method.equals("WECHAT")) return "WECHAT";
    if (method.equals("ALIPAY")) return "ALIPAY";
    if (method.equals("BANK") || method.equals("TRANSFER")) return "BANK";
    if (method.equals("CARD") || method.equals("POS")) return "CARD";
    if (method.equals("CASH")) return "CASH";
    if (method.equals("QR_CODE")) return "QR_CODE";
    return method;
  }

  private String payMethodLabel(String method) {
    if ("CASH".equals(method)) return "现金";
    if ("BANK".equals(method)) return "转账";
    if ("CARD".equals(method)) return "刷卡";
    if ("ALIPAY".equals(method)) return "支付宝";
    if ("WECHAT".equals(method)) return "微信";
    if ("WECHAT_OFFLINE".equals(method)) return "微信线下";
    if ("QR_CODE".equals(method)) return "扫码";
    return method;
  }

  private FinanceModuleEntrySummaryResponse buildModuleEntrySummary(
      String moduleKey,
      Long orgId,
      LocalDate today,
      LocalDateTime startOfToday,
      LocalDateTime endOfToday,
      String thisMonth,
      List<ConsumptionRecord> monthConsumption,
      List<ConsumptionRecord> todayConsumption,
      List<BillMonthly> thisMonthBills,
      List<PaymentRecord> todayPayments) {
    FinanceModuleEntrySummaryResponse response = new FinanceModuleEntrySummaryResponse();
    response.setModuleKey(moduleKey);
    response.setBizDate(today);
    switch (moduleKey) {
      case "MEDICAL_FLOW" -> fillConsumptionEntry(response, monthConsumption, todayConsumption, "MEDICINE", "医护收费存在来源缺失，请核查医嘱、处置与收费映射");
      case "MEDICAL_ERRORS" -> fillMedicalErrorEntry(response, orgId, startOfToday, endOfToday, monthConsumption);
      case "DINING_FLOW" -> fillConsumptionEntry(response, monthConsumption, todayConsumption, "DINING", "餐饮扣费数据缺失，请核查点餐与扣费映射");
      case "LOGISTICS_FLOW" -> fillLogisticsEntry(response, monthConsumption, todayConsumption);
      case "ADJUSTMENTS" -> fillAdjustmentEntry(response, orgId);
      case "DISCHARGE_STATUS_SYNC" -> fillDischargeSyncEntry(response, orgId, thisMonth);
      case "RECONCILE_INVOICE" -> fillReconcileInvoiceEntry(response, orgId, thisMonth, thisMonthBills, todayPayments);
      case "RECONCILE_CENTER" -> fillReconcileCenterEntry(response, orgId, today, thisMonth, thisMonthBills, todayPayments);
      case "BALANCE_WARN" -> fillBalanceWarnEntry(response, orgId, today, todayPayments);
      case "APPROVAL_FLOW" -> fillApprovalFlowEntry(response, orgId);
      default -> fillDefaultEntry(response, monthConsumption, todayConsumption, thisMonthBills);
    }
    return response;
  }

  private void fillMedicalErrorEntry(
      FinanceModuleEntrySummaryResponse response,
      Long orgId,
      LocalDateTime startOfToday,
      LocalDateTime endOfToday,
      List<ConsumptionRecord> monthConsumption) {
    FinanceWorkbenchOverviewResponse.MedicalFlowCard card = buildMedicalFlowCard(orgId, startOfToday, endOfToday);
    List<ConsumptionRecord> medicalMonth = monthConsumption.stream()
        .filter(item -> isMedicalCategory(item.getCategory(), item.getSourceType()))
        .toList();
    response.setTodayAmount(card.getTodayFlowAmount());
    response.setMonthAmount(sumConsumptionAmount(medicalMonth));
    response.setTotalCount(card.getTodayFlowCount());
    response.setPendingCount(card.getPendingReviewCount());
    response.setExceptionCount(card.getDuplicateBillingCount() + card.getMissingOrderLinkCount());
    if (response.getExceptionCount() > 0) {
      response.setWarningMessage("存在医护费用异常，请优先处理重复计费与医嘱缺失");
    }
    response.getTopItems().add(entryItem("今日医护流水", card.getTodayFlowCount(), card.getTodayFlowAmount()));
    response.getTopItems().add(entryItem("重复计费", card.getDuplicateBillingCount(), BigDecimal.ZERO));
    response.getTopItems().add(entryItem("缺少医嘱关联", card.getMissingOrderLinkCount(), BigDecimal.ZERO));
    response.getTopItems().add(entryItem("待审核", card.getPendingReviewCount(), BigDecimal.ZERO));
  }

  private void fillConsumptionEntry(
      FinanceModuleEntrySummaryResponse response,
      List<ConsumptionRecord> monthConsumption,
      List<ConsumptionRecord> todayConsumption,
      String category,
      String warningMessage) {
    List<ConsumptionRecord> monthRows = monthConsumption.stream()
        .filter(item -> category.equalsIgnoreCase(normalizeText(item.getCategory(), "")))
        .toList();
    List<ConsumptionRecord> todayRows = todayConsumption.stream()
        .filter(item -> category.equalsIgnoreCase(normalizeText(item.getCategory(), "")))
        .toList();
    response.setTodayAmount(sumConsumptionAmount(todayRows));
    response.setMonthAmount(sumConsumptionAmount(monthRows));
    response.setTotalCount((long) monthRows.size());
    response.setPendingCount(0L);
    long missingSource = monthRows.stream()
        .filter(item -> item.getSourceId() == null || item.getSourceId() <= 0)
        .count();
    response.setExceptionCount(missingSource);
    if (missingSource > 0) {
      response.setWarningMessage(warningMessage);
    }
    topEldersByConsumption(monthRows).forEach(response.getTopItems()::add);
  }

  private void fillLogisticsEntry(
      FinanceModuleEntrySummaryResponse response,
      List<ConsumptionRecord> monthConsumption,
      List<ConsumptionRecord> todayConsumption) {
    List<ConsumptionRecord> monthRows = monthConsumption.stream()
        .filter(this::isLogisticsConsumption)
        .toList();
    List<ConsumptionRecord> todayRows = todayConsumption.stream()
        .filter(this::isLogisticsConsumption)
        .toList();
    response.setTodayAmount(sumConsumptionAmount(todayRows));
    response.setMonthAmount(sumConsumptionAmount(monthRows));
    response.setTotalCount((long) monthRows.size());
    response.setPendingCount(0L);
    long noCategory = monthRows.stream()
        .filter(item -> normalizeText(item.getCategory(), "").isBlank())
        .count();
    response.setExceptionCount(noCategory);
    if (noCategory > 0) {
      response.setWarningMessage("后勤收费存在未分类记录，请补齐科目映射");
    }
    topEldersByConsumption(monthRows).forEach(response.getTopItems()::add);
  }

  private void fillAdjustmentEntry(FinanceModuleEntrySummaryResponse response, Long orgId) {
    FinanceWorkbenchOverviewResponse.PendingCard pendingCard = buildPendingCard(orgId, LocalDate.now());
    long pending = pendingCard.getPendingDiscountCount() + pendingCard.getPendingRefundCount()
        + pendingCard.getPendingDischargeSettlementCount();
    response.setTotalCount(pending);
    response.setPendingCount(pending);
    response.setExceptionCount(0L);
    response.setTodayAmount(BigDecimal.ZERO);
    response.setMonthAmount(BigDecimal.ZERO);
    if (pending > 0) {
      response.setWarningMessage("当前存在待审核费用调整/退住结算事项");
    }
    response.getTopItems().add(entryItem("待审批减免", pendingCard.getPendingDiscountCount(), BigDecimal.ZERO));
    response.getTopItems().add(entryItem("待审批退款", pendingCard.getPendingRefundCount(), BigDecimal.ZERO));
    response.getTopItems().add(entryItem("待审核退住结算", pendingCard.getPendingDischargeSettlementCount(), BigDecimal.ZERO));
  }

  private void fillBalanceWarnEntry(
      FinanceModuleEntrySummaryResponse response,
      Long orgId,
      LocalDate today,
      List<PaymentRecord> todayPayments) {
    List<BillMonthly> outstandingBills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .gt(BillMonthly::getOutstandingAmount, BigDecimal.ZERO));
    FinanceWorkbenchOverviewResponse.RiskCard riskCard = buildRiskCard(orgId, outstandingBills, today);
    response.setTodayAmount(todayPayments.stream()
        .map(PaymentRecord::getAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add));
    response.setMonthAmount(riskCard.getOverdueAmount());
    response.setTotalCount(riskCard.getOverdueElderCount());
    response.setPendingCount(riskCard.getLowBalanceCount());
    response.setExceptionCount(riskCard.getExpiringContractCount());
    if (riskCard.getOverdueElderCount() > 0 || riskCard.getLowBalanceCount() > 0) {
      response.setWarningMessage("存在欠费或余额预警长者，请尽快催缴并跟进充值");
    }
    response.getTopItems().add(entryItem("欠费长者", riskCard.getOverdueElderCount(), riskCard.getOverdueAmount()));
    response.getTopItems().add(entryItem("余额低于阈值", riskCard.getLowBalanceCount(), BigDecimal.ZERO));
    response.getTopItems().add(entryItem("30天内到期合同", riskCard.getExpiringContractCount(), BigDecimal.ZERO));
  }

  private void fillApprovalFlowEntry(FinanceModuleEntrySummaryResponse response, Long orgId) {
    FinanceWorkbenchOverviewResponse.PendingCard pendingCard = buildPendingCard(orgId, LocalDate.now());
    Long oaPending = oaApprovalMapper.selectCount(
        Wrappers.lambdaQuery(OaApproval.class)
            .eq(OaApproval::getIsDeleted, 0)
            .eq(orgId != null, OaApproval::getOrgId, orgId)
            .eq(OaApproval::getStatus, "PENDING")
            .and(q -> q.like(OaApproval::getTitle, "费用")
                .or().like(OaApproval::getTitle, "退款")
                .or().like(OaApproval::getTitle, "冲正")
                .or().like(OaApproval::getTitle, "减免")));
    long pendingFinance = pendingCard.getPendingDiscountCount() + pendingCard.getPendingRefundCount()
        + pendingCard.getPendingDischargeSettlementCount();
    long oaPendingCount = oaPending == null ? 0L : oaPending;
    long totalPending = pendingFinance + oaPendingCount;
    response.setTodayAmount(BigDecimal.ZERO);
    response.setMonthAmount(BigDecimal.ZERO);
    response.setTotalCount(totalPending);
    response.setPendingCount(totalPending);
    response.setExceptionCount(0L);
    if (totalPending > 0) {
      response.setWarningMessage("当前存在待审批事项，建议优先处理退款与退住结算");
    }
    response.getTopItems().add(entryItem("OA审批待办", oaPendingCount, BigDecimal.ZERO));
    response.getTopItems().add(entryItem("减免待审批", pendingCard.getPendingDiscountCount(), BigDecimal.ZERO));
    response.getTopItems().add(entryItem("退款待审批", pendingCard.getPendingRefundCount(), BigDecimal.ZERO));
    response.getTopItems().add(entryItem("退住结算待审核", pendingCard.getPendingDischargeSettlementCount(), BigDecimal.ZERO));
  }

  private void fillDischargeSyncEntry(FinanceModuleEntrySummaryResponse response, Long orgId, String thisMonth) {
    List<DischargeSettlement> settlements = dischargeSettlementMapper.selectList(
        Wrappers.lambdaQuery(DischargeSettlement.class)
            .eq(DischargeSettlement::getIsDeleted, 0)
            .eq(orgId != null, DischargeSettlement::getOrgId, orgId));
    long settledCount = settlements.stream()
        .filter(item -> FeeWorkflowConstants.SETTLEMENT_SETTLED.equals(item.getStatus()))
        .count();
    long pendingCount = settlements.stream()
        .filter(item -> !FeeWorkflowConstants.SETTLEMENT_SETTLED.equals(item.getStatus()))
        .count();
    long unsynced = settlements.stream()
        .filter(item -> FeeWorkflowConstants.SETTLEMENT_SETTLED.equals(item.getStatus()))
        .filter(item -> item.getFinanceRefunded() == null || item.getFinanceRefunded() != 1)
        .count();
    BigDecimal monthAmount = settlements.stream()
        .filter(item -> item.getCreateTime() != null && thisMonth.equals(YearMonth.from(item.getCreateTime()).toString()))
        .map(DischargeSettlement::getRefundAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal todayAmount = settlements.stream()
        .filter(item -> item.getSettledTime() != null && item.getSettledTime().toLocalDate().isEqual(LocalDate.now()))
        .map(DischargeSettlement::getRefundAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    response.setTodayAmount(todayAmount);
    response.setMonthAmount(monthAmount);
    response.setTotalCount(settledCount);
    response.setPendingCount(pendingCount);
    response.setExceptionCount(unsynced);
    if (unsynced > 0) {
      response.setWarningMessage("存在已结算未完成状态回写记录");
    }
    response.getTopItems().add(entryItem("已结算", settledCount, BigDecimal.ZERO));
    response.getTopItems().add(entryItem("待处理", pendingCount, BigDecimal.ZERO));
    response.getTopItems().add(entryItem("待回写", unsynced, BigDecimal.ZERO));
  }

  private void fillReconcileCenterEntry(
      FinanceModuleEntrySummaryResponse response,
      Long orgId,
      LocalDate today,
      String thisMonth,
      List<BillMonthly> thisMonthBills,
      List<PaymentRecord> todayPayments) {
    FinanceWorkbenchOverviewResponse.ReconcileCard card = buildReconcileCard(orgId, thisMonth, thisMonthBills, todayPayments);
    List<FinanceReconcileExceptionItem> exceptions = buildReconcileExceptions(orgId, today, null);
    response.setTodayAmount(todayPayments.stream()
        .map(PaymentRecord::getAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add));
    response.setMonthAmount(BigDecimal.valueOf(card.getBillPaidUnmatchedCount() == null ? 0 : card.getBillPaidUnmatchedCount())
        .add(BigDecimal.valueOf(card.getDuplicatedOrReversalPendingCount() == null ? 0 : card.getDuplicatedOrReversalPendingCount()))
        .add(BigDecimal.valueOf(card.getInvoiceUnlinkedCount() == null ? 0 : card.getInvoiceUnlinkedCount())));
    response.setTotalCount((long) exceptions.size());
    response.setPendingCount((long) (card.getBillPaidUnmatchedCount() + card.getDuplicatedOrReversalPendingCount()));
    response.setExceptionCount((long) card.getInvoiceUnlinkedCount());
    if (!exceptions.isEmpty()) {
      response.setWarningMessage("存在对账异常，请优先处理重复收款与账单未核销问题");
    }
    response.getTopItems().add(entryItem("账单已收未核销", card.getBillPaidUnmatchedCount(), BigDecimal.ZERO));
    response.getTopItems().add(entryItem("重复/冲正未完成", card.getDuplicatedOrReversalPendingCount(), BigDecimal.ZERO));
    response.getTopItems().add(entryItem("发票未关联", card.getInvoiceUnlinkedCount(), BigDecimal.ZERO));
  }

  private void fillReconcileInvoiceEntry(
      FinanceModuleEntrySummaryResponse response,
      Long orgId,
      String thisMonth,
      List<BillMonthly> thisMonthBills,
      List<PaymentRecord> todayPayments) {
    FinanceWorkbenchOverviewResponse.ReconcileCard card = buildReconcileCard(orgId, thisMonth, thisMonthBills, todayPayments);
    response.setTodayAmount(todayPayments.stream()
        .map(PaymentRecord::getAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add));
    response.setMonthAmount(thisMonthBills.stream()
        .map(BillMonthly::getPaidAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add));
    response.setTotalCount((long) todayPayments.size());
    response.setPendingCount(card.getInvoiceUnlinkedCount());
    response.setExceptionCount(card.getBillPaidUnmatchedCount() + card.getDuplicatedOrReversalPendingCount());
    if (response.getExceptionCount() > 0 || response.getPendingCount() > 0) {
      response.setWarningMessage("存在对账异常或发票未关联账单，请及时核销");
    }
    response.getTopItems().add(entryItem("账单已收未核销", card.getBillPaidUnmatchedCount(), BigDecimal.ZERO));
    response.getTopItems().add(entryItem("重复/冲正异常", card.getDuplicatedOrReversalPendingCount(), BigDecimal.ZERO));
    response.getTopItems().add(entryItem("发票未关联", card.getInvoiceUnlinkedCount(), BigDecimal.ZERO));
  }

  private void fillDefaultEntry(
      FinanceModuleEntrySummaryResponse response,
      List<ConsumptionRecord> monthConsumption,
      List<ConsumptionRecord> todayConsumption,
      List<BillMonthly> thisMonthBills) {
    response.setTodayAmount(sumConsumptionAmount(todayConsumption));
    response.setMonthAmount(sumConsumptionAmount(monthConsumption));
    response.setTotalCount((long) monthConsumption.size());
    long pending = thisMonthBills.stream()
        .filter(item -> safe(item.getOutstandingAmount()).compareTo(BigDecimal.ZERO) > 0)
        .count();
    response.setPendingCount(pending);
    response.setExceptionCount(0L);
  }

  private boolean isLogisticsConsumption(ConsumptionRecord item) {
    String category = normalizeText(item.getCategory(), "").toUpperCase(Locale.ROOT);
    String sourceType = normalizeText(item.getSourceType(), "").toUpperCase(Locale.ROOT);
    String remark = normalizeText(item.getRemark(), "").toLowerCase(Locale.ROOT);
    if ("DINING".equals(category) || "MEDICINE".equals(category)) {
      return false;
    }
    return sourceType.contains("LOGISTICS")
        || sourceType.contains("MATERIAL")
        || remark.contains("后勤")
        || remark.contains("物资")
        || "OTHER".equals(category);
  }

  private BigDecimal sumConsumptionAmount(List<ConsumptionRecord> rows) {
    return rows.stream()
        .map(ConsumptionRecord::getAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private List<FinanceModuleEntrySummaryResponse.EntryItem> topEldersByConsumption(List<ConsumptionRecord> rows) {
    Map<String, BigDecimal> elderAmountMap = new HashMap<>();
    Map<String, Long> elderCountMap = new HashMap<>();
    for (ConsumptionRecord item : rows) {
      String elderName = normalizeText(item.getElderName(), "未识别长者");
      elderAmountMap.merge(elderName, safe(item.getAmount()), BigDecimal::add);
      elderCountMap.merge(elderName, 1L, Long::sum);
    }
    return elderAmountMap.entrySet().stream()
        .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
        .limit(5)
        .map(entry -> entryItem(entry.getKey(), elderCountMap.getOrDefault(entry.getKey(), 0L), entry.getValue()))
        .toList();
  }

  private FinanceModuleEntrySummaryResponse.EntryItem entryItem(String label, long count, BigDecimal amount) {
    FinanceModuleEntrySummaryResponse.EntryItem item = new FinanceModuleEntrySummaryResponse.EntryItem();
    item.setLabel(label);
    item.setCount(count);
    item.setAmount(amount == null ? BigDecimal.ZERO : amount);
    return item;
  }

  private BigDecimal percentage(BigDecimal value, BigDecimal total) {
    if (value == null || total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return value
        .multiply(BigDecimal.valueOf(100))
        .divide(total, 2, RoundingMode.HALF_UP);
  }

  private BigDecimal safe(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private String normalizeText(String value, String defaultValue) {
    if (value == null || value.isBlank()) {
      return defaultValue;
    }
    return value.trim();
  }

  private boolean containsAny(String value, String... candidates) {
    if (value == null || value.isBlank()) {
      return false;
    }
    String source = value.toLowerCase(Locale.ROOT);
    for (String candidate : candidates) {
      if (candidate != null && !candidate.isBlank() && source.contains(candidate.toLowerCase(Locale.ROOT))) {
        return true;
      }
    }
    return false;
  }

  private void markInvalidMeterRow(
      FinanceAllocationMeterValidateResponse.RowResult result,
      String code,
      String message) {
    result.setValid(false);
    result.setLevel("ERROR");
    result.setCode(code);
    result.setMessage(message);
  }

  private FinanceConfigChangeLogItem toConfigLogItem(AuditLog item) {
    FinanceConfigChangeLogItem row = new FinanceConfigChangeLogItem();
    row.setId(item.getId());
    row.setActorId(item.getActorId());
    row.setActorName(item.getActorName());
    row.setActionType(item.getActionType());
    row.setEntityType(item.getEntityType());
    row.setEntityId(item.getEntityId());
    row.setDetail(item.getDetail());
    row.setCreateTime(item.getCreateTime());
    return row;
  }

  private FinanceAllocationRuleItem toAllocationRuleItem(BillingConfigEntry item) {
    FinanceAllocationRuleItem row = new FinanceAllocationRuleItem();
    row.setId(item.getId());
    row.setConfigKey(item.getConfigKey());
    row.setConfigValue(item.getConfigValue());
    row.setEffectiveMonth(item.getEffectiveMonth());
    row.setStatus(item.getStatus());
    row.setRemark(item.getRemark());
    row.setRuleType(resolveRuleType(item.getConfigKey()));
    row.setRuleTypeLabel(resolveRuleTypeLabel(row.getRuleType()));
    return row;
  }

  private String resolveRuleType(String configKey) {
    String key = normalizeText(configKey, "").toLowerCase(Locale.ROOT);
    if (key.contains("water") || key.contains("electric") || key.contains("tv")
        || key.contains("network") || key.contains("utility")
        || key.contains("水") || key.contains("电") || key.contains("电视") || key.contains("网络")) {
      return "UTILITY";
    }
    if (key.contains("room") || key.contains("bed") || key.contains("房") || key.contains("床")) {
      return "ROOM";
    }
    if (key.contains("person") || key.contains("人数")) {
      return "PERSON";
    }
    if (key.contains("day") || key.contains("天数")) {
      return "DAYS";
    }
    if (key.contains("area") || key.contains("面积")) {
      return "AREA";
    }
    return "GENERAL";
  }

  private String resolveRuleTypeLabel(String ruleType) {
    if ("UTILITY".equals(ruleType)) return "水电电视网络";
    if ("ROOM".equals(ruleType)) return "按房间/床位";
    if ("PERSON".equals(ruleType)) return "按人数";
    if ("DAYS".equals(ruleType)) return "按天数";
    if ("AREA".equals(ruleType)) return "按面积";
    return "通用规则";
  }

  private boolean isAllocationOrUtilityRule(String configKey) {
    String key = normalizeText(configKey, "").toLowerCase(Locale.ROOT);
    return key.contains("alloc")
        || key.contains("share")
        || key.contains("utility")
        || key.contains("water")
        || key.contains("electric")
        || key.contains("tv")
        || key.contains("network")
        || key.contains("room")
        || key.contains("bed")
        || key.contains("水")
        || key.contains("电")
        || key.contains("电视")
        || key.contains("网络")
        || key.contains("分摊")
        || key.contains("房")
        || key.contains("床");
  }

  private List<FinanceBillingConfigUpsertRequest> defaultAllocationTemplates(String effectiveMonth) {
    List<FinanceBillingConfigUpsertRequest> templates = new ArrayList<>();
    templates.add(rule("ALLOC_ELECTRIC_FREE_KWH_PER_PERSON", BigDecimal.TEN, effectiveMonth,
        "每位老人每月赠送免费电量（度）"));
    templates.add(rule("ALLOC_ELECTRIC_FREE_KWH_DOUBLE_ROOM", BigDecimal.valueOf(20), effectiveMonth,
        "双人满住每月赠送免费电量（度）"));
    templates.add(rule("ALLOC_ELECTRIC_BASE_PRICE", BigDecimal.ONE, effectiveMonth,
        "基础电价（元/度）"));
    templates.add(rule("ALLOC_ELECTRIC_SPLIT_MODE", BigDecimal.ONE, effectiveMonth,
        "电费分摊模式：1=按在住人数均摊"));
    templates.add(rule("ALLOC_WATER_BASE_PRICE", BigDecimal.ZERO, effectiveMonth,
        "水费基础价格（默认0元）"));
    templates.add(rule("ALLOC_TV_BASE_PRICE", BigDecimal.ZERO, effectiveMonth,
        "电视费基础价格（默认0元）"));
    templates.add(rule("ALLOC_NETWORK_BASE_PRICE", BigDecimal.ZERO, effectiveMonth,
        "网络费基础价格（默认0元）"));
    return templates;
  }

  private FinanceBillingConfigUpsertRequest rule(
      String key,
      BigDecimal value,
      String effectiveMonth,
      String remark) {
    FinanceBillingConfigUpsertRequest request = new FinanceBillingConfigUpsertRequest();
    request.setConfigKey(key);
    request.setConfigValue(value);
    request.setEffectiveMonth(effectiveMonth);
    request.setStatus(1);
    request.setRemark(remark);
    return request;
  }

  private FinanceReconcileExceptionItem reconcileItem(
      String type,
      String label,
      Long billId,
      Long paymentId,
      Long elderId,
      String elderName,
      BigDecimal amount,
      String detail,
      String suggestion,
      LocalDateTime occurredAt) {
    FinanceReconcileExceptionItem item = new FinanceReconcileExceptionItem();
    item.setExceptionType(type);
    item.setExceptionTypeLabel(label);
    item.setBillId(billId);
    item.setPaymentId(paymentId);
    item.setElderId(elderId);
    item.setElderName(elderName);
    item.setAmount(amount);
    item.setDetail(detail);
    item.setSuggestion(suggestion);
    item.setOccurredAt(occurredAt);
    return item;
  }

  private FinanceDischargeStatusSyncResponse buildDischargeStatusSyncResponse(Long orgId, int limit) {
    FinanceDischargeStatusSyncResponse response = new FinanceDischargeStatusSyncResponse();
    response.setCheckedAt(LocalDateTime.now());
    List<DischargeSettlement> settlements = dischargeSettlementMapper.selectList(
        Wrappers.lambdaQuery(DischargeSettlement.class)
            .eq(DischargeSettlement::getIsDeleted, 0)
            .eq(orgId != null, DischargeSettlement::getOrgId, orgId)
            .eq(DischargeSettlement::getStatus, FeeWorkflowConstants.SETTLEMENT_SETTLED)
            .orderByDesc(DischargeSettlement::getSettledTime)
            .last("LIMIT " + Math.max(limit, 1)));
    response.setSettledCount((long) settlements.size());
    if (settlements.isEmpty()) {
      response.setIssueCount(0L);
      return response;
    }
    List<Long> elderIds = settlements.stream()
        .map(DischargeSettlement::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectList(
            Wrappers.lambdaQuery(ElderProfile.class)
                .eq(ElderProfile::getIsDeleted, 0)
                .eq(orgId != null, ElderProfile::getOrgId, orgId)
                .in(ElderProfile::getId, elderIds))
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (a, b) -> a));
    Map<Long, Bed> occupiedBedMap = elderIds.isEmpty()
        ? Map.of()
        : bedMapper.selectList(
            Wrappers.lambdaQuery(Bed.class)
                .eq(Bed::getIsDeleted, 0)
                .eq(orgId != null, Bed::getOrgId, orgId)
                .in(Bed::getElderId, elderIds))
            .stream()
            .filter(item -> item.getElderId() != null)
            .collect(Collectors.toMap(Bed::getElderId, Function.identity(), (a, b) -> a));

    for (DischargeSettlement settlement : settlements) {
      ElderProfile elder = settlement.getElderId() == null ? null : elderMap.get(settlement.getElderId());
      Bed occupiedBed = settlement.getElderId() == null ? null : occupiedBedMap.get(settlement.getElderId());
      String issueType = null;
      String issueMessage = null;
      if (elder == null) {
        issueType = "ELDER_MISSING";
        issueMessage = "老人档案不存在，无法校验退住回写";
      } else if (!Integer.valueOf(3).equals(elder.getStatus())) {
        issueType = "ELDER_STATUS_NOT_DISCHARGED";
        issueMessage = "老人状态未回写为“已退住”";
      } else if (elder.getBedId() != null || occupiedBed != null) {
        issueType = "BED_NOT_RELEASED";
        issueMessage = "床位未释放，仍存在占用关系";
      }
      if (issueType == null) {
        continue;
      }
      FinanceDischargeStatusSyncResponse.Row row = new FinanceDischargeStatusSyncResponse.Row();
      row.setSettlementId(settlement.getId());
      row.setElderId(settlement.getElderId());
      row.setElderName(settlement.getElderName());
      row.setSettledTime(settlement.getSettledTime());
      row.setSettlementStatus(settlement.getStatus());
      row.setElderStatus(elder == null ? null : elder.getStatus());
      row.setElderBedId(elder == null ? null : elder.getBedId());
      row.setOccupiedBedId(occupiedBed == null ? null : occupiedBed.getId());
      row.setIssueType(issueType);
      row.setIssueMessage(issueMessage);
      response.getRows().add(row);
    }
    response.setIssueCount((long) response.getRows().size());
    return response;
  }

  private FinanceLedgerHealthResponse.IssueItem ledgerIssue(
      String issueType,
      String issueTypeLabel,
      Long billId,
      Long paymentId,
      Long elderId,
      String elderName,
      BigDecimal expectedAmount,
      BigDecimal actualAmount,
      String detail) {
    FinanceLedgerHealthResponse.IssueItem item = new FinanceLedgerHealthResponse.IssueItem();
    item.setIssueType(issueType);
    item.setIssueTypeLabel(issueTypeLabel);
    item.setBillId(billId);
    item.setPaymentId(paymentId);
    item.setElderId(elderId);
    item.setElderName(elderName);
    item.setExpectedAmount(expectedAmount == null ? BigDecimal.ZERO : expectedAmount);
    item.setActualAmount(actualAmount == null ? BigDecimal.ZERO : actualAmount);
    item.setDetail(detail);
    return item;
  }

  private FinanceInvoiceReceiptItem toInvoiceItem(
      PaymentRecord payment,
      Map<Long, BillMonthly> billMap,
      Map<Long, ElderProfile> elderMap) {
    FinanceInvoiceReceiptItem item = new FinanceInvoiceReceiptItem();
    item.setPaymentId(payment.getId());
    item.setBillId(payment.getBillMonthlyId());
    BillMonthly bill = billMap.get(payment.getBillMonthlyId());
    Long elderId = bill == null ? null : bill.getElderId();
    item.setElderId(elderId);
    item.setElderName(elderMap.get(elderId) == null ? null : elderMap.get(elderId).getFullName());
    item.setAmount(safe(payment.getAmount()));
    item.setPayMethod(normalizePayMethod(payment.getPayMethod()));
    item.setPayMethodLabel(payMethodLabel(item.getPayMethod()));
    boolean linked = containsAny(payment.getRemark(), "发票", "invoice", "收据");
    item.setInvoiceStatus(linked ? "LINKED" : "UNLINKED");
    item.setInvoiceStatusLabel(linked ? "已关联" : "未关联");
    item.setReceiptNo(normalizeText(payment.getExternalTxnId(), "RCPT-" + payment.getId()));
    item.setRemark(payment.getRemark());
    item.setPaidAt(payment.getPaidAt());
    return item;
  }

  private boolean filterInvoiceItem(FinanceInvoiceReceiptItem item, String invoiceStatus, String keyword) {
    if (invoiceStatus != null && !invoiceStatus.isBlank() && !invoiceStatus.equalsIgnoreCase(item.getInvoiceStatus())) {
      return false;
    }
    if (keyword == null || keyword.isBlank()) {
      return true;
    }
    String text = keyword.trim().toLowerCase(Locale.ROOT);
    return containsAny(item.getElderName(), text)
        || containsAny(item.getReceiptNo(), text)
        || containsAny(item.getRemark(), text);
  }

  private List<FinanceInvoiceReceiptItem> queryInvoiceReceiptItems(
      Long orgId,
      LocalDate targetDate,
      String method,
      String invoiceStatus,
      String keyword) {
    LocalDateTime start = targetDate.atStartOfDay();
    LocalDateTime end = targetDate.plusDays(1).atStartOfDay();
    List<PaymentRecord> payments = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(orgId != null, PaymentRecord::getOrgId, orgId)
            .ge(PaymentRecord::getPaidAt, start)
            .lt(PaymentRecord::getPaidAt, end)
            .eq(method != null && !method.isBlank(), PaymentRecord::getPayMethod, method)
            .orderByDesc(PaymentRecord::getPaidAt));

    List<Long> billIds = payments.stream().map(PaymentRecord::getBillMonthlyId).filter(Objects::nonNull).distinct().toList();
    Map<Long, BillMonthly> billMap = billIds.isEmpty()
        ? Map.of()
        : billMonthlyMapper.selectBatchIds(billIds)
            .stream()
            .collect(Collectors.toMap(BillMonthly::getId, Function.identity(), (a, b) -> a));
    List<Long> elderIds = billMap.values().stream().map(BillMonthly::getElderId).filter(Objects::nonNull).distinct().toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds)
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (a, b) -> a));

    return payments.stream()
        .map(payment -> toInvoiceItem(payment, billMap, elderMap))
        .filter(item -> filterInvoiceItem(item, invoiceStatus, keyword))
        .toList();
  }

  private List<FinancePaymentAdjustmentItem> queryPaymentAdjustmentItems(Long orgId, YearMonth targetMonth) {
    String month = targetMonth.toString();
    LocalDateTime startTime = targetMonth.atDay(1).atStartOfDay();
    LocalDateTime endTime = targetMonth.plusMonths(1).atDay(1).atStartOfDay();

    List<BillMonthly> invalidBills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getBillMonth, month)
            .eq(BillMonthly::getStatus, 9)
            .orderByDesc(BillMonthly::getUpdateTime)
            .orderByDesc(BillMonthly::getId));

    List<PaymentRecord> adjustedPayments = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(orgId != null, PaymentRecord::getOrgId, orgId)
            .ge(PaymentRecord::getPaidAt, startTime)
            .lt(PaymentRecord::getPaidAt, endTime)
            .orderByDesc(PaymentRecord::getPaidAt)
            .orderByDesc(PaymentRecord::getId))
        .stream()
        .filter(item -> isPaymentAdjustmentRemark(item.getRemark()))
        .toList();

    Set<Long> elderIds = new HashSet<>();
    invalidBills.stream().map(BillMonthly::getElderId).filter(Objects::nonNull).forEach(elderIds::add);
    Set<Long> billIds = adjustedPayments.stream()
        .map(PaymentRecord::getBillMonthlyId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Map<Long, BillMonthly> paymentBillMap = billIds.isEmpty()
        ? Map.of()
        : billMonthlyMapper.selectBatchIds(billIds)
            .stream()
            .collect(Collectors.toMap(BillMonthly::getId, Function.identity(), (a, b) -> a));
    paymentBillMap.values().stream().map(BillMonthly::getElderId).filter(Objects::nonNull).forEach(elderIds::add);
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(new ArrayList<>(elderIds))
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (a, b) -> a));
    List<OaApproval> approvals = oaApprovalMapper.selectList(
        Wrappers.lambdaQuery(OaApproval.class)
            .eq(OaApproval::getIsDeleted, 0)
            .eq(orgId != null, OaApproval::getOrgId, orgId)
            .eq(OaApproval::getApprovalType, "REIMBURSE")
            .orderByDesc(OaApproval::getCreateTime));
    Map<String, OaApproval> approvalMap = approvals.stream()
        .filter(item -> containsAny(item.getFormData(), "PAYMENT_ADJUSTMENT"))
        .collect(Collectors.toMap(
            this::paymentAdjustmentApprovalKey,
            Function.identity(),
            (a, b) -> a.getUpdateTime() != null && b.getUpdateTime() != null && a.getUpdateTime().isAfter(b.getUpdateTime()) ? a : b));

    List<FinancePaymentAdjustmentItem> rows = new ArrayList<>();
    for (BillMonthly bill : invalidBills) {
      FinancePaymentAdjustmentItem row = new FinancePaymentAdjustmentItem();
      row.setType("INVALID_BILL");
      row.setTypeLabel("作废账单");
      row.setBillId(bill.getId());
      row.setElderId(bill.getElderId());
      ElderProfile elder = elderMap.get(bill.getElderId());
      row.setElderName(elder == null ? null : elder.getFullName());
      row.setAmount(safe(bill.getTotalAmount()));
      row.setStatus("INVALID");
      row.setDetail("账单已作废，需复核是否需要重新生成或回补收款");
      row.setRemark(safe(bill.getOutstandingAmount()).compareTo(BigDecimal.ZERO) > 0 ? "作废时仍存在欠费金额" : "已作废");
      fillPaymentAdjustmentApproval(row, approvalMap.get(paymentAdjustmentApprovalKey("INVALID_BILL", bill.getId(), null)));
      row.setOccurredAt(bill.getUpdateTime() == null ? bill.getCreateTime() : bill.getUpdateTime());
      rows.add(row);
    }
    for (PaymentRecord payment : adjustedPayments) {
      FinancePaymentAdjustmentItem row = new FinancePaymentAdjustmentItem();
      row.setType("PAYMENT_ADJUSTMENT");
      row.setTypeLabel("收款修正");
      row.setBillId(payment.getBillMonthlyId());
      row.setPaymentId(payment.getId());
      BillMonthly bill = paymentBillMap.get(payment.getBillMonthlyId());
      Long elderId = bill == null ? null : bill.getElderId();
      row.setElderId(elderId);
      ElderProfile elder = elderMap.get(elderId);
      row.setElderName(elder == null ? null : elder.getFullName());
      row.setAmount(safe(payment.getAmount()));
      row.setStatus("ADJUSTED");
      row.setDetail(paymentAdjustmentDetail(payment.getRemark()));
      row.setRemark(payment.getRemark());
      fillPaymentAdjustmentApproval(row, approvalMap.get(paymentAdjustmentApprovalKey("PAYMENT_ADJUSTMENT", payment.getBillMonthlyId(), payment.getId())));
      row.setOccurredAt(payment.getPaidAt() == null ? payment.getUpdateTime() : payment.getPaidAt());
      rows.add(row);
    }
    rows.sort(Comparator.comparing(FinancePaymentAdjustmentItem::getOccurredAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed());
    return rows;
  }

  private boolean isPaymentAdjustmentRemark(String remark) {
    String normalized = normalizeText(remark, "");
    if (normalized.isBlank()) {
      return false;
    }
    String upper = normalized.toUpperCase(Locale.ROOT);
    return upper.contains("支付方式变更")
        || normalized.contains("冲正")
        || normalized.contains("退款")
        || normalized.contains("作废");
  }

  private void fillPaymentAdjustmentApproval(FinancePaymentAdjustmentItem item, OaApproval approval) {
    if (item == null || approval == null) {
      return;
    }
    item.setApprovalId(approval.getId());
    item.setApprovalStatus(approval.getStatus());
    item.setApprovalStatusLabel(approvalStatusLabel(approval.getStatus()));
    item.setApprovalTitle(approval.getTitle());
  }

  private String paymentAdjustmentApprovalKey(String type, Long billId, Long paymentId) {
    return normalizeText(type, "") + "|" + stringOf(billId) + "|" + stringOf(paymentId);
  }

  private String paymentAdjustmentApprovalKey(OaApproval approval) {
    Map<String, Object> form = readAuditDetail(approval.getFormData());
    return paymentAdjustmentApprovalKey(
        stringValue(form.get("type")),
        toLong(form.get("billId")),
        toLong(form.get("paymentId")));
  }

  private String paymentAdjustmentDetail(String remark) {
    String normalized = normalizeText(remark, "");
    if (normalized.isBlank()) {
      return "收款记录发生修正";
    }
    if (normalized.contains("支付方式变更")) {
      return normalized;
    }
    if (normalized.contains("冲正")) {
      return "收款已标记为冲正处理";
    }
    if (normalized.contains("退款")) {
      return "收款已标记为退款处理";
    }
    if (normalized.contains("作废")) {
      return "收款已标记为作废处理";
    }
    return normalized;
  }

  private FinanceHandleLogItem recordHandleAction(
      Long orgId,
      String entityType,
      FinanceHandleActionRequest request,
      Long entityId,
      String defaultStatus) {
    Map<String, Object> detail = new LinkedHashMap<>();
    detail.put("sourceModule", normalizeText(request.getSourceModule(), ""));
    detail.put("issueType", normalizeText(request.getIssueType(), ""));
    detail.put("billId", request.getBillId());
    detail.put("paymentId", request.getPaymentId());
    detail.put("elderId", request.getElderId());
    detail.put("status", normalizeText(request.getStatus(), defaultStatus));
    detail.put("stage", normalizeText(request.getStage(), ""));
    detail.put("nextAction", normalizeText(request.getNextAction(), ""));
    detail.put("remark", normalizeText(request.getRemark(), ""));
    detail.put("promisedDate", request.getPromisedDate() == null ? null : request.getPromisedDate().toString());
    detail.put("ownerName", normalizeText(request.getOwnerName(), ""));
    detail.put("dueDate", request.getDueDate() == null ? null : request.getDueDate().toString());
    detail.put("reviewResult", normalizeText(request.getReviewResult(), ""));
    detail.put("contactName", normalizeText(request.getContactName(), ""));
    detail.put("contactChannel", normalizeText(request.getContactChannel(), ""));
    detail.put("contactResult", normalizeText(request.getContactResult(), ""));
    detail.put("nextReminderAt", request.getNextReminderAt() == null ? null : request.getNextReminderAt().toString());
    AuditLog log = writeAuditLog(orgId, "FINANCE_HANDLE", entityType, entityId, detail);
    return toHandleLogItem(log, detail);
  }

  private List<FinanceHandleLogItem> queryHandleLogs(
      Long orgId,
      String entityType,
      Long billId,
      Long paymentId,
      Long elderId,
      String sourceModule,
      int limit) {
    List<AuditLog> logs = auditLogMapper.selectList(
        Wrappers.lambdaQuery(AuditLog.class)
            .eq(AuditLog::getOrgId, orgId)
            .eq(AuditLog::getEntityType, entityType)
            .orderByDesc(AuditLog::getCreateTime)
            .last("LIMIT " + Math.max(limit, 1)));
    List<FinanceHandleLogItem> result = new ArrayList<>();
    for (AuditLog log : logs) {
      Map<String, Object> detail = readAuditDetail(log.getDetail());
      if (billId != null && !Objects.equals(toLong(detail.get("billId")), billId)) {
        continue;
      }
      if (paymentId != null && !Objects.equals(toLong(detail.get("paymentId")), paymentId)) {
        continue;
      }
      if (elderId != null && !Objects.equals(toLong(detail.get("elderId")), elderId)) {
        continue;
      }
      if (sourceModule != null && !sourceModule.isBlank()
          && !normalizeText(sourceModule, "").equalsIgnoreCase(stringValue(detail.get("sourceModule")))) {
        continue;
      }
      result.add(toHandleLogItem(log, detail));
    }
    return result;
  }

  private FinanceHandleLogItem toHandleLogItem(AuditLog log, Map<String, Object> detail) {
    FinanceHandleLogItem item = new FinanceHandleLogItem();
    item.setId(log.getId());
    item.setSourceModule(stringValue(detail.get("sourceModule")));
    item.setSourceModuleLabel(handleSourceModuleLabel(item.getSourceModule()));
    item.setIssueType(stringValue(detail.get("issueType")));
    item.setIssueTypeLabel(handleIssueTypeLabel(item.getIssueType()));
    item.setStatus(stringValue(detail.get("status")));
    item.setStatusLabel(handleStatusLabel(item.getStatus()));
    item.setStage(stringValue(detail.get("stage")));
    item.setStageLabel(collectionStageLabel(item.getStage()));
    item.setNextAction(stringValue(detail.get("nextAction")));
    item.setRemark(stringValue(detail.get("remark")));
    item.setPromisedDate(toLocalDate(detail.get("promisedDate")));
    item.setOwnerName(stringValue(detail.get("ownerName")));
    item.setDueDate(toLocalDate(detail.get("dueDate")));
    item.setReviewResult(stringValue(detail.get("reviewResult")));
    item.setContactName(stringValue(detail.get("contactName")));
    item.setContactChannel(stringValue(detail.get("contactChannel")));
    item.setContactResult(stringValue(detail.get("contactResult")));
    item.setNextReminderAt(toLocalDateTime(detail.get("nextReminderAt")));
    item.setBillId(toLong(detail.get("billId")));
    item.setPaymentId(toLong(detail.get("paymentId")));
    item.setElderId(toLong(detail.get("elderId")));
    item.setActorName(log.getActorName());
    item.setCreateTime(log.getCreateTime());
    return item;
  }

  private AuditLog writeAuditLog(
      Long orgId,
      String actionType,
      String entityType,
      Long entityId,
      Map<String, Object> detail) {
    AuditLog log = new AuditLog();
    log.setTenantId(orgId);
    log.setOrgId(orgId);
    log.setActorId(AuthContext.getStaffId());
    log.setActorName(AuthContext.getUsername());
    log.setActionType(actionType);
    log.setEntityType(entityType);
    log.setEntityId(entityId);
    log.setDetail(toJson(detail));
    auditLogMapper.insert(log);
    return log;
  }

  private Map<String, Object> readAuditDetail(String detail) {
    if (detail == null || detail.isBlank()) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(detail, new TypeReference<Map<String, Object>>() {});
    } catch (Exception ex) {
      return Map.of("raw", detail);
    }
  }

  private String toJson(Map<String, Object> payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (Exception ex) {
      return "{}";
    }
  }

  private List<FinanceIssueCenterItem> buildIssueCenterItems(Long orgId, LocalDate targetDate) {
    List<FinanceIssueCenterItem> rows = new ArrayList<>();
    Map<String, FinanceHandleLogItem> latestHandleMap = latestHandleLogMap(
        queryHandleLogs(orgId, "FINANCE_ISSUE_HANDLE", null, null, null, null, 500));
    FinanceLedgerHealthResponse ledger = ledgerHealth(200).getData();
    if (ledger != null && ledger.getIssues() != null) {
      for (FinanceLedgerHealthResponse.IssueItem issue : ledger.getIssues()) {
        BigDecimal amount = safe(issue.getExpectedAmount()).max(safe(issue.getActualAmount()));
        String riskLevel = issueCenterRiskLevel("LEDGER", issue.getIssueType(), amount, issue.getDetail());
        FinanceIssueCenterItem item = issueCenterItem(
            "LEDGER",
            "一致性巡检",
            issue.getIssueType(),
            issue.getIssueTypeLabel(),
            riskLevel,
            issue.getBillId(),
            issue.getPaymentId(),
            issue.getElderId(),
            issue.getElderName(),
            amount,
            issue.getDetail(),
            "优先校正账单与收款、流水之间的落账关系",
            issue.getBillId() == null ? "/finance/reconcile/ledger-health" : "/finance/bill/" + issue.getBillId(),
            issue.getBillId() == null ? "查看巡检" : "查看账单",
            ledger.getCheckedAt());
        applyLatestHandle(item, latestHandleMap.get(issueHandleKey(item.getSourceModule(), item.getBillId(), item.getPaymentId(), item.getElderId())));
        rows.add(item);
      }
    }

    for (FinanceReconcileExceptionItem issue : buildReconcileExceptions(orgId, targetDate, null)) {
      String riskLevel = issueCenterRiskLevel("RECONCILE", issue.getExceptionType(), issue.getAmount(), issue.getDetail());
      String actionPath = switch (normalizeText(issue.getExceptionType(), "")) {
        case "INVOICE_UNLINKED" -> "/finance/reconcile/invoice?filter=unlinked";
        case "DUPLICATED_PAYMENT" -> "/finance/payments/refund-reversal";
        default -> issue.getBillId() == null ? "/finance/reconcile/center?filter=unmatched" : "/finance/bill/" + issue.getBillId();
      };
      FinanceIssueCenterItem item = issueCenterItem(
          "RECONCILE",
          "对账中心",
          issue.getExceptionType(),
          issue.getExceptionTypeLabel(),
          riskLevel,
          issue.getBillId(),
          issue.getPaymentId(),
          issue.getElderId(),
          issue.getElderName(),
          issue.getAmount(),
          issue.getDetail(),
          issue.getSuggestion(),
          actionPath,
          "去处理",
          issue.getOccurredAt());
      applyLatestHandle(item, latestHandleMap.get(issueHandleKey(item.getSourceModule(), item.getBillId(), item.getPaymentId(), item.getElderId())));
      rows.add(item);
    }

    for (FinanceAutoDebitExceptionItem issue : autoDebitExceptions(targetDate.toString()).getData()) {
      String detail = normalizeText(issue.getReasonLabel(), "自动扣费异常") + "，账期 " + normalizeText(issue.getBillMonth(), "-");
      FinanceIssueCenterItem item = issueCenterItem(
          "AUTO_DEBIT",
          "催缴异常",
          issue.getReasonCode(),
          issue.getReasonLabel(),
          issueCenterRiskLevel("AUTO_DEBIT", issue.getReasonCode(), issue.getOutstandingAmount(), detail),
          issue.getBillId(),
          null,
          issue.getElderId(),
          issue.getElderName(),
          issue.getOutstandingAmount(),
          detail,
          issue.getSuggestion(),
          issue.getBillId() == null ? "/finance/bills/auto-deduct-errors" : "/finance/bill/" + issue.getBillId(),
          issue.getBillId() == null ? "查看异常" : "查看账单",
          targetDate.atTime(18, 0));
      applyLatestHandle(item, latestHandleMap.get(issueHandleKey(item.getSourceModule(), item.getBillId(), item.getPaymentId(), item.getElderId())));
      rows.add(item);
    }

    YearMonth targetMonth = YearMonth.from(targetDate);
    for (FinancePaymentAdjustmentItem item : queryPaymentAdjustmentItems(orgId, targetMonth).stream()
        .filter(row -> row.getOccurredAt() != null && row.getOccurredAt().toLocalDate().isEqual(targetDate))
        .toList()) {
      String detail = normalizeText(item.getDetail(), item.getTypeLabel());
      FinanceIssueCenterItem issueItem = issueCenterItem(
          "PAYMENT_ADJUSTMENT",
          "退款/冲正",
          item.getType(),
          item.getTypeLabel(),
          issueCenterRiskLevel("PAYMENT_ADJUSTMENT", item.getType(), item.getAmount(), item.getRemark()),
          item.getBillId(),
          item.getPaymentId(),
          item.getElderId(),
          item.getElderName(),
          item.getAmount(),
          detail,
          normalizeText(item.getRemark(), "需要复核是否已完成冲正、退款或重新出单"),
          item.getBillId() == null ? "/finance/payments/refund-reversal" : "/finance/bill/" + item.getBillId(),
          item.getBillId() == null ? "查看修正" : "查看账单",
          item.getOccurredAt());
      applyLatestHandle(issueItem, latestHandleMap.get(issueHandleKey(issueItem.getSourceModule(), issueItem.getBillId(), issueItem.getPaymentId(), issueItem.getElderId())));
      rows.add(issueItem);
    }
    return rows;
  }

  private boolean matchesIssueCenter(
      FinanceIssueCenterItem item,
      String riskLevel,
      String sourceModule,
      String keyword) {
    if (riskLevel != null && !riskLevel.isBlank()
        && !normalizeText(riskLevel, "").equalsIgnoreCase(normalizeText(item.getRiskLevel(), ""))) {
      return false;
    }
    if (sourceModule != null && !sourceModule.isBlank()
        && !normalizeText(sourceModule, "").equalsIgnoreCase(normalizeText(item.getSourceModule(), ""))) {
      return false;
    }
    if (keyword == null || keyword.isBlank()) {
      return true;
    }
    String text = keyword.trim().toLowerCase(Locale.ROOT);
    return containsAny(item.getElderName(), text)
        || containsAny(item.getIssueTypeLabel(), text)
        || containsAny(item.getDetail(), text)
        || containsAny(item.getSuggestion(), text)
        || containsAny(stringOf(item.getBillId()), text)
        || containsAny(stringOf(item.getPaymentId()), text);
  }

  private FinanceIssueCenterItem issueCenterItem(
      String sourceModule,
      String sourceModuleLabel,
      String issueType,
      String issueTypeLabel,
      String riskLevel,
      Long billId,
      Long paymentId,
      Long elderId,
      String elderName,
      BigDecimal amount,
      String detail,
      String suggestion,
      String actionPath,
      String actionLabel,
      LocalDateTime occurredAt) {
    FinanceIssueCenterItem item = new FinanceIssueCenterItem();
    item.setSourceModule(sourceModule);
    item.setSourceModuleLabel(sourceModuleLabel);
    item.setIssueType(issueType);
    item.setIssueTypeLabel(issueTypeLabel);
    item.setRiskLevel(riskLevel);
    item.setRiskLevelLabel(riskLevelLabel(riskLevel));
    item.setBillId(billId);
    item.setPaymentId(paymentId);
    item.setElderId(elderId);
    item.setElderName(elderName);
    item.setAmount(safe(amount));
    item.setDetail(detail);
    item.setSuggestion(suggestion);
    item.setActionPath(actionPath);
    item.setActionLabel(actionLabel);
    item.setOccurredAt(occurredAt);
    return item;
  }

  private List<FinanceCollectionFollowUpItem> buildCollectionFollowUpItems(Long orgId, YearMonth targetMonth) {
    List<BillMonthly> overdueBills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .le(BillMonthly::getBillMonth, targetMonth.toString())
            .gt(BillMonthly::getOutstandingAmount, BigDecimal.ZERO)
            .orderByDesc(BillMonthly::getOutstandingAmount)
            .orderByAsc(BillMonthly::getBillMonth));
    if (overdueBills.isEmpty()) {
      return List.of();
    }

    Map<Long, List<BillMonthly>> billsByElder = overdueBills.stream()
        .filter(item -> item.getElderId() != null)
        .collect(Collectors.groupingBy(BillMonthly::getElderId));
    List<Long> elderIds = new ArrayList<>(billsByElder.keySet());
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds).stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (a, b) -> a));
    Map<Long, ElderAccount> accountMap = elderIds.isEmpty()
        ? Map.of()
        : elderAccountMapper.selectList(
            Wrappers.lambdaQuery(ElderAccount.class)
                .eq(ElderAccount::getIsDeleted, 0)
                .eq(orgId != null, ElderAccount::getOrgId, orgId)
                .in(ElderAccount::getElderId, elderIds))
            .stream()
            .collect(Collectors.toMap(ElderAccount::getElderId, Function.identity(), (a, b) -> a));
    Map<Long, LocalDate> contractExpireMap = elderIds.isEmpty()
        ? Map.of()
        : crmContractMapper.selectList(
            Wrappers.lambdaQuery(CrmContract.class)
                .eq(CrmContract::getIsDeleted, 0)
                .eq(orgId != null, CrmContract::getOrgId, orgId)
                .in(CrmContract::getElderId, elderIds))
            .stream()
            .filter(item -> item.getElderId() != null && item.getEffectiveTo() != null)
            .collect(Collectors.toMap(
                CrmContract::getElderId,
                CrmContract::getEffectiveTo,
                (a, b) -> a.isAfter(b) ? a : b));

    List<Long> billIds = overdueBills.stream().map(BillMonthly::getId).filter(Objects::nonNull).toList();
    List<PaymentRecord> payments = billIds.isEmpty()
        ? List.of()
        : paymentRecordMapper.selectList(
            Wrappers.lambdaQuery(PaymentRecord.class)
                .eq(PaymentRecord::getIsDeleted, 0)
                .eq(orgId != null, PaymentRecord::getOrgId, orgId)
                .in(PaymentRecord::getBillMonthlyId, billIds)
                .orderByDesc(PaymentRecord::getPaidAt));
    Map<Long, Long> elderByBillId = overdueBills.stream()
        .filter(item -> item.getId() != null && item.getElderId() != null)
        .collect(Collectors.toMap(BillMonthly::getId, BillMonthly::getElderId, (a, b) -> a));
    Map<Long, LocalDateTime> lastPaymentMap = new HashMap<>();
    for (PaymentRecord payment : payments) {
      Long elderId = elderByBillId.get(payment.getBillMonthlyId());
      if (elderId == null || payment.getPaidAt() == null) {
        continue;
      }
      LocalDateTime current = lastPaymentMap.get(elderId);
      if (current == null || payment.getPaidAt().isAfter(current)) {
        lastPaymentMap.put(elderId, payment.getPaidAt());
      }
    }

    LocalDate baseDate = targetMonth.atEndOfMonth();
    Map<Long, FinanceHandleLogItem> latestFollowMap = latestCollectionHandleMap(
        queryHandleLogs(orgId, "FINANCE_COLLECTION_HANDLE", null, null, null, null, 500));
    List<FinanceCollectionFollowUpItem> rows = new ArrayList<>();
    for (Map.Entry<Long, List<BillMonthly>> entry : billsByElder.entrySet()) {
      Long elderId = entry.getKey();
      List<BillMonthly> elderBills = entry.getValue();
      if (elderBills.isEmpty()) {
        continue;
      }
      BigDecimal outstandingAmount = elderBills.stream()
          .map(BillMonthly::getOutstandingAmount)
          .filter(Objects::nonNull)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      BillMonthly primaryBill = elderBills.stream()
          .max(Comparator.comparing(BillMonthly::getOutstandingAmount, Comparator.nullsLast(BigDecimal::compareTo))
              .thenComparing(BillMonthly::getBillMonth, Comparator.nullsLast(String::compareTo)))
          .orElse(elderBills.get(0));
      String oldestBillMonth = elderBills.stream()
          .map(BillMonthly::getBillMonth)
          .filter(Objects::nonNull)
          .min(String::compareTo)
          .orElse(null);
      String latestBillMonth = elderBills.stream()
          .map(BillMonthly::getBillMonth)
          .filter(Objects::nonNull)
          .max(String::compareTo)
          .orElse(null);
      int overdueMonths = oldestBillMonth == null
          ? 1
          : (int) ChronoUnit.MONTHS.between(YearMonth.parse(oldestBillMonth), targetMonth) + 1;
      ElderProfile elder = elderMap.get(elderId);
      ElderAccount account = accountMap.get(elderId);
      BigDecimal balance = account == null ? BigDecimal.ZERO : safe(account.getBalance());
      LocalDate contractExpireDate = contractExpireMap.get(elderId);
      List<String> reasons = new ArrayList<>();
      if (overdueMonths >= 2) {
        reasons.add("连续欠费" + overdueMonths + "个月");
      } else {
        reasons.add("存在当期未结账单");
      }
      if (balance.compareTo(BigDecimal.valueOf(200)) < 0) {
        reasons.add("账户余额偏低");
      }
      if (contractExpireDate != null && !contractExpireDate.isAfter(baseDate.plusDays(30))) {
        reasons.add("合同30天内到期");
      }
      String riskLevel = collectionRiskLevel(outstandingAmount, overdueMonths, balance, contractExpireDate, baseDate);
      String stage = collectionStage(riskLevel);

      FinanceCollectionFollowUpItem item = new FinanceCollectionFollowUpItem();
      item.setElderId(elderId);
      item.setElderName(elder == null ? null : elder.getFullName());
      item.setPrimaryBillId(primaryBill.getId());
      item.setOldestBillMonth(oldestBillMonth);
      item.setLatestBillMonth(latestBillMonth);
      item.setOutstandingAmount(outstandingAmount);
      item.setBalance(balance);
      item.setOverdueMonths(overdueMonths);
      item.setRiskLevel(riskLevel);
      item.setRiskLevelLabel(riskLevelLabel(riskLevel));
      item.setStage(stage);
      item.setStageLabel(collectionStageLabel(stage));
      item.setLastPaymentAt(lastPaymentMap.get(elderId));
      item.setContractExpireDate(contractExpireDate);
      item.setFollowUpReason(String.join(" / ", reasons));
      item.setSuggestion(collectionSuggestion(stage, contractExpireDate, balance));
      item.setActionPath(primaryBill.getId() == null ? "/finance/accounts/list" : "/finance/bill/" + primaryBill.getId());
      item.setActionLabel(primaryBill.getId() == null ? "查看账户" : "查看账单");
      applyLatestCollectionHandle(item, latestFollowMap.get(elderId));
      rows.add(item);
    }
    return rows;
  }

  private boolean matchesCollectionFollowUp(
      FinanceCollectionFollowUpItem item,
      String riskLevel,
      String stage,
      String keyword) {
    if (riskLevel != null && !riskLevel.isBlank()
        && !normalizeText(riskLevel, "").equalsIgnoreCase(normalizeText(item.getRiskLevel(), ""))) {
      return false;
    }
    if (stage != null && !stage.isBlank()
        && !normalizeText(stage, "").equalsIgnoreCase(normalizeText(item.getStage(), ""))) {
      return false;
    }
    if (keyword == null || keyword.isBlank()) {
      return true;
    }
    String text = keyword.trim().toLowerCase(Locale.ROOT);
    return containsAny(item.getElderName(), text)
        || containsAny(item.getFollowUpReason(), text)
        || containsAny(item.getSuggestion(), text)
        || containsAny(stringOf(item.getElderId()), text)
        || containsAny(stringOf(item.getPrimaryBillId()), text);
  }

  private FinanceMonthCloseSummaryResponse buildMonthCloseSummary(Long orgId, YearMonth targetMonth) {
    FinanceMonthCloseSummaryResponse response = new FinanceMonthCloseSummaryResponse();
    response.setMonth(targetMonth.toString());

    List<BillMonthly> monthBills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getBillMonth, targetMonth.toString()));
    LocalDateTime startTime = targetMonth.atDay(1).atStartOfDay();
    LocalDateTime endTime = targetMonth.plusMonths(1).atDay(1).atStartOfDay();
    List<PaymentRecord> monthPayments = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(orgId != null, PaymentRecord::getOrgId, orgId)
            .ge(PaymentRecord::getPaidAt, startTime)
            .lt(PaymentRecord::getPaidAt, endTime));
    List<FinancePaymentAdjustmentItem> adjustments = queryPaymentAdjustmentItems(orgId, targetMonth);
    FinanceLedgerHealthResponse ledger = ledgerHealth(200).getData();
    List<FinanceCollectionFollowUpItem> followUps = buildCollectionFollowUpItems(orgId, targetMonth);

    long billCount = monthBills.size();
    long settledBillCount = monthBills.stream()
        .filter(item -> safe(item.getOutstandingAmount()).compareTo(BigDecimal.ZERO) <= 0)
        .count();
    long outstandingBillCount = monthBills.stream()
        .filter(item -> safe(item.getOutstandingAmount()).compareTo(BigDecimal.ZERO) > 0)
        .count();
    long unlinkedInvoiceCount = monthPayments.stream()
        .filter(item -> !containsAny(item.getRemark(), "发票", "invoice", "收据"))
        .count();
    long issueCount = ledger == null ? 0L : ledger.getTotalIssueCount();
    long highRiskFollowUpCount = followUps.stream()
        .filter(item -> "HIGH".equalsIgnoreCase(item.getRiskLevel()))
        .count();

    response.setBillCount(billCount);
    response.setSettledBillCount(settledBillCount);
    response.setOutstandingBillCount(outstandingBillCount);
    response.setPaymentCount((long) monthPayments.size());
    response.setAdjustmentCount((long) adjustments.size());
    response.setUnlinkedInvoiceCount(unlinkedInvoiceCount);
    response.setIssueCount(issueCount);

    response.getSteps().add(monthCloseStep(
        "BILL_GENERATION",
        "账单生成",
        billCount > 0 ? "COMPLETED" : "BLOCKED",
        billCount,
        billCount > 0 ? "本月账单已生成，可继续推进收款、发票与月结动作" : "本月未发现账单，需先生成账单",
        "/finance/bills/in-resident",
        "查看账单"));
    response.getSteps().add(monthCloseStep(
        "PAYMENT_POSTING",
        "收款入账",
        outstandingBillCount == 0 ? "COMPLETED" : (monthPayments.isEmpty() ? "BLOCKED" : "IN_PROGRESS"),
        outstandingBillCount,
        outstandingBillCount == 0 ? "本月账单均已完成入账" : "仍有 " + outstandingBillCount + " 张账单未结清",
        "/finance/payments/records",
        "查看流水"));
    response.getSteps().add(monthCloseStep(
        "RECONCILE",
        "对账巡检",
        issueCount == 0 ? "COMPLETED" : "BLOCKED",
        issueCount,
        issueCount == 0 ? "账单、收款与流水一致" : "存在 " + issueCount + " 个一致性问题需要先修正",
        "/finance/reconcile/issue-center",
        "处理异常"));
    response.getSteps().add(monthCloseStep(
        "INVOICE_LINK",
        "发票与收据",
        unlinkedInvoiceCount == 0 ? "COMPLETED" : "IN_PROGRESS",
        unlinkedInvoiceCount,
        unlinkedInvoiceCount == 0 ? "本月收款均已完成票据关联" : "仍有 " + unlinkedInvoiceCount + " 笔收款未补齐票据关联",
        "/finance/fees/payment-and-invoice",
        "处理票据"));
    response.getSteps().add(monthCloseStep(
        "ADJUSTMENT_REVIEW",
        "退款/冲正复核",
        adjustments.isEmpty() ? "COMPLETED" : "IN_PROGRESS",
        (long) adjustments.size(),
        adjustments.isEmpty() ? "本月暂无待复核的退款/冲正/作废记录" : "本月存在 " + adjustments.size() + " 条收款修正记录待复核闭环",
        "/finance/payments/refund-reversal",
        "查看修正"));
    response.getSteps().add(monthCloseStep(
        "COLLECTION_FOLLOW_UP",
        "欠费催缴",
        followUps.isEmpty() ? "COMPLETED" : (highRiskFollowUpCount > 0 ? "BLOCKED" : "IN_PROGRESS"),
        (long) followUps.size(),
        followUps.isEmpty() ? "本月无欠费跟进对象" : "仍有 " + followUps.size() + " 位长者处于催缴跟进中",
        "/finance/bills/follow-up",
        "查看催缴"));

    int totalSteps = response.getSteps().size();
    int completedSteps = (int) response.getSteps().stream()
        .filter(item -> "COMPLETED".equalsIgnoreCase(item.getStatus()))
        .count();
    int blockedSteps = (int) response.getSteps().stream()
        .filter(item -> "BLOCKED".equalsIgnoreCase(item.getStatus()))
        .count();
    int warningSteps = (int) response.getSteps().stream()
        .filter(item -> "IN_PROGRESS".equalsIgnoreCase(item.getStatus()))
        .count();
    response.setTotalSteps(totalSteps);
    response.setCompletedSteps(completedSteps);
    response.setBlockedSteps(blockedSteps);
    response.setWarningSteps(warningSteps);
    response.setCompletionRate(totalSteps <= 0 ? 0 : (int) Math.round(completedSteps * 100.0 / totalSteps));

    if (issueCount > 0) {
      response.getNotes().add("一致性巡检仍有 " + issueCount + " 个问题，建议先处理异常修复中心任务。");
    }
    if (unlinkedInvoiceCount > 0) {
      response.getNotes().add("仍有 " + unlinkedInvoiceCount + " 笔收款未补齐发票/收据关联。");
    }
    if (highRiskFollowUpCount > 0) {
      response.getNotes().add("当前存在 " + highRiskFollowUpCount + " 位高风险欠费长者，月结前需完成催缴闭环。");
    }
    if (response.getNotes().isEmpty()) {
      response.getNotes().add("本月月结链路较为顺畅，可按计划执行关账与复盘。");
    }
    AuditLog closeLog = latestMonthCloseLog(orgId, targetMonth);
    if (closeLog != null) {
      Map<String, Object> detail = readAuditDetail(closeLog.getDetail());
      response.setCloseRemark(stringValue(detail.get("remark")));
    }
    FinanceMonthLockStatusResponse lockStatus = financeMonthLockService.getMonthLockStatus(orgId, targetMonth);
    response.setCloseStatus(lockStatus.getCloseStatus());
    response.setCloseStatusLabel(lockStatus.getCloseStatusLabel());
    response.setClosedBy(lockStatus.getLockedBy());
    response.setClosedAt(lockStatus.getLockedAt());
    response.setLocked(lockStatus.isLocked());
    response.setLockStatusLabel(lockStatus.isLocked() ? "已锁账" : ("UNLOCKED".equalsIgnoreCase(lockStatus.getCloseStatus()) ? "已反锁" : "未锁账"));
    response.setUnlockedBy(lockStatus.getUnlockedBy());
    response.setUnlockedAt(lockStatus.getUnlockedAt());
    response.setUnlockReason(lockStatus.getUnlockReason());
    if (lockStatus.isLocked()) {
      response.getNotes().add(0, "本月已锁账，如需跨期处理请先申请跨期审批或由主管反锁账。");
    }
    response.setCanClose(blockedSteps == 0);
    return response;
  }

  private FinanceMonthCloseSummaryResponse.Step monthCloseStep(
      String key,
      String label,
      String status,
      Long count,
      String detail,
      String actionPath,
      String actionLabel) {
    FinanceMonthCloseSummaryResponse.Step step = new FinanceMonthCloseSummaryResponse.Step();
    step.setKey(key);
    step.setLabel(label);
    step.setStatus(status);
    step.setStatusLabel(monthCloseStatusLabel(status));
    step.setCount(count == null ? 0L : count);
    step.setDetail(detail);
    step.setActionPath(actionPath);
    step.setActionLabel(actionLabel);
    return step;
  }

  private AuditLog latestMonthCloseLog(Long orgId, YearMonth month) {
    return auditLogMapper.selectOne(
        Wrappers.lambdaQuery(AuditLog.class)
            .eq(AuditLog::getOrgId, orgId)
            .eq(AuditLog::getEntityType, "FINANCE_MONTH_CLOSE")
            .eq(AuditLog::getEntityId, monthEntityId(month))
            .orderByDesc(AuditLog::getCreateTime)
            .last("LIMIT 1"));
  }

  private void applyLatestHandle(FinanceIssueCenterItem item, FinanceHandleLogItem log) {
    if (item == null || log == null) {
      return;
    }
    item.setLatestHandleStatus(log.getStatus());
    item.setLatestHandleStatusLabel(log.getStatusLabel());
    item.setLatestHandleRemark(log.getRemark());
    item.setLatestOwnerName(log.getOwnerName());
    item.setLatestDueDate(log.getDueDate());
    item.setLatestReviewResult(log.getReviewResult());
    item.setLatestHandleAt(log.getCreateTime());
  }

  private void applyLatestCollectionHandle(FinanceCollectionFollowUpItem item, FinanceHandleLogItem log) {
    if (item == null || log == null) {
      return;
    }
    item.setLatestHandleStatus(log.getStatus());
    item.setLatestHandleStatusLabel(log.getStatusLabel());
    item.setLatestHandleRemark(log.getRemark());
    item.setLatestOwnerName(log.getOwnerName());
    item.setLatestContactName(log.getContactName());
    item.setLatestContactChannel(log.getContactChannel());
    item.setLatestContactResult(log.getContactResult());
    item.setPromisedDate(log.getPromisedDate());
    item.setNextReminderAt(log.getNextReminderAt());
    item.setLatestHandleAt(log.getCreateTime());
    if (log.getStage() != null && !log.getStage().isBlank()) {
      item.setStage(log.getStage());
      item.setStageLabel(collectionStageLabel(log.getStage()));
    }
  }

  private Map<String, FinanceHandleLogItem> latestHandleLogMap(List<FinanceHandleLogItem> rows) {
    Map<String, FinanceHandleLogItem> map = new HashMap<>();
    for (FinanceHandleLogItem row : rows) {
      String key = issueHandleKey(row.getSourceModule(), row.getBillId(), row.getPaymentId(), row.getElderId());
      if (key.isBlank()) {
        continue;
      }
      map.putIfAbsent(key, row);
    }
    return map;
  }

  private Map<Long, FinanceHandleLogItem> latestCollectionHandleMap(List<FinanceHandleLogItem> rows) {
    Map<Long, FinanceHandleLogItem> map = new HashMap<>();
    for (FinanceHandleLogItem row : rows) {
      if (row.getElderId() == null) {
        continue;
      }
      map.putIfAbsent(row.getElderId(), row);
    }
    return map;
  }

  private String issueHandleKey(String module, Long billId, Long paymentId, Long elderId) {
    return normalizeText(module, "") + "|" + stringOf(billId) + "|" + stringOf(paymentId) + "|" + stringOf(elderId);
  }

  private Long resolveAuditEntityId(Long billId, Long paymentId, Long elderId) {
    if (paymentId != null) return paymentId;
    if (billId != null) return billId;
    if (elderId != null) return elderId;
    return 0L;
  }

  private Long monthEntityId(YearMonth month) {
    return Long.valueOf(month.toString().replace("-", ""));
  }

  private String buildAdjustmentApprovalTitle(FinancePaymentAdjustmentApprovalRequest request) {
    String typeLabel = "INVALID_BILL".equalsIgnoreCase(request.getType()) ? "作废账单复核" : "退款/冲正审批";
    String elderName = normalizeText(request.getElderName(), "未绑定长者");
    return "财务" + typeLabel + " - " + elderName;
  }

  private FinanceSearchResponse buildFinanceSearchResponse(Long orgId, String keyword, int limit) {
    if (keyword == null || keyword.isBlank()) {
      throw new IllegalArgumentException("搜索关键词不能为空");
    }
    String text = keyword.trim();
    FinanceSearchResponse response = new FinanceSearchResponse();
    response.setKeyword(text);

    elderMapper.selectList(
            Wrappers.lambdaQuery(ElderProfile.class)
                .eq(ElderProfile::getIsDeleted, 0)
                .eq(orgId != null, ElderProfile::getOrgId, orgId)
                .and(q -> q.like(ElderProfile::getFullName, text)
                    .or().like(ElderProfile::getPhone, text)
                    .or().like(ElderProfile::getElderCode, text))
                .orderByDesc(ElderProfile::getUpdateTime)
                .last("LIMIT " + limit))
        .forEach(item -> response.getElders().add(searchRow(
            "ELDER", item.getId(), normalizeText(item.getFullName(), "未命名长者"),
            "手机号 " + normalizeText(item.getPhone(), "-"),
            "编号 " + normalizeText(item.getElderCode(), "-"),
            "/elder/detail/" + item.getId(),
            item.getUpdateTime())));

    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .and(q -> q.like(BillMonthly::getBillMonth, text)
                .or().like(BillMonthly::getId, text))
            .orderByDesc(BillMonthly::getUpdateTime)
            .last("LIMIT " + limit));
    Map<Long, ElderProfile> billElderMap = elderMapByIds(orgId, bills.stream().map(BillMonthly::getElderId).filter(Objects::nonNull).toList());
    bills.forEach(item -> response.getBills().add(searchRow(
        "BILL", item.getId(), "账单#" + item.getId(),
        normalizeText(billElderMap.get(item.getElderId()) == null ? null : billElderMap.get(item.getElderId()).getFullName(), "未绑定长者")
            + " · " + normalizeText(item.getBillMonth(), "-"),
        "待收 " + safe(item.getOutstandingAmount()) + " 元",
        "/finance/bill/" + item.getId(),
        item.getUpdateTime())));

    paymentRecordMapper.selectList(
            Wrappers.lambdaQuery(PaymentRecord.class)
                .eq(PaymentRecord::getIsDeleted, 0)
                .eq(orgId != null, PaymentRecord::getOrgId, orgId)
                .and(q -> q.like(PaymentRecord::getExternalTxnId, text)
                    .or().like(PaymentRecord::getRemark, text)
                    .or().like(PaymentRecord::getId, text))
                .orderByDesc(PaymentRecord::getPaidAt)
                .last("LIMIT " + limit))
        .forEach(item -> response.getPayments().add(searchRow(
            "PAYMENT",
            item.getId(),
            "收款#" + item.getId(),
            payMethodLabel(normalizePayMethod(item.getPayMethod())) + " · " + safe(item.getAmount()) + " 元",
            "外部流水 " + normalizeText(item.getExternalTxnId(), "-"),
            item.getBillMonthlyId() == null ? "/finance/payments/records" : "/finance/bill/" + item.getBillMonthlyId(),
            item.getPaidAt())));

    crmContractMapper.selectList(
            Wrappers.lambdaQuery(CrmContract.class)
                .eq(CrmContract::getIsDeleted, 0)
                .eq(orgId != null, CrmContract::getOrgId, orgId)
                .and(q -> q.like(CrmContract::getContractNo, text)
                    .or().like(CrmContract::getElderName, text)
                    .or().like(CrmContract::getName, text))
                .orderByDesc(CrmContract::getUpdateTime)
                .last("LIMIT " + limit))
        .forEach(item -> response.getContracts().add(searchRow(
            "CONTRACT",
            item.getId(),
            normalizeText(item.getContractNo(), "合同#" + item.getId()),
            normalizeText(item.getElderName(), normalizeText(item.getName(), "未命名合同")),
            "到期 " + stringOf(item.getEffectiveTo()),
            "/elder/contracts-invoices",
            item.getUpdateTime())));
    return response;
  }

  private FinanceSearchResponse.Row searchRow(
      String type,
      Long id,
      String title,
      String subtitle,
      String extra,
      String actionPath,
      LocalDateTime time) {
    FinanceSearchResponse.Row row = new FinanceSearchResponse.Row();
    row.setType(type);
    row.setId(id);
    row.setTitle(title);
    row.setSubtitle(subtitle);
    row.setExtra(extra);
    row.setActionPath(actionPath);
    row.setTime(time);
    return row;
  }

  private List<FinanceSearchResponse.Row> flattenSearchRows(FinanceSearchResponse response) {
    List<FinanceSearchResponse.Row> rows = new ArrayList<>();
    if (response == null) {
      return rows;
    }
    if (response.getElders() != null) {
      rows.addAll(response.getElders());
    }
    if (response.getBills() != null) {
      rows.addAll(response.getBills());
    }
    if (response.getPayments() != null) {
      rows.addAll(response.getPayments());
    }
    if (response.getContracts() != null) {
      rows.addAll(response.getContracts());
    }
    rows.sort(Comparator.comparing(FinanceSearchResponse.Row::getTime, Comparator.nullsLast(LocalDateTime::compareTo)).reversed());
    return rows;
  }

  private String searchTypeLabel(String type) {
    if ("ELDER".equalsIgnoreCase(type)) return "长者";
    if ("BILL".equalsIgnoreCase(type)) return "账单";
    if ("PAYMENT".equalsIgnoreCase(type)) return "收款流水";
    if ("CONTRACT".equalsIgnoreCase(type)) return "合同";
    return normalizeText(type, "财务对象");
  }

  private Map<Long, ElderProfile> elderMapByIds(Long orgId, List<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      return Map.of();
    }
    return elderMapper.selectList(
            Wrappers.lambdaQuery(ElderProfile.class)
                .eq(ElderProfile::getIsDeleted, 0)
                .eq(orgId != null, ElderProfile::getOrgId, orgId)
                .in(ElderProfile::getId, elderIds))
        .stream()
        .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (a, b) -> a));
  }

  private String issueCenterRiskLevel(String sourceModule, String issueType, BigDecimal amount, String detail) {
    String module = normalizeText(sourceModule, "").toUpperCase(Locale.ROOT);
    String type = normalizeText(issueType, "").toUpperCase(Locale.ROOT);
    BigDecimal value = safe(amount);
    if ("LEDGER".equals(module)) {
      if ("MISSING_PAYMENT_FLOW".equals(type) || "PAID_AMOUNT_MISMATCH".equals(type)) {
        return "HIGH";
      }
      return value.compareTo(BigDecimal.valueOf(3000)) >= 0 ? "HIGH" : "MEDIUM";
    }
    if ("RECONCILE".equals(module)) {
      if ("DUPLICATED_PAYMENT".equals(type)) {
        return "HIGH";
      }
      if ("BILL_PAID_UNMATCHED".equals(type)) {
        return value.compareTo(BigDecimal.valueOf(1000)) >= 0 ? "HIGH" : "MEDIUM";
      }
      return value.compareTo(BigDecimal.valueOf(2000)) >= 0 ? "HIGH" : "LOW";
    }
    if ("AUTO_DEBIT".equals(module)) {
      if (value.compareTo(BigDecimal.valueOf(3000)) >= 0 || containsAny(detail, "余额不足")) {
        return "HIGH";
      }
      return value.compareTo(BigDecimal.valueOf(1000)) >= 0 ? "MEDIUM" : "LOW";
    }
    if ("PAYMENT_ADJUSTMENT".equals(module)) {
      if ("INVALID_BILL".equals(type) && (containsAny(detail, "欠费") || value.compareTo(BigDecimal.valueOf(1000)) >= 0)) {
        return "HIGH";
      }
      return value.compareTo(BigDecimal.valueOf(1000)) >= 0 ? "MEDIUM" : "LOW";
    }
    return "MEDIUM";
  }

  private String collectionRiskLevel(
      BigDecimal outstandingAmount,
      int overdueMonths,
      BigDecimal balance,
      LocalDate contractExpireDate,
      LocalDate baseDate) {
    if (safe(outstandingAmount).compareTo(BigDecimal.valueOf(3000)) >= 0 || overdueMonths >= 2) {
      return "HIGH";
    }
    if (contractExpireDate != null && !contractExpireDate.isAfter(baseDate.plusDays(7))) {
      return "HIGH";
    }
    if (safe(outstandingAmount).compareTo(BigDecimal.valueOf(1000)) >= 0 || overdueMonths >= 1) {
      return "MEDIUM";
    }
    if (balance.compareTo(BigDecimal.valueOf(200)) < 0) {
      return "MEDIUM";
    }
    if (contractExpireDate != null && !contractExpireDate.isAfter(baseDate.plusDays(30))) {
      return "MEDIUM";
    }
    return "LOW";
  }

  private String collectionStage(String riskLevel) {
    if ("HIGH".equalsIgnoreCase(riskLevel)) {
      return "URGENT";
    }
    if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
      return "FOLLOW_UP";
    }
    return "WATCH";
  }

  private String collectionStageLabel(String stage) {
    if ("URGENT".equalsIgnoreCase(stage)) {
      return "立即催缴";
    }
    if ("FOLLOW_UP".equalsIgnoreCase(stage)) {
      return "本周跟进";
    }
    return "持续观察";
  }

  private String collectionSuggestion(String stage, LocalDate contractExpireDate, BigDecimal balance) {
    if ("URGENT".equalsIgnoreCase(stage)) {
      return "建议当天电话沟通并同步家属，必要时先锁定退款/冲正处理后再推进补缴。";
    }
    if (contractExpireDate != null && balance.compareTo(BigDecimal.valueOf(200)) < 0) {
      return "建议结合续约与充值一起推进，避免跨月继续挂账。";
    }
    return "建议通过短信或微信提醒后，在本周内完成一次人工回访。";
  }

  private String riskLevelLabel(String riskLevel) {
    if ("HIGH".equalsIgnoreCase(riskLevel)) {
      return "高风险";
    }
    if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
      return "中风险";
    }
    return "低风险";
  }

  private String monthCloseStatusLabel(String status) {
    if ("COMPLETED".equalsIgnoreCase(status)) {
      return "已完成";
    }
    if ("BLOCKED".equalsIgnoreCase(status)) {
      return "阻塞";
    }
    return "处理中";
  }

  private String monthCloseExecuteStatusLabel(String status) {
    if ("CLOSED".equalsIgnoreCase(status)) {
      return "已关账";
    }
    if ("FORCED_CLOSED".equalsIgnoreCase(status)) {
      return "强制关账";
    }
    if ("UNLOCKED".equalsIgnoreCase(status)) {
      return "已反锁";
    }
    if ("OPEN".equalsIgnoreCase(status)) {
      return "未关账";
    }
    return "阻塞";
  }

  private String handleStatusLabel(String status) {
    if ("DONE".equalsIgnoreCase(status)) return "已完成";
    if ("FOLLOWING".equalsIgnoreCase(status)) return "跟进中";
    if ("UPDATED".equalsIgnoreCase(status)) return "已更新";
    if ("WAITING".equalsIgnoreCase(status)) return "待回访";
    if ("FOLLOWED".equalsIgnoreCase(status)) return "已跟进";
    return normalizeText(status, "已记录");
  }

  private String handleSourceModuleLabel(String sourceModule) {
    if ("LEDGER".equalsIgnoreCase(sourceModule)) return "一致性巡检";
    if ("RECONCILE".equalsIgnoreCase(sourceModule)) return "对账中心";
    if ("AUTO_DEBIT".equalsIgnoreCase(sourceModule)) return "催缴异常";
    if ("PAYMENT_ADJUSTMENT".equalsIgnoreCase(sourceModule)) return "退款/冲正";
    if ("COLLECTION".equalsIgnoreCase(sourceModule)) return "欠费催缴";
    return normalizeText(sourceModule, "财务事项");
  }

  private String handleIssueTypeLabel(String issueType) {
    if (issueType == null || issueType.isBlank()) {
      return "财务处理";
    }
    return issueType;
  }

  private String approvalStatusLabel(String status) {
    if ("PENDING".equalsIgnoreCase(status)) return "审批中";
    if ("APPROVED".equalsIgnoreCase(status)) return "已通过";
    if ("REJECTED".equalsIgnoreCase(status)) return "已驳回";
    return "未提交";
  }

  private String stringValue(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private Long toLong(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    try {
      return Long.valueOf(String.valueOf(value));
    } catch (Exception ex) {
      return null;
    }
  }

  private LocalDate toLocalDate(Object value) {
    if (value == null) {
      return null;
    }
    try {
      return LocalDate.parse(String.valueOf(value));
    } catch (Exception ex) {
      return null;
    }
  }

  private LocalDateTime toLocalDateTime(Object value) {
    if (value == null) {
      return null;
    }
    try {
      return LocalDateTime.parse(String.valueOf(value));
    } catch (Exception ex) {
      return null;
    }
  }

  private ResponseEntity<byte[]> csvResponse(String filenamePrefix, List<String> headers, List<List<String>> rows) {
    StringBuilder builder = new StringBuilder();
    builder.append('\uFEFF');
    builder.append(headers.stream().map(this::escapeCsv).collect(Collectors.joining(","))).append('\n');
    for (List<String> row : rows) {
      builder.append(row.stream().map(this::escapeCsv).collect(Collectors.joining(","))).append('\n');
    }
    byte[] bytes = builder.toString().getBytes(StandardCharsets.UTF_8);
    String filename = filenamePrefix + "-" + LocalDate.now() + ".csv";
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .body(bytes);
  }

  private String escapeCsv(String value) {
    String text = value == null ? "" : value;
    boolean needQuote = text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");
    if (!needQuote) {
      return text;
    }
    return "\"" + text.replace("\"", "\"\"") + "\"";
  }

  private String stringOf(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
