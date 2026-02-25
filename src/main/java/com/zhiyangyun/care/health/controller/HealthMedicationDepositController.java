package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthMedicationDeposit;
import com.zhiyangyun.care.health.mapper.HealthMedicationDepositMapper;
import com.zhiyangyun.care.health.model.HealthMedicationDepositRequest;
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
@RequestMapping("/api/health/medication/deposit")
public class HealthMedicationDepositController {
  private final HealthMedicationDepositMapper mapper;
  private final ElderResolveSupport elderResolveSupport;

  public HealthMedicationDepositController(HealthMedicationDepositMapper mapper, ElderResolveSupport elderResolveSupport) {
    this.mapper = mapper;
    this.elderResolveSupport = elderResolveSupport;
  }

  @GetMapping("/page")
  public Result<IPage<HealthMedicationDeposit>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(HealthMedicationDeposit.class)
        .eq(HealthMedicationDeposit::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationDeposit::getOrgId, orgId)
        .eq(elderId != null, HealthMedicationDeposit::getElderId, elderId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(HealthMedicationDeposit::getElderName, keyword)
          .or().like(HealthMedicationDeposit::getDrugName, keyword));
    }
    wrapper.orderByDesc(HealthMedicationDeposit::getDepositDate);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<HealthMedicationDeposit> create(@Valid @RequestBody HealthMedicationDepositRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    HealthMedicationDeposit item = new HealthMedicationDeposit();
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setDrugId(request.getDrugId());
    item.setDrugName(request.getDrugName());
    item.setDepositDate(request.getDepositDate());
    item.setQuantity(request.getQuantity());
    item.setUnit(request.getUnit());
    item.setExpireDate(request.getExpireDate());
    item.setDepositorName(request.getDepositorName());
    item.setRemark(request.getRemark());
    item.setCreatedBy(AuthContext.getStaffId());
    mapper.insert(item);
    return Result.ok(item);
  }

  @PutMapping("/{id}")
  public Result<HealthMedicationDeposit> update(@PathVariable Long id, @Valid @RequestBody HealthMedicationDepositRequest request) {
    HealthMedicationDeposit item = mapper.selectById(id);
    if (item == null) {
      return Result.ok(null);
    }
    Long elderId = elderResolveSupport.resolveElderId(AuthContext.getOrgId(), request.getElderId(), request.getElderName());
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setDrugId(request.getDrugId());
    item.setDrugName(request.getDrugName());
    item.setDepositDate(request.getDepositDate());
    item.setQuantity(request.getQuantity());
    item.setUnit(request.getUnit());
    item.setExpireDate(request.getExpireDate());
    item.setDepositorName(request.getDepositorName());
    item.setRemark(request.getRemark());
    mapper.updateById(item);
    return Result.ok(item);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    HealthMedicationDeposit item = mapper.selectById(id);
    if (item != null) {
      item.setIsDeleted(1);
      mapper.updateById(item);
    }
    return Result.ok(null);
  }
}
