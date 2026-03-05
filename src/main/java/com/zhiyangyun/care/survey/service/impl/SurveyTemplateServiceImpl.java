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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    if (!StringUtils.hasText(template.getContent())) {
      template.setContent(template.getDescription());
    }
    if (template.getStatus() == null || Integer.valueOf(1).equals(template.getStatus())) {
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
    existing.setContent(StringUtils.hasText(input.getContent()) ? input.getContent() : input.getDescription());
    existing.setTargetType(input.getTargetType());
    if (input.getStatus() != null) {
      if (Integer.valueOf(1).equals(input.getStatus()) && !Integer.valueOf(1).equals(existing.getStatus())) {
        throw new IllegalArgumentException("请使用“发布”按钮发布问卷");
      }
      existing.setStatus(input.getStatus());
    }
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
    response.setContent(StringUtils.hasText(template.getContent()) ? template.getContent() : template.getDescription());
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
  public IPage<SurveyTemplate> pagePublishedAvailable(Long orgId, long pageNo, long pageSize, String keyword, String targetType,
      LocalDate date) {
    LocalDate current = date == null ? LocalDate.now() : date;
    var wrapper = Wrappers.lambdaQuery(SurveyTemplate.class)
        .eq(SurveyTemplate::getTenantId, orgId)
        .eq(SurveyTemplate::getOrgId, orgId)
        .eq(SurveyTemplate::getIsDeleted, 0)
        .eq(SurveyTemplate::getStatus, 1)
        .and(w -> w.isNull(SurveyTemplate::getStartDate).or().le(SurveyTemplate::getStartDate, current))
        .and(w -> w.isNull(SurveyTemplate::getEndDate).or().ge(SurveyTemplate::getEndDate, current));
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(SurveyTemplate::getTemplateName, keyword)
          .or().like(SurveyTemplate::getTemplateCode, keyword));
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
  public SurveyTemplate publish(Long orgId, Long id) {
    SurveyTemplate existing = templateMapper.selectOne(Wrappers.lambdaQuery(SurveyTemplate.class)
        .eq(SurveyTemplate::getId, id)
        .eq(SurveyTemplate::getTenantId, orgId)
        .eq(SurveyTemplate::getOrgId, orgId)
        .eq(SurveyTemplate::getIsDeleted, 0));
    if (existing == null) {
      return null;
    }
    List<SurveyTemplateQuestion> relations = templateQuestionMapper.selectList(
        Wrappers.lambdaQuery(SurveyTemplateQuestion.class)
            .eq(SurveyTemplateQuestion::getTenantId, orgId)
            .eq(SurveyTemplateQuestion::getOrgId, orgId)
            .eq(SurveyTemplateQuestion::getTemplateId, id)
            .eq(SurveyTemplateQuestion::getIsDeleted, 0));
    if (relations.isEmpty()) {
      throw new IllegalArgumentException("请先配置问卷题目后再发布");
    }
    if (existing.getStartDate() != null && existing.getEndDate() != null
        && existing.getEndDate().isBefore(existing.getStartDate())) {
      throw new IllegalArgumentException("问卷结束日期不能早于开始日期");
    }
    if (existing.getEndDate() != null && existing.getEndDate().isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("问卷结束日期已过期，不能发布");
    }
    if (Integer.valueOf(1).equals(existing.getScoreEnabled())) {
      if (existing.getTotalScore() == null || existing.getTotalScore() <= 0) {
        throw new IllegalArgumentException("计分问卷请先设置总分");
      }
      List<Long> questionIds = relations.stream().map(SurveyTemplateQuestion::getQuestionId).toList();
      List<SurveyQuestion> questions = questionMapper.selectBatchIds(questionIds);
      boolean hasScoreQuestion = questions.stream()
          .anyMatch(item -> item != null && Integer.valueOf(1).equals(item.getScoreEnabled()));
      if (!hasScoreQuestion) {
        throw new IllegalArgumentException("计分问卷至少需包含一个计分题目");
      }
    }
    existing.setStatus(1);
    templateMapper.updateById(existing);
    return existing;
  }

  @Override
  public SurveyTemplate disable(Long orgId, Long id) {
    SurveyTemplate existing = templateMapper.selectOne(Wrappers.lambdaQuery(SurveyTemplate.class)
        .eq(SurveyTemplate::getId, id)
        .eq(SurveyTemplate::getTenantId, orgId)
        .eq(SurveyTemplate::getOrgId, orgId)
        .eq(SurveyTemplate::getIsDeleted, 0));
    if (existing == null) {
      return null;
    }
    existing.setStatus(2);
    templateMapper.updateById(existing);
    return existing;
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
