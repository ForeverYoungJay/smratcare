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
import java.time.LocalDateTime;
import java.util.Locale;
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
    lead.setSource(normalizeSource(request.getSource()));
    lead.setCustomerTag(request.getCustomerTag());
    lead.setStatus(request.getStatus());
    lead.setContractSignedFlag(normalizeSignedFlag(request.getContractSignedFlag()));
    lead.setContractSignedAt(resolveContractTime(request.getContractSignedAt(), request.getContractSignedFlag()));
    lead.setContractNo(blankToNull(request.getContractNo()));
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
    lead.setSource(normalizeSource(request.getSource()));
    lead.setCustomerTag(request.getCustomerTag());
    lead.setStatus(request.getStatus());
    lead.setContractSignedFlag(normalizeSignedFlag(request.getContractSignedFlag()));
    lead.setContractSignedAt(resolveContractTime(request.getContractSignedAt(), request.getContractSignedFlag()));
    lead.setContractNo(blankToNull(request.getContractNo()));
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
  public IPage<CrmLeadResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer status, String source, String customerTag) {
    var wrapper = Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId);
    if (status != null) {
      wrapper.eq(CrmLead::getStatus, status);
    }
    if (source != null && !source.isBlank()) {
      wrapper.eq(CrmLead::getSource, source);
    }
    if (customerTag != null && !customerTag.isBlank()) {
      wrapper.eq(CrmLead::getCustomerTag, customerTag);
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
    response.setCustomerTag(lead.getCustomerTag());
    response.setStatus(lead.getStatus());
    response.setContractSignedFlag(lead.getContractSignedFlag());
    response.setContractSignedAt(lead.getContractSignedAt());
    response.setContractNo(lead.getContractNo());
    response.setNextFollowDate(lead.getNextFollowDate() == null ? null : lead.getNextFollowDate().toString());
    response.setRemark(lead.getRemark());
    response.setCreateTime(lead.getCreateTime());
    return response;
  }

  private LocalDate parseDate(String date) {
    if (date == null || date.isBlank()) {
      return null;
    }
    return LocalDate.parse(date);
  }

  private String normalizeSource(String source) {
    if (source == null || source.isBlank()) {
      return null;
    }
    String value = source.trim().replace("　", " ").replace(" ", "");
    String lower = value.toLowerCase(Locale.ROOT);
    if (lower.contains("抖音") || lower.contains("douyin") || lower.contains("dy")) {
      return "抖音";
    }
    if (lower.contains("微信") || lower.contains("weixin") || lower.contains("wx")) {
      return "微信";
    }
    if (lower.contains("转介绍") || lower.contains("介绍")) {
      return "转介绍";
    }
    if (lower.contains("自然到访") || lower.contains("到访")) {
      return "自然到访";
    }
    if (lower.contains("线上") || lower.contains("官网") || lower.contains("小程序")) {
      return "线上咨询";
    }
    if (lower.contains("社区")) {
      return "社区活动";
    }
    if ("其他".equals(value) || "other".equals(lower)) {
      return "其他";
    }
    return value;
  }

  private Integer normalizeSignedFlag(Integer contractSignedFlag) {
    return contractSignedFlag != null && contractSignedFlag == 1 ? 1 : 0;
  }

  private LocalDateTime resolveContractTime(LocalDateTime signedAt, Integer flag) {
    if (flag == null || flag != 1) {
      return null;
    }
    return signedAt == null ? LocalDateTime.now() : signedAt;
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
