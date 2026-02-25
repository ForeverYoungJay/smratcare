package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaActivityPlan;
import com.zhiyangyun.care.oa.mapper.OaActivityPlanMapper;
import com.zhiyangyun.care.oa.model.OaActivityPlanRequest;
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
@RequestMapping("/api/oa/activity-plan")
public class OaActivityPlanController {
  private final OaActivityPlanMapper activityPlanMapper;

  public OaActivityPlanController(OaActivityPlanMapper activityPlanMapper) {
    this.activityPlanMapper = activityPlanMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaActivityPlan>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaActivityPlan.class)
        .eq(OaActivityPlan::getIsDeleted, 0)
        .eq(orgId != null, OaActivityPlan::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), OaActivityPlan::getStatus, status)
        .ge(dateFrom != null, OaActivityPlan::getPlanDate, dateFrom)
        .le(dateTo != null, OaActivityPlan::getPlanDate, dateTo);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaActivityPlan::getTitle, keyword)
          .or().like(OaActivityPlan::getOrganizer, keyword)
          .or().like(OaActivityPlan::getLocation, keyword));
    }
    wrapper.orderByDesc(OaActivityPlan::getPlanDate).orderByDesc(OaActivityPlan::getCreateTime);
    return Result.ok(activityPlanMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<OaActivityPlan> create(@Valid @RequestBody OaActivityPlanRequest request) {
    Long orgId = AuthContext.getOrgId();
    OaActivityPlan plan = new OaActivityPlan();
    plan.setTenantId(orgId);
    plan.setOrgId(orgId);
    plan.setTitle(request.getTitle());
    plan.setPlanDate(request.getPlanDate());
    plan.setStartTime(request.getStartTime());
    plan.setEndTime(request.getEndTime());
    plan.setLocation(request.getLocation());
    plan.setOrganizer(request.getOrganizer());
    plan.setParticipantTarget(request.getParticipantTarget());
    plan.setStatus(request.getStatus() == null ? "PLANNED" : request.getStatus());
    plan.setContent(request.getContent());
    plan.setRemark(request.getRemark());
    plan.setCreatedBy(AuthContext.getStaffId());
    activityPlanMapper.insert(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}")
  public Result<OaActivityPlan> update(@PathVariable Long id, @Valid @RequestBody OaActivityPlanRequest request) {
    OaActivityPlan plan = activityPlanMapper.selectById(id);
    if (plan == null) {
      return Result.ok(null);
    }
    plan.setTitle(request.getTitle());
    plan.setPlanDate(request.getPlanDate());
    plan.setStartTime(request.getStartTime());
    plan.setEndTime(request.getEndTime());
    plan.setLocation(request.getLocation());
    plan.setOrganizer(request.getOrganizer());
    plan.setParticipantTarget(request.getParticipantTarget());
    plan.setStatus(request.getStatus());
    plan.setContent(request.getContent());
    plan.setRemark(request.getRemark());
    activityPlanMapper.updateById(plan);
    return Result.ok(plan);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaActivityPlan plan = activityPlanMapper.selectById(id);
    if (plan != null) {
      plan.setIsDeleted(1);
      activityPlanMapper.updateById(plan);
    }
    return Result.ok(null);
  }
}
