package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningDish;
import com.zhiyangyun.care.life.entity.DiningMealOrder;
import com.zhiyangyun.care.life.entity.DiningRecipe;
import com.zhiyangyun.care.life.mapper.DiningDishMapper;
import com.zhiyangyun.care.life.mapper.DiningMealOrderMapper;
import com.zhiyangyun.care.life.mapper.DiningRecipeMapper;
import com.zhiyangyun.care.life.model.DiningConstants;
import com.zhiyangyun.care.life.model.DiningDishAnalyticsItem;
import com.zhiyangyun.care.life.model.DiningDishAnalyticsResponse;
import com.zhiyangyun.care.life.model.DiningDishRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/life/dining/dish")
public class DiningDishController {
  private final DiningDishMapper dishMapper;
  private final DiningRecipeMapper recipeMapper;
  private final DiningMealOrderMapper mealOrderMapper;

  public DiningDishController(
      DiningDishMapper dishMapper,
      DiningRecipeMapper recipeMapper,
      DiningMealOrderMapper mealOrderMapper) {
    this.dishMapper = dishMapper;
    this.recipeMapper = recipeMapper;
    this.mealOrderMapper = mealOrderMapper;
  }

  @GetMapping("/page")
  public Result<IPage<DiningDish>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String mealType,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(DiningDish.class)
        .eq(DiningDish::getIsDeleted, 0)
        .eq(orgId != null, DiningDish::getOrgId, orgId)
        .eq(mealType != null && !mealType.isBlank(), DiningDish::getMealType, mealType)
        .eq(status != null && !status.isBlank(), DiningDish::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(DiningDish::getDishName, keyword)
          .or().like(DiningDish::getDishCategory, keyword));
    }
    wrapper.orderByDesc(DiningDish::getUpdateTime);
    return Result.ok(dishMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/list")
  public Result<List<DiningDish>> list(
      @RequestParam(required = false) String mealType,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(dishMapper.selectList(Wrappers.lambdaQuery(DiningDish.class)
        .eq(DiningDish::getIsDeleted, 0)
        .eq(orgId != null, DiningDish::getOrgId, orgId)
        .eq(mealType != null && !mealType.isBlank(), DiningDish::getMealType, mealType)
        .eq(status != null && !status.isBlank(), DiningDish::getStatus, status)
        .orderByAsc(DiningDish::getDishName)));
  }

  @GetMapping("/analytics")
  public Result<DiningDishAnalyticsResponse> analytics(@RequestParam(required = false) LocalDate month) {
    LocalDate monthDate = month == null ? LocalDate.now().withDayOfMonth(1) : month.withDayOfMonth(1);
    LocalDate monthEnd = monthDate.withDayOfMonth(monthDate.lengthOfMonth());
    Long orgId = AuthContext.getOrgId();

    List<DiningDish> dishes = dishMapper.selectList(Wrappers.lambdaQuery(DiningDish.class)
        .eq(DiningDish::getIsDeleted, 0)
        .eq(orgId != null, DiningDish::getOrgId, orgId)
        .orderByAsc(DiningDish::getDishCategory)
        .orderByAsc(DiningDish::getMealType)
        .orderByAsc(DiningDish::getDishName));
    Map<Long, Long> recipeCounter = new HashMap<>();
    for (DiningRecipe recipe : recipeMapper.selectList(Wrappers.lambdaQuery(DiningRecipe.class)
        .eq(DiningRecipe::getIsDeleted, 0)
        .eq(orgId != null, DiningRecipe::getOrgId, orgId)
        .between(DiningRecipe::getPlanDate, monthDate, monthEnd))) {
      for (Long dishId : parseDishIds(recipe.getDishIds())) {
        recipeCounter.put(dishId, recipeCounter.getOrDefault(dishId, 0L) + 1L);
      }
    }

    Map<Long, Long> orderCounter = new HashMap<>();
    for (DiningMealOrder order : mealOrderMapper.selectList(Wrappers.lambdaQuery(DiningMealOrder.class)
        .eq(DiningMealOrder::getIsDeleted, 0)
        .eq(orgId != null, DiningMealOrder::getOrgId, orgId)
        .between(DiningMealOrder::getOrderDate, monthDate, monthEnd))) {
      for (Long dishId : parseDishIds(order.getDishIds())) {
        orderCounter.put(dishId, orderCounter.getOrDefault(dishId, 0L) + 1L);
      }
    }

    List<DiningDishAnalyticsItem> items = dishes.stream()
        .map(dish -> toAnalyticsItem(dish, recipeCounter.getOrDefault(dish.getId(), 0L), orderCounter.getOrDefault(dish.getId(), 0L)))
        .filter(Objects::nonNull)
        .sorted(Comparator.comparing(DiningDishAnalyticsItem::getDishCategory, Comparator.nullsLast(String::compareTo))
            .thenComparing(DiningDishAnalyticsItem::getMealType, Comparator.nullsLast(String::compareTo))
            .thenComparing(DiningDishAnalyticsItem::getDishName, Comparator.nullsLast(String::compareTo)))
        .toList();

    DiningDishAnalyticsResponse response = new DiningDishAnalyticsResponse();
    response.setMonth(monthDate.toString());
    response.setItems(items);
    response.setTotalDishCount(items.size());
    response.setTotalRecipeCount(items.stream().mapToLong(DiningDishAnalyticsItem::getMonthlyRecipeCount).sum());
    response.setTotalPlannedPurchaseQty(items.stream()
        .map(DiningDishAnalyticsItem::getMonthlyPlannedPurchaseQty)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add));
    response.setTotalEstimatedCost(items.stream()
        .map(DiningDishAnalyticsItem::getMonthlyEstimatedCost)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add));
    return Result.ok(response);
  }

  @PostMapping
  public Result<DiningDish> create(@Valid @RequestBody DiningDishRequest request) {
    Long orgId = AuthContext.getOrgId();
    validateUnitPrice(request.getUnitPrice());
    DiningDish dish = new DiningDish();
    dish.setTenantId(orgId);
    dish.setOrgId(orgId);
    dish.setDishName(request.getDishName().trim());
    dish.setDishCategory(normalizeText(request.getDishCategory()));
    dish.setMealType(normalizeText(request.getMealType()));
    dish.setUnitPrice(request.getUnitPrice());
    dish.setCurrentDiningCount(normalizeDiningCount(request.getCurrentDiningCount()));
    dish.setPurchaseQty(normalizePurchaseQty(request.getPurchaseQty()));
    dish.setPurchaseUnit(normalizePurchaseUnit(request.getPurchaseUnit()));
    dish.setCalories(request.getCalories());
    dish.setNutritionInfo(normalizeText(request.getNutritionInfo()));
    dish.setAllergenTags(normalizeText(request.getAllergenTags()));
    dish.setTagIds(normalizeText(request.getTagIds()));
    String status = request.getStatus() == null ? DiningConstants.STATUS_ENABLED : request.getStatus();
    validateStatus(status);
    dish.setStatus(status);
    dish.setRemark(normalizeText(request.getRemark()));
    dish.setCreatedBy(AuthContext.getStaffId());
    dishMapper.insert(dish);
    return Result.ok(dish);
  }

  @PutMapping("/{id}")
  public Result<DiningDish> update(@PathVariable Long id, @Valid @RequestBody DiningDishRequest request) {
    DiningDish dish = getDishInOrg(id, AuthContext.getOrgId());
    if (dish == null) {
      return Result.ok(null);
    }
    validateUnitPrice(request.getUnitPrice());
    dish.setDishName(request.getDishName().trim());
    dish.setDishCategory(normalizeText(request.getDishCategory()));
    dish.setMealType(normalizeText(request.getMealType()));
    dish.setUnitPrice(request.getUnitPrice());
    dish.setCurrentDiningCount(normalizeDiningCount(request.getCurrentDiningCount()));
    dish.setPurchaseQty(normalizePurchaseQty(request.getPurchaseQty()));
    dish.setPurchaseUnit(normalizePurchaseUnit(request.getPurchaseUnit()));
    dish.setCalories(request.getCalories());
    dish.setNutritionInfo(normalizeText(request.getNutritionInfo()));
    dish.setAllergenTags(normalizeText(request.getAllergenTags()));
    dish.setTagIds(normalizeText(request.getTagIds()));
    if (request.getStatus() != null) {
      validateStatus(request.getStatus());
      dish.setStatus(request.getStatus());
    }
    dish.setRemark(normalizeText(request.getRemark()));
    dishMapper.updateById(dish);
    return Result.ok(dish);
  }

  @PutMapping("/{id}/status")
  public Result<DiningDish> changeStatus(@PathVariable Long id, @RequestParam String status) {
    DiningDish dish = getDishInOrg(id, AuthContext.getOrgId());
    if (dish == null) {
      return Result.ok(null);
    }
    validateStatus(status);
    dish.setStatus(status);
    dishMapper.updateById(dish);
    return Result.ok(dish);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    DiningDish dish = getDishInOrg(id, AuthContext.getOrgId());
    if (dish != null) {
      dish.setIsDeleted(1);
      dishMapper.updateById(dish);
    }
    return Result.ok(null);
  }

  private DiningDish getDishInOrg(Long id, Long orgId) {
    return dishMapper.selectOne(Wrappers.lambdaQuery(DiningDish.class)
        .eq(DiningDish::getId, id)
        .eq(DiningDish::getIsDeleted, 0)
        .eq(orgId != null, DiningDish::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private void validateStatus(String status) {
    if (status == null || !DiningConstants.ENABLE_DISABLE_STATUS_SET.contains(status)) {
      throw new IllegalArgumentException(DiningConstants.MSG_INVALID_ENABLE_STATUS);
    }
  }

  private void validateUnitPrice(BigDecimal unitPrice) {
    if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("单价不能小于0");
    }
  }

  private Integer normalizeDiningCount(Integer currentDiningCount) {
    if (currentDiningCount == null || currentDiningCount < 0) {
      return 0;
    }
    return currentDiningCount;
  }

  private BigDecimal normalizePurchaseQty(BigDecimal purchaseQty) {
    if (purchaseQty == null || purchaseQty.compareTo(BigDecimal.ZERO) < 0) {
      return BigDecimal.ZERO;
    }
    return purchaseQty;
  }

  private String normalizePurchaseUnit(String purchaseUnit) {
    String normalized = normalizeText(purchaseUnit);
    return normalized == null ? "斤" : normalized;
  }

  private DiningDishAnalyticsItem toAnalyticsItem(DiningDish dish, long recipeCount, long orderCount) {
    if (dish == null) {
      return null;
    }
    DiningDishAnalyticsItem item = new DiningDishAnalyticsItem();
    item.setId(dish.getId());
    item.setDishName(dish.getDishName());
    item.setDishCategory(dish.getDishCategory());
    item.setMealType(dish.getMealType());
    item.setUnitPrice(dish.getUnitPrice());
    item.setCurrentDiningCount(normalizeDiningCount(dish.getCurrentDiningCount()));
    item.setPurchaseQty(normalizePurchaseQty(dish.getPurchaseQty()));
    item.setPurchaseUnit(normalizePurchaseUnit(dish.getPurchaseUnit()));
    item.setMonthlyRecipeCount(recipeCount);
    item.setMonthlyOrderCount(orderCount);
    item.setMonthlyPlannedPurchaseQty(normalizePurchaseQty(dish.getPurchaseQty()).multiply(BigDecimal.valueOf(recipeCount)));
    item.setMonthlyEstimatedCost(defaultDecimal(dish.getUnitPrice()).multiply(normalizePurchaseQty(dish.getPurchaseQty())).multiply(BigDecimal.valueOf(recipeCount)));
    return item;
  }

  private BigDecimal defaultDecimal(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
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

  private String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
