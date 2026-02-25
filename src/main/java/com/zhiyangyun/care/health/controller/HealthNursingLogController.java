package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthNursingLog;
import com.zhiyangyun.care.health.mapper.HealthNursingLogMapper;
import com.zhiyangyun.care.health.model.HealthNursingLogRequest;
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
@RequestMapping("/api/health/nursing-log")
public class HealthNursingLogController {
  private final HealthNursingLogMapper mapper;
  private final ElderResolveSupport elderResolveSupport;
  private final HealthInspectionClosureService inspectionClosureService;

  public HealthNursingLogController(
      HealthNursingLogMapper mapper,
      ElderResolveSupport elderResolveSupport,
      HealthInspectionClosureService inspectionClosureService) {
    this.mapper = mapper;
    this.elderResolveSupport = elderResolveSupport;
    this.inspectionClosureService = inspectionClosureService;
  }

  @GetMapping("/page")
  public Result<IPage<HealthNursingLog>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(HealthNursingLog.class)
        .eq(HealthNursingLog::getIsDeleted, 0)
        .eq(orgId != null, HealthNursingLog::getOrgId, orgId)
        .eq(elderId != null, HealthNursingLog::getElderId, elderId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(HealthNursingLog::getElderName, keyword)
          .or().like(HealthNursingLog::getContent, keyword)
          .or().like(HealthNursingLog::getStaffName, keyword));
    }
    wrapper.orderByDesc(HealthNursingLog::getLogTime);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<HealthNursingLog> create(@Valid @RequestBody HealthNursingLogRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    HealthNursingLog item = new HealthNursingLog();
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setSourceInspectionId(request.getSourceInspectionId());
    item.setLogTime(request.getLogTime());
    item.setLogType(request.getLogType());
    item.setContent(request.getContent());
    item.setStaffName(request.getStaffName());
    item.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "PENDING" : request.getStatus());
    item.setRemark(request.getRemark());
    item.setCreatedBy(AuthContext.getStaffId());
    mapper.insert(item);
    inspectionClosureService.syncFromNursingLog(item);
    return Result.ok(item);
  }

  @PutMapping("/{id}")
  public Result<HealthNursingLog> update(@PathVariable Long id, @Valid @RequestBody HealthNursingLogRequest request) {
    HealthNursingLog item = mapper.selectById(id);
    if (item == null) {
      return Result.ok(null);
    }
    Long elderId = elderResolveSupport.resolveElderId(AuthContext.getOrgId(), request.getElderId(), request.getElderName());
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setSourceInspectionId(request.getSourceInspectionId());
    item.setLogTime(request.getLogTime());
    item.setLogType(request.getLogType());
    item.setContent(request.getContent());
    item.setStaffName(request.getStaffName());
    item.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "PENDING" : request.getStatus());
    item.setRemark(request.getRemark());
    mapper.updateById(item);
    inspectionClosureService.syncFromNursingLog(item);
    return Result.ok(item);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    HealthNursingLog item = mapper.selectById(id);
    if (item != null) {
      item.setIsDeleted(1);
      mapper.updateById(item);
    }
    return Result.ok(null);
  }
}
