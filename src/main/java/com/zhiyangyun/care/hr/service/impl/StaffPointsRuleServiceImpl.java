package com.zhiyangyun.care.hr.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.hr.entity.StaffPointsRule;
import com.zhiyangyun.care.hr.mapper.StaffPointsRuleMapper;
import com.zhiyangyun.care.hr.model.StaffPointsRuleRequest;
import com.zhiyangyun.care.hr.model.StaffPointsRuleResponse;
import com.zhiyangyun.care.mapper.CareTaskTemplateMapper;
import com.zhiyangyun.care.entity.CareTaskTemplate;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StaffPointsRuleServiceImpl implements com.zhiyangyun.care.hr.service.StaffPointsRuleService {
  private final StaffPointsRuleMapper ruleMapper;
  private final CareTaskTemplateMapper templateMapper;

  public StaffPointsRuleServiceImpl(StaffPointsRuleMapper ruleMapper,
      CareTaskTemplateMapper templateMapper) {
    this.ruleMapper = ruleMapper;
    this.templateMapper = templateMapper;
  }

  @Override
  public IPage<StaffPointsRuleResponse> page(Long orgId, long pageNo, long pageSize) {
    var wrapper = Wrappers.lambdaQuery(StaffPointsRule.class)
        .eq(StaffPointsRule::getIsDeleted, 0)
        .eq(orgId != null, StaffPointsRule::getOrgId, orgId)
        .orderByDesc(StaffPointsRule::getUpdateTime);
    IPage<StaffPointsRule> page = ruleMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, CareTaskTemplate> templateMap = templateMapper.selectBatchIds(
            page.getRecords().stream().map(StaffPointsRule::getTemplateId).filter(java.util.Objects::nonNull).toList())
        .stream()
        .collect(Collectors.toMap(CareTaskTemplate::getId, t -> t));

    IPage<StaffPointsRuleResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(rule -> {
      StaffPointsRuleResponse item = new StaffPointsRuleResponse();
      item.setId(rule.getId());
      item.setTemplateId(rule.getTemplateId());
      CareTaskTemplate template = templateMap.get(rule.getTemplateId());
      item.setTemplateName(template == null ? (rule.getTemplateId() == null ? "默认规则" : null) : template.getTaskName());
      item.setBasePoints(rule.getBasePoints());
      item.setScoreWeight(rule.getScoreWeight());
      item.setSuspiciousPenalty(rule.getSuspiciousPenalty());
      item.setFailPoints(rule.getFailPoints());
      item.setStatus(rule.getStatus());
      item.setRemark(rule.getRemark());
      item.setUpdateTime(rule.getUpdateTime());
      return item;
    }).toList());
    return resp;
  }

  @Override
  public StaffPointsRuleResponse upsert(Long orgId, StaffPointsRuleRequest request) {
    StaffPointsRule rule = null;
    if (request.getId() != null) {
      rule = ruleMapper.selectById(request.getId());
    }
    if (rule == null) {
      rule = ruleMapper.selectOne(
          Wrappers.lambdaQuery(StaffPointsRule.class)
              .eq(StaffPointsRule::getIsDeleted, 0)
              .eq(orgId != null, StaffPointsRule::getOrgId, orgId)
              .eq(request.getTemplateId() != null, StaffPointsRule::getTemplateId, request.getTemplateId())
              .isNull(request.getTemplateId() == null, StaffPointsRule::getTemplateId));
    }
    if (rule == null) {
      rule = new StaffPointsRule();
      rule.setOrgId(orgId);
      rule.setTemplateId(request.getTemplateId());
    }
    if (request.getBasePoints() != null) {
      rule.setBasePoints(request.getBasePoints());
    }
    if (request.getScoreWeight() != null) {
      rule.setScoreWeight(request.getScoreWeight());
    }
    if (request.getSuspiciousPenalty() != null) {
      rule.setSuspiciousPenalty(request.getSuspiciousPenalty());
    }
    if (request.getFailPoints() != null) {
      rule.setFailPoints(request.getFailPoints());
    }
    if (request.getStatus() != null) {
      rule.setStatus(request.getStatus());
    }
    if (request.getRemark() != null) {
      rule.setRemark(request.getRemark());
    }
    if (rule.getBasePoints() == null) {
      rule.setBasePoints(1);
    }
    if (rule.getScoreWeight() == null) {
      rule.setScoreWeight(java.math.BigDecimal.ZERO);
    }
    if (rule.getSuspiciousPenalty() == null) {
      rule.setSuspiciousPenalty(0);
    }
    if (rule.getFailPoints() == null) {
      rule.setFailPoints(0);
    }
    if (rule.getStatus() == null) {
      rule.setStatus(1);
    }

    if (rule.getId() == null) {
      ruleMapper.insert(rule);
    } else {
      ruleMapper.updateById(rule);
    }

    StaffPointsRuleResponse response = new StaffPointsRuleResponse();
    response.setId(rule.getId());
    response.setTemplateId(rule.getTemplateId());
    if (rule.getTemplateId() == null) {
      response.setTemplateName("默认规则");
    }
    response.setBasePoints(rule.getBasePoints());
    response.setScoreWeight(rule.getScoreWeight());
    response.setSuspiciousPenalty(rule.getSuspiciousPenalty());
    response.setFailPoints(rule.getFailPoints());
    response.setStatus(rule.getStatus());
    response.setRemark(rule.getRemark());
    response.setUpdateTime(rule.getUpdateTime());
    return response;
  }

  @Override
  public void delete(Long orgId, Long id) {
    StaffPointsRule rule = ruleMapper.selectById(id);
    if (rule == null || (orgId != null && !orgId.equals(rule.getOrgId()))) {
      return;
    }
    rule.setIsDeleted(1);
    ruleMapper.updateById(rule);
  }
}
