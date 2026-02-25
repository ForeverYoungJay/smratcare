package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthArchive;
import com.zhiyangyun.care.health.mapper.HealthArchiveMapper;
import com.zhiyangyun.care.health.model.HealthArchiveRequest;
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
@RequestMapping("/api/health/archive")
public class HealthArchiveController {
  private final HealthArchiveMapper mapper;
  private final ElderResolveSupport elderResolveSupport;

  public HealthArchiveController(HealthArchiveMapper mapper, ElderResolveSupport elderResolveSupport) {
    this.mapper = mapper;
    this.elderResolveSupport = elderResolveSupport;
  }

  @GetMapping("/page")
  public Result<IPage<HealthArchive>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(HealthArchive.class)
        .eq(HealthArchive::getIsDeleted, 0)
        .eq(orgId != null, HealthArchive::getOrgId, orgId)
        .eq(elderId != null, HealthArchive::getElderId, elderId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.like(HealthArchive::getElderName, keyword);
    }
    wrapper.orderByDesc(HealthArchive::getUpdateTime);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<HealthArchive> create(@Valid @RequestBody HealthArchiveRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    HealthArchive item = new HealthArchive();
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setBloodType(request.getBloodType());
    item.setAllergyHistory(request.getAllergyHistory());
    item.setChronicDisease(request.getChronicDisease());
    item.setMedicalHistory(request.getMedicalHistory());
    item.setEmergencyContact(request.getEmergencyContact());
    item.setEmergencyPhone(request.getEmergencyPhone());
    item.setRemark(request.getRemark());
    item.setCreatedBy(AuthContext.getStaffId());
    mapper.insert(item);
    return Result.ok(item);
  }

  @PutMapping("/{id}")
  public Result<HealthArchive> update(@PathVariable Long id, @Valid @RequestBody HealthArchiveRequest request) {
    HealthArchive item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item == null || item.getIsDeleted() != null && item.getIsDeleted() == 1
        || orgId != null && !orgId.equals(item.getOrgId())) {
      return Result.ok(null);
    }
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setBloodType(request.getBloodType());
    item.setAllergyHistory(request.getAllergyHistory());
    item.setChronicDisease(request.getChronicDisease());
    item.setMedicalHistory(request.getMedicalHistory());
    item.setEmergencyContact(request.getEmergencyContact());
    item.setEmergencyPhone(request.getEmergencyPhone());
    item.setRemark(request.getRemark());
    mapper.updateById(item);
    return Result.ok(item);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    HealthArchive item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item != null && (item.getIsDeleted() == null || item.getIsDeleted() == 0)
        && (orgId == null || orgId.equals(item.getOrgId()))) {
      item.setIsDeleted(1);
      mapper.updateById(item);
    }
    return Result.ok(null);
  }
}
