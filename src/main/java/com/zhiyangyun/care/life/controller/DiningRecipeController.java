package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningRecipe;
import com.zhiyangyun.care.life.mapper.DiningRecipeMapper;
import com.zhiyangyun.care.life.model.DiningConstants;
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
      @RequestParam(required = false) String status,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo) {
    if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
      throw new IllegalArgumentException(DiningConstants.MSG_INVALID_DATE_RANGE);
    }
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(DiningRecipe.class)
        .eq(DiningRecipe::getIsDeleted, 0)
        .eq(orgId != null, DiningRecipe::getOrgId, orgId)
        .eq(mealType != null && !mealType.isBlank(), DiningRecipe::getMealType, mealType)
        .eq(status != null && !status.isBlank(), DiningRecipe::getStatus, status);
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
    recipe.setRecipeName(request.getRecipeName().trim());
    recipe.setMealType(normalizeText(request.getMealType()));
    recipe.setDishIds(normalizeText(request.getDishIds()));
    recipe.setDishNames(request.getDishNames().trim());
    recipe.setPlanDate(request.getPlanDate());
    recipe.setSuitableCrowd(normalizeText(request.getSuitableCrowd()));
    String status = request.getStatus() == null ? DiningConstants.STATUS_ENABLED : request.getStatus();
    validateStatus(status);
    recipe.setStatus(status);
    recipe.setRemark(normalizeText(request.getRemark()));
    recipe.setCreatedBy(AuthContext.getStaffId());
    recipeMapper.insert(recipe);
    return Result.ok(recipe);
  }

  @PutMapping("/{id}")
  public Result<DiningRecipe> update(@PathVariable Long id, @Valid @RequestBody DiningRecipeRequest request) {
    DiningRecipe recipe = getRecipeInOrg(id, AuthContext.getOrgId());
    if (recipe == null) {
      return Result.ok(null);
    }
    recipe.setRecipeName(request.getRecipeName().trim());
    recipe.setMealType(normalizeText(request.getMealType()));
    recipe.setDishIds(normalizeText(request.getDishIds()));
    recipe.setDishNames(request.getDishNames().trim());
    recipe.setPlanDate(request.getPlanDate());
    recipe.setSuitableCrowd(normalizeText(request.getSuitableCrowd()));
    if (request.getStatus() != null) {
      validateStatus(request.getStatus());
      recipe.setStatus(request.getStatus());
    }
    recipe.setRemark(normalizeText(request.getRemark()));
    recipeMapper.updateById(recipe);
    return Result.ok(recipe);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    DiningRecipe recipe = getRecipeInOrg(id, AuthContext.getOrgId());
    if (recipe != null) {
      recipe.setIsDeleted(1);
      recipeMapper.updateById(recipe);
    }
    return Result.ok(null);
  }

  private DiningRecipe getRecipeInOrg(Long id, Long orgId) {
    return recipeMapper.selectOne(Wrappers.lambdaQuery(DiningRecipe.class)
        .eq(DiningRecipe::getId, id)
        .eq(DiningRecipe::getIsDeleted, 0)
        .eq(orgId != null, DiningRecipe::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private void validateStatus(String status) {
    if (status == null || !DiningConstants.ENABLE_DISABLE_STATUS_SET.contains(status)) {
      throw new IllegalArgumentException(DiningConstants.MSG_INVALID_ENABLE_STATUS);
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
