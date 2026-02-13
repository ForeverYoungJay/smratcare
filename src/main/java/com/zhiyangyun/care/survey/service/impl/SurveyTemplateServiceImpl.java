package com.zhiyangyun.care.survey.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.survey.entity.SurveyQuestion;
import com.zhiyangyun.care.survey.entity.SurveyTemplate;
import com.zhiyangyun.care.survey.entity.SurveyTemplateQuestion;
import com.zhiyangyun.care.survey.mapper.SurveyQuestionMapper;
import com.zhiyangyun.care.survey.mapper.SurveyTemplateMapper;
import com.zhiyangyun.care.survey.mapper.SurveyTemplateQuestionMapper;
import com.zhiyangyun.care.survey.model.SurveyTemplateDetailResponse;
import com.zhiyangyun.care.survey.model.SurveyTemplateQuestionDetail;
import com.zhiyangyun.care.survey.model.SurveyTemplateQuestionItem;
import com.zhiyangyun.care.survey.service.SurveyTemplateService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SurveyTemplateServiceImpl implements SurveyTemplateService {
  private final SurveyTemplateMapper templateMapper;
  private final SurveyTemplateQuestionMapper templateQuestionMapper;
  private final SurveyQuestionMapper questionMapper;

  public SurveyTemplateServiceImpl(SurveyTemplateMapper templateMapper,
      SurveyTemplateQuestionMapper templateQuestionMapper,
      SurveyQuestionMapper questionMapper) {
    this.templateMapper = templateMapper;
    this.templateQuestionMapper = templateQuestionMapper;
    this.questionMapper = questionMapper;
  }

  @Override
  public SurveyTemplate create(Long orgId, SurveyTemplate template) {
    template.setTenantId(orgId);
    template.setOrgId(orgId);
    if (template.getStatus() == null) {
      template.setStatus(0);
    }
    if (template.getAnonymousFlag() == null) {
      template.setAnonymousFlag(0);
    }
    if (template.getScoreEnabled() == null) {
      template.setScoreEnabled(0);
    }
    template.setIsDeleted(0);
    templateMapper.insert(template);
    return template;
  }

  @Override
  public SurveyTemplate update(Long orgId, Long id, SurveyTemplate input) {
    SurveyTemplate existing = templateMapper.selectOne(Wrappers.lambdaQuery(SurveyTemplate.class)
        .eq(SurveyTemplate::getId, id)
        .eq(SurveyTemplate::getTenantId, orgId)
        .eq(SurveyTemplate::getOrgId, orgId)
        .eq(SurveyTemplate::getIsDeleted, 0));
    if (existing == null) {
      return null;
    }
    existing.setTemplateCode(input.getTemplateCode());
    existing.setTemplateName(input.getTemplateName());
    existing.setDescription(input.getDescription());
    existing.setTargetType(input.getTargetType());
    existing.setStatus(input.getStatus() == null ? existing.getStatus() : input.getStatus());
    existing.setStartDate(input.getStartDate());
    existing.setEndDate(input.getEndDate());
    existing.setAnonymousFlag(input.getAnonymousFlag() == null ? existing.getAnonymousFlag() : input.getAnonymousFlag());
    existing.setScoreEnabled(input.getScoreEnabled() == null ? existing.getScoreEnabled() : input.getScoreEnabled());
    existing.setTotalScore(input.getTotalScore());
    templateMapper.updateById(existing);
    return existing;
  }

  @Override
  public SurveyTemplateDetailResponse getDetail(Long orgId, Long id) {
    SurveyTemplate template = templateMapper.selectOne(Wrappers.lambdaQuery(SurveyTemplate.class)
        .eq(SurveyTemplate::getId, id)
        .eq(SurveyTemplate::getTenantId, orgId)
        .eq(SurveyTemplate::getOrgId, orgId)
        .eq(SurveyTemplate::getIsDeleted, 0));
    if (template == null) {
      return null;
    }
    SurveyTemplateDetailResponse response = new SurveyTemplateDetailResponse();
    response.setId(template.getId());
    response.setTemplateCode(template.getTemplateCode());
    response.setTemplateName(template.getTemplateName());
    response.setDescription(template.getDescription());
    response.setTargetType(template.getTargetType());
    response.setStatus(template.getStatus());
    response.setStartDate(template.getStartDate() == null ? null : template.getStartDate().toString());
    response.setEndDate(template.getEndDate() == null ? null : template.getEndDate().toString());
    response.setAnonymousFlag(template.getAnonymousFlag());
    response.setScoreEnabled(template.getScoreEnabled());
    response.setTotalScore(template.getTotalScore());

    List<SurveyTemplateQuestion> relations = templateQuestionMapper.selectList(
        Wrappers.lambdaQuery(SurveyTemplateQuestion.class)
            .eq(SurveyTemplateQuestion::getTenantId, orgId)
            .eq(SurveyTemplateQuestion::getOrgId, orgId)
            .eq(SurveyTemplateQuestion::getTemplateId, id)
            .eq(SurveyTemplateQuestion::getIsDeleted, 0)
            .orderByAsc(SurveyTemplateQuestion::getSortNo)
            .orderByAsc(SurveyTemplateQuestion::getId));

    if (relations.isEmpty()) {
      response.setQuestions(List.of());
      return response;
    }

    List<Long> questionIds = relations.stream().map(SurveyTemplateQuestion::getQuestionId).toList();
    List<SurveyQuestion> questions = questionMapper.selectBatchIds(questionIds);
    Map<Long, SurveyQuestion> questionMap = new HashMap<>();
    for (SurveyQuestion question : questions) {
      questionMap.put(question.getId(), question);
    }

    List<SurveyTemplateQuestionDetail> details = new ArrayList<>();
    for (SurveyTemplateQuestion relation : relations) {
      SurveyQuestion question = questionMap.get(relation.getQuestionId());
      if (question == null) {
        continue;
      }
      SurveyTemplateQuestionDetail detail = new SurveyTemplateQuestionDetail();
      detail.setQuestionId(question.getId());
      detail.setQuestionCode(question.getQuestionCode());
      detail.setTitle(question.getTitle());
      detail.setQuestionType(question.getQuestionType());
      detail.setOptionsJson(question.getOptionsJson());
      detail.setRequiredFlag(relation.getRequiredFlag() == null ? question.getRequiredFlag() : relation.getRequiredFlag());
      detail.setScoreEnabled(question.getScoreEnabled());
      detail.setMaxScore(question.getMaxScore());
      detail.setStatus(question.getStatus());
      detail.setSortNo(relation.getSortNo());
      detail.setWeight(relation.getWeight());
      details.add(detail);
    }
    response.setQuestions(details);
    return response;
  }

  @Override
  public IPage<SurveyTemplate> page(Long orgId, long pageNo, long pageSize, String keyword, Integer status, String targetType) {
    var wrapper = Wrappers.lambdaQuery(SurveyTemplate.class)
        .eq(SurveyTemplate::getTenantId, orgId)
        .eq(SurveyTemplate::getOrgId, orgId)
        .eq(SurveyTemplate::getIsDeleted, 0);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(SurveyTemplate::getTemplateName, keyword)
          .or().like(SurveyTemplate::getTemplateCode, keyword));
    }
    if (status != null) {
      wrapper.eq(SurveyTemplate::getStatus, status);
    }
    if (targetType != null && !targetType.isBlank()) {
      wrapper.eq(SurveyTemplate::getTargetType, targetType);
    }
    wrapper.orderByDesc(SurveyTemplate::getUpdateTime);
    return templateMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  public void setQuestions(Long orgId, Long templateId, List<SurveyTemplateQuestionItem> items) {
    SurveyTemplate template = templateMapper.selectOne(Wrappers.lambdaQuery(SurveyTemplate.class)
        .eq(SurveyTemplate::getId, templateId)
        .eq(SurveyTemplate::getTenantId, orgId)
        .eq(SurveyTemplate::getOrgId, orgId)
        .eq(SurveyTemplate::getIsDeleted, 0));
    if (template == null) {
      throw new IllegalArgumentException("Template not found");
    }
    templateQuestionMapper.update(null, Wrappers.lambdaUpdate(SurveyTemplateQuestion.class)
        .set(SurveyTemplateQuestion::getIsDeleted, 1)
        .eq(SurveyTemplateQuestion::getTenantId, orgId)
        .eq(SurveyTemplateQuestion::getOrgId, orgId)
        .eq(SurveyTemplateQuestion::getTemplateId, templateId)
        .eq(SurveyTemplateQuestion::getIsDeleted, 0));

    for (SurveyTemplateQuestionItem item : items) {
      SurveyTemplateQuestion relation = new SurveyTemplateQuestion();
      relation.setTenantId(orgId);
      relation.setOrgId(orgId);
      relation.setTemplateId(templateId);
      relation.setQuestionId(item.getQuestionId());
      relation.setSortNo(item.getSortNo() == null ? 0 : item.getSortNo());
      relation.setRequiredFlag(item.getRequiredFlag() == null ? 0 : item.getRequiredFlag());
      relation.setWeight(item.getWeight());
      relation.setIsDeleted(0);
      templateQuestionMapper.insert(relation);
    }
  }

  @Override
  public void delete(Long orgId, Long id) {
    SurveyTemplate existing = templateMapper.selectOne(Wrappers.lambdaQuery(SurveyTemplate.class)
        .eq(SurveyTemplate::getId, id)
        .eq(SurveyTemplate::getTenantId, orgId)
        .eq(SurveyTemplate::getOrgId, orgId)
        .eq(SurveyTemplate::getIsDeleted, 0));
    if (existing == null) {
      return;
    }
    existing.setIsDeleted(1);
    templateMapper.updateById(existing);
  }
}
