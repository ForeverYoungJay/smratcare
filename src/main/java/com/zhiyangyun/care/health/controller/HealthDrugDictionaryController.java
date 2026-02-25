package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthDrugDictionary;
import com.zhiyangyun.care.health.mapper.HealthDrugDictionaryMapper;
import com.zhiyangyun.care.health.model.HealthDrugDictionaryRequest;
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
@RequestMapping("/api/health/drug-dictionary")
public class HealthDrugDictionaryController {
  private final HealthDrugDictionaryMapper mapper;

  public HealthDrugDictionaryController(HealthDrugDictionaryMapper mapper) {
    this.mapper = mapper;
  }

  @GetMapping("/page")
  public Result<IPage<HealthDrugDictionary>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(HealthDrugDictionary.class)
        .eq(HealthDrugDictionary::getIsDeleted, 0)
        .eq(orgId != null, HealthDrugDictionary::getOrgId, orgId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(HealthDrugDictionary::getDrugName, keyword)
          .or().like(HealthDrugDictionary::getDrugCode, keyword)
          .or().like(HealthDrugDictionary::getCategory, keyword));
    }
    wrapper.orderByDesc(HealthDrugDictionary::getUpdateTime);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<HealthDrugDictionary> create(@Valid @RequestBody HealthDrugDictionaryRequest request) {
    Long orgId = AuthContext.getOrgId();
    HealthDrugDictionary item = new HealthDrugDictionary();
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setDrugCode(request.getDrugCode());
    item.setDrugName(request.getDrugName());
    item.setSpecification(request.getSpecification());
    item.setUnit(request.getUnit());
    item.setManufacturer(request.getManufacturer());
    item.setCategory(request.getCategory());
    item.setRemark(request.getRemark());
    item.setCreatedBy(AuthContext.getStaffId());
    mapper.insert(item);
    return Result.ok(item);
  }

  @PutMapping("/{id}")
  public Result<HealthDrugDictionary> update(@PathVariable Long id, @Valid @RequestBody HealthDrugDictionaryRequest request) {
    HealthDrugDictionary item = mapper.selectById(id);
    if (item == null) {
      return Result.ok(null);
    }
    item.setDrugCode(request.getDrugCode());
    item.setDrugName(request.getDrugName());
    item.setSpecification(request.getSpecification());
    item.setUnit(request.getUnit());
    item.setManufacturer(request.getManufacturer());
    item.setCategory(request.getCategory());
    item.setRemark(request.getRemark());
    mapper.updateById(item);
    return Result.ok(item);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    HealthDrugDictionary item = mapper.selectById(id);
    if (item != null) {
      item.setIsDeleted(1);
      mapper.updateById(item);
    }
    return Result.ok(null);
  }
}
