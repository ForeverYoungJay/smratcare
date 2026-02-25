package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthInspection;
import com.zhiyangyun.care.health.mapper.HealthInspectionMapper;
import com.zhiyangyun.care.health.model.HealthInspectionRequest;
import com.zhiyangyun.care.health.service.HealthInspectionClosureService;
import com.zhiyangyun.care.health.support.ElderResolveSupport;
import jakarta.validation.Valid;
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
@RequestMapping("/api/health/inspection")
public class HealthInspectionController {
  private final HealthInspectionMapper mapper;
  private final ElderResolveSupport elderResolveSupport;
  private final HealthInspectionClosureService inspectionClosureService;

  public HealthInspectionController(
      HealthInspectionMapper mapper,
      ElderResolveSupport elderResolveSupport,
      HealthInspectionClosureService inspectionClosureService) {
    this.mapper = mapper;
    this.elderResolveSupport = elderResolveSupport;
    this.inspectionClosureService = inspectionClosureService;
  }

  @GetMapping("/page")
  public Result<IPage<HealthInspection>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(elderId != null, HealthInspection::getElderId, elderId)
        .eq(status != null && !status.isBlank(), HealthInspection::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(HealthInspection::getElderName, keyword)
          .or().like(HealthInspection::getInspectionItem, keyword)
          .or().like(HealthInspection::getInspectorName, keyword));
    }
    wrapper.orderByDesc(HealthInspection::getInspectionDate);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<HealthInspection> create(@Valid @RequestBody HealthInspectionRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    HealthInspection item = new HealthInspection();
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setInspectionDate(request.getInspectionDate());
    item.setInspectionItem(request.getInspectionItem());
    item.setResult(request.getResult());
    item.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "NORMAL" : request.getStatus());
    item.setInspectorName(request.getInspectorName());
    item.setFollowUpAction(request.getFollowUpAction());
    item.setRemark(request.getRemark());
    item.setCreatedBy(AuthContext.getStaffId());
    mapper.insert(item);
    inspectionClosureService.syncFromInspection(item, AuthContext.getStaffId());
    return Result.ok(item);
  }

  @PutMapping("/{id}")
  public Result<HealthInspection> update(@PathVariable Long id, @Valid @RequestBody HealthInspectionRequest request) {
    HealthInspection item = mapper.selectById(id);
    if (item == null) {
      return Result.ok(null);
    }
    Long elderId = elderResolveSupport.resolveElderId(AuthContext.getOrgId(), request.getElderId(), request.getElderName());
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setInspectionDate(request.getInspectionDate());
    item.setInspectionItem(request.getInspectionItem());
    item.setResult(request.getResult());
    item.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "NORMAL" : request.getStatus());
    item.setInspectorName(request.getInspectorName());
    item.setFollowUpAction(request.getFollowUpAction());
    item.setRemark(request.getRemark());
    mapper.updateById(item);
    inspectionClosureService.syncFromInspection(item, AuthContext.getStaffId());
    return Result.ok(item);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    HealthInspection item = mapper.selectById(id);
    if (item != null) {
      item.setIsDeleted(1);
      mapper.updateById(item);
    }
    return Result.ok(null);
  }
}
