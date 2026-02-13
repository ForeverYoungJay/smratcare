package com.zhiyangyun.care.survey.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.survey.entity.SurveyTemplate;
import com.zhiyangyun.care.survey.model.SurveyTemplateDetailResponse;
import com.zhiyangyun.care.survey.model.SurveyTemplateQuestionUpdateRequest;
import com.zhiyangyun.care.survey.model.SurveyTemplateRequest;
import com.zhiyangyun.care.survey.service.SurveyTemplateService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
@RequestMapping("/api/admin/survey/templates")
public class AdminSurveyTemplateController {
  private final SurveyTemplateService templateService;
  private final AuditLogService auditLogService;

  public AdminSurveyTemplateController(SurveyTemplateService templateService, AuditLogService auditLogService) {
    this.templateService = templateService;
    this.auditLogService = auditLogService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<SurveyTemplate> create(@Valid @RequestBody SurveyTemplateRequest request) {
    SurveyTemplate template = toEntity(request);
    template.setTenantId(AuthContext.getOrgId());
    template.setCreatedBy(AuthContext.getStaffId());
    SurveyTemplate created = templateService.create(AuthContext.getOrgId(), template);
    auditLogService.record(AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "SURVEY_TEMPLATE", created.getId(), "创建问卷模板");
    return Result.ok(created);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<SurveyTemplate> update(@PathVariable Long id, @Valid @RequestBody SurveyTemplateRequest request) {
    SurveyTemplate template = toEntity(request);
    template.setTenantId(AuthContext.getOrgId());
    SurveyTemplate updated = templateService.update(AuthContext.getOrgId(), id, template);
    if (updated == null) {
      return Result.error(404, "Template not found");
    }
    auditLogService.record(AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "SURVEY_TEMPLATE", updated.getId(), "更新问卷模板");
    return Result.ok(updated);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public Result<SurveyTemplateDetailResponse> get(@PathVariable Long id) {
    SurveyTemplateDetailResponse detail = templateService.getDetail(AuthContext.getOrgId(), id);
    if (detail == null) {
      return Result.error(404, "Template not found");
    }
    return Result.ok(detail);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/page")
  public Result<IPage<SurveyTemplate>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String targetType) {
    return Result.ok(templateService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword, status, targetType));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}/questions")
  public Result<Void> setQuestions(@PathVariable Long id, @Valid @RequestBody SurveyTemplateQuestionUpdateRequest request) {
    templateService.setQuestions(AuthContext.getOrgId(), id, request.getQuestions());
    auditLogService.record(AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "SURVEY_TEMPLATE", id, "配置问卷题目");
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    SurveyTemplateDetailResponse detail = templateService.getDetail(AuthContext.getOrgId(), id);
    if (detail == null) {
      return Result.error(404, "Template not found");
    }
    templateService.delete(AuthContext.getOrgId(), id);
    auditLogService.record(AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "SURVEY_TEMPLATE", id, "删除问卷模板");
    return Result.ok(null);
  }

  private SurveyTemplate toEntity(SurveyTemplateRequest request) {
    SurveyTemplate template = new SurveyTemplate();
    template.setTemplateCode(request.getTemplateCode());
    template.setTemplateName(request.getTemplateName());
    template.setDescription(request.getDescription());
    template.setTargetType(request.getTargetType());
    template.setStatus(request.getStatus());
    template.setAnonymousFlag(request.getAnonymousFlag());
    template.setScoreEnabled(request.getScoreEnabled());
    template.setTotalScore(request.getTotalScore());
    if (request.getStartDate() != null && !request.getStartDate().isBlank()) {
      template.setStartDate(LocalDate.parse(request.getStartDate(), DateTimeFormatter.ISO_DATE));
    }
    if (request.getEndDate() != null && !request.getEndDate().isBlank()) {
      template.setEndDate(LocalDate.parse(request.getEndDate(), DateTimeFormatter.ISO_DATE));
    }
    return template;
  }
}
