package com.zhiyangyun.care.survey.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.survey.entity.SurveySubmission;
import com.zhiyangyun.care.survey.entity.SurveySubmissionItem;
import com.zhiyangyun.care.survey.model.SurveyPerformanceItem;
import com.zhiyangyun.care.survey.model.SurveyStatsSummaryResponse;
import com.zhiyangyun.care.survey.model.SurveySubmissionAnswerItem;
import com.zhiyangyun.care.survey.model.SurveySubmissionRequest;
import com.zhiyangyun.care.survey.model.SurveySubmissionResponse;
import com.zhiyangyun.care.survey.service.SurveySubmissionService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/survey")
@PreAuthorize("hasAnyRole('ADMIN','STAFF','GUARD','FAMILY')")
public class SurveySubmissionController {
  private final SurveySubmissionService submissionService;
  private final AuditLogService auditLogService;

  public SurveySubmissionController(SurveySubmissionService submissionService, AuditLogService auditLogService) {
    this.submissionService = submissionService;
    this.auditLogService = auditLogService;
  }

  @PostMapping("/submissions")
  public Result<SurveySubmissionResponse> submit(@Valid @RequestBody SurveySubmissionRequest request) {
    SurveySubmission submission = new SurveySubmission();
    submission.setTemplateId(request.getTemplateId());
    submission.setTargetType(request.getTargetType());
    submission.setTargetId(request.getTargetId());
    submission.setRelatedStaffId(request.getRelatedStaffId());
    submission.setAnonymousFlag(request.getAnonymousFlag());

    List<SurveySubmissionItem> items = new ArrayList<>();
    for (SurveySubmissionAnswerItem answer : request.getAnswers()) {
      SurveySubmissionItem item = new SurveySubmissionItem();
      item.setQuestionId(answer.getQuestionId());
      item.setAnswerJson(answer.getAnswerJson());
      item.setAnswerText(answer.getAnswerText());
      item.setScore(answer.getScore());
      items.add(item);
    }

    SurveySubmission saved = submissionService.submit(
        AuthContext.getOrgId(),
        AuthContext.getStaffId(),
        AuthContext.getUsername(),
        null,
        submission,
        items);

    auditLogService.record(AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "SURVEY_SUBMISSION", saved.getId(), "提交问卷");

    SurveySubmissionResponse response = new SurveySubmissionResponse();
    response.setSubmissionId(saved.getId());
    response.setScoreTotal(saved.getScoreTotal());
    return Result.ok(response);
  }

  @GetMapping("/submissions/page")
  public Result<IPage<SurveySubmission>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long templateId,
      @RequestParam(required = false) String targetType,
      @RequestParam(required = false) Long targetId) {
    return Result.ok(submissionService.page(AuthContext.getOrgId(), pageNo, pageSize, templateId, targetType, targetId));
  }

  @GetMapping("/stats/summary")
  public Result<SurveyStatsSummaryResponse> summary(
      @RequestParam(required = false) Long templateId,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    LocalDate from = dateFrom == null || dateFrom.isBlank() ? null : LocalDate.parse(dateFrom, DateTimeFormatter.ISO_DATE);
    LocalDate to = dateTo == null || dateTo.isBlank() ? null : LocalDate.parse(dateTo, DateTimeFormatter.ISO_DATE);
    return Result.ok(submissionService.summary(AuthContext.getOrgId(), templateId, from, to));
  }

  @GetMapping("/stats/performance")
  public Result<List<SurveyPerformanceItem>> performance(
      @RequestParam(required = false) Long templateId,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    LocalDate from = dateFrom == null || dateFrom.isBlank() ? null : LocalDate.parse(dateFrom, DateTimeFormatter.ISO_DATE);
    LocalDate to = dateTo == null || dateTo.isBlank() ? null : LocalDate.parse(dateTo, DateTimeFormatter.ISO_DATE);
    return Result.ok(submissionService.performance(AuthContext.getOrgId(), templateId, from, to));
  }
}
