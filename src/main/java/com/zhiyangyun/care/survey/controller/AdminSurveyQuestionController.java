package com.zhiyangyun.care.survey.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.survey.entity.SurveyQuestion;
import com.zhiyangyun.care.survey.mapper.SurveyQuestionMapper;
import com.zhiyangyun.care.survey.model.SurveyQuestionRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/admin/survey/questions")
public class AdminSurveyQuestionController {
  private final SurveyQuestionMapper questionMapper;
  private final AuditLogService auditLogService;

  public AdminSurveyQuestionController(SurveyQuestionMapper questionMapper, AuditLogService auditLogService) {
    this.questionMapper = questionMapper;
    this.auditLogService = auditLogService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<SurveyQuestion> create(@Valid @RequestBody SurveyQuestionRequest request) {
    SurveyQuestion question = new SurveyQuestion();
    Long orgId = AuthContext.getOrgId();
    question.setTenantId(orgId);
    question.setOrgId(orgId);
    question.setQuestionCode(request.getQuestionCode());
    question.setTitle(request.getTitle());
    question.setQuestionType(request.getQuestionType());
    question.setOptionsJson(request.getOptionsJson());
    question.setRequiredFlag(request.getRequiredFlag() == null ? 0 : request.getRequiredFlag());
    question.setScoreEnabled(request.getScoreEnabled() == null ? 0 : request.getScoreEnabled());
    question.setMaxScore(request.getMaxScore());
    question.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    question.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
    question.setIsDeleted(0);
    questionMapper.insert(question);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "SURVEY_QUESTION", question.getId(), "新增题库题目");
    return Result.ok(question);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<SurveyQuestion> update(@PathVariable Long id, @Valid @RequestBody SurveyQuestionRequest request) {
    Long orgId = AuthContext.getOrgId();
    SurveyQuestion question = questionMapper.selectOne(Wrappers.lambdaQuery(SurveyQuestion.class)
        .eq(SurveyQuestion::getId, id)
        .eq(SurveyQuestion::getTenantId, orgId)
        .eq(SurveyQuestion::getOrgId, orgId)
        .eq(SurveyQuestion::getIsDeleted, 0));
    if (question == null) {
      return Result.error(404, "Question not found");
    }
    question.setQuestionCode(request.getQuestionCode());
    question.setTitle(request.getTitle());
    question.setQuestionType(request.getQuestionType());
    question.setOptionsJson(request.getOptionsJson());
    question.setRequiredFlag(request.getRequiredFlag() == null ? question.getRequiredFlag() : request.getRequiredFlag());
    question.setScoreEnabled(request.getScoreEnabled() == null ? question.getScoreEnabled() : request.getScoreEnabled());
    question.setMaxScore(request.getMaxScore());
    question.setStatus(request.getStatus() == null ? question.getStatus() : request.getStatus());
    question.setSortNo(request.getSortNo() == null ? question.getSortNo() : request.getSortNo());
    questionMapper.updateById(question);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "SURVEY_QUESTION", question.getId(), "更新题库题目");
    return Result.ok(question);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    SurveyQuestion question = questionMapper.selectOne(Wrappers.lambdaQuery(SurveyQuestion.class)
        .eq(SurveyQuestion::getId, id)
        .eq(SurveyQuestion::getTenantId, orgId)
        .eq(SurveyQuestion::getOrgId, orgId)
        .eq(SurveyQuestion::getIsDeleted, 0));
    if (question == null) {
      return Result.error(404, "Question not found");
    }
    question.setIsDeleted(1);
    questionMapper.updateById(question);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "SURVEY_QUESTION", question.getId(), "删除题库题目");
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public Result<SurveyQuestion> get(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    SurveyQuestion question = questionMapper.selectOne(Wrappers.lambdaQuery(SurveyQuestion.class)
        .eq(SurveyQuestion::getId, id)
        .eq(SurveyQuestion::getTenantId, orgId)
        .eq(SurveyQuestion::getOrgId, orgId)
        .eq(SurveyQuestion::getIsDeleted, 0));
    if (question == null) {
      return Result.error(404, "Question not found");
    }
    return Result.ok(question);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/page")
  public Result<IPage<SurveyQuestion>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String questionType) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(SurveyQuestion.class)
        .eq(SurveyQuestion::getTenantId, orgId)
        .eq(SurveyQuestion::getOrgId, orgId)
        .eq(SurveyQuestion::getIsDeleted, 0);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(SurveyQuestion::getTitle, keyword)
          .or().like(SurveyQuestion::getQuestionCode, keyword));
    }
    if (status != null) {
      wrapper.eq(SurveyQuestion::getStatus, status);
    }
    if (questionType != null && !questionType.isBlank()) {
      wrapper.eq(SurveyQuestion::getQuestionType, questionType);
    }
    wrapper.orderByAsc(SurveyQuestion::getSortNo)
        .orderByDesc(SurveyQuestion::getUpdateTime);
    return Result.ok(questionMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }
}
