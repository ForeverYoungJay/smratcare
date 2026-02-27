package com.zhiyangyun.care.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.crm.entity.CrmMarketingPlan;
import com.zhiyangyun.care.crm.mapper.CrmMarketingPlanMapper;
import com.zhiyangyun.care.crm.model.MarketingPlanRequest;
import com.zhiyangyun.care.crm.model.MarketingPlanResponse;
import com.zhiyangyun.care.crm.service.MarketingPlanService;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MarketingPlanServiceImpl implements MarketingPlanService {
  private final CrmMarketingPlanMapper marketingPlanMapper;

  public MarketingPlanServiceImpl(CrmMarketingPlanMapper marketingPlanMapper) {
    this.marketingPlanMapper = marketingPlanMapper;
  }

  @Override
  public IPage<MarketingPlanResponse> page(
      Long orgId,
      long pageNo,
      long pageSize,
      String moduleType,
      String status,
      String keyword) {
    Page<CrmMarketingPlan> page = new Page<>(pageNo, pageSize);
    LambdaQueryWrapper<CrmMarketingPlan> query = baseQuery(orgId)
        .eq(StringUtils.hasText(moduleType), CrmMarketingPlan::getModuleType, normalizeModuleType(moduleType))
        .eq(StringUtils.hasText(status), CrmMarketingPlan::getStatus, normalizeStatus(status))
        .and(StringUtils.hasText(keyword), w -> w
            .like(CrmMarketingPlan::getTitle, keyword)
            .or()
            .like(CrmMarketingPlan::getSummary, keyword)
            .or()
            .like(CrmMarketingPlan::getTarget, keyword)
            .or()
            .like(CrmMarketingPlan::getOwner, keyword))
        .orderByAsc(CrmMarketingPlan::getPriority)
        .orderByDesc(CrmMarketingPlan::getEffectiveDate)
        .orderByDesc(CrmMarketingPlan::getCreateTime);
    Page<CrmMarketingPlan> data = marketingPlanMapper.selectPage(page, query);
    Page<MarketingPlanResponse> result = new Page<>(data.getCurrent(), data.getSize(), data.getTotal());
    result.setRecords(data.getRecords().stream().map(this::toResponse).toList());
    return result;
  }

  @Override
  public List<MarketingPlanResponse> listByModule(Long orgId, String moduleType) {
    LambdaQueryWrapper<CrmMarketingPlan> query = baseQuery(orgId)
        .eq(StringUtils.hasText(moduleType), CrmMarketingPlan::getModuleType, normalizeModuleType(moduleType))
        .eq(CrmMarketingPlan::getStatus, "ACTIVE")
        .orderByAsc(CrmMarketingPlan::getPriority)
        .orderByDesc(CrmMarketingPlan::getEffectiveDate)
        .orderByDesc(CrmMarketingPlan::getUpdateTime);
    return marketingPlanMapper.selectList(query).stream().map(this::toResponse).toList();
  }

  @Override
  public MarketingPlanResponse create(Long orgId, Long staffId, MarketingPlanRequest request) {
    CrmMarketingPlan entity = new CrmMarketingPlan();
    entity.setOrgId(orgId);
    entity.setTenantId(orgId);
    entity.setCreatedBy(staffId);
    applyRequest(entity, request);
    marketingPlanMapper.insert(entity);
    return toResponse(entity);
  }

  @Override
  public MarketingPlanResponse update(Long orgId, Long id, MarketingPlanRequest request) {
    CrmMarketingPlan entity = getById(orgId, id);
    applyRequest(entity, request);
    marketingPlanMapper.updateById(entity);
    return toResponse(entity);
  }

  @Override
  public void remove(Long orgId, Long id) {
    CrmMarketingPlan entity = getById(orgId, id);
    entity.setIsDeleted(1);
    marketingPlanMapper.updateById(entity);
  }

  private LambdaQueryWrapper<CrmMarketingPlan> baseQuery(Long orgId) {
    return new LambdaQueryWrapper<CrmMarketingPlan>()
        .eq(CrmMarketingPlan::getIsDeleted, 0)
        .eq(Objects.nonNull(orgId), CrmMarketingPlan::getOrgId, orgId);
  }

  private CrmMarketingPlan getById(Long orgId, Long id) {
    CrmMarketingPlan entity = marketingPlanMapper.selectById(id);
    if (entity == null || entity.getIsDeleted() == 1 || !Objects.equals(entity.getOrgId(), orgId)) {
      throw new IllegalArgumentException("营销方案不存在或已删除");
    }
    return entity;
  }

  private void applyRequest(CrmMarketingPlan entity, MarketingPlanRequest request) {
    entity.setModuleType(normalizeModuleType(request.getModuleType()));
    entity.setTitle(trimToNull(request.getTitle()));
    entity.setSummary(trimToNull(request.getSummary()));
    entity.setContent(trimToNull(request.getContent()));
    entity.setQuarterLabel(trimToNull(request.getQuarterLabel()));
    entity.setTarget(trimToNull(request.getTarget()));
    entity.setOwner(trimToNull(request.getOwner()));
    entity.setPriority(request.getPriority() == null ? 50 : request.getPriority());
    entity.setStatus(normalizeStatus(request.getStatus()));
    entity.setEffectiveDate(request.getEffectiveDate());
  }

  private MarketingPlanResponse toResponse(CrmMarketingPlan entity) {
    MarketingPlanResponse response = new MarketingPlanResponse();
    response.setId(entity.getId());
    response.setModuleType(entity.getModuleType());
    response.setTitle(entity.getTitle());
    response.setSummary(entity.getSummary());
    response.setContent(entity.getContent());
    response.setQuarterLabel(entity.getQuarterLabel());
    response.setTarget(entity.getTarget());
    response.setOwner(entity.getOwner());
    response.setPriority(entity.getPriority());
    response.setStatus(entity.getStatus());
    response.setEffectiveDate(entity.getEffectiveDate());
    response.setCreateTime(entity.getCreateTime());
    response.setUpdateTime(entity.getUpdateTime());
    return response;
  }

  private String normalizeModuleType(String moduleType) {
    String value = trimToNull(moduleType);
    return value == null ? null : value.toUpperCase();
  }

  private String normalizeStatus(String status) {
    String value = trimToNull(status);
    return value == null ? "ACTIVE" : value.toUpperCase();
  }

  private String trimToNull(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    return value.trim();
  }
}
