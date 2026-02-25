package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthMedicationRegistration;
import com.zhiyangyun.care.health.mapper.HealthMedicationRegistrationMapper;
import com.zhiyangyun.care.health.model.HealthMedicationRegistrationRequest;
import com.zhiyangyun.care.health.service.HealthMedicationTaskService;
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
@RequestMapping("/api/health/medication/registration")
public class HealthMedicationRegistrationController {
  private final HealthMedicationRegistrationMapper mapper;
  private final ElderResolveSupport elderResolveSupport;
  private final HealthMedicationTaskService medicationTaskService;

  public HealthMedicationRegistrationController(
      HealthMedicationRegistrationMapper mapper,
      ElderResolveSupport elderResolveSupport,
      HealthMedicationTaskService medicationTaskService) {
    this.mapper = mapper;
    this.elderResolveSupport = elderResolveSupport;
    this.medicationTaskService = medicationTaskService;
  }

  @GetMapping("/page")
  public Result<IPage<HealthMedicationRegistration>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(HealthMedicationRegistration.class)
        .eq(HealthMedicationRegistration::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationRegistration::getOrgId, orgId)
        .eq(elderId != null, HealthMedicationRegistration::getElderId, elderId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(HealthMedicationRegistration::getElderName, keyword)
          .or().like(HealthMedicationRegistration::getDrugName, keyword)
          .or().like(HealthMedicationRegistration::getNurseName, keyword));
    }
    wrapper.orderByDesc(HealthMedicationRegistration::getRegisterTime);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<HealthMedicationRegistration> create(@Valid @RequestBody HealthMedicationRegistrationRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    HealthMedicationRegistration item = new HealthMedicationRegistration();
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setDrugId(request.getDrugId());
    item.setDrugName(request.getDrugName());
    item.setRegisterTime(request.getRegisterTime());
    item.setDosageTaken(request.getDosageTaken());
    item.setUnit(request.getUnit());
    item.setNurseName(request.getNurseName());
    item.setRemark(request.getRemark());
    item.setCreatedBy(AuthContext.getStaffId());
    mapper.insert(item);
    medicationTaskService.completeTaskByRegistration(item);
    return Result.ok(item);
  }

  @PutMapping("/{id}")
  public Result<HealthMedicationRegistration> update(@PathVariable Long id, @Valid @RequestBody HealthMedicationRegistrationRequest request) {
    HealthMedicationRegistration item = mapper.selectById(id);
    if (item == null) {
      return Result.ok(null);
    }
    Long elderId = elderResolveSupport.resolveElderId(AuthContext.getOrgId(), request.getElderId(), request.getElderName());
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setDrugId(request.getDrugId());
    item.setDrugName(request.getDrugName());
    item.setRegisterTime(request.getRegisterTime());
    item.setDosageTaken(request.getDosageTaken());
    item.setUnit(request.getUnit());
    item.setNurseName(request.getNurseName());
    item.setRemark(request.getRemark());
    mapper.updateById(item);
    medicationTaskService.completeTaskByRegistration(item);
    return Result.ok(item);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    HealthMedicationRegistration item = mapper.selectById(id);
    if (item != null) {
      item.setIsDeleted(1);
      mapper.updateById(item);
    }
    return Result.ok(null);
  }
}
