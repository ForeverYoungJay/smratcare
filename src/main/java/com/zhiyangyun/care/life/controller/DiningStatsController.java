package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningDeliveryArea;
import com.zhiyangyun.care.life.entity.DiningDeliveryRecord;
import com.zhiyangyun.care.life.entity.DiningDish;
import com.zhiyangyun.care.life.entity.DiningMealOrder;
import com.zhiyangyun.care.life.entity.DiningRecipe;
import com.zhiyangyun.care.life.mapper.DiningDeliveryAreaMapper;
import com.zhiyangyun.care.life.mapper.DiningDeliveryRecordMapper;
import com.zhiyangyun.care.life.mapper.DiningDishMapper;
import com.zhiyangyun.care.life.mapper.DiningMealOrderMapper;
import com.zhiyangyun.care.life.mapper.DiningRecipeMapper;
import com.zhiyangyun.care.life.model.DiningConstants;
import com.zhiyangyun.care.life.model.DiningStatsBuildingItem;
import com.zhiyangyun.care.life.model.DiningStatsMealTypeItem;
import com.zhiyangyun.care.life.model.DiningStatsProcurementItem;
import com.zhiyangyun.care.life.model.DiningStatsSummaryResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/life/dining/stats")
public class DiningStatsController {
  private final DiningMealOrderMapper mealOrderMapper;
  private final DiningDeliveryAreaMapper deliveryAreaMapper;
  private final DiningDeliveryRecordMapper deliveryRecordMapper;
  private final DiningDishMapper dishMapper;
  private final DiningRecipeMapper recipeMapper;

  public DiningStatsController(
      DiningMealOrderMapper mealOrderMapper,
      DiningDeliveryAreaMapper deliveryAreaMapper,
      DiningDeliveryRecordMapper deliveryRecordMapper,
      DiningDishMapper dishMapper,
      DiningRecipeMapper recipeMapper) {
    this.mealOrderMapper = mealOrderMapper;
    this.deliveryAreaMapper = deliveryAreaMapper;
    this.deliveryRecordMapper = deliveryRecordMapper;
    this.dishMapper = dishMapper;
    this.recipeMapper = recipeMapper;
  }

