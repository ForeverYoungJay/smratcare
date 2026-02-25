package com.zhiyangyun.care.nursing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.nursing.entity.ShiftTemplate;
import com.zhiyangyun.care.nursing.mapper.ShiftTemplateMapper;
import com.zhiyangyun.care.nursing.model.ShiftTemplateRequest;
import com.zhiyangyun.care.nursing.model.ShiftTemplateResponse;
import com.zhiyangyun.care.nursing.service.ShiftTemplateService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ShiftTemplateServiceImpl implements ShiftTemplateService {
  private final ShiftTemplateMapper shiftTemplateMapper;

  public ShiftTemplateServiceImpl(ShiftTemplateMapper shiftTemplateMapper) {
    this.shiftTemplateMapper = shiftTemplateMapper;
  }

  @Override
  public ShiftTemplateResponse create(ShiftTemplateRequest request) {
    ShiftTemplate template = new ShiftTemplate();
    fillEntity(template, request);
    template.setCreatedBy(request.getCreatedBy());
    shiftTemplateMapper.insert(template);
    return toResponse(template);
  }

  @Override
  public ShiftTemplateResponse update(Long id, ShiftTemplateRequest request) {
    ShiftTemplate template = shiftTemplateMapper.selectById(id);
    if (template == null || !request.getTenantId().equals(template.getTenantId())) {
      return null;
    }
    fillEntity(template, request);
    shiftTemplateMapper.updateById(template);
    return toResponse(template);
  }

  @Override
  public ShiftTemplateResponse get(Long id, Long tenantId) {
    ShiftTemplate template = shiftTemplateMapper.selectById(id);
    if (template == null || (tenantId != null && !tenantId.equals(template.getTenantId()))) {
      return null;
    }
    return toResponse(template);
  }

  @Override
  public IPage<ShiftTemplateResponse> page(Long tenantId, long pageNo, long pageSize, String keyword,
      Integer enabled) {
    var wrapper = Wrappers.lambdaQuery(ShiftTemplate.class)
        .eq(ShiftTemplate::getIsDeleted, 0)
        .eq(tenantId != null, ShiftTemplate::getTenantId, tenantId)
        .eq(enabled != null, ShiftTemplate::getEnabled, enabled)
        .orderByAsc(ShiftTemplate::getShiftCode);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(ShiftTemplate::getName, keyword).or().like(ShiftTemplate::getShiftCode, keyword));
    }
    IPage<ShiftTemplate> page = shiftTemplateMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public List<ShiftTemplateResponse> list(Long tenantId, Integer enabled) {
    var wrapper = Wrappers.lambdaQuery(ShiftTemplate.class)
        .eq(ShiftTemplate::getIsDeleted, 0)
        .eq(tenantId != null, ShiftTemplate::getTenantId, tenantId)
        .eq(enabled != null, ShiftTemplate::getEnabled, enabled)
        .orderByAsc(ShiftTemplate::getShiftCode);
    return shiftTemplateMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  public void delete(Long id, Long tenantId) {
    ShiftTemplate template = shiftTemplateMapper.selectById(id);
    if (template != null && tenantId != null && tenantId.equals(template.getTenantId())) {
      template.setIsDeleted(1);
      shiftTemplateMapper.updateById(template);
    }
  }

  private void fillEntity(ShiftTemplate template, ShiftTemplateRequest request) {
    template.setTenantId(request.getTenantId());
    template.setOrgId(request.getOrgId());
    template.setName(request.getName());
    template.setShiftCode(request.getShiftCode());
    template.setStartTime(request.getStartTime());
    template.setEndTime(request.getEndTime());
    template.setCrossDay(request.getCrossDay() == null ? 0 : request.getCrossDay());
    template.setRequiredStaffCount(request.getRequiredStaffCount() == null ? 1 : request.getRequiredStaffCount());
    template.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
    template.setRemark(request.getRemark());
  }

  private ShiftTemplateResponse toResponse(ShiftTemplate template) {
    ShiftTemplateResponse response = new ShiftTemplateResponse();
    response.setId(template.getId());
    response.setTenantId(template.getTenantId());
    response.setOrgId(template.getOrgId());
    response.setName(template.getName());
    response.setShiftCode(template.getShiftCode());
    response.setStartTime(template.getStartTime());
    response.setEndTime(template.getEndTime());
    response.setCrossDay(template.getCrossDay());
    response.setRequiredStaffCount(template.getRequiredStaffCount());
    response.setEnabled(template.getEnabled());
    response.setRemark(template.getRemark());
    return response;
  }
}
