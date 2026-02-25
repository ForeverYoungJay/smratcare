package com.zhiyangyun.care.assessment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.assessment.entity.AssessmentScaleTemplate;
import com.zhiyangyun.care.assessment.mapper.AssessmentScaleTemplateMapper;
import com.zhiyangyun.care.assessment.model.AssessmentScaleTemplateRequest;
import com.zhiyangyun.care.assessment.model.AssessmentScorePreviewRequest;
import com.zhiyangyun.care.assessment.model.AssessmentScorePreviewResponse;
import com.zhiyangyun.care.assessment.model.AssessmentScoreResult;
import com.zhiyangyun.care.assessment.service.AssessmentScoringService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/assessment/templates")
public class AssessmentScaleTemplateController {
  private final AssessmentScaleTemplateMapper templateMapper;
  private final AssessmentScoringService scoringService;

  public AssessmentScaleTemplateController(AssessmentScaleTemplateMapper templateMapper,
      AssessmentScoringService scoringService) {
    this.templateMapper = templateMapper;
    this.scoringService = scoringService;
  }

  @GetMapping("/page")
  @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
  public Result<IPage<AssessmentScaleTemplate>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String assessmentType,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(AssessmentScaleTemplate.class)
        .eq(AssessmentScaleTemplate::getIsDeleted, 0)
        .eq(orgId != null, AssessmentScaleTemplate::getOrgId, orgId)
        .eq(assessmentType != null && !assessmentType.isBlank(), AssessmentScaleTemplate::getAssessmentType, assessmentType)
        .eq(status != null, AssessmentScaleTemplate::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(AssessmentScaleTemplate::getTemplateName, keyword)
          .or().like(AssessmentScaleTemplate::getTemplateCode, keyword));
    }
    wrapper.orderByDesc(AssessmentScaleTemplate::getUpdateTime);
    return Result.ok(templateMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/list")
  @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
  public Result<List<AssessmentScaleTemplate>> list(
      @RequestParam(required = false) String assessmentType) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(AssessmentScaleTemplate.class)
        .eq(AssessmentScaleTemplate::getIsDeleted, 0)
        .eq(orgId != null, AssessmentScaleTemplate::getOrgId, orgId)
        .eq(AssessmentScaleTemplate::getStatus, 1)
        .eq(assessmentType != null && !assessmentType.isBlank(), AssessmentScaleTemplate::getAssessmentType, assessmentType)
        .orderByAsc(AssessmentScaleTemplate::getTemplateCode);
    return Result.ok(templateMapper.selectList(wrapper));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
  public Result<AssessmentScaleTemplate> get(@PathVariable Long id) {
    AssessmentScaleTemplate template = templateMapper.selectById(id);
    if (template == null || template.getIsDeleted() == 1) {
      return Result.ok(null);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(template.getOrgId())) {
      throw new IllegalArgumentException("无权限访问该模板");
    }
    return Result.ok(template);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public Result<AssessmentScaleTemplate> create(@Valid @RequestBody AssessmentScaleTemplateRequest request) {
    Long orgId = AuthContext.getOrgId();
    AssessmentScaleTemplate template = new AssessmentScaleTemplate();
    patch(template, request);
    template.setTenantId(orgId);
    template.setOrgId(orgId);
    template.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    template.setCreatedBy(AuthContext.getStaffId());
    template.setIsDeleted(0);
    templateMapper.insert(template);
    return Result.ok(template);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<AssessmentScaleTemplate> update(@PathVariable Long id, @Valid @RequestBody AssessmentScaleTemplateRequest request) {
    AssessmentScaleTemplate template = templateMapper.selectById(id);
    if (template == null || template.getIsDeleted() == 1) {
      return Result.ok(null);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(template.getOrgId())) {
      throw new IllegalArgumentException("无权限访问该模板");
    }
    patch(template, request);
    template.setStatus(request.getStatus() == null ? template.getStatus() : request.getStatus());
    templateMapper.updateById(template);
    return Result.ok(template);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<Void> delete(@PathVariable Long id) {
    AssessmentScaleTemplate template = templateMapper.selectById(id);
    if (template != null && template.getIsDeleted() != 1) {
      Long orgId = AuthContext.getOrgId();
      if (orgId != null && !orgId.equals(template.getOrgId())) {
        throw new IllegalArgumentException("无权限访问该模板");
      }
      template.setIsDeleted(1);
      templateMapper.updateById(template);
    }
    return Result.ok(null);
  }

  @PostMapping("/score-preview")
  @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
  public Result<AssessmentScorePreviewResponse> preview(@Valid @RequestBody AssessmentScorePreviewRequest request) {
    Long orgId = AuthContext.getOrgId();
    AssessmentScaleTemplate template = scoringService.getActiveTemplate(orgId, request.getTemplateId());
    if (template == null) {
      throw new IllegalArgumentException("Template not found");
    }
    AssessmentScoreResult scoreResult = scoringService.score(template, request.getDetailJson());
    AssessmentScorePreviewResponse response = new AssessmentScorePreviewResponse();
    response.setScore(scoreResult.getScore());
    response.setLevelCode(scoreResult.getLevelCode());
    response.setReason(scoreResult.getReason());
    return Result.ok(response);
  }

  private void patch(AssessmentScaleTemplate template, AssessmentScaleTemplateRequest request) {
    template.setTemplateCode(request.getTemplateCode());
    template.setTemplateName(request.getTemplateName());
    template.setAssessmentType(request.getAssessmentType());
    template.setDescription(request.getDescription());
    template.setScoreRulesJson(request.getScoreRulesJson());
    template.setLevelRulesJson(request.getLevelRulesJson());
  }
}