  @GetMapping("/summary")
  public Result<DiningStatsSummaryResponse> summary(
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo) {
    LocalDate toDate = dateTo == null ? LocalDate.now() : dateTo;
    LocalDate fromDate = dateFrom == null ? toDate.minusDays(6) : dateFrom;
    Long orgId = AuthContext.getOrgId();

    List<DiningMealOrder> orders = mealOrderMapper.selectList(Wrappers.lambdaQuery(DiningMealOrder.class)
        .eq(DiningMealOrder::getIsDeleted, 0)
        .eq(orgId != null, DiningMealOrder::getOrgId, orgId)
        .between(DiningMealOrder::getOrderDate, fromDate, toDate));

    Map<Long, DiningDeliveryArea> deliveryAreaMap = deliveryAreaMapper.selectList(Wrappers.lambdaQuery(DiningDeliveryArea.class)
            .eq(DiningDeliveryArea::getIsDeleted, 0)
            .eq(orgId != null, DiningDeliveryArea::getOrgId, orgId))
        .stream()
        .collect(Collectors.toMap(DiningDeliveryArea::getId, item -> item, (left, right) -> left));

    Map<String, Long> mealTypeCounter = new HashMap<>();
    Map<String, DiningStatsBuildingItem> buildingCounter = new LinkedHashMap<>();
    BigDecimal totalAmount = BigDecimal.ZERO;
    long deliveredOrders = 0L;
    List<Long> orderIds = new ArrayList<>();
    for (DiningMealOrder order : orders) {
      orderIds.add(order.getId());
      if (order.getTotalAmount() != null) {
        totalAmount = totalAmount.add(order.getTotalAmount());
      }
      if (DiningConstants.ORDER_STATUS_DELIVERED.equals(order.getStatus())) {
        deliveredOrders++;
      }
      String mealType = order.getMealType() == null ? DiningConstants.MEAL_TYPE_UNKNOWN : order.getMealType();
      mealTypeCounter.put(mealType, mealTypeCounter.getOrDefault(mealType, 0L) + 1L);

      String buildingName = resolveBuildingName(order, deliveryAreaMap);
      DiningStatsBuildingItem buildingItem = buildingCounter.computeIfAbsent(buildingName, key -> {
        DiningStatsBuildingItem item = new DiningStatsBuildingItem();
        item.setBuildingName(key);
        item.setOrderCount(0L);
        item.setTotalAmount(BigDecimal.ZERO);
        return item;
      });
      buildingItem.setOrderCount(buildingItem.getOrderCount() + 1L);
      buildingItem.setTotalAmount(buildingItem.getTotalAmount().add(defaultDecimal(order.getTotalAmount())));
    }

    long totalOrders = orders.size();
    BigDecimal deliveryRate = percentage(deliveredOrders, totalOrders);

    Map<Long, DiningMealOrder> orderMap = orders.stream()
        .collect(Collectors.toMap(DiningMealOrder::getId, item -> item, (left, right) -> left));
    List<DiningDeliveryRecord> deliveryRecords = orderIds.isEmpty()
        ? List.of()
        : deliveryRecordMapper.selectList(Wrappers.lambdaQuery(DiningDeliveryRecord.class)
            .eq(DiningDeliveryRecord::getIsDeleted, 0)
            .eq(orgId != null, DiningDeliveryRecord::getOrgId, orgId)
            .in(DiningDeliveryRecord::getMealOrderId, orderIds));
    long trackedDeliveryOrders = 0L;
    long onTimeDeliveredOrders = 0L;
    for (DiningDeliveryRecord record : deliveryRecords) {
      DiningMealOrder order = orderMap.get(record.getMealOrderId());
      if (order == null || order.getExpectedDeliveryTime() == null) {
        continue;
      }
      if (!DiningConstants.DELIVERY_STATUS_DELIVERED.equals(record.getStatus()) || record.getDeliveredAt() == null) {
        continue;
      }
      trackedDeliveryOrders++;
      if (!resolveSignedTime(record).isAfter(order.getExpectedDeliveryTime())) {
        onTimeDeliveredOrders++;
      }
    }

    List<DiningStatsMealTypeItem> mealTypeStats = mealTypeCounter.entrySet().stream()
        .map(entry -> new DiningStatsMealTypeItem(entry.getKey(), entry.getValue()))
        .sorted(Comparator.comparingLong(DiningStatsMealTypeItem::getOrderCount).reversed())
        .toList();

    List<DiningStatsBuildingItem> buildingStats = buildingCounter.values().stream()
        .sorted(Comparator.comparing(DiningStatsBuildingItem::getTotalAmount, Comparator.nullsFirst(BigDecimal::compareTo)).reversed())
        .toList();

    List<DiningStatsProcurementItem> procurementItems = buildNextMonthProcurementItems(orgId);
    BigDecimal nextMonthEstimatedCost = procurementItems.stream()
        .map(DiningStatsProcurementItem::getEstimatedCost)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    DiningStatsSummaryResponse response = new DiningStatsSummaryResponse();
    response.setTotalOrders(totalOrders);
    response.setTotalAmount(totalAmount);
    response.setDeliveredOrders(deliveredOrders);
    response.setDeliveryRate(deliveryRate);
    response.setOnTimeDeliveredOrders(onTimeDeliveredOrders);
    response.setTrackedDeliveryOrders(trackedDeliveryOrders);
    response.setOnTimeDeliveryRate(percentage(onTimeDeliveredOrders, trackedDeliveryOrders));
    response.setMealTypeStats(mealTypeStats);
    response.setBuildingStats(buildingStats);
    response.setProcurementItems(procurementItems);
    response.setNextMonthEstimatedCost(nextMonthEstimatedCost);
    return Result.ok(response);
  }

