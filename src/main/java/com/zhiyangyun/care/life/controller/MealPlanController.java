package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.MealPlan;
import com.zhiyangyun.care.life.mapper.MealPlanMapper;
import com.zhiyangyun.care.life.model.MealPlanRequest;
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
@RequestMapping("/api/life/meal-plan")
public class MealPlanController {
  private final MealPlanMapper mealPlanMapper;

  public MealPlanController(MealPlanMapper mealPlanMapper) {
    this.mealPlanMapper = mealPlanMapper;
  }

  @GetMapping("/page")
  public Result<IPage<MealPlan>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(MealPlan.class)
        .eq(MealPlan::getIsDeleted, 0)
        .eq(orgId != null, MealPlan::getOrgId, orgId);
    if (dateFrom != null && dateTo != null) {
      wrapper.between(MealPlan::getPlanDate, dateFrom, dateTo);
    } else if (dateFrom != null) {
      wrapper.ge(MealPlan::getPlanDate, dateFrom);
    } else if (dateTo != null) {
      wrapper.le(MealPlan::getPlanDate, dateTo);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(MealPlan::getMenu, keyword)
          .or().like(MealPlan::getMealType, keyword));
    }
    wrapper.orderByDesc(MealPlan::getPlanDate);
    return Result.ok(mealPlanMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<MealPlan> create(@Valid @RequestBody MealPlanRequest request) {
    Long orgId = AuthContext.getOrgId();
    MealPlan plan = new MealPlan();
    plan.setTenantId(orgId);
    plan.setOrgId(orgId);
    plan.setPlanDate(request.getPlanDate());
    plan.setMealType(request.getMealType());
    plan.setMenu(request.getMenu());
    plan.setCalories(request.getCalories());
    plan.setRemark(request.getRemark());
    plan.setCreatedBy(AuthContext.getStaffId());
    mealPlanMapper.insert(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}")
  public Result<MealPlan> update(@PathVariable Long id, @Valid @RequestBody MealPlanRequest request) {
    MealPlan existing = mealPlanMapper.selectById(id);
    if (existing == null) {
      return Result.ok(null);
    }
    existing.setPlanDate(request.getPlanDate());
    existing.setMealType(request.getMealType());
    existing.setMenu(request.getMenu());
    existing.setCalories(request.getCalories());
    existing.setRemark(request.getRemark());
    mealPlanMapper.updateById(existing);
    return Result.ok(existing);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    MealPlan existing = mealPlanMapper.selectById(id);
    if (existing != null) {
      existing.setIsDeleted(1);
      mealPlanMapper.updateById(existing);
    }
    return Result.ok(null);
  }
}
