package com.zhiyangyun.care.report.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.Org;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmission;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischarge;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeMapper;
import com.zhiyangyun.care.store.entity.StoreOrder;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/stats")
public class StatisticsController {
  private static final String MONTH_PATTERN = "^\\d{4}-(0[1-9]|1[0-2])$";
  private static final String DATE_PATTERN = "^\\d{4}-(0[1-9]|1[0-2])-([0-2]\\d|3[0-1])$";

  private final ElderAdmissionMapper admissionMapper;
  private final ElderDischargeMapper dischargeMapper;
  private final ElderMapper elderMapper;
  private final BillMonthlyMapper billMonthlyMapper;
  private final StoreOrderMapper storeOrderMapper;
  private final BedMapper bedMapper;
  private final OrgMapper orgMapper;

  public StatisticsController(
      ElderAdmissionMapper admissionMapper,
      ElderDischargeMapper dischargeMapper,
      ElderMapper elderMapper,
      BillMonthlyMapper billMonthlyMapper,
      StoreOrderMapper storeOrderMapper,
      BedMapper bedMapper,
      OrgMapper orgMapper) {
    this.admissionMapper = admissionMapper;
    this.dischargeMapper = dischargeMapper;
    this.elderMapper = elderMapper;
    this.billMonthlyMapper = billMonthlyMapper;
    this.storeOrderMapper = storeOrderMapper;
    this.bedMapper = bedMapper;
    this.orgMapper = orgMapper;
  }

  @GetMapping("/check-in")
  public Result<CheckInStatsResponse> checkIn(
      @RequestParam(required = false) @Pattern(regexp = MONTH_PATTERN, message = "from must be YYYY-MM") String from,
      @RequestParam(required = false) @Pattern(regexp = MONTH_PATTERN, message = "to must be YYYY-MM") String to,
      @RequestParam(defaultValue = "6") @Min(1) @Max(36) int months,
      @RequestParam(required = false) Long orgId) {
    Long scopedOrgId = resolveAccessibleOrgId(orgId);
    YearMonth end = parseYearMonth(to, YearMonth.now());
    YearMonth start = parseYearMonth(from, end.minusMonths(Math.max(months - 1L, 0L)));
    validateMonthRange(start, end, 36);

    List<ElderAdmission> admissions = admissionMapper.selectList(
        Wrappers.lambdaQuery(ElderAdmission.class)
            .eq(ElderAdmission::getIsDeleted, 0)
            .eq(scopedOrgId != null, ElderAdmission::getOrgId, scopedOrgId)
            .ge(ElderAdmission::getAdmissionDate, start.atDay(1))
            .le(ElderAdmission::getAdmissionDate, end.atEndOfMonth()));
    List<ElderDischarge> discharges = dischargeMapper.selectList(
        Wrappers.lambdaQuery(ElderDischarge.class)
            .eq(ElderDischarge::getIsDeleted, 0)
            .eq(scopedOrgId != null, ElderDischarge::getOrgId, scopedOrgId)
            .ge(ElderDischarge::getDischargeDate, start.atDay(1))
            .le(ElderDischarge::getDischargeDate, end.atEndOfMonth()));

    Map<String, Long> admissionMap = initMonthCountMap(start, end);
    Map<String, Long> dischargeMap = initMonthCountMap(start, end);
    admissions.forEach(item -> {
      if (item.getAdmissionDate() != null) {
        incrementMonthMap(admissionMap, YearMonth.from(item.getAdmissionDate()).toString());
      }
    });
    discharges.forEach(item -> {
      if (item.getDischargeDate() != null) {
        incrementMonthMap(dischargeMap, YearMonth.from(item.getDischargeDate()).toString());
      }
    });

    CheckInStatsResponse response = new CheckInStatsResponse();
    response.setTotalAdmissions((long) admissions.size());
    response.setTotalDischarges((long) discharges.size());
    response.setNetIncrease(response.getTotalAdmissions() - response.getTotalDischarges());
    response.setDischargeToAdmissionRate(calculateRate(response.getTotalDischarges(), response.getTotalAdmissions()));
    response.setCurrentResidents(elderMapper.selectCount(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(scopedOrgId != null, ElderProfile::getOrgId, scopedOrgId)
            .eq(ElderProfile::getStatus, 1)));
    response.setMonthlyAdmissions(toMonthCountList(admissionMap));
    response.setMonthlyDischarges(toMonthCountList(dischargeMap));
    response.setMonthlyNetIncrease(toMonthNetCountList(admissionMap, dischargeMap));
    return Result.ok(response);
  }