  private List<DiningStatsProcurementItem> buildNextMonthProcurementItems(Long orgId) {
    LocalDate nextMonthStart = LocalDate.now().plusMonths(1).withDayOfMonth(1);
    LocalDate nextMonthEnd = nextMonthStart.withDayOfMonth(nextMonthStart.lengthOfMonth());
    List<DiningDish> dishes = dishMapper.selectList(Wrappers.lambdaQuery(DiningDish.class)
        .eq(DiningDish::getIsDeleted, 0)
        .eq(orgId != null, DiningDish::getOrgId, orgId));
    Map<Long, DiningDish> dishMap = dishes.stream()
        .collect(Collectors.toMap(DiningDish::getId, item -> item, (left, right) -> left));
    Map<Long, Integer> recipeCounter = new HashMap<>();

    List<DiningRecipe> recipes = recipeMapper.selectList(Wrappers.lambdaQuery(DiningRecipe.class)
        .eq(DiningRecipe::getIsDeleted, 0)
        .eq(orgId != null, DiningRecipe::getOrgId, orgId)
        .between(DiningRecipe::getPlanDate, nextMonthStart, nextMonthEnd));
    for (DiningRecipe recipe : recipes) {
      for (Long dishId : parseDishIds(recipe.getDishIds())) {
        recipeCounter.put(dishId, recipeCounter.getOrDefault(dishId, 0) + 1);
      }
    }

    return recipeCounter.entrySet().stream()
        .map(entry -> toProcurementItem(dishMap.get(entry.getKey()), entry.getValue()))
        .filter(Objects::nonNull)
        .sorted(Comparator.comparing(DiningStatsProcurementItem::getEstimatedCost, Comparator.nullsFirst(BigDecimal::compareTo)).reversed())
        .toList();
  }

  private DiningStatsProcurementItem toProcurementItem(DiningDish dish, int recipeCount) {
    if (dish == null) {
      return null;
    }
    DiningStatsProcurementItem item = new DiningStatsProcurementItem();
    item.setDishId(dish.getId());
    item.setDishName(dish.getDishName());
    item.setDishCategory(dish.getDishCategory());
    item.setMealType(dish.getMealType());
    item.setRecipeCount(recipeCount);
    item.setCurrentDiningCount(dish.getCurrentDiningCount());
    item.setPurchaseQty(defaultDecimal(dish.getPurchaseQty()));
    item.setPurchaseUnit(dish.getPurchaseUnit() == null ? "斤" : dish.getPurchaseUnit());
    item.setTotalPlannedQty(defaultDecimal(dish.getPurchaseQty()).multiply(BigDecimal.valueOf(recipeCount)));
    item.setEstimatedCost(defaultDecimal(dish.getUnitPrice()).multiply(defaultDecimal(dish.getPurchaseQty())).multiply(BigDecimal.valueOf(recipeCount)));
    return item;
  }

  private List<Long> parseDishIds(String dishIds) {
    if (dishIds == null || dishIds.isBlank()) {
      return List.of();
    }
    List<Long> ids = new ArrayList<>();
    for (String token : dishIds.split(",")) {
      String trimmed = token.trim();
      if (trimmed.isEmpty()) {
        continue;
      }
      try {
        ids.add(Long.parseLong(trimmed));
      } catch (NumberFormatException ignored) {
        // ignore invalid snapshot token
      }
    }
    return ids;
  }

  private String resolveBuildingName(DiningMealOrder order, Map<Long, DiningDeliveryArea> deliveryAreaMap) {
    if (order.getDeliveryAreaId() != null) {
      DiningDeliveryArea area = deliveryAreaMap.get(order.getDeliveryAreaId());
      if (area != null && area.getBuildingName() != null && !area.getBuildingName().isBlank()) {
        return area.getBuildingName().trim();
      }
    }
    return "未分配楼栋";
  }

  private BigDecimal percentage(long numerator, long denominator) {
    if (denominator <= 0) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(numerator)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
  }

  private BigDecimal defaultDecimal(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private java.time.LocalDateTime resolveSignedTime(DiningDeliveryRecord record) {
    if (record.getSignedAt() != null) {
      return record.getSignedAt();
    }
    if (record.getQrScanAt() != null) {
      return record.getQrScanAt();
    }
    return record.getDeliveredAt();
  }
}
