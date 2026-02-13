package com.zhiyangyun.care.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.entity.CareTaskTemplate;
import com.zhiyangyun.care.mapper.CareTaskTemplateMapper;
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
@RequestMapping("/api/care/template")
public class CareTaskTemplateController {
  private final CareTaskTemplateMapper templateMapper;

  public CareTaskTemplateController(CareTaskTemplateMapper templateMapper) {
    this.templateMapper = templateMapper;
  }

  @GetMapping("/page")
  public Result<IPage<CareTaskTemplate>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    Long tenantId = AuthContext.getOrgId();
    LambdaQueryWrapper<CareTaskTemplate> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(tenantId != null, CareTaskTemplate::getTenantId, tenantId)
        .eq(CareTaskTemplate::getIsDeleted, 0);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(CareTaskTemplate::getTaskName, keyword)
          .or().like(CareTaskTemplate::getCareLevelRequired, keyword));
    }
    wrapper.orderByDesc(CareTaskTemplate::getCreateTime);
    return Result.ok(templateMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<CareTaskTemplate> create(@RequestBody CareTaskTemplate template) {
    Long tenantId = AuthContext.getOrgId();
    template.setTenantId(tenantId);
    template.setOrgId(tenantId);
    template.setCreatedBy(AuthContext.getStaffId());
    if (template.getEnabled() == null) {
      template.setEnabled(true);
    }
    if (template.getChargeAmount() == null) {
      template.setChargeAmount(java.math.BigDecimal.ZERO);
    }
    templateMapper.insert(template);
    return Result.ok(template);
  }

  @PutMapping("/{id}")
  public Result<CareTaskTemplate> update(@PathVariable Long id, @RequestBody CareTaskTemplate template) {
    CareTaskTemplate existing = templateMapper.selectById(id);
    Long tenantId = AuthContext.getOrgId();
    if (existing == null || (tenantId != null && !tenantId.equals(existing.getTenantId()))) {
      return Result.ok(null);
    }
    template.setId(id);
    template.setTenantId(tenantId);
    template.setOrgId(tenantId);
    if (template.getTaskName() == null) {
      template.setTaskName(existing.getTaskName());
    }
    if (template.getFrequencyPerDay() == null) {
      template.setFrequencyPerDay(existing.getFrequencyPerDay());
    }
    if (template.getCareLevelRequired() == null) {
      template.setCareLevelRequired(existing.getCareLevelRequired());
    }
    if (template.getChargeAmount() == null) {
      template.setChargeAmount(existing.getChargeAmount());
    }
    if (template.getEnabled() == null) {
      template.setEnabled(existing.getEnabled());
    }
    templateMapper.updateById(template);
    return Result.ok(template);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    CareTaskTemplate existing = templateMapper.selectById(id);
    Long tenantId = AuthContext.getOrgId();
    if (existing != null && (tenantId == null || tenantId.equals(existing.getTenantId()))) {
      existing.setIsDeleted(1);
      templateMapper.updateById(existing);
    }
    return Result.ok(null);
  }
}
