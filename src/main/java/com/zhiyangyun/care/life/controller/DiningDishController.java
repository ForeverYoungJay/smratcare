package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningDish;
import com.zhiyangyun.care.life.mapper.DiningDishMapper;
import com.zhiyangyun.care.life.model.DiningDishRequest;
import jakarta.validation.Valid;
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
    DiningDish dish = new DiningDish();
    dish.setTenantId(orgId);
    dish.setOrgId(orgId);
    dish.setDishName(request.getDishName());
    dish.setDishCategory(request.getDishCategory());
    dish.setMealType(request.getMealType());
    dish.setUnitPrice(request.getUnitPrice());
    dish.setCalories(request.getCalories());
    dish.setNutritionInfo(request.getNutritionInfo());
    dish.setAllergenTags(request.getAllergenTags());
    dish.setTagIds(request.getTagIds());
    dish.setStatus(request.getStatus() == null ? "ENABLED" : request.getStatus());
    dish.setRemark(request.getRemark());
    dish.setCreatedBy(AuthContext.getStaffId());
    dishMapper.insert(dish);
    return Result.ok(dish);
  }

  @PutMapping("/{id}")
  public Result<DiningDish> update(@PathVariable Long id, @Valid @RequestBody DiningDishRequest request) {
    DiningDish dish = dishMapper.selectById(id);
    if (dish == null || dish.getIsDeleted() != null && dish.getIsDeleted() == 1) {
      return Result.ok(null);
    }
    dish.setDishName(request.getDishName());
    dish.setDishCategory(request.getDishCategory());
    dish.setMealType(request.getMealType());
    dish.setUnitPrice(request.getUnitPrice());
    dish.setCalories(request.getCalories());
    dish.setNutritionInfo(request.getNutritionInfo());
    dish.setAllergenTags(request.getAllergenTags());
    dish.setTagIds(request.getTagIds());
    dish.setStatus(request.getStatus() == null ? dish.getStatus() : request.getStatus());
    dish.setRemark(request.getRemark());
    dishMapper.updateById(dish);
    return Result.ok(dish);
  }

  @PutMapping("/{id}/status")
  public Result<DiningDish> changeStatus(@PathVariable Long id, @RequestParam String status) {
    DiningDish dish = dishMapper.selectById(id);
    if (dish == null || dish.getIsDeleted() != null && dish.getIsDeleted() == 1) {
      return Result.ok(null);
    }
    dish.setStatus(status);
    dishMapper.updateById(dish);
    return Result.ok(dish);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    DiningDish dish = dishMapper.selectById(id);
    if (dish != null) {
      dish.setIsDeleted(1);
      dishMapper.updateById(dish);
    }
    return Result.ok(null);
  }
}
