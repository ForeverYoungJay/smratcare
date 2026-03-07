package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.zhiyangyun.care.finance.model.FinanceConfigChangeLogItem;
import com.zhiyangyun.care.finance.model.FinanceConfigImpactPreviewRequest;
import com.zhiyangyun.care.finance.model.FinanceConfigImpactPreviewResponse;
import com.zhiyangyun.care.finance.model.FinanceDischargeStatusSyncExecuteRequest;
import com.zhiyangyun.care.finance.model.FinanceDischargeStatusSyncExecuteResponse;
import com.zhiyangyun.care.finance.model.FinanceDischargeStatusSyncResponse;
import com.zhiyangyun.care.finance.model.FinanceInvoiceReceiptItem;
import com.zhiyangyun.care.finance.model.FinanceLedgerHealthResponse;
import com.zhiyangyun.care.finance.model.FinanceModuleEntrySummaryResponse;
import com.zhiyangyun.care.finance.model.FinanceMasterDataOverviewResponse;
import com.zhiyangyun.care.finance.model.FinanceReconcileExceptionItem;
import com.zhiyangyun.care.finance.model.FinanceRoomOpsDetailResponse;
import com.zhiyangyun.care.finance.model.FinanceWorkbenchOverviewResponse;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/workbench")
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
      AuditLogService auditLogService) {
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
    response.setPending(buildPendingCard(orgId));
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

    return Result.ok(result);
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

  private FinanceWorkbenchOverviewResponse.PendingCard buildPendingCard(Long orgId) {
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
      case "MEDICAL_ERRORS" -> fillMedicalErrorEntry(response, orgId, startOfToday, endOfToday, monthConsumption);
      case "DINING_FLOW" -> fillConsumptionEntry(response, monthConsumption, todayConsumption, "DINING", "餐饮扣费数据缺失，请核查点餐与扣费映射");
      case "LOGISTICS_FLOW" -> fillLogisticsEntry(response, monthConsumption, todayConsumption);
      case "ADJUSTMENTS" -> fillAdjustmentEntry(response, orgId);
      case "DISCHARGE_STATUS_SYNC" -> fillDischargeSyncEntry(response, orgId, thisMonth);
      case "RECONCILE_INVOICE" -> fillReconcileInvoiceEntry(response, orgId, thisMonth, thisMonthBills, todayPayments);
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
    FinanceWorkbenchOverviewResponse.PendingCard pendingCard = buildPendingCard(orgId);
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
    FinanceWorkbenchOverviewResponse.PendingCard pendingCard = buildPendingCard(orgId);
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
