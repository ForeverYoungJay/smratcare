package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.entity.BillItem;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.bill.mapper.BillItemMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.finance.model.FinanceArrearsItem;
import com.zhiyangyun.care.finance.model.FinanceCategoryConsumptionAnalysisResponse;
import com.zhiyangyun.care.finance.model.FinanceReportEntrySummaryResponse;
import com.zhiyangyun.care.finance.model.FinanceReportMonthlyItem;
import com.zhiyangyun.care.finance.model.FinanceStoreSalesItem;
import com.zhiyangyun.care.finance.entity.DischargeSettlement;
import com.zhiyangyun.care.finance.entity.FinanceRefundVoucher;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.mapper.DischargeSettlementMapper;
import com.zhiyangyun.care.finance.mapper.FinanceRefundVoucherMapper;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.entity.OrderItem;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.entity.StoreOrder;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.OrderItemMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/report")
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class FinanceReportController {
  private final BillMonthlyMapper billMonthlyMapper;
  private final BillItemMapper billItemMapper;
  private final StoreOrderMapper storeOrderMapper;
  private final ElderMapper elderMapper;
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;
  private final OrderItemMapper orderItemMapper;
  private final ProductMapper productMapper;
  private final InventoryBatchMapper inventoryBatchMapper;
  private final PaymentRecordMapper paymentRecordMapper;
  private final FinanceRefundVoucherMapper financeRefundVoucherMapper;
  private final DischargeSettlementMapper dischargeSettlementMapper;

  public FinanceReportController(
      BillMonthlyMapper billMonthlyMapper,
      BillItemMapper billItemMapper,
      StoreOrderMapper storeOrderMapper,
      ElderMapper elderMapper,
      BedMapper bedMapper,
      RoomMapper roomMapper,
      OrderItemMapper orderItemMapper,
      ProductMapper productMapper,
      InventoryBatchMapper inventoryBatchMapper,
      PaymentRecordMapper paymentRecordMapper,
      FinanceRefundVoucherMapper financeRefundVoucherMapper,
      DischargeSettlementMapper dischargeSettlementMapper) {
    this.billMonthlyMapper = billMonthlyMapper;
    this.billItemMapper = billItemMapper;
    this.storeOrderMapper = storeOrderMapper;
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.roomMapper = roomMapper;
    this.orderItemMapper = orderItemMapper;
    this.productMapper = productMapper;
    this.inventoryBatchMapper = inventoryBatchMapper;
    this.paymentRecordMapper = paymentRecordMapper;
    this.financeRefundVoucherMapper = financeRefundVoucherMapper;
    this.dischargeSettlementMapper = dischargeSettlementMapper;
  }

  @GetMapping("/entry-summary")
  public Result<FinanceReportEntrySummaryResponse> entrySummary(
      @RequestParam(required = false) String reportKey,
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to,
      @RequestParam(defaultValue = "8") int top) {
    Long orgId = AuthContext.getOrgId();
    YearMonth end = to == null || to.isBlank() ? YearMonth.now() : YearMonth.parse(to);
    YearMonth start = from == null || from.isBlank() ? end.minusMonths(5) : YearMonth.parse(from);
    LocalDate startDate = start.atDay(1);
    LocalDate endDate = end.atEndOfMonth();
    LocalDateTime startTime = startDate.atStartOfDay();
    LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();

    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .ge(BillMonthly::getBillMonth, start.toString())
            .le(BillMonthly::getBillMonth, end.toString()));
    List<PaymentRecord> payments = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(orgId != null, PaymentRecord::getOrgId, orgId)
            .ge(PaymentRecord::getPaidAt, startTime)
            .lt(PaymentRecord::getPaidAt, endTime));
    List<StoreOrder> orders = paidStoreOrders(orgId, startTime, endTime);
    List<Bed> beds = bedMapper.selectList(
        Wrappers.lambdaQuery(Bed.class)
            .eq(Bed::getIsDeleted, 0)
            .eq(orgId != null, Bed::getOrgId, orgId));
    List<Room> rooms = roomMapper.selectList(
        Wrappers.lambdaQuery(Room.class)
            .eq(Room::getIsDeleted, 0)
            .eq(orgId != null, Room::getOrgId, orgId));
    Map<Long, Bed> elderBedMap = beds.stream()
        .filter(item -> item.getElderId() != null)
        .collect(Collectors.toMap(Bed::getElderId, Function.identity(), (a, b) -> a));
    Map<Long, Room> roomMap = rooms.stream()
        .collect(Collectors.toMap(Room::getId, Function.identity(), (a, b) -> a));

    FinanceReportEntrySummaryResponse response = new FinanceReportEntrySummaryResponse();
    String normalizedKey = normalizeText(reportKey, "OVERALL").toUpperCase(Locale.ROOT);
    response.setReportKey(normalizedKey);
    response.setPeriodFrom(start.toString());
    response.setPeriodTo(end.toString());
    BigDecimal billedRevenue = sumBillAmount(bills, false);
    BigDecimal totalReceived = sumPayments(payments);
    BigDecimal refundTotal = sumRefundAmount(orgId, startTime, endTime);
    BigDecimal netRevenue = totalReceived.subtract(refundTotal);
    response.setBilledRevenue(billedRevenue);
    response.setTotalReceived(totalReceived);
    response.setRefundTotal(refundTotal);
    response.setNetRevenue(netRevenue);
    response.setTotalRevenue(netRevenue);
    response.setArrearsTotal(sumBillAmount(bills, true));
    response.setArrearsElderCount((long) bills.stream()
        .filter(item -> safe(item.getOutstandingAmount()).compareTo(BigDecimal.ZERO) > 0)
        .map(BillMonthly::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .count());
    response.setTotalStoreSales(orders.stream()
        .map(StoreOrder::getTotalAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add));

    Map<Long, BillMonthly> paymentBillMap = loadPaymentBillMap(payments);
    Map<Long, List<BillItem>> itemsByBillId = loadBillItemsByBillId(orgId, paymentBillMap.keySet());
    response.setTopCategories(topCategories(payments, paymentBillMap, itemsByBillId, top));
    response.setTopRooms(topRooms(payments, paymentBillMap, elderBedMap, roomMap, top));
    applyReportWarning(response);
    return Result.ok(response);
  }

  @GetMapping("/monthly-revenue")
  public Result<List<FinanceReportMonthlyItem>> monthlyRevenue(
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to,
      @RequestParam(defaultValue = "6") int months) {
    Long orgId = AuthContext.getOrgId();
    YearMonth end = to == null || to.isBlank() ? YearMonth.now() : YearMonth.parse(to);
    YearMonth start = from == null || from.isBlank() ? end.minusMonths(months - 1L) : YearMonth.parse(from);
    LocalDateTime startTime = start.atDay(1).atStartOfDay();
    LocalDateTime endTime = end.plusMonths(1).atDay(1).atStartOfDay();
    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .ge(BillMonthly::getBillMonth, start.toString())
            .le(BillMonthly::getBillMonth, end.toString()));
    List<PaymentRecord> payments = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(orgId != null, PaymentRecord::getOrgId, orgId)
            .ge(PaymentRecord::getPaidAt, startTime)
            .lt(PaymentRecord::getPaidAt, endTime));
    Map<String, BigDecimal> refundMap = refundAmountByMonth(orgId, startTime, endTime);
    Map<String, BigDecimal> sumMap = new LinkedHashMap<>();
    Map<String, BigDecimal> billedMap = new LinkedHashMap<>();
    Map<String, BigDecimal> receivedMap = new LinkedHashMap<>();
    YearMonth cursor = start;
    while (!cursor.isAfter(end)) {
      sumMap.put(cursor.toString(), BigDecimal.ZERO);
      billedMap.put(cursor.toString(), BigDecimal.ZERO);
      receivedMap.put(cursor.toString(), BigDecimal.ZERO);
      cursor = cursor.plusMonths(1);
    }
    for (BillMonthly bill : bills) {
      String month = bill.getBillMonth();
      BigDecimal amount = bill.getTotalAmount() == null ? BigDecimal.ZERO : bill.getTotalAmount();
      billedMap.put(month, billedMap.getOrDefault(month, BigDecimal.ZERO).add(amount));
    }
    for (PaymentRecord payment : payments) {
      if (payment.getPaidAt() == null) {
        continue;
      }
      String month = YearMonth.from(payment.getPaidAt()).toString();
      BigDecimal amount = payment.getAmount() == null ? BigDecimal.ZERO : payment.getAmount();
      receivedMap.put(month, receivedMap.getOrDefault(month, BigDecimal.ZERO).add(amount));
    }
    for (Map.Entry<String, BigDecimal> entry : receivedMap.entrySet()) {
      BigDecimal refundAmount = refundMap.getOrDefault(entry.getKey(), BigDecimal.ZERO);
      sumMap.put(entry.getKey(), entry.getValue().subtract(refundAmount));
    }
    List<FinanceReportMonthlyItem> result = new ArrayList<>();
    for (Map.Entry<String, BigDecimal> entry : sumMap.entrySet()) {
      FinanceReportMonthlyItem item = new FinanceReportMonthlyItem();
      item.setMonth(entry.getKey());
      item.setAmount(entry.getValue());
      item.setBilledAmount(billedMap.getOrDefault(entry.getKey(), BigDecimal.ZERO));
      item.setReceivedAmount(receivedMap.getOrDefault(entry.getKey(), BigDecimal.ZERO));
      item.setRefundAmount(refundMap.getOrDefault(entry.getKey(), BigDecimal.ZERO));
      item.setNetAmount(entry.getValue());
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
    List<StoreOrder> orders = paidStoreOrders(orgId, startTime, endTime);
    Map<String, BigDecimal> sumMap = new LinkedHashMap<>();
    YearMonth cursor = start;
    while (!cursor.isAfter(end)) {
      sumMap.put(cursor.toString(), BigDecimal.ZERO);
      cursor = cursor.plusMonths(1);
    }
    for (StoreOrder order : orders) {
      LocalDateTime paidTime = effectiveOrderTime(order);
      if (paidTime == null) {
        continue;
      }
      String month = YearMonth.from(paidTime).toString();
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

  @GetMapping("/category-consumption-analysis")
  public Result<FinanceCategoryConsumptionAnalysisResponse> categoryConsumptionAnalysis(
      @RequestParam String from,
      @RequestParam String to,
      @RequestParam(required = false) String itemKeyword,
      @RequestParam(required = false) String building,
      @RequestParam(required = false) String floorNo) {
    Long orgId = AuthContext.getOrgId();
    LocalDate start = LocalDate.parse(from);
    LocalDate end = LocalDate.parse(to);
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("开始日期不能晚于结束日期");
    }
    LocalDateTime startTime = start.atStartOfDay();
    LocalDateTime endTime = end.plusDays(1).atStartOfDay();

    List<StoreOrder> orders = paidStoreOrders(orgId, startTime, endTime);
    String buildingFilter = normalizeText(building, "");
    String floorFilter = normalizeText(floorNo, "");
    if (!buildingFilter.isBlank() || !floorFilter.isBlank()) {
      List<Bed> beds = bedMapper.selectList(
          Wrappers.lambdaQuery(Bed.class)
              .eq(Bed::getIsDeleted, 0)
              .eq(orgId != null, Bed::getOrgId, orgId)
              .isNotNull(Bed::getElderId)
              .isNotNull(Bed::getRoomId));
      Map<Long, Room> roomMap = roomMapper.selectList(
              Wrappers.lambdaQuery(Room.class)
                  .eq(Room::getIsDeleted, 0)
                  .eq(orgId != null, Room::getOrgId, orgId))
          .stream()
          .collect(Collectors.toMap(Room::getId, Function.identity(), (a, b) -> a));
      Map<Long, Room> elderRoomMap = new HashMap<>();
      for (Bed bed : beds) {
        if (bed.getElderId() == null || bed.getRoomId() == null || elderRoomMap.containsKey(bed.getElderId())) {
          continue;
        }
        Room room = roomMap.get(bed.getRoomId());
        if (room != null) {
          elderRoomMap.put(bed.getElderId(), room);
        }
      }
      orders = orders.stream()
          .filter(order -> {
            Room room = order.getElderId() == null ? null : elderRoomMap.get(order.getElderId());
            if (room == null) return false;
            boolean buildingMatched = buildingFilter.isBlank() || normalizeText(room.getBuilding(), "").contains(buildingFilter);
            boolean floorMatched = floorFilter.isBlank() || normalizeText(room.getFloorNo(), "").contains(floorFilter);
            return buildingMatched && floorMatched;
          })
          .toList();
    }
    FinanceCategoryConsumptionAnalysisResponse response = new FinanceCategoryConsumptionAnalysisResponse();
    response.setFrom(from);
    response.setTo(to);
    response.setItemKeyword(normalizeText(itemKeyword, ""));
    if (orders.isEmpty()) {
      return Result.ok(response);
    }

    Map<Long, StoreOrder> orderMap = orders.stream()
        .collect(Collectors.toMap(StoreOrder::getId, Function.identity(), (a, b) -> a));
    List<Long> orderIds = new ArrayList<>(orderMap.keySet());
    List<OrderItem> items = orderItemMapper.selectList(
        Wrappers.lambdaQuery(OrderItem.class)
            .eq(OrderItem::getIsDeleted, 0)
            .eq(orgId != null, OrderItem::getOrgId, orgId)
            .in(OrderItem::getOrderId, orderIds));
    if (items.isEmpty()) {
      return Result.ok(response);
    }

    List<Long> productIds = items.stream()
        .map(OrderItem::getProductId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Product> productMap = productIds.isEmpty()
        ? Map.of()
        : productMapper.selectBatchIds(productIds).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
    Map<Long, BigDecimal> costMap = buildProductCostMap(orgId, productIds);
    String keyword = normalizeText(itemKeyword, "").toLowerCase(Locale.ROOT);

    Map<LocalDate, BigDecimal> dayTotalMap = new LinkedHashMap<>();
    Map<LocalDate, BigDecimal> dayKeywordMap = new LinkedHashMap<>();
    Map<String, FinanceCategoryConsumptionAnalysisResponse.CategoryProfitItem> categoryMap = new LinkedHashMap<>();
    BigDecimal totalAmount = BigDecimal.ZERO;
    BigDecimal keywordAmount = BigDecimal.ZERO;

    for (OrderItem item : items) {
      StoreOrder order = orderMap.get(item.getOrderId());
      LocalDateTime orderTime = effectiveOrderTime(order);
      if (order == null || orderTime == null) {
        continue;
      }
      LocalDate day = orderTime.toLocalDate();
      BigDecimal itemAmount = safe(item.getTotalAmount());
      totalAmount = totalAmount.add(itemAmount);
      dayTotalMap.merge(day, itemAmount, BigDecimal::add);

      String productName = normalizeText(item.getProductNameSnapshot(),
          normalizeText(item.getProductName(), ""));
      boolean keywordMatched = keyword.isBlank() || productName.toLowerCase(Locale.ROOT).contains(keyword);
      if (keywordMatched) {
        keywordAmount = keywordAmount.add(itemAmount);
        dayKeywordMap.merge(day, itemAmount, BigDecimal::add);
      }

      Product product = item.getProductId() == null ? null : productMap.get(item.getProductId());
      String category = normalizeText(product == null ? null : product.getCategory(), "未分类");
      FinanceCategoryConsumptionAnalysisResponse.CategoryProfitItem bucket = categoryMap.computeIfAbsent(
          category,
          key -> {
            FinanceCategoryConsumptionAnalysisResponse.CategoryProfitItem row =
                new FinanceCategoryConsumptionAnalysisResponse.CategoryProfitItem();
            row.setCategory(key);
            return row;
          });
      bucket.setTotalAmount(bucket.getTotalAmount().add(itemAmount));
      BigDecimal unitCost = costMap.getOrDefault(item.getProductId(), BigDecimal.ZERO);
      BigDecimal itemCost = unitCost.multiply(BigDecimal.valueOf(item.getQuantity() == null ? 0 : item.getQuantity()));
      bucket.setTotalCost(bucket.getTotalCost().add(itemCost));
    }

    response.setTotalAmount(totalAmount);
    response.setItemAmount(keywordAmount);
    response.setItemRatio(ratio(keywordAmount, totalAmount));

    LocalDate cursor = start;
    while (!cursor.isAfter(end)) {
      FinanceCategoryConsumptionAnalysisResponse.TrendItem trendItem =
          new FinanceCategoryConsumptionAnalysisResponse.TrendItem();
      trendItem.setPeriod(cursor.toString());
      trendItem.setAmount(dayKeywordMap.getOrDefault(cursor, BigDecimal.ZERO));
      trendItem.setRatio(ratio(dayKeywordMap.getOrDefault(cursor, BigDecimal.ZERO), dayTotalMap.getOrDefault(cursor, BigDecimal.ZERO)));
      response.getTrend().add(trendItem);
      cursor = cursor.plusDays(1);
    }

    response.setCategoryProfit(categoryMap.values().stream()
        .peek(item -> {
          item.setTotalProfit(item.getTotalAmount().subtract(item.getTotalCost()));
          item.setProfitRate(ratio(item.getTotalProfit(), item.getTotalAmount()));
        })
        .sorted(Comparator.comparing(FinanceCategoryConsumptionAnalysisResponse.CategoryProfitItem::getTotalProfit).reversed())
        .toList());
    return Result.ok(response);
  }

  private BigDecimal sumBillAmount(List<BillMonthly> bills, boolean outstanding) {
    return bills.stream()
        .map(item -> outstanding ? item.getOutstandingAmount() : item.getTotalAmount())
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private List<FinanceReportEntrySummaryResponse.NameAmountItem> topCategories(
      List<PaymentRecord> payments,
      Map<Long, BillMonthly> paymentBillMap,
      Map<Long, List<BillItem>> itemsByBillId,
      int top) {
    Map<String, BigDecimal> sumMap = new HashMap<>();
    for (PaymentRecord payment : payments) {
      BillMonthly bill = payment.getBillMonthlyId() == null ? null : paymentBillMap.get(payment.getBillMonthlyId());
      if (bill == null) {
        continue;
      }
      List<BillItem> items = itemsByBillId.getOrDefault(bill.getId(), List.of());
      Map<BillItem, BigDecimal> allocated = allocatePaymentToItems(items, safe(payment.getAmount()), safe(bill.getTotalAmount()));
      for (Map.Entry<BillItem, BigDecimal> entry : allocated.entrySet()) {
        BillItem item = entry.getKey();
        String key = normalizeCategory(item.getItemType(), item.getItemName());
        sumMap.merge(key, entry.getValue(), BigDecimal::add);
      }
    }
    return sumMap.entrySet().stream()
        .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
        .limit(Math.max(top, 1))
        .map(entry -> {
          FinanceReportEntrySummaryResponse.NameAmountItem row = new FinanceReportEntrySummaryResponse.NameAmountItem();
          row.setLabel(categoryLabel(entry.getKey()));
          row.setAmount(entry.getValue());
          row.setExtra(entry.getKey());
          return row;
        })
        .toList();
  }

  private List<FinanceReportEntrySummaryResponse.NameAmountItem> topRooms(
      List<PaymentRecord> payments,
      Map<Long, BillMonthly> paymentBillMap,
      Map<Long, Bed> elderBedMap,
      Map<Long, Room> roomMap,
      int top) {
    Map<String, BigDecimal> roomIncome = new HashMap<>();
    for (PaymentRecord payment : payments) {
      BillMonthly bill = payment.getBillMonthlyId() == null ? null : paymentBillMap.get(payment.getBillMonthlyId());
      if (bill == null) {
        continue;
      }
      if (bill.getElderId() == null) continue;
      Bed bed = elderBedMap.get(bill.getElderId());
      if (bed == null || bed.getRoomId() == null) continue;
      Room room = roomMap.get(bed.getRoomId());
      if (room == null) continue;
      String roomLabel = normalizeText(room.getBuilding(), "未标注楼栋")
          + "-" + normalizeText(room.getFloorNo(), "未标注楼层")
          + "-" + normalizeText(room.getRoomNo(), "未知房间");
      roomIncome.merge(roomLabel, safe(payment.getAmount()), BigDecimal::add);
    }
    return roomIncome.entrySet().stream()
        .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
        .limit(Math.max(top, 1))
        .map(entry -> {
          FinanceReportEntrySummaryResponse.NameAmountItem row = new FinanceReportEntrySummaryResponse.NameAmountItem();
          row.setLabel(entry.getKey());
          row.setAmount(entry.getValue());
          return row;
        })
        .toList();
  }

  private void applyReportWarning(FinanceReportEntrySummaryResponse response) {
    String key = normalizeText(response.getReportKey(), "OVERALL").toUpperCase(Locale.ROOT);
    if (response.getArrearsTotal().compareTo(BigDecimal.ZERO) > 0) {
      response.setWarningMessage("存在欠费风险，请核查催缴与自动扣费策略");
    }
    if ("REVENUE_STRUCTURE".equals(key) && response.getTopCategories().isEmpty()) {
      response.setWarningMessage("当前周期无有效营收结构数据，请检查账单科目映射");
      return;
    }
    if ("FLOOR_ROOM".equals(key) && response.getTopRooms().isEmpty()) {
      response.setWarningMessage("当前周期无房间收支明细，请检查床位绑定与账单关联");
      return;
    }
    if ("OCCUPANCY_CONSUMPTION".equals(key) && response.getTotalStoreSales().compareTo(BigDecimal.ZERO) <= 0) {
      response.setWarningMessage("入住消费统计偏低，请核查消费与商城流水入账");
      return;
    }
    if ("MONTHLY_OPS".equals(key)
        && response.getArrearsElderCount() > 0
        && response.getArrearsTotal().compareTo(safe(response.getNetRevenue()).multiply(BigDecimal.valueOf(0.3))) > 0) {
      response.setWarningMessage("欠费占比偏高，建议优先处理回款与续费");
    }
  }

  private List<StoreOrder> paidStoreOrders(Long orgId, LocalDateTime startTime, LocalDateTime endTime) {
    return storeOrderMapper.selectList(
            Wrappers.lambdaQuery(StoreOrder.class)
                .eq(StoreOrder::getIsDeleted, 0)
                .eq(orgId != null, StoreOrder::getOrgId, orgId)
                .eq(StoreOrder::getPayStatus, 1))
        .stream()
        .filter(item -> {
          LocalDateTime paidTime = effectiveOrderTime(item);
          return paidTime != null && !paidTime.isBefore(startTime) && paidTime.isBefore(endTime);
        })
        .toList();
  }

  private LocalDateTime effectiveOrderTime(StoreOrder order) {
    if (order == null) {
      return null;
    }
    return order.getPayTime() == null ? order.getCreateTime() : order.getPayTime();
  }

  private Map<Long, BillMonthly> loadPaymentBillMap(List<PaymentRecord> payments) {
    List<Long> billIds = payments.stream()
        .map(PaymentRecord::getBillMonthlyId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    if (billIds.isEmpty()) {
      return Map.of();
    }
    return billMonthlyMapper.selectBatchIds(billIds).stream()
        .filter(item -> item != null && !Integer.valueOf(1).equals(item.getIsDeleted()))
        .collect(Collectors.toMap(BillMonthly::getId, Function.identity(), (a, b) -> a));
  }

  private Map<Long, List<BillItem>> loadBillItemsByBillId(Long orgId, java.util.Set<Long> billIds) {
    if (billIds == null || billIds.isEmpty()) {
      return Map.of();
    }
    return billItemMapper.selectList(
            Wrappers.lambdaQuery(BillItem.class)
                .eq(BillItem::getIsDeleted, 0)
                .eq(orgId != null, BillItem::getOrgId, orgId)
                .in(BillItem::getBillMonthlyId, billIds))
        .stream()
        .collect(Collectors.groupingBy(BillItem::getBillMonthlyId));
  }

  private Map<BillItem, BigDecimal> allocatePaymentToItems(
      List<BillItem> items,
      BigDecimal paymentAmount,
      BigDecimal billTotalAmount) {
    if (items == null || items.isEmpty() || paymentAmount.compareTo(BigDecimal.ZERO) <= 0
        || billTotalAmount.compareTo(BigDecimal.ZERO) <= 0) {
      return Map.of();
    }
    Map<BillItem, BigDecimal> result = new LinkedHashMap<>();
    BigDecimal allocated = BigDecimal.ZERO;
    for (int i = 0; i < items.size(); i++) {
      BillItem item = items.get(i);
      BigDecimal amount;
      if (i == items.size() - 1) {
        amount = paymentAmount.subtract(allocated);
      } else {
        amount = paymentAmount.multiply(safe(item.getAmount()))
            .divide(billTotalAmount, 2, RoundingMode.HALF_UP);
        allocated = allocated.add(amount);
      }
      result.put(item, amount.max(BigDecimal.ZERO));
    }
    return result;
  }

  private BigDecimal sumPayments(List<PaymentRecord> payments) {
    return payments.stream()
        .map(PaymentRecord::getAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal sumRefundAmount(Long orgId, LocalDateTime startTime, LocalDateTime endTime) {
    return refundAmountByMonth(orgId, startTime, endTime).values().stream()
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private Map<String, BigDecimal> refundAmountByMonth(Long orgId, LocalDateTime startTime, LocalDateTime endTime) {
    Map<String, BigDecimal> result = new LinkedHashMap<>();
    financeRefundVoucherMapper.selectList(
            Wrappers.lambdaQuery(FinanceRefundVoucher.class)
                .eq(FinanceRefundVoucher::getIsDeleted, 0)
                .eq(orgId != null, FinanceRefundVoucher::getOrgId, orgId)
                .eq(FinanceRefundVoucher::getStatus, "PAID")
                .ge(FinanceRefundVoucher::getExecutedAt, startTime)
                .lt(FinanceRefundVoucher::getExecutedAt, endTime))
        .forEach(item -> {
          if (item.getExecutedAt() == null) {
            return;
          }
          String month = YearMonth.from(item.getExecutedAt()).toString();
          result.merge(month, safe(item.getAmount()), BigDecimal::add);
        });
    dischargeSettlementMapper.selectList(
            Wrappers.lambdaQuery(DischargeSettlement.class)
                .eq(DischargeSettlement::getIsDeleted, 0)
                .eq(orgId != null, DischargeSettlement::getOrgId, orgId)
                .eq(DischargeSettlement::getFinanceRefunded, 1)
                .isNull(DischargeSettlement::getRefundVoucherId)
                .ge(DischargeSettlement::getFinanceRefundTime, startTime)
                .lt(DischargeSettlement::getFinanceRefundTime, endTime))
        .forEach(item -> {
          if (item.getFinanceRefundTime() == null) {
            return;
          }
          String month = YearMonth.from(item.getFinanceRefundTime()).toString();
          result.merge(month, safe(item.getRefundAmount()), BigDecimal::add);
        });
    return result;
  }

  private String normalizeCategory(String itemType, String itemName) {
    String text = (normalizeText(itemType, "") + " " + normalizeText(itemName, "")).toLowerCase(Locale.ROOT);
    if (text.contains("bed") || text.contains("房") || text.contains("床")) return "ROOM";
    if (text.contains("nurs") || text.contains("护理")) return "NURSING";
    if (text.contains("med") || text.contains("医") || text.contains("药")) return "MEDICAL";
    if (text.contains("meal") || text.contains("餐")) return "DINING";
    if (text.contains("water") || text.contains("electric") || text.contains("utility")
        || text.contains("水") || text.contains("电") || text.contains("电视") || text.contains("网络")) return "UTILITY";
    if (text.contains("service") || text.contains("增购")) return "ADDON";
    return "OTHER";
  }

  private String categoryLabel(String category) {
    if ("ROOM".equals(category)) return "房费";
    if ("NURSING".equals(category)) return "护理";
    if ("MEDICAL".equals(category)) return "医护";
    if ("DINING".equals(category)) return "餐饮";
    if ("UTILITY".equals(category)) return "水电电视网络";
    if ("ADDON".equals(category)) return "增购服务";
    return "其他";
  }

  private BigDecimal safe(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private Map<Long, BigDecimal> buildProductCostMap(Long orgId, List<Long> productIds) {
    if (productIds == null || productIds.isEmpty()) {
      return Map.of();
    }
    List<InventoryBatch> batches = inventoryBatchMapper.selectList(
        Wrappers.lambdaQuery(InventoryBatch.class)
            .eq(InventoryBatch::getIsDeleted, 0)
            .eq(orgId != null, InventoryBatch::getOrgId, orgId)
            .in(InventoryBatch::getProductId, productIds)
            .isNotNull(InventoryBatch::getCostPrice)
            .orderByDesc(InventoryBatch::getUpdateTime)
            .orderByDesc(InventoryBatch::getCreateTime));
    Map<Long, BigDecimal> costMap = new HashMap<>();
    for (InventoryBatch batch : batches) {
      if (batch.getProductId() == null || costMap.containsKey(batch.getProductId())) {
        continue;
      }
      costMap.put(batch.getProductId(), safe(batch.getCostPrice()));
    }
    return costMap;
  }

  private BigDecimal ratio(BigDecimal value, BigDecimal total) {
    if (total == null || total.compareTo(BigDecimal.ZERO) <= 0 || value == null) {
      return BigDecimal.ZERO;
    }
    return value.divide(total, 4, RoundingMode.HALF_UP);
  }

  private String normalizeText(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return value.trim();
  }
}