  @GetMapping("/consumption")
  public Result<ConsumptionStatsResponse> consumption(
      @RequestParam(required = false) @Pattern(regexp = MONTH_PATTERN, message = "from must be YYYY-MM") String from,
      @RequestParam(required = false) @Pattern(regexp = MONTH_PATTERN, message = "to must be YYYY-MM") String to,
      @RequestParam(defaultValue = "6") @Min(1) @Max(36) int months,
      @RequestParam(required = false) Long orgId) {
    Long scopedOrgId = resolveAccessibleOrgId(orgId);
    YearMonth end = parseYearMonth(to, YearMonth.now());
    YearMonth start = parseYearMonth(from, end.minusMonths(Math.max(months - 1L, 0L)));
    validateMonthRange(start, end, 36);
    LocalDateTime startTime = start.atDay(1).atStartOfDay();
    LocalDateTime endTime = end.atEndOfMonth().plusDays(1).atStartOfDay();

    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(scopedOrgId != null, BillMonthly::getOrgId, scopedOrgId)
            .ge(BillMonthly::getBillMonth, start.toString())
            .le(BillMonthly::getBillMonth, end.toString()));
    List<StoreOrder> orders = storeOrderMapper.selectList(
        Wrappers.lambdaQuery(StoreOrder.class)
            .eq(StoreOrder::getIsDeleted, 0)
            .eq(scopedOrgId != null, StoreOrder::getOrgId, scopedOrgId)
            .ge(StoreOrder::getCreateTime, startTime)
            .lt(StoreOrder::getCreateTime, endTime));

    Map<String, BigDecimal> billByMonth = initMonthAmountMap(start, end);
    Map<String, BigDecimal> storeByMonth = initMonthAmountMap(start, end);
    BigDecimal totalBill = BigDecimal.ZERO;
    BigDecimal totalStore = BigDecimal.ZERO;
    Map<Long, BigDecimal> topByElder = new LinkedHashMap<>();

    for (BillMonthly bill : bills) {
      BigDecimal amount = safeAmount(bill.getTotalAmount());
      totalBill = totalBill.add(amount);
      billByMonth.put(bill.getBillMonth(), billByMonth.getOrDefault(bill.getBillMonth(), BigDecimal.ZERO).add(amount));
      if (bill.getElderId() != null) {
        topByElder.merge(bill.getElderId(), amount, BigDecimal::add);
      }
    }
    for (StoreOrder order : orders) {
      if (order.getCreateTime() == null) {
        continue;
      }
      BigDecimal amount = safeAmount(order.getTotalAmount());
      totalStore = totalStore.add(amount);
      String month = YearMonth.from(order.getCreateTime()).toString();
      storeByMonth.put(month, storeByMonth.getOrDefault(month, BigDecimal.ZERO).add(amount));
      if (order.getElderId() != null) {
        topByElder.merge(order.getElderId(), amount, BigDecimal::add);
      }
    }

    Map<Long, ElderProfile> elderMap = buildElderMap(new ArrayList<>(topByElder.keySet()));
    List<ElderAmountItem> topElders = topByElder.entrySet().stream()
        .map(entry -> {
          ElderAmountItem item = new ElderAmountItem();
          item.setElderId(entry.getKey());
          ElderProfile elder = elderMap.get(entry.getKey());
          item.setElderName(elder == null ? null : elder.getFullName());
          item.setAmount(entry.getValue());
          return item;
        })
        .sorted(Comparator.comparing(ElderAmountItem::getAmount).reversed())
        .limit(10)
        .toList();

