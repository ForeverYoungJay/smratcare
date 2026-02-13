package com.zhiyangyun.care.crm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.model.CrmLeadRequest;
import com.zhiyangyun.care.crm.model.CrmLeadResponse;
import com.zhiyangyun.care.crm.service.CrmLeadService;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class CrmLeadServiceImpl implements CrmLeadService {
  private final CrmLeadMapper leadMapper;

  public CrmLeadServiceImpl(CrmLeadMapper leadMapper) {
    this.leadMapper = leadMapper;
  }

  @Override
  public CrmLeadResponse create(CrmLeadRequest request) {
    CrmLead lead = new CrmLead();
    lead.setTenantId(request.getTenantId());
    lead.setOrgId(request.getOrgId());
    lead.setName(request.getName());
    lead.setPhone(request.getPhone());
    lead.setSource(request.getSource());
    lead.setStatus(request.getStatus());
    lead.setNextFollowDate(parseDate(request.getNextFollowDate()));
    lead.setRemark(request.getRemark());
    lead.setCreatedBy(request.getCreatedBy());
    leadMapper.insert(lead);
    return toResponse(lead);
  }

  @Override
  public CrmLeadResponse update(Long id, CrmLeadRequest request) {
    CrmLead lead = leadMapper.selectById(id);
    if (lead == null || !request.getTenantId().equals(lead.getTenantId())) {
      return null;
    }
    lead.setName(request.getName());
    lead.setPhone(request.getPhone());
    lead.setSource(request.getSource());
    lead.setStatus(request.getStatus());
    lead.setNextFollowDate(parseDate(request.getNextFollowDate()));
    lead.setRemark(request.getRemark());
    leadMapper.updateById(lead);
    return toResponse(lead);
  }

  @Override
  public CrmLeadResponse get(Long id, Long tenantId) {
    CrmLead lead = leadMapper.selectById(id);
    if (lead == null || (tenantId != null && !tenantId.equals(lead.getTenantId()))) {
      return null;
    }
    return toResponse(lead);
  }

  @Override
  public IPage<CrmLeadResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer status) {
    var wrapper = Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId);
    if (status != null) {
      wrapper.eq(CrmLead::getStatus, status);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(CrmLead::getName, keyword).or().like(CrmLead::getPhone, keyword));
    }
    IPage<CrmLead> page = leadMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public void delete(Long id, Long tenantId) {
    CrmLead lead = leadMapper.selectById(id);
    if (lead != null && tenantId != null && tenantId.equals(lead.getTenantId())) {
      lead.setIsDeleted(1);
      leadMapper.updateById(lead);
    }
  }

  private CrmLeadResponse toResponse(CrmLead lead) {
    CrmLeadResponse response = new CrmLeadResponse();
    response.setId(lead.getId());
    response.setTenantId(lead.getTenantId());
    response.setOrgId(lead.getOrgId());
    response.setName(lead.getName());
    response.setPhone(lead.getPhone());
    response.setSource(lead.getSource());
    response.setStatus(lead.getStatus());
    response.setNextFollowDate(lead.getNextFollowDate() == null ? null : lead.getNextFollowDate().toString());
    response.setRemark(lead.getRemark());
    return response;
  }

  private LocalDate parseDate(String date) {
    if (date == null || date.isBlank()) {
      return null;
    }
    return LocalDate.parse(date);
  }
}
