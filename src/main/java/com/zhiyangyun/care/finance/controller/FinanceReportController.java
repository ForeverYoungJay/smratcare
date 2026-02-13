package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.model.FinanceArrearsItem;
import com.zhiyangyun.care.finance.model.FinanceReportMonthlyItem;
import com.zhiyangyun.care.finance.model.FinanceStoreSalesItem;
import com.zhiyangyun.care.store.entity.StoreOrder;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/report")
public class FinanceReportController {
  private final BillMonthlyMapper billMonthlyMapper;
  private final StoreOrderMapper storeOrderMapper;
  private final ElderMapper elderMapper;

  public FinanceReportController(
      BillMonthlyMapper billMonthlyMapper,
      StoreOrderMapper storeOrderMapper,
      ElderMapper elderMapper) {
    this.billMonthlyMapper = billMonthlyMapper;
    this.storeOrderMapper = storeOrderMapper;
    this.elderMapper = elderMapper;
  }

  @GetMapping("/monthly-revenue")
  public Result<List<FinanceReportMonthlyItem>> monthlyRevenue(
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to,
      @RequestParam(defaultValue = "6") int months) {
    Long orgId = AuthContext.getOrgId();
    YearMonth end = to == null || to.isBlank() ? YearMonth.now() : YearMonth.parse(to);
    YearMonth start = from == null || from.isBlank() ? end.minusMonths(months - 1L) : YearMonth.parse(from);
    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .ge(BillMonthly::getBillMonth, start.toString())
            .le(BillMonthly::getBillMonth, end.toString()));
    Map<String, BigDecimal> sumMap = new LinkedHashMap<>();
    YearMonth cursor = start;
    while (!cursor.isAfter(end)) {
      sumMap.put(cursor.toString(), BigDecimal.ZERO);
      cursor = cursor.plusMonths(1);
    }
    for (BillMonthly bill : bills) {
      String month = bill.getBillMonth();
      BigDecimal amount = bill.getTotalAmount() == null ? BigDecimal.ZERO : bill.getTotalAmount();
      sumMap.put(month, sumMap.getOrDefault(month, BigDecimal.ZERO).add(amount));
    }
    List<FinanceReportMonthlyItem> result = new ArrayList<>();
    for (Map.Entry<String, BigDecimal> entry : sumMap.entrySet()) {
      FinanceReportMonthlyItem item = new FinanceReportMonthlyItem();
      item.setMonth(entry.getKey());
      item.setAmount(entry.getValue());
      result.add(item);
    }
    return Result.ok(result);
  }

  @GetMapping("/arrears-top")
  public Result<List<FinanceArrearsItem>> arrearsTop(@RequestParam(defaultValue = "10") int limit) {
    Long orgId = AuthContext.getOrgId();
    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .gt(BillMonthly::getOutstandingAmount, BigDecimal.ZERO));
    Map<Long, BigDecimal> sumByElder = new LinkedHashMap<>();
    for (BillMonthly bill : bills) {
      sumByElder.merge(bill.getElderId(),
          bill.getOutstandingAmount() == null ? BigDecimal.ZERO : bill.getOutstandingAmount(),
          BigDecimal::add);
    }
    List<Long> elderIds = new ArrayList<>(sumByElder.keySet());
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectList(
            Wrappers.lambdaQuery(ElderProfile.class)
                .in(ElderProfile::getId, elderIds))
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (a, b) -> a));
    List<FinanceArrearsItem> result = sumByElder.entrySet().stream()
        .map(entry -> {
          FinanceArrearsItem item = new FinanceArrearsItem();
          item.setElderId(entry.getKey());
          ElderProfile elder = elderMap.get(entry.getKey());
          item.setElderName(elder == null ? null : elder.getFullName());
          item.setOutstandingAmount(entry.getValue());
          return item;
        })
        .sorted(Comparator.comparing(FinanceArrearsItem::getOutstandingAmount).reversed())
        .limit(limit)
        .toList();
    return Result.ok(result);
  }

  @GetMapping("/store-sales")
  public Result<List<FinanceStoreSalesItem>> storeSales(
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to,
      @RequestParam(defaultValue = "6") int months) {
    Long orgId = AuthContext.getOrgId();
    YearMonth end = to == null || to.isBlank() ? YearMonth.now() : YearMonth.parse(to);
    YearMonth start = from == null || from.isBlank() ? end.minusMonths(months - 1L) : YearMonth.parse(from);
    LocalDate startDate = start.atDay(1);
    LocalDate endDate = end.atEndOfMonth();
    LocalDateTime startTime = startDate.atStartOfDay();
    LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();
    List<StoreOrder> orders = storeOrderMapper.selectList(
        Wrappers.lambdaQuery(StoreOrder.class)
            .eq(StoreOrder::getIsDeleted, 0)
            .eq(orgId != null, StoreOrder::getOrgId, orgId)
            .ge(StoreOrder::getCreateTime, startTime)
            .lt(StoreOrder::getCreateTime, endTime));
    Map<String, BigDecimal> sumMap = new LinkedHashMap<>();
    YearMonth cursor = start;
    while (!cursor.isAfter(end)) {
      sumMap.put(cursor.toString(), BigDecimal.ZERO);
      cursor = cursor.plusMonths(1);
    }
    for (StoreOrder order : orders) {
      String month = YearMonth.from(order.getCreateTime()).toString();
      BigDecimal amount = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
      sumMap.put(month, sumMap.getOrDefault(month, BigDecimal.ZERO).add(amount));
    }
    List<FinanceStoreSalesItem> result = new ArrayList<>();
    for (Map.Entry<String, BigDecimal> entry : sumMap.entrySet()) {
      FinanceStoreSalesItem item = new FinanceStoreSalesItem();
      item.setMonth(entry.getKey());
      item.setAmount(entry.getValue());
      result.add(item);
    }
    return Result.ok(result);
  }
}
