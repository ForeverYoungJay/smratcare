package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningDish;
import com.zhiyangyun.care.life.mapper.DiningDishMapper;
import com.zhiyangyun.care.life.model.DiningConstants;
import com.zhiyangyun.care.life.model.DiningDishRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
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

  public DiningDishController(DiningDishMapper dishMapper) {
    this.dishMapper = dishMapper;
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

  private String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
