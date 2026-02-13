package com.zhiyangyun.care.survey.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.hr.service.StaffPointsService;
import com.zhiyangyun.care.survey.entity.SurveyQuestion;
import com.zhiyangyun.care.survey.entity.SurveySubmission;
import com.zhiyangyun.care.survey.entity.SurveySubmissionItem;
import com.zhiyangyun.care.survey.entity.SurveyTemplate;
import com.zhiyangyun.care.survey.entity.SurveyTemplateQuestion;
import com.zhiyangyun.care.survey.mapper.SurveyQuestionMapper;
import com.zhiyangyun.care.survey.mapper.SurveySubmissionItemMapper;
import com.zhiyangyun.care.survey.mapper.SurveySubmissionMapper;
import com.zhiyangyun.care.survey.mapper.SurveyTemplateMapper;
import com.zhiyangyun.care.survey.mapper.SurveyTemplateQuestionMapper;
import com.zhiyangyun.care.survey.model.SurveyPerformanceItem;
import com.zhiyangyun.care.survey.model.SurveyStatsSummaryResponse;
import com.zhiyangyun.care.survey.service.SurveySubmissionService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SurveySubmissionServiceImpl implements SurveySubmissionService {
  private final SurveySubmissionMapper submissionMapper;
  private final SurveySubmissionItemMapper submissionItemMapper;
  private final SurveyTemplateMapper templateMapper;
  private final SurveyTemplateQuestionMapper templateQuestionMapper;
  private final SurveyQuestionMapper questionMapper;
  private final StaffPointsService staffPointsService;
  private final StaffMapper staffMapper;

  public SurveySubmissionServiceImpl(SurveySubmissionMapper submissionMapper,
      SurveySubmissionItemMapper submissionItemMapper,
      SurveyTemplateMapper templateMapper,
      SurveyTemplateQuestionMapper templateQuestionMapper,
      SurveyQuestionMapper questionMapper,
      StaffPointsService staffPointsService,
      StaffMapper staffMapper) {
    this.submissionMapper = submissionMapper;
    this.submissionItemMapper = submissionItemMapper;
    this.templateMapper = templateMapper;
    this.templateQuestionMapper = templateQuestionMapper;
    this.questionMapper = questionMapper;
    this.staffPointsService = staffPointsService;
    this.staffMapper = staffMapper;
  }

  @Override
  public SurveySubmission submit(Long orgId, Long submitterId, String submitterName, String submitterRole,
                                 SurveySubmission submission, List<SurveySubmissionItem> items) {
    SurveyTemplate template = templateMapper.selectOne(Wrappers.lambdaQuery(SurveyTemplate.class)
        .eq(SurveyTemplate::getId, submission.getTemplateId())
        .eq(SurveyTemplate::getTenantId, orgId)
        .eq(SurveyTemplate::getOrgId, orgId)
        .eq(SurveyTemplate::getIsDeleted, 0));
    if (template == null) {
      throw new IllegalArgumentException("Template not found");
    }
    if (template.getStatus() != null && template.getStatus() != 1) {
      throw new IllegalArgumentException("Template not published");
    }
    List<SurveyTemplateQuestion> relations = templateQuestionMapper.selectList(
        Wrappers.lambdaQuery(SurveyTemplateQuestion.class)
            .eq(SurveyTemplateQuestion::getTenantId, orgId)
            .eq(SurveyTemplateQuestion::getOrgId, orgId)
            .eq(SurveyTemplateQuestion::getTemplateId, submission.getTemplateId())
            .eq(SurveyTemplateQuestion::getIsDeleted, 0));
    if (relations.isEmpty()) {
      throw new IllegalArgumentException("Template has no questions");
    }
    List<Long> questionIds = relations.stream().map(SurveyTemplateQuestion::getQuestionId).toList();
    List<SurveyQuestion> questions = questionMapper.selectBatchIds(questionIds);
    Map<Long, SurveyQuestion> questionMap = new HashMap<>();
    for (SurveyQuestion question : questions) {
      questionMap.put(question.getId(), question);
    }
    Map<Long, SurveyTemplateQuestion> relationMap = new HashMap<>();
    for (SurveyTemplateQuestion relation : relations) {
      relationMap.put(relation.getQuestionId(), relation);
    }
    validateAnswers(template, relationMap, questionMap, items);

    submission.setOrgId(orgId);
    submission.setTenantId(orgId);
    submission.setSubmitterId(submitterId);
    submission.setSubmitterName(submitterName);
    submission.setSubmitterRole(submitterRole);
    if (submission.getAnonymousFlag() == null) {
      submission.setAnonymousFlag(0);
    }
    submission.setStatus(1);
    submission.setIsDeleted(0);

    int totalScore = 0;
    boolean hasScore = false;
    for (SurveySubmissionItem item : items) {
      SurveyQuestion question = questionMap.get(item.getQuestionId());
      if (question == null) {
        continue;
      }
      if (template.getScoreEnabled() != null && template.getScoreEnabled() == 1
          && question.getScoreEnabled() != null && question.getScoreEnabled() == 1) {
        if (item.getScore() != null) {
          totalScore += item.getScore();
          hasScore = true;
        }
      } else {
        item.setScore(null);
      }
    }
    if (hasScore) {
      if (template.getTotalScore() != null && totalScore > template.getTotalScore()) {
        throw new IllegalArgumentException("Score exceeds template total");
      }
      submission.setScoreTotal(totalScore);
      submission.setScoreEffective(totalScore);
    }

    submissionMapper.insert(submission);
    for (SurveySubmissionItem item : items) {
      item.setTenantId(orgId);
      item.setOrgId(orgId);
      item.setSubmissionId(submission.getId());
      item.setIsDeleted(0);
      submissionItemMapper.insert(item);
    }

    if (submission.getRelatedStaffId() != null && submission.getScoreTotal() != null) {
      staffPointsService.awardSurveyPoints(orgId, submission.getRelatedStaffId(), submission.getId(),
          submission.getScoreTotal(), "问卷评分积分");
    }
    return submission;
  }

  @Override
  public IPage<SurveySubmission> page(Long orgId, long pageNo, long pageSize, Long templateId, String targetType, Long targetId) {
    var wrapper = Wrappers.lambdaQuery(SurveySubmission.class)
        .eq(SurveySubmission::getTenantId, orgId)
        .eq(SurveySubmission::getOrgId, orgId)
        .eq(SurveySubmission::getIsDeleted, 0);
    if (templateId != null) {
      wrapper.eq(SurveySubmission::getTemplateId, templateId);
    }
    if (targetType != null && !targetType.isBlank()) {
      wrapper.eq(SurveySubmission::getTargetType, targetType);
    }
    if (targetId != null) {
      wrapper.eq(SurveySubmission::getTargetId, targetId);
    }
    wrapper.orderByDesc(SurveySubmission::getCreateTime);
    return submissionMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  public SurveyStatsSummaryResponse summary(Long orgId, Long templateId, LocalDate from, LocalDate to) {
    List<SurveySubmission> submissions = loadSubmissions(orgId, templateId, from, to);
    long total = submissions.size();
    int sum = 0;
    int scored = 0;
    for (SurveySubmission submission : submissions) {
      if (submission.getScoreTotal() != null) {
        sum += submission.getScoreTotal();
        scored++;
      }
    }
    SurveyStatsSummaryResponse response = new SurveyStatsSummaryResponse();
    response.setTotalSubmissions(total);
    response.setScoreTotal(sum);
    response.setAverageScore(scored == 0 ? 0.0 : (double) sum / scored);
    return response;
  }

  @Override
  public List<SurveyPerformanceItem> performance(Long orgId, Long templateId, LocalDate from, LocalDate to) {
    List<SurveySubmission> submissions = loadSubmissions(orgId, templateId, from, to);
    Map<Long, List<SurveySubmission>> grouped = new HashMap<>();
    for (SurveySubmission submission : submissions) {
      if (submission.getRelatedStaffId() == null) {
        continue;
      }
      grouped.computeIfAbsent(submission.getRelatedStaffId(), k -> new ArrayList<>()).add(submission);
    }
    Map<Long, StaffAccount> staffMap;
    if (grouped.isEmpty()) {
      staffMap = Map.of();
    } else {
      List<Long> staffIds = grouped.keySet().stream().toList();
      staffMap = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
              .in(StaffAccount::getId, staffIds)
              .eq(StaffAccount::getIsDeleted, 0))
          .stream()
          .collect(java.util.stream.Collectors.toMap(StaffAccount::getId, item -> item, (a, b) -> a));
    }
    List<SurveyPerformanceItem> items = new ArrayList<>();
    for (Map.Entry<Long, List<SurveySubmission>> entry : grouped.entrySet()) {
      long staffId = entry.getKey();
      int sum = 0;
      int count = 0;
      for (SurveySubmission submission : entry.getValue()) {
        if (submission.getScoreTotal() != null) {
          sum += submission.getScoreTotal();
          count++;
        }
      }
      SurveyPerformanceItem item = new SurveyPerformanceItem();
      item.setStaffId(staffId);
      StaffAccount staff = staffMap.get(staffId);
      item.setStaffName(staff == null ? null : staff.getRealName());
      item.setSubmissions((long) entry.getValue().size());
      item.setAverageScore(count == 0 ? 0.0 : (double) sum / count);
      items.add(item);
    }
    return items;
  }

  private List<SurveySubmission> loadSubmissions(Long orgId, Long templateId, LocalDate from, LocalDate to) {
    LocalDateTime start = from == null ? null : from.atStartOfDay();
    LocalDateTime end = to == null ? null : to.plusDays(1).atStartOfDay().minusNanos(1);
    var wrapper = Wrappers.lambdaQuery(SurveySubmission.class)
        .eq(SurveySubmission::getTenantId, orgId)
        .eq(SurveySubmission::getOrgId, orgId)
        .eq(SurveySubmission::getIsDeleted, 0);
    if (templateId != null) {
      wrapper.eq(SurveySubmission::getTemplateId, templateId);
    }
    if (start != null) {
      wrapper.ge(SurveySubmission::getCreateTime, start);
    }
    if (end != null) {
      wrapper.le(SurveySubmission::getCreateTime, end);
    }
    wrapper.orderByDesc(SurveySubmission::getCreateTime);
    return submissionMapper.selectList(wrapper);
  }

  private void validateAnswers(SurveyTemplate template,
      Map<Long, SurveyTemplateQuestion> relationMap,
      Map<Long, SurveyQuestion> questionMap,
      List<SurveySubmissionItem> items) {
    Map<Long, SurveySubmissionItem> answerMap = new HashMap<>();
    for (SurveySubmissionItem item : items) {
      answerMap.put(item.getQuestionId(), item);
      if (!relationMap.containsKey(item.getQuestionId())) {
        throw new IllegalArgumentException("Answer contains invalid question");
      }
    }

    for (Map.Entry<Long, SurveyTemplateQuestion> entry : relationMap.entrySet()) {
      Long questionId = entry.getKey();
      SurveyQuestion question = questionMap.get(questionId);
      if (question == null || question.getIsDeleted() != null && question.getIsDeleted() == 1) {
        continue;
      }
      SurveySubmissionItem item = answerMap.get(questionId);
      Integer required = entry.getValue().getRequiredFlag();
      if (required == null) {
        required = question.getRequiredFlag();
      }
      if (required != null && required == 1) {
        if (item == null || !hasAnswer(item)) {
          throw new IllegalArgumentException("Required question missing");
        }
      }

      if (item != null) {
        if (template.getScoreEnabled() != null && template.getScoreEnabled() == 1
            && question.getScoreEnabled() != null && question.getScoreEnabled() == 1) {
          if (item.getScore() == null) {
            throw new IllegalArgumentException("Score required for scored question");
          }
          if (question.getMaxScore() != null && item.getScore() > question.getMaxScore()) {
            throw new IllegalArgumentException("Score exceeds max score");
          }
          if (item.getScore() < 0) {
            throw new IllegalArgumentException("Score must be non-negative");
          }
        } else if (item.getScore() != null) {
          throw new IllegalArgumentException("Score not allowed for unscored question");
        }
      }
    }
  }

  private boolean hasAnswer(SurveySubmissionItem item) {
    if (item == null) {
      return false;
    }
    if (item.getAnswerText() != null && !item.getAnswerText().isBlank()) {
      return true;
    }
    return item.getAnswerJson() != null && !item.getAnswerJson().isBlank();
  }
}