    ConsumptionStatsResponse response = new ConsumptionStatsResponse();
    response.setTotalBillConsumption(totalBill);
    response.setTotalStoreConsumption(totalStore);
    BigDecimal totalConsumption = totalBill.add(totalStore);
    response.setTotalConsumption(totalConsumption);
    response.setBillConsumptionRatio(calculateShareRate(totalBill, totalConsumption));
    response.setStoreConsumptionRatio(calculateShareRate(totalStore, totalConsumption));
    response.setAverageMonthlyConsumption(calculateAverage(totalConsumption, billByMonth.size()));
    response.setMonthlyBillConsumption(toMonthAmountList(billByMonth));
    response.setMonthlyStoreConsumption(toMonthAmountList(storeByMonth));
    response.setMonthlyTotalConsumption(toMergedMonthAmountList(billByMonth, storeByMonth));
    response.setTopConsumerElders(topElders);
    return Result.ok(response);
  }

  @GetMapping("/elder-info")
  public Result<ElderInfoStatsResponse> elderInfo(@RequestParam(required = false) Long orgId) {
    Long scopedOrgId = resolveAccessibleOrgId(orgId);
    List<ElderProfile> elders = elderMapper.selectList(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(scopedOrgId != null, ElderProfile::getOrgId, scopedOrgId));

    Map<String, Long> gender = new LinkedHashMap<>();
    gender.put("男", 0L);
    gender.put("女", 0L);
    gender.put("未知", 0L);

    Map<String, Long> ageBuckets = new LinkedHashMap<>();
    ageBuckets.put("60岁以下", 0L);
    ageBuckets.put("60-69岁", 0L);
    ageBuckets.put("70-79岁", 0L);
    ageBuckets.put("80-89岁", 0L);
    ageBuckets.put("90岁及以上", 0L);

    Map<String, Long> careLevel = new LinkedHashMap<>();
    Map<String, Long> elderStatus = new LinkedHashMap<>();

    LocalDate today = LocalDate.now();
    for (ElderProfile elder : elders) {
      String genderLabel = switch (elder.getGender() == null ? -1 : elder.getGender()) {
        case 1 -> "男";
        case 2 -> "女";
        default -> "未知";
      };
      gender.put(genderLabel, gender.getOrDefault(genderLabel, 0L) + 1);

      String level = elder.getCareLevel() == null || elder.getCareLevel().isBlank()
          ? "未分级"
          : elder.getCareLevel();
      careLevel.put(level, careLevel.getOrDefault(level, 0L) + 1);

      String statusLabel = elderStatusLabel(elder.getStatus());
      elderStatus.put(statusLabel, elderStatus.getOrDefault(statusLabel, 0L) + 1);

      String ageBucket = resolveAgeBucket(elder.getBirthDate(), today);
      ageBuckets.put(ageBucket, ageBuckets.getOrDefault(ageBucket, 0L) + 1);
    }

    ElderInfoStatsResponse response = new ElderInfoStatsResponse();
    response.setTotalElders((long) elders.size());
    response.setInHospitalCount(elderStatus.getOrDefault("在院", 0L));
    response.setDischargedCount(elderStatus.getOrDefault("离院", 0L));
    response.setGenderDistribution(toNameCountList(gender));
    response.setAgeDistribution(toNameCountList(ageBuckets));
    response.setCareLevelDistribution(toNameCountList(careLevel));
    response.setStatusDistribution(toNameCountList(elderStatus));
    return Result.ok(response);
  }

  @GetMapping("/org/monthly-operation")
  public Result<List<OrgMonthlyOperationItem>> orgMonthlyOperation(
      @RequestParam(required = false) @Pattern(regexp = MONTH_PATTERN, message = "from must be YYYY-MM") String from,
      @RequestParam(required = false) @Pattern(regexp = MONTH_PATTERN, message = "to must be YYYY-MM") String to,
      @RequestParam(defaultValue = "6") @Min(1) @Max(36) int months,
      @RequestParam(required = false) Long orgId) {
    Long scopedOrgId = resolveAccessibleOrgId(orgId);
    YearMonth end = parseYearMonth(to, YearMonth.now());
    YearMonth start = parseYearMonth(from, end.minusMonths(Math.max(months - 1L, 0L)));
    validateMonthRange(start, end, 36);
    return Result.ok(buildOrgMonthlyOperation(scopedOrgId, start, end));
  }

  @GetMapping(value = "/org/monthly-operation/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportOrgMonthlyOperation(
      @RequestParam(required = false) @Pattern(regexp = MONTH_PATTERN, message = "from must be YYYY-MM") String from,
      @RequestParam(required = false) @Pattern(regexp = MONTH_PATTERN, message = "to must be YYYY-MM") String to,
      @RequestParam(defaultValue = "6") @Min(1) @Max(36) int months,
      @RequestParam(required = false) Long orgId) {
    Long scopedOrgId = resolveAccessibleOrgId(orgId);
    YearMonth end = parseYearMonth(to, YearMonth.now());
    YearMonth start = parseYearMonth(from, end.minusMonths(Math.max(months - 1L, 0L)));
    validateMonthRange(start, end, 36);

    List<OrgMonthlyOperationItem> rows = buildOrgMonthlyOperation(scopedOrgId, start, end);
    List<String> headers = List.of("机构ID", "机构名称", "月份", "入住", "离院", "营收", "总床位", "已使用床位", "床位使用率(%)");
    List<List<String>> body = rows.stream()
        .map(item -> List.of(
            stringOf(item.getOrgId()),
            stringOf(item.getOrgName()),
            stringOf(item.getMonth()),
            stringOf(item.getAdmissions()),
            stringOf(item.getDischarges()),
            stringOf(item.getRevenue()),
            stringOf(item.getTotalBeds()),
            stringOf(item.getOccupiedBeds()),
            stringOf(item.getOccupancyRate())))
        .toList();
    return csvResponse("org-monthly-operation", headers, body);
  }

  @GetMapping("/org/elder-flow")
  public Result<List<MonthFlowItem>> orgElderFlow(
      @RequestParam(required = false) @Pattern(regexp = MONTH_PATTERN, message = "from must be YYYY-MM") String from,
      @RequestParam(required = false) @Pattern(regexp = MONTH_PATTERN, message = "to must be YYYY-MM") String to,
      @RequestParam(defaultValue = "6") @Min(1) @Max(36) int months,
      @RequestParam(required = false) Long orgId) {
    Long scopedOrgId = resolveAccessibleOrgId(orgId);
    YearMonth end = parseYearMonth(to, YearMonth.now());
    YearMonth start = parseYearMonth(from, end.minusMonths(Math.max(months - 1L, 0L)));
    validateMonthRange(start, end, 36);
    LocalDate startDate = start.atDay(1);
    LocalDate endDate = end.atEndOfMonth();

    List<ElderAdmission> admissions = admissionMapper.selectList(
        Wrappers.lambdaQuery(ElderAdmission.class)
            .eq(ElderAdmission::getIsDeleted, 0)
            .eq(scopedOrgId != null, ElderAdmission::getOrgId, scopedOrgId)
            .ge(ElderAdmission::getAdmissionDate, startDate)
            .le(ElderAdmission::getAdmissionDate, endDate));
    List<ElderDischarge> discharges = dischargeMapper.selectList(
        Wrappers.lambdaQuery(ElderDischarge.class)
            .eq(ElderDischarge::getIsDeleted, 0)
            .eq(scopedOrgId != null, ElderDischarge::getOrgId, scopedOrgId)
            .ge(ElderDischarge::getDischargeDate, startDate)
            .le(ElderDischarge::getDischargeDate, endDate));

    Map<String, Long> admissionMap = initMonthCountMap(start, end);
    Map<String, Long> dischargeMap = initMonthCountMap(start, end);
    admissions.forEach(item -> {
      if (item.getAdmissionDate() != null) {
        incrementMonthMap(admissionMap, YearMonth.from(item.getAdmissionDate()).toString());
      }
    });
    discharges.forEach(item -> {
      if (item.getDischargeDate() != null) {
        incrementMonthMap(dischargeMap, YearMonth.from(item.getDischargeDate()).toString());
      }
    });

    List<MonthFlowItem> result = new ArrayList<>();
    YearMonth cursor = start;
    while (!cursor.isAfter(end)) {
      String month = cursor.toString();
      MonthFlowItem item = new MonthFlowItem();
      item.setMonth(month);
      item.setAdmissions(admissionMap.getOrDefault(month, 0L));
      item.setDischarges(dischargeMap.getOrDefault(month, 0L));
      result.add(item);
      cursor = cursor.plusMonths(1);
    }
    return Result.ok(result);
  }

  @GetMapping("/org/bed-usage")
  public Result<BedUsageStatsResponse> orgBedUsage(@RequestParam(required = false) Long orgId) {
    Long scopedOrgId = resolveAccessibleOrgId(orgId);
    List<Bed> beds = bedMapper.selectList(
        Wrappers.lambdaQuery(Bed.class)
            .eq(Bed::getIsDeleted, 0)
            .eq(scopedOrgId != null, Bed::getOrgId, scopedOrgId));

    long total = beds.size();
    long occupied = beds.stream().filter(item -> item.getStatus() != null && item.getStatus() == 2).count();
    long maintenance = beds.stream().filter(item -> item.getStatus() != null && item.getStatus() == 3).count();
    long available = total - occupied - maintenance;
    if (available < 0) {
      available = 0;
    }

    BedUsageStatsResponse response = new BedUsageStatsResponse();
    response.setTotalBeds(total);
    response.setOccupiedBeds(occupied);
    response.setMaintenanceBeds(maintenance);
    response.setAvailableBeds(available);
    response.setOccupancyRate(calculateRate(occupied, total));
    response.setMaintenanceRate(calculateRate(maintenance, total));
    response.setAvailableRate(calculateRate(available, total));
    return Result.ok(response);
  }

  @GetMapping("/monthly-revenue")
  public Result<MonthlyRevenueStatsResponse> monthlyRevenue(
      @RequestParam(required = false) @Pattern(regexp = MONTH_PATTERN, message = "from must be YYYY-MM") String from,
      @RequestParam(required = false) @Pattern(regexp = MONTH_PATTERN, message = "to must be YYYY-MM") String to,
      @RequestParam(defaultValue = "6") @Min(1) @Max(36) int months,
      @RequestParam(required = false) Long orgId) {
    Long scopedOrgId = resolveAccessibleOrgId(orgId);
    YearMonth end = parseYearMonth(to, YearMonth.now());
    YearMonth start = parseYearMonth(from, end.minusMonths(Math.max(months - 1L, 0L)));
    validateMonthRange(start, end, 36);

    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(scopedOrgId != null, BillMonthly::getOrgId, scopedOrgId)
            .ge(BillMonthly::getBillMonth, start.toString())
            .le(BillMonthly::getBillMonth, end.toString()));
    Map<String, BigDecimal> monthMap = initMonthAmountMap(start, end);
    BigDecimal total = BigDecimal.ZERO;
    for (BillMonthly bill : bills) {
      BigDecimal amount = safeAmount(bill.getTotalAmount());
      monthMap.put(bill.getBillMonth(), monthMap.getOrDefault(bill.getBillMonth(), BigDecimal.ZERO).add(amount));
      total = total.add(amount);
    }

    MonthlyRevenueStatsResponse response = new MonthlyRevenueStatsResponse();
    response.setTotalRevenue(total);
    response.setMonthlyRevenue(toMonthAmountList(monthMap));
    response.setAverageMonthlyRevenue(calculateAverage(total, monthMap.size()));
    response.setRevenueGrowthRate(calculateMonthOverMonthGrowth(monthMap));
    return Result.ok(response);
  }

  @GetMapping("/elder-flow-report")
  public Result<FlowReportPageResponse> elderFlowReport(
      @RequestParam(required = false) @Pattern(regexp = DATE_PATTERN, message = "fromDate must be YYYY-MM-DD") String fromDate,
      @RequestParam(required = false) @Pattern(regexp = DATE_PATTERN, message = "toDate must be YYYY-MM-DD") String toDate,
      @RequestParam(required = false) String eventType,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") @Min(1) long pageNo,
      @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
      @RequestParam(required = false) Long orgId) {
    Long scopedOrgId = resolveAccessibleOrgId(orgId);
    LocalDate end = parseDate(toDate, LocalDate.now());
    LocalDate start = parseDate(fromDate, end.minusDays(29));
    validateDateRange(start, end, 366);

    List<FlowReportItem> filtered = buildFlowRows(scopedOrgId, start, end, eventType, keyword);
    long total = filtered.size();
    int startIndex = (int) Math.max((pageNo - 1) * pageSize, 0);
    int endIndex = (int) Math.min(startIndex + pageSize, total);
    List<FlowReportItem> pageList = startIndex >= total ? List.of() : filtered.subList(startIndex, endIndex);

    FlowReportPageResponse response = new FlowReportPageResponse();
    response.setList(pageList);
    response.setTotal(total);
    response.setPageNo(pageNo);
    response.setPageSize(pageSize);
    return Result.ok(response);
  }

  @GetMapping(value = "/elder-flow-report/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportElderFlowReport(
      @RequestParam(required = false) @Pattern(regexp = DATE_PATTERN, message = "fromDate must be YYYY-MM-DD") String fromDate,
      @RequestParam(required = false) @Pattern(regexp = DATE_PATTERN, message = "toDate must be YYYY-MM-DD") String toDate,
      @RequestParam(required = false) String eventType,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long orgId) {
    Long scopedOrgId = resolveAccessibleOrgId(orgId);
    LocalDate end = parseDate(toDate, LocalDate.now());
    LocalDate start = parseDate(fromDate, end.minusDays(29));
    validateDateRange(start, end, 366);

    List<FlowReportItem> rows = buildFlowRows(scopedOrgId, start, end, eventType, keyword);
    List<String> headers = List.of("日期", "事件类型", "老人ID", "老人姓名", "备注");
    List<List<String>> body = rows.stream()
        .map(item -> List.of(
            stringOf(item.getEventDate()),
            "ADMISSION".equals(item.getEventType()) ? "入院" : "离院",
            stringOf(item.getElderId()),
            stringOf(item.getElderName()),
            stringOf(item.getRemark())))
        .toList();
    return csvResponse("elder-flow-report", headers, body);
  }

  private List<OrgMonthlyOperationItem> buildOrgMonthlyOperation(Long orgId, YearMonth start, YearMonth end) {
    LocalDate endDate = end.atEndOfMonth();

    List<ElderAdmission> admissions = admissionMapper.selectList(
        Wrappers.lambdaQuery(ElderAdmission.class)
            .eq(ElderAdmission::getIsDeleted, 0)
            .eq(orgId != null, ElderAdmission::getOrgId, orgId)
            .le(ElderAdmission::getAdmissionDate, endDate));
    List<ElderDischarge> discharges = dischargeMapper.selectList(
        Wrappers.lambdaQuery(ElderDischarge.class)
            .eq(ElderDischarge::getIsDeleted, 0)
            .eq(orgId != null, ElderDischarge::getOrgId, orgId)
            .le(ElderDischarge::getDischargeDate, endDate));
    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .ge(BillMonthly::getBillMonth, start.toString())
            .le(BillMonthly::getBillMonth, end.toString()));

    long totalBeds = bedMapper.selectCount(
        Wrappers.lambdaQuery(Bed.class)
            .eq(Bed::getIsDeleted, 0)
            .eq(orgId != null, Bed::getOrgId, orgId));

    Map<String, Long> admissionMap = initMonthCountMap(start, end);
    Map<String, Long> dischargeMap = initMonthCountMap(start, end);
    long occupied = 0L;
    for (ElderAdmission item : admissions) {
      if (item.getAdmissionDate() == null) {
        continue;
      }
      YearMonth month = YearMonth.from(item.getAdmissionDate());
      if (month.isBefore(start)) {
        occupied += 1L;
      } else {
        incrementMonthMap(admissionMap, month.toString());
      }
    }
    for (ElderDischarge item : discharges) {
      if (item.getDischargeDate() == null) {
        continue;
      }
      YearMonth month = YearMonth.from(item.getDischargeDate());
      if (month.isBefore(start)) {
        occupied -= 1L;
      } else {
        incrementMonthMap(dischargeMap, month.toString());
      }
    }
    if (occupied < 0) {
      occupied = 0;
    }

    Map<String, BigDecimal> revenueMap = initMonthAmountMap(start, end);
    for (BillMonthly bill : bills) {
      BigDecimal amount = safeAmount(bill.getTotalAmount());
      revenueMap.put(bill.getBillMonth(), revenueMap.getOrDefault(bill.getBillMonth(), BigDecimal.ZERO).add(amount));
    }

    List<OrgMonthlyOperationItem> result = new ArrayList<>();
    YearMonth cursor = start;
    String orgName = resolveOrgName(orgId);
    while (!cursor.isAfter(end)) {
      String month = cursor.toString();
      long monthAdmissions = admissionMap.getOrDefault(month, 0L);
      long monthDischarges = dischargeMap.getOrDefault(month, 0L);
      occupied = occupied + monthAdmissions - monthDischarges;
      if (occupied < 0) {
        occupied = 0;
      }
      if (totalBeds > 0 && occupied > totalBeds) {
        occupied = totalBeds;
      }
      OrgMonthlyOperationItem item = new OrgMonthlyOperationItem();
      item.setMonth(month);
      item.setOrgId(orgId);
      item.setOrgName(orgName);
      item.setAdmissions(monthAdmissions);
      item.setDischarges(monthDischarges);
      item.setRevenue(revenueMap.getOrDefault(month, BigDecimal.ZERO));
      item.setTotalBeds(totalBeds);
      item.setOccupiedBeds(occupied);
      item.setOccupancyRate(calculateRate(occupied, totalBeds));
      result.add(item);
      cursor = cursor.plusMonths(1);
    }
    return result;
  }

  private List<FlowReportItem> buildFlowRows(Long orgId, LocalDate start, LocalDate end, String eventType, String keyword) {
    String normalizedType = normalizeEventType(eventType);
    List<FlowReportItem> rows = new ArrayList<>();

    if (!"DISCHARGE".equals(normalizedType)) {
      List<ElderAdmission> admissions = admissionMapper.selectList(
          Wrappers.lambdaQuery(ElderAdmission.class)
              .eq(ElderAdmission::getIsDeleted, 0)
              .eq(orgId != null, ElderAdmission::getOrgId, orgId)
              .ge(ElderAdmission::getAdmissionDate, start)
              .le(ElderAdmission::getAdmissionDate, end));
      for (ElderAdmission item : admissions) {
        FlowReportItem row = new FlowReportItem();
        row.setEventType("ADMISSION");
        row.setEventDate(item.getAdmissionDate());
        row.setElderId(item.getElderId());
        row.setRemark(item.getRemark());
        rows.add(row);
      }
    }
    if (!"ADMISSION".equals(normalizedType)) {
      List<ElderDischarge> discharges = dischargeMapper.selectList(
          Wrappers.lambdaQuery(ElderDischarge.class)
              .eq(ElderDischarge::getIsDeleted, 0)
              .eq(orgId != null, ElderDischarge::getOrgId, orgId)
              .ge(ElderDischarge::getDischargeDate, start)
              .le(ElderDischarge::getDischargeDate, end));
      for (ElderDischarge item : discharges) {
        FlowReportItem row = new FlowReportItem();
        row.setEventType("DISCHARGE");
        row.setEventDate(item.getDischargeDate());
        row.setElderId(item.getElderId());
        row.setRemark(item.getReason());
        rows.add(row);
      }
    }

    Map<Long, ElderProfile> elderMap = buildElderMap(rows.stream()
        .map(FlowReportItem::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList());

    return rows.stream()
        .peek(row -> {
          if (row.getElderId() != null) {
            ElderProfile elder = elderMap.get(row.getElderId());
            row.setElderName(elder == null ? null : elder.getFullName());
          }
        })
        .filter(item -> keyword == null || keyword.isBlank()
            || (item.getElderName() != null && item.getElderName().contains(keyword)))
        .sorted(Comparator.comparing(FlowReportItem::getEventDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
        .toList();
  }

  private String normalizeEventType(String eventType) {
    if (eventType == null || eventType.isBlank()) {
      return null;
    }
    if ("ADMISSION".equalsIgnoreCase(eventType)) {
      return "ADMISSION";
    }
    if ("DISCHARGE".equalsIgnoreCase(eventType)) {
      return "DISCHARGE";
    }
    throw new IllegalArgumentException("eventType must be ADMISSION or DISCHARGE");
  }

  private Long resolveAccessibleOrgId(Long requestedOrgId) {
    Long currentOrgId = AuthContext.getOrgId();
    boolean admin = AuthContext.isAdmin();

    if (requestedOrgId == null) {
      if (currentOrgId != null) {
        return currentOrgId;
      }
      if (admin) {
        throw new IllegalArgumentException("Admin request requires orgId");
      }
      throw new IllegalArgumentException("Unable to determine orgId from token");
    }

    if (admin) {
      return requestedOrgId;
    }
    if (currentOrgId == null || !requestedOrgId.equals(currentOrgId)) {
      throw new AccessDeniedException("No permission to access another organization");
    }
    return requestedOrgId;
  }

  private Map<Long, ElderProfile> buildElderMap(List<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      return Map.of();
    }
    return elderMapper.selectList(
            Wrappers.lambdaQuery(ElderProfile.class)
                .in(ElderProfile::getId, elderIds))
        .stream()
        .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (a, b) -> a));
  }

  private String elderStatusLabel(Integer status) {
    if (status == null) {
      return "未知";
    }
    return switch (status) {
      case 1 -> "在院";
      case 2 -> "请假";
      case 3 -> "离院";
      default -> "未知";
    };
  }

  private String resolveAgeBucket(LocalDate birthDate, LocalDate today) {
    if (birthDate == null || birthDate.isAfter(today)) {
      return "未知";
    }
    int years = Period.between(birthDate, today).getYears();
    if (years < 60) {
      return "60岁以下";
    }
    if (years < 70) {
      return "60-69岁";
    }
    if (years < 80) {
      return "70-79岁";
    }
    if (years < 90) {
      return "80-89岁";
    }
    return "90岁及以上";
  }

  private String resolveOrgName(Long orgId) {
    if (orgId == null) {
      return null;
    }
    Org org = orgMapper.selectById(orgId);
    return org == null ? null : org.getOrgName();
  }

  private BigDecimal safeAmount(BigDecimal amount) {
    return amount == null ? BigDecimal.ZERO : amount;
  }

  private Map<String, Long> initMonthCountMap(YearMonth start, YearMonth end) {
    Map<String, Long> map = new LinkedHashMap<>();
    YearMonth cursor = start;
    while (!cursor.isAfter(end)) {
      map.put(cursor.toString(), 0L);
      cursor = cursor.plusMonths(1);
    }
    return map;
  }

  private Map<String, BigDecimal> initMonthAmountMap(YearMonth start, YearMonth end) {
    Map<String, BigDecimal> map = new LinkedHashMap<>();
    YearMonth cursor = start;
    while (!cursor.isAfter(end)) {
      map.put(cursor.toString(), BigDecimal.ZERO);
      cursor = cursor.plusMonths(1);
    }
    return map;
  }

  private void incrementMonthMap(Map<String, Long> map, String month) {
    map.put(month, map.getOrDefault(month, 0L) + 1L);
  }

  private List<MonthCountItem> toMonthCountList(Map<String, Long> monthMap) {
    List<MonthCountItem> result = new ArrayList<>();
    for (Map.Entry<String, Long> entry : monthMap.entrySet()) {
      MonthCountItem item = new MonthCountItem();
      item.setMonth(entry.getKey());
      item.setCount(entry.getValue());
      result.add(item);
    }
    return result;
  }

  private List<MonthAmountItem> toMonthAmountList(Map<String, BigDecimal> monthMap) {
    List<MonthAmountItem> result = new ArrayList<>();
    for (Map.Entry<String, BigDecimal> entry : monthMap.entrySet()) {
      MonthAmountItem item = new MonthAmountItem();
      item.setMonth(entry.getKey());
      item.setAmount(entry.getValue());
      result.add(item);
    }
    return result;
  }

  private List<MonthAmountItem> toMergedMonthAmountList(
      Map<String, BigDecimal> first, Map<String, BigDecimal> second) {
    Map<String, BigDecimal> merged = new LinkedHashMap<>();
    for (Map.Entry<String, BigDecimal> entry : first.entrySet()) {
      String month = entry.getKey();
      merged.put(month, safeAmount(entry.getValue()).add(safeAmount(second.get(month))));
    }
    return toMonthAmountList(merged);
  }

  private List<MonthCountItem> toMonthNetCountList(Map<String, Long> income, Map<String, Long> outcome) {
    List<MonthCountItem> result = new ArrayList<>();
    for (Map.Entry<String, Long> entry : income.entrySet()) {
      MonthCountItem item = new MonthCountItem();
      item.setMonth(entry.getKey());
      item.setCount(entry.getValue() - outcome.getOrDefault(entry.getKey(), 0L));
      result.add(item);
    }
    return result;
  }

  private List<NameCountItem> toNameCountList(Map<String, Long> data) {
    List<NameCountItem> result = new ArrayList<>();
    for (Map.Entry<String, Long> entry : data.entrySet()) {
      NameCountItem item = new NameCountItem();
      item.setName(entry.getKey());
      item.setCount(entry.getValue());
      result.add(item);
    }
    return result;
  }

  private BigDecimal calculateRate(long numerator, long denominator) {
    if (denominator <= 0) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(numerator)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateShareRate(BigDecimal part, BigDecimal total) {
    if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return safeAmount(part)
        .multiply(BigDecimal.valueOf(100))
        .divide(total, 2, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateAverage(BigDecimal total, int periodCount) {
    if (periodCount <= 0) {
      return BigDecimal.ZERO;
    }
    return safeAmount(total).divide(BigDecimal.valueOf(periodCount), 2, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateMonthOverMonthGrowth(Map<String, BigDecimal> monthMap) {
    if (monthMap == null || monthMap.size() < 2) {
      return BigDecimal.ZERO;
    }
    List<BigDecimal> values = new ArrayList<>(monthMap.values());
    BigDecimal previous = safeAmount(values.get(values.size() - 2));
    BigDecimal current = safeAmount(values.get(values.size() - 1));
    if (previous.compareTo(BigDecimal.ZERO) <= 0) {
      return current.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
    }
    return current.subtract(previous)
        .multiply(BigDecimal.valueOf(100))
        .divide(previous, 2, RoundingMode.HALF_UP);
  }

  private YearMonth parseYearMonth(String value, YearMonth fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    try {
      return YearMonth.parse(value);
    } catch (DateTimeParseException ex) {
      throw new IllegalArgumentException("Invalid month format, expected YYYY-MM");
    }
  }

  private LocalDate parseDate(String value, LocalDate fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    try {
      return LocalDate.parse(value);
    } catch (DateTimeParseException ex) {
      throw new IllegalArgumentException("Invalid date format, expected YYYY-MM-DD");
    }
  }

  private void validateMonthRange(YearMonth start, YearMonth end, int maxSpanMonths) {
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("from must be earlier than or equal to to");
    }
    long span = end.getYear() * 12L + end.getMonthValue() - (start.getYear() * 12L + start.getMonthValue()) + 1;
    if (span > maxSpanMonths) {
      throw new IllegalArgumentException("Date range is too large");
    }
  }

  private void validateDateRange(LocalDate start, LocalDate end, int maxDays) {
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("fromDate must be earlier than or equal to toDate");
    }
    if (start.plusDays(maxDays).isBefore(end)) {
      throw new IllegalArgumentException("Date range is too large");
    }
  }

  private ResponseEntity<byte[]> csvResponse(String filenameBase, List<String> headers, List<List<String>> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append(toCsvLine(headers)).append("\n");
    for (List<String> row : rows) {
      sb.append(toCsvLine(row)).append("\n");
    }
    byte[] content = sb.toString().getBytes(StandardCharsets.UTF_8);
    String filename = filenameBase + "-" + LocalDate.now() + ".csv";

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .body(content);
  }

  private String toCsvLine(List<String> values) {
    return values.stream()
        .map(this::escapeCsv)
        .collect(Collectors.joining(","));
  }

  private String escapeCsv(String value) {
    String text = value == null ? "" : value;
    String escaped = text.replace("\"", "\"\"");
    return "\"" + escaped + "\"";
  }

  private String stringOf(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  @Data
  public static class MonthCountItem {
    private String month;
    private Long count;
  }

  @Data
  public static class MonthAmountItem {
    private String month;
    private BigDecimal amount;
  }

  @Data
  public static class NameCountItem {
    private String name;
    private Long count;
  }

  @Data
  public static class ElderAmountItem {
    private Long elderId;
    private String elderName;
    private BigDecimal amount;
  }

  @Data
  public static class CheckInStatsResponse {
    private Long totalAdmissions;
    private Long totalDischarges;
    private Long netIncrease;
    private BigDecimal dischargeToAdmissionRate;
    private Long currentResidents;
    private List<MonthCountItem> monthlyAdmissions;
    private List<MonthCountItem> monthlyDischarges;
    private List<MonthCountItem> monthlyNetIncrease;
  }

  @Data
  public static class ConsumptionStatsResponse {
    private BigDecimal totalBillConsumption;
    private BigDecimal totalStoreConsumption;
    private BigDecimal totalConsumption;
    private BigDecimal billConsumptionRatio;
    private BigDecimal storeConsumptionRatio;
    private BigDecimal averageMonthlyConsumption;
    private List<MonthAmountItem> monthlyBillConsumption;
    private List<MonthAmountItem> monthlyStoreConsumption;
    private List<MonthAmountItem> monthlyTotalConsumption;
    private List<ElderAmountItem> topConsumerElders;
  }

  @Data
  public static class ElderInfoStatsResponse {
    private Long totalElders;
    private Long inHospitalCount;
    private Long dischargedCount;
    private List<NameCountItem> genderDistribution;
    private List<NameCountItem> ageDistribution;
    private List<NameCountItem> careLevelDistribution;
    private List<NameCountItem> statusDistribution;
  }

  @Data
  public static class OrgMonthlyOperationItem {
    private Long orgId;
    private String orgName;
    private String month;
    private Long admissions;
    private Long discharges;
    private BigDecimal revenue;
    private Long totalBeds;
    private Long occupiedBeds;
    private BigDecimal occupancyRate;
  }

  @Data
  public static class MonthFlowItem {
    private String month;
    private Long admissions;
    private Long discharges;
  }

  @Data
  public static class BedUsageStatsResponse {
    private Long totalBeds;
    private Long occupiedBeds;
    private Long availableBeds;
    private Long maintenanceBeds;
    private BigDecimal occupancyRate;
    private BigDecimal maintenanceRate;
    private BigDecimal availableRate;
  }

  @Data
  public static class MonthlyRevenueStatsResponse {
    private BigDecimal totalRevenue;
    private BigDecimal averageMonthlyRevenue;
    private BigDecimal revenueGrowthRate;
    private List<MonthAmountItem> monthlyRevenue;
  }

  @Data
  public static class FlowReportItem {
    private String eventType;
    private LocalDate eventDate;
    private Long elderId;
    private String elderName;
    private String remark;
  }

  @Data
  public static class FlowReportPageResponse {
    private List<FlowReportItem> list;
    private Long total;
    private Long pageNo;
    private Long pageSize;
  }
}
