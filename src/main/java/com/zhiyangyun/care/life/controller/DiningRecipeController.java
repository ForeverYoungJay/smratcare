package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningRecipe;
import com.zhiyangyun.care.life.mapper.DiningRecipeMapper;
import com.zhiyangyun.care.life.model.DiningRecipeRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@RequestMapping("/api/life/dining/recipe")
public class DiningRecipeController {
  private final DiningRecipeMapper recipeMapper;

  public DiningRecipeController(DiningRecipeMapper recipeMapper) {
    this.recipeMapper = recipeMapper;
  }

  @GetMapping("/page")
  public Result<IPage<DiningRecipe>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String mealType,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(DiningRecipe.class)
        .eq(DiningRecipe::getIsDeleted, 0)
        .eq(orgId != null, DiningRecipe::getOrgId, orgId)
        .eq(mealType != null && !mealType.isBlank(), DiningRecipe::getMealType, mealType);
    if (dateFrom != null && dateTo != null) {
      wrapper.between(DiningRecipe::getPlanDate, dateFrom, dateTo);
    } else if (dateFrom != null) {
      wrapper.ge(DiningRecipe::getPlanDate, dateFrom);
    } else if (dateTo != null) {
      wrapper.le(DiningRecipe::getPlanDate, dateTo);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(DiningRecipe::getRecipeName, keyword)
          .or().like(DiningRecipe::getDishNames, keyword));
    }
    wrapper.orderByDesc(DiningRecipe::getPlanDate).orderByDesc(DiningRecipe::getUpdateTime);
    return Result.ok(recipeMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<DiningRecipe> create(@Valid @RequestBody DiningRecipeRequest request) {
    Long orgId = AuthContext.getOrgId();
    DiningRecipe recipe = new DiningRecipe();
    recipe.setTenantId(orgId);
    recipe.setOrgId(orgId);
    recipe.setRecipeName(request.getRecipeName());
    recipe.setMealType(request.getMealType());
    recipe.setDishIds(request.getDishIds());
    recipe.setDishNames(request.getDishNames());
    recipe.setPlanDate(request.getPlanDate());
    recipe.setSuitableCrowd(request.getSuitableCrowd());
    recipe.setStatus(request.getStatus() == null ? "ENABLED" : request.getStatus());
    recipe.setRemark(request.getRemark());
    recipe.setCreatedBy(AuthContext.getStaffId());
    recipeMapper.insert(recipe);
    return Result.ok(recipe);
  }

  @PutMapping("/{id}")
  public Result<DiningRecipe> update(@PathVariable Long id, @Valid @RequestBody DiningRecipeRequest request) {
    DiningRecipe recipe = recipeMapper.selectById(id);
    if (recipe == null || recipe.getIsDeleted() != null && recipe.getIsDeleted() == 1) {
      return Result.ok(null);
    }
    recipe.setRecipeName(request.getRecipeName());
    recipe.setMealType(request.getMealType());
    recipe.setDishIds(request.getDishIds());
    recipe.setDishNames(request.getDishNames());
    recipe.setPlanDate(request.getPlanDate());
    recipe.setSuitableCrowd(request.getSuitableCrowd());
    recipe.setStatus(request.getStatus() == null ? recipe.getStatus() : request.getStatus());
    recipe.setRemark(request.getRemark());
    recipeMapper.updateById(recipe);
    return Result.ok(recipe);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    DiningRecipe recipe = recipeMapper.selectById(id);
    if (recipe != null) {
      recipe.setIsDeleted(1);
      recipeMapper.updateById(recipe);
    }
    return Result.ok(null);
  }
}
