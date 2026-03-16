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

  public FinanceReportController(
      BillMonthlyMapper billMonthlyMapper,
      BillItemMapper billItemMapper,
      StoreOrderMapper storeOrderMapper,
      ElderMapper elderMapper,
      BedMapper bedMapper,
      RoomMapper roomMapper,
      OrderItemMapper orderItemMapper,
      ProductMapper productMapper,
      InventoryBatchMapper inventoryBatchMapper) {
    this.billMonthlyMapper = billMonthlyMapper;
    this.billItemMapper = billItemMapper;
    this.storeOrderMapper = storeOrderMapper;
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.roomMapper = roomMapper;
    this.orderItemMapper = orderItemMapper;
    this.productMapper = productMapper;
    this.inventoryBatchMapper = inventoryBatchMapper;
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
    List<StoreOrder> orders = storeOrderMapper.selectList(
        Wrappers.lambdaQuery(StoreOrder.class)
            .eq(StoreOrder::getIsDeleted, 0)
            .eq(orgId != null, StoreOrder::getOrgId, orgId)
            .ge(StoreOrder::getCreateTime, startTime)
            .lt(StoreOrder::getCreateTime, endTime));
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
    response.setTotalRevenue(sumBillAmount(bills, false));
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

    List<Long> billIds = bills.stream().map(BillMonthly::getId).filter(Objects::nonNull).toList();
    if (!billIds.isEmpty()) {
      List<BillItem> items = billItemMapper.selectList(
          Wrappers.lambdaQuery(BillItem.class)
              .eq(BillItem::getIsDeleted, 0)
              .eq(orgId != null, BillItem::getOrgId, orgId)
              .in(BillItem::getBillMonthlyId, billIds));
      response.setTopCategories(topCategories(items, top));
    }
    response.setTopRooms(topRooms(bills, elderBedMap, roomMap, top));
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

    List<StoreOrder> orders = storeOrderMapper.selectList(
        Wrappers.lambdaQuery(StoreOrder.class)
            .eq(StoreOrder::getIsDeleted, 0)
            .eq(StoreOrder::getPayStatus, 1)
            .eq(orgId != null, StoreOrder::getOrgId, orgId)
            .ge(StoreOrder::getCreateTime, startTime)
            .lt(StoreOrder::getCreateTime, endTime));
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
      if (order == null || order.getCreateTime() == null) {
        continue;
      }
      LocalDate day = order.getCreateTime().toLocalDate();
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

  private List<FinanceReportEntrySummaryResponse.NameAmountItem> topCategories(List<BillItem> items, int top) {
    Map<String, BigDecimal> sumMap = new HashMap<>();
    for (BillItem item : items) {
      String key = normalizeCategory(item.getItemType(), item.getItemName());
      sumMap.merge(key, safe(item.getAmount()), BigDecimal::add);
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
      List<BillMonthly> bills,
      Map<Long, Bed> elderBedMap,
      Map<Long, Room> roomMap,
      int top) {
    Map<String, BigDecimal> roomIncome = new HashMap<>();
    for (BillMonthly bill : bills) {
      if (bill.getElderId() == null) continue;
      Bed bed = elderBedMap.get(bill.getElderId());
      if (bed == null || bed.getRoomId() == null) continue;
      Room room = roomMap.get(bed.getRoomId());
      if (room == null) continue;
      String roomLabel = normalizeText(room.getBuilding(), "未标注楼栋")
          + "-" + normalizeText(room.getFloorNo(), "未标注楼层")
          + "-" + normalizeText(room.getRoomNo(), "未知房间");
      roomIncome.merge(roomLabel, safe(bill.getTotalAmount()), BigDecimal::add);
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
        && response.getArrearsTotal().compareTo(response.getTotalRevenue().multiply(BigDecimal.valueOf(0.3))) > 0) {
      response.setWarningMessage("欠费占比偏高，建议优先处理回款与续费");
    }
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
