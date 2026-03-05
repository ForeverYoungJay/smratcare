package com.zhiyangyun.care.survey.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.survey.entity.SurveySubmission;
import com.zhiyangyun.care.survey.entity.SurveySubmissionItem;
import com.zhiyangyun.care.survey.entity.SurveyTemplate;
import com.zhiyangyun.care.survey.model.SurveyPerformanceItem;
import com.zhiyangyun.care.survey.model.SurveyStatsSummaryResponse;
import com.zhiyangyun.care.survey.model.SurveySubmissionAnswerItem;
import com.zhiyangyun.care.survey.model.SurveySubmissionRequest;
import com.zhiyangyun.care.survey.model.SurveySubmissionResponse;
import com.zhiyangyun.care.survey.service.SurveySubmissionService;
import com.zhiyangyun.care.survey.model.SurveyTemplateDetailResponse;
import com.zhiyangyun.care.survey.model.SurveyTemplateVerifyResponse;
import com.zhiyangyun.care.survey.service.SurveyTemplateService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
  private final SurveyTemplateService templateService;
  private final AuditLogService auditLogService;

  public SurveySubmissionController(
      SurveySubmissionService submissionService,
      SurveyTemplateService templateService,
      AuditLogService auditLogService) {
    this.submissionService = submissionService;
    this.templateService = templateService;
    this.auditLogService = auditLogService;
  }

  @GetMapping("/templates/page")
  public Result<IPage<SurveyTemplate>> publishedTemplatePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String targetType) {
    return Result.ok(templateService.pagePublishedAvailable(AuthContext.getOrgId(), pageNo, pageSize, keyword, targetType, LocalDate.now()));
  }

  @GetMapping("/templates/{id}")
  public Result<SurveyTemplateDetailResponse> publishedTemplateDetail(@PathVariable Long id) {
    SurveyTemplateDetailResponse detail = templateService.getDetail(AuthContext.getOrgId(), id);
    if (detail == null) {
      return Result.error(404, "Template not found");
    }
    SurveyTemplateVerifyResponse verify = verifyTemplate(detail, null, null);
    if (!verify.isValid()) {
      return Result.error(400, verify.getMessage());
    }
    return Result.ok(detail);
  }

  @GetMapping("/templates/{id}/verify")
  public Result<SurveyTemplateVerifyResponse> verifyPublishedTemplate(
      @PathVariable Long id,
      @RequestParam(required = false) String templateCode,
      @RequestParam(required = false) String targetType) {
    SurveyTemplateDetailResponse detail = templateService.getDetail(AuthContext.getOrgId(), id);
    if (detail == null) {
      return Result.error(404, "Template not found");
    }
    return Result.ok(verifyTemplate(detail, templateCode, targetType));
  }

  @PostMapping("/submissions")
  public Result<SurveySubmissionResponse> submit(@Valid @RequestBody SurveySubmissionRequest request) {
    SurveyTemplateDetailResponse detail = templateService.getDetail(AuthContext.getOrgId(), request.getTemplateId());
    if (detail == null) {
      return Result.error(404, "Template not found");
    }
    SurveyTemplateVerifyResponse verify = verifyTemplate(detail, request.getTemplateCode(), request.getTargetType());
    if (!verify.isValid()) {
      return Result.error(400, verify.getMessage());
    }

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

  private SurveyTemplateVerifyResponse verifyTemplate(SurveyTemplateDetailResponse detail, String templateCode, String targetType) {
    SurveyTemplateVerifyResponse response = new SurveyTemplateVerifyResponse();
    response.setTemplateId(detail.getId());
    response.setTemplateCode(detail.getTemplateCode());
    response.setTemplateName(detail.getTemplateName());
    response.setTargetType(detail.getTargetType());
    response.setStatus(detail.getStatus());
    response.setHasQuestions(detail.getQuestions() != null && !detail.getQuestions().isEmpty());
    if (templateCode != null && !templateCode.isBlank()
        && (detail.getTemplateCode() == null || !templateCode.equals(detail.getTemplateCode()))) {
      response.setValid(false);
      response.setMessage("问卷二维码参数异常，请重新获取");
      return response;
    }
    String normalizedTargetType = targetType == null ? null : targetType.trim();
    if (normalizedTargetType != null && !normalizedTargetType.isBlank()
        && (detail.getTargetType() == null || !normalizedTargetType.equalsIgnoreCase(detail.getTargetType()))) {
      response.setValid(false);
      response.setMessage("问卷对象类型不匹配，请重新扫码");
      return response;
    }
    if (!Integer.valueOf(1).equals(detail.getStatus())) {
      response.setValid(false);
      response.setMessage("问卷未发布或已停用");
      return response;
    }
    if (!response.isHasQuestions()) {
      response.setValid(false);
      response.setMessage("问卷未配置题目");
      return response;
    }
    LocalDate now = LocalDate.now();
    LocalDate startDate =
        detail.getStartDate() == null || detail.getStartDate().isBlank()
            ? null
            : LocalDate.parse(detail.getStartDate(), DateTimeFormatter.ISO_DATE);
    LocalDate endDate =
        detail.getEndDate() == null || detail.getEndDate().isBlank()
            ? null
            : LocalDate.parse(detail.getEndDate(), DateTimeFormatter.ISO_DATE);
    if (startDate != null && now.isBefore(startDate)) {
      response.setValid(false);
      response.setMessage("问卷未到生效日期");
      return response;
    }
    if (endDate != null && now.isAfter(endDate)) {
      response.setValid(false);
      response.setMessage("问卷已过期");
      return response;
    }
    response.setValid(true);
    response.setMessage("问卷可正常填写");
    return response;
  }

  @GetMapping("/submissions/page")
  public Result<IPage<SurveySubmission>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long templateId,
      @RequestParam(required = false) String targetType,
      @RequestParam(required = false) Long targetId,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    LocalDate from = dateFrom == null || dateFrom.isBlank() ? null : LocalDate.parse(dateFrom, DateTimeFormatter.ISO_DATE);
    LocalDate to = dateTo == null || dateTo.isBlank() ? null : LocalDate.parse(dateTo, DateTimeFormatter.ISO_DATE);
    return Result.ok(submissionService.page(AuthContext.getOrgId(), pageNo, pageSize, templateId, targetType, targetId, from, to));
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
