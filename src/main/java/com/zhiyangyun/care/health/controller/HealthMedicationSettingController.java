package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthMedicationSetting;
import com.zhiyangyun.care.health.mapper.HealthMedicationSettingMapper;
import com.zhiyangyun.care.health.model.HealthMedicationSettingRequest;
import com.zhiyangyun.care.health.service.HealthMedicationTaskService;
import com.zhiyangyun.care.health.support.ElderResolveSupport;
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
@RequestMapping("/api/health/medication/setting")
public class HealthMedicationSettingController {
  private final HealthMedicationSettingMapper mapper;
  private final ElderResolveSupport elderResolveSupport;
  private final HealthMedicationTaskService medicationTaskService;

  public HealthMedicationSettingController(
      HealthMedicationSettingMapper mapper,
      ElderResolveSupport elderResolveSupport,
      HealthMedicationTaskService medicationTaskService) {
    this.mapper = mapper;
    this.elderResolveSupport = elderResolveSupport;
    this.medicationTaskService = medicationTaskService;
  }

  @GetMapping("/page")
  public Result<IPage<HealthMedicationSetting>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(HealthMedicationSetting.class)
        .eq(HealthMedicationSetting::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationSetting::getOrgId, orgId)
        .eq(elderId != null, HealthMedicationSetting::getElderId, elderId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(HealthMedicationSetting::getElderName, keyword)
          .or().like(HealthMedicationSetting::getDrugName, keyword));
    }
    wrapper.orderByDesc(HealthMedicationSetting::getUpdateTime);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<HealthMedicationSetting> create(@Valid @RequestBody HealthMedicationSettingRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    HealthMedicationSetting item = new HealthMedicationSetting();
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setDrugId(request.getDrugId());
    item.setDrugName(request.getDrugName());
    item.setDosage(request.getDosage());
    item.setFrequency(request.getFrequency());
    item.setMedicationTime(request.getMedicationTime());
    item.setStartDate(request.getStartDate());
    item.setEndDate(request.getEndDate());
    item.setMinRemainQty(request.getMinRemainQty());
    item.setRemark(request.getRemark());
    item.setCreatedBy(AuthContext.getStaffId());
    mapper.insert(item);
    medicationTaskService.generateTasksForSetting(item, LocalDate.now());
    return Result.ok(item);
  }

  @PutMapping("/{id}")
  public Result<HealthMedicationSetting> update(@PathVariable Long id, @Valid @RequestBody HealthMedicationSettingRequest request) {
    HealthMedicationSetting item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item == null || item.getIsDeleted() != null && item.getIsDeleted() == 1
        || orgId != null && !orgId.equals(item.getOrgId())) {
      return Result.ok(null);
    }
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setDrugId(request.getDrugId());
    item.setDrugName(request.getDrugName());
    item.setDosage(request.getDosage());
    item.setFrequency(request.getFrequency());
    item.setMedicationTime(request.getMedicationTime());
    item.setStartDate(request.getStartDate());
    item.setEndDate(request.getEndDate());
    item.setMinRemainQty(request.getMinRemainQty());
    item.setRemark(request.getRemark());
    mapper.updateById(item);
    medicationTaskService.generateTasksForSetting(item, LocalDate.now());
    return Result.ok(item);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    HealthMedicationSetting item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item != null && (item.getIsDeleted() == null || item.getIsDeleted() == 0)
        && (orgId == null || orgId.equals(item.getOrgId()))) {
      item.setIsDeleted(1);
      mapper.updateById(item);
    }
    return Result.ok(null);
  }
}
