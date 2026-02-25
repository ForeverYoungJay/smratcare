package com.zhiyangyun.care.dashboard.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.dashboard.model.DashboardSummary;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmission;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischarge;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeMapper;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.entity.StoreOrder;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
  private static final int SUMMARY_MONTH_SPAN = 6;

  private final CareTaskDailyMapper careTaskDailyMapper;
  private final ProductMapper productMapper;
  private final InventoryBatchMapper inventoryBatchMapper;
  private final BillMonthlyMapper billMonthlyMapper;
  private final ElderAdmissionMapper elderAdmissionMapper;
  private final ElderDischargeMapper elderDischargeMapper;
  private final StoreOrderMapper storeOrderMapper;
  private final BedMapper bedMapper;
  private final ElderMapper elderMapper;

  public DashboardController(CareTaskDailyMapper careTaskDailyMapper,
      ProductMapper productMapper,
      InventoryBatchMapper inventoryBatchMapper,
      BillMonthlyMapper billMonthlyMapper,
      ElderAdmissionMapper elderAdmissionMapper,
      ElderDischargeMapper elderDischargeMapper,
      StoreOrderMapper storeOrderMapper,
      BedMapper bedMapper,
      ElderMapper elderMapper) {
    this.careTaskDailyMapper = careTaskDailyMapper;
    this.productMapper = productMapper;
    this.inventoryBatchMapper = inventoryBatchMapper;
    this.billMonthlyMapper = billMonthlyMapper;
    this.elderAdmissionMapper = elderAdmissionMapper;
    this.elderDischargeMapper = elderDischargeMapper;
    this.storeOrderMapper = storeOrderMapper;
    this.bedMapper = bedMapper;
    this.elderMapper = elderMapper;
  }

  @GetMapping("/summary")
  public Result<DashboardSummary> summary() {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();

    long careTasksToday = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(CareTaskDaily::getTaskDate, today)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId));

    long abnormalTasksToday = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(CareTaskDaily::getTaskDate, today)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getStatus, "EXCEPTION"));

    List<Product> products = productMapper.selectList(Wrappers.lambdaQuery(Product.class)
        .eq(Product::getIsDeleted, 0)
        .eq(orgId != null, Product::getOrgId, orgId));
    List<InventoryBatch> batches = inventoryBatchMapper.selectList(Wrappers.lambdaQuery(InventoryBatch.class)
        .eq(InventoryBatch::getIsDeleted, 0)
        .eq(orgId != null, InventoryBatch::getOrgId, orgId));

    Map<Long, Integer> qtyByProduct = new HashMap<>();
    for (InventoryBatch batch : batches) {
      if (batch.getProductId() == null) continue;
      qtyByProduct.merge(batch.getProductId(), batch.getQuantity() == null ? 0 : batch.getQuantity(), Integer::sum);
    }

    long inventoryAlerts = products.stream()
        .filter(p -> {
          int qty = qtyByProduct.getOrDefault(p.getId(), 0);
          int safety = p.getSafetyStock() == null ? 0 : p.getSafetyStock();
          return qty < safety;
        })
        .count();

    long unpaidBills = billMonthlyMapper.selectCount(Wrappers.lambdaQuery(BillMonthly.class)
        .eq(BillMonthly::getIsDeleted, 0)
        .eq(orgId != null, BillMonthly::getOrgId, orgId)
        .lt(BillMonthly::getStatus, 2));

    YearMonth end = YearMonth.now();
    YearMonth start = end.minusMonths(SUMMARY_MONTH_SPAN - 1L);
    LocalDate startDate = start.atDay(1);
    LocalDate endDate = end.atEndOfMonth();
    LocalDateTime startTime = startDate.atStartOfDay();
    LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();

    long totalAdmissions = elderAdmissionMapper.selectCount(
        Wrappers.lambdaQuery(ElderAdmission.class)
            .eq(ElderAdmission::getIsDeleted, 0)
            .eq(orgId != null, ElderAdmission::getOrgId, orgId)
            .ge(ElderAdmission::getAdmissionDate, startDate)
            .le(ElderAdmission::getAdmissionDate, endDate));

    long totalDischarges = elderDischargeMapper.selectCount(
        Wrappers.lambdaQuery(ElderDischarge.class)
            .eq(ElderDischarge::getIsDeleted, 0)
            .eq(orgId != null, ElderDischarge::getOrgId, orgId)
            .ge(ElderDischarge::getDischargeDate, startDate)
            .le(ElderDischarge::getDischargeDate, endDate));

    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .ge(BillMonthly::getBillMonth, start.toString())
            .le(BillMonthly::getBillMonth, end.toString()));
    List<StoreOrder> orders = storeOrderMapper.selectList(
        Wrappers.lambdaQuery(StoreOrder.class)
            .eq(StoreOrder::getIsDeleted, 0)
            .eq(orgId != null, StoreOrder::getOrgId, orgId)
            .ge(StoreOrder::getCreateTime, startTime)
            .lt(StoreOrder::getCreateTime, endTime));

    Map<String, BigDecimal> revenueByMonth = initMonthAmountMap(start, end);
    BigDecimal totalBillConsumption = BigDecimal.ZERO;
    for (BillMonthly bill : bills) {
      BigDecimal amount = safeAmount(bill.getTotalAmount());
      totalBillConsumption = totalBillConsumption.add(amount);
      if (bill.getBillMonth() != null && revenueByMonth.containsKey(bill.getBillMonth())) {
        revenueByMonth.put(bill.getBillMonth(), revenueByMonth.get(bill.getBillMonth()).add(amount));
      }
    }
    BigDecimal totalStoreConsumption = BigDecimal.ZERO;
    for (StoreOrder order : orders) {
      BigDecimal amount = safeAmount(order.getTotalAmount());
      totalStoreConsumption = totalStoreConsumption.add(amount);
    }
    BigDecimal totalConsumption = totalBillConsumption.add(totalStoreConsumption);

    List<Bed> beds = bedMapper.selectList(
        Wrappers.lambdaQuery(Bed.class)
            .eq(Bed::getIsDeleted, 0)
            .eq(orgId != null, Bed::getOrgId, orgId));
    long totalBeds = beds.size();
    long occupiedBeds = beds.stream().filter(item -> item.getStatus() != null && item.getStatus() == 2).count();
    long maintenanceBeds = beds.stream().filter(item -> item.getStatus() != null && item.getStatus() == 3).count();
    long availableBeds = totalBeds - occupiedBeds - maintenanceBeds;
    if (availableBeds < 0) {
      availableBeds = 0;
    }

    long inHospitalCount = elderMapper.selectCount(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(orgId != null, ElderProfile::getOrgId, orgId)
            .eq(ElderProfile::getStatus, 1));
    long dischargedCount = elderMapper.selectCount(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(orgId != null, ElderProfile::getOrgId, orgId)
            .eq(ElderProfile::getStatus, 3));

    DashboardSummary summary = new DashboardSummary();
    summary.setCareTasksToday(careTasksToday);
    summary.setAbnormalTasksToday(abnormalTasksToday);
    summary.setInventoryAlerts(inventoryAlerts);
    summary.setUnpaidBills(unpaidBills);
    summary.setTotalAdmissions(totalAdmissions);
    summary.setTotalDischarges(totalDischarges);
    summary.setCheckInNetIncrease(totalAdmissions - totalDischarges);
    summary.setDischargeToAdmissionRate(calculateRate(totalDischarges, totalAdmissions));
    summary.setTotalBillConsumption(totalBillConsumption);
    summary.setTotalStoreConsumption(totalStoreConsumption);
    summary.setTotalConsumption(totalConsumption);
    summary.setAverageMonthlyConsumption(calculateAverage(totalConsumption, SUMMARY_MONTH_SPAN));
    summary.setBillConsumptionRatio(calculateShareRate(totalBillConsumption, totalConsumption));
    summary.setStoreConsumptionRatio(calculateShareRate(totalStoreConsumption, totalConsumption));
    summary.setInHospitalCount(inHospitalCount);
    summary.setDischargedCount(dischargedCount);
    summary.setTotalBeds(totalBeds);
    summary.setOccupiedBeds(occupiedBeds);
    summary.setAvailableBeds(availableBeds);
    summary.setBedOccupancyRate(calculateRate(occupiedBeds, totalBeds));
    summary.setBedAvailableRate(calculateRate(availableBeds, totalBeds));
    BigDecimal totalRevenue = revenueByMonth.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    summary.setTotalRevenue(totalRevenue);
    summary.setAverageMonthlyRevenue(calculateAverage(totalRevenue, SUMMARY_MONTH_SPAN));
    summary.setRevenueGrowthRate(calculateMonthOverMonthGrowth(revenueByMonth));
    return Result.ok(summary);
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

  private BigDecimal safeAmount(BigDecimal amount) {
    return amount == null ? BigDecimal.ZERO : amount;
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
}
