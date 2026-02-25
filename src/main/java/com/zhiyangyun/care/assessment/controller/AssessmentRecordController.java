package com.zhiyangyun.care.assessment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.assessment.entity.AssessmentRecord;
import com.zhiyangyun.care.assessment.entity.AssessmentScaleTemplate;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.assessment.model.AssessmentRecordRequest;
import com.zhiyangyun.care.assessment.model.AssessmentScoreResult;
import com.zhiyangyun.care.assessment.service.AssessmentScoringService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@RequestMapping("/api/assessment/records")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class AssessmentRecordController {
  private final AssessmentRecordMapper recordMapper;
  private final ElderMapper elderMapper;
  private final AssessmentScoringService scoringService;

  public AssessmentRecordController(AssessmentRecordMapper recordMapper, ElderMapper elderMapper,
      AssessmentScoringService scoringService) {
    this.recordMapper = recordMapper;
    this.elderMapper = elderMapper;
    this.scoringService = scoringService;
  }

  @GetMapping("/page")
  public Result<IPage<AssessmentRecord>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String assessmentType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) Long templateId,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(AssessmentRecord.class)
        .eq(AssessmentRecord::getIsDeleted, 0)
        .eq(orgId != null, AssessmentRecord::getOrgId, orgId)
        .eq(elderId != null, AssessmentRecord::getElderId, elderId)
        .eq(templateId != null, AssessmentRecord::getTemplateId, templateId)
        .eq(assessmentType != null && !assessmentType.isBlank(), AssessmentRecord::getAssessmentType, assessmentType)
        .eq(status != null && !status.isBlank(), AssessmentRecord::getStatus, status);

    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(AssessmentRecord::getElderName, keyword)
          .or().like(AssessmentRecord::getAssessorName, keyword)
          .or().like(AssessmentRecord::getArchiveNo, keyword)
          .or().like(AssessmentRecord::getResultSummary, keyword));
    }
    if (dateFrom != null && !dateFrom.isBlank()) {
      wrapper.ge(AssessmentRecord::getAssessmentDate, LocalDate.parse(dateFrom));
    }
    if (dateTo != null && !dateTo.isBlank()) {
      wrapper.le(AssessmentRecord::getAssessmentDate, LocalDate.parse(dateTo));
    }

    wrapper.orderByDesc(AssessmentRecord::getAssessmentDate).orderByDesc(AssessmentRecord::getUpdateTime);
    return Result.ok(recordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/{id}")
  public Result<AssessmentRecord> get(@PathVariable Long id) {
    AssessmentRecord record = recordMapper.selectById(id);
    if (record == null || record.getIsDeleted() == 1) {
      return Result.ok(null);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(record.getOrgId())) {
      throw new IllegalArgumentException("无权限访问该评估记录");
    }
    return Result.ok(record);
  }

  @PostMapping
  public Result<AssessmentRecord> create(@Valid @RequestBody AssessmentRecordRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long elderId = resolveElderId(orgId, request.getElderId(), request.getElderName());

    AssessmentRecord record = new AssessmentRecord();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setElderId(elderId);
    record.setElderName(resolveElderName(elderId, request.getElderName()));
    patchFromRequest(record, request);
    applyAutoScore(orgId, record);

    if (record.getStatus() == null || record.getStatus().isBlank()) {
      record.setStatus("COMPLETED");
    }
    if (record.getScoreAuto() == null) {
      record.setScoreAuto(1);
    }
    if (record.getAssessorId() == null) {
      record.setAssessorId(AuthContext.getStaffId());
    }
    if (record.getAssessorName() == null || record.getAssessorName().isBlank()) {
      record.setAssessorName(AuthContext.getUsername());
    }
    if (record.getSource() == null || record.getSource().isBlank()) {
      record.setSource("MANUAL");
    }
    record.setCreatedBy(AuthContext.getStaffId());
    record.setIsDeleted(0);
    recordMapper.insert(record);
    return Result.ok(record);
  }

  @PutMapping("/{id}")
  public Result<AssessmentRecord> update(@PathVariable Long id, @Valid @RequestBody AssessmentRecordRequest request) {
    AssessmentRecord record = recordMapper.selectById(id);
    if (record == null || record.getIsDeleted() == 1) {
      return Result.ok(null);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(record.getOrgId())) {
      throw new IllegalArgumentException("无权限访问该评估记录");
    }

    Long elderId = resolveElderId(orgId, request.getElderId(), request.getElderName());
    record.setElderId(elderId);
    record.setElderName(resolveElderName(elderId, request.getElderName()));
    patchFromRequest(record, request);
    applyAutoScore(orgId, record);

    if (record.getAssessorId() == null) {
      record.setAssessorId(AuthContext.getStaffId());
    }
    if (record.getScoreAuto() == null) {
      record.setScoreAuto(1);
    }
    if (record.getAssessorName() == null || record.getAssessorName().isBlank()) {
      record.setAssessorName(AuthContext.getUsername());
    }
    if (record.getSource() == null || record.getSource().isBlank()) {
      record.setSource("MANUAL");
    }

    recordMapper.updateById(record);
    return Result.ok(record);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    AssessmentRecord record = recordMapper.selectById(id);
    if (record != null && record.getIsDeleted() != 1) {
      Long orgId = AuthContext.getOrgId();
      if (orgId != null && !orgId.equals(record.getOrgId())) {
        throw new IllegalArgumentException("无权限访问该评估记录");
      }
      record.setIsDeleted(1);
      recordMapper.updateById(record);
    }
    return Result.ok(null);
  }

  private void patchFromRequest(AssessmentRecord record, AssessmentRecordRequest request) {
    record.setAssessmentType(request.getAssessmentType());
    record.setTemplateId(request.getTemplateId());
    record.setLevelCode(request.getLevelCode());
    record.setScore(request.getScore());
    record.setAssessmentDate(request.getAssessmentDate());
    record.setNextAssessmentDate(request.getNextAssessmentDate());
    record.setAssessorId(request.getAssessorId());
    record.setAssessorName(request.getAssessorName());
    record.setStatus(request.getStatus());
    record.setResultSummary(request.getResultSummary());
    record.setSuggestion(request.getSuggestion());
    record.setDetailJson(request.getDetailJson());
    record.setScoreAuto(request.getScoreAuto());
    record.setArchiveNo(request.getArchiveNo());
    record.setSource(request.getSource());
  }

  private void applyAutoScore(Long orgId, AssessmentRecord record) {
    if (record.getTemplateId() == null) {
      return;
    }
    AssessmentScaleTemplate template = scoringService.getActiveTemplate(orgId, record.getTemplateId());
    if (template == null) {
      throw new IllegalArgumentException("Template not found");
    }
    if (!record.getAssessmentType().equals(template.getAssessmentType())) {
      throw new IllegalArgumentException("Template type mismatch");
    }
    if (record.getScoreAuto() != null && record.getScoreAuto() == 0) {
      return;
    }
    if (record.getDetailJson() == null || record.getDetailJson().isBlank()) {
      return;
    }
    AssessmentScoreResult result = scoringService.score(template, record.getDetailJson());
    record.setScore(result.getScore());
    if (record.getLevelCode() == null || record.getLevelCode().isBlank()) {
      record.setLevelCode(result.getLevelCode());
    }
    record.setScoreAuto(1);
  }

  private Long resolveElderId(Long orgId, Long elderId, String elderName) {
    if (elderId != null) {
      return elderId;
    }
    if (elderName == null || elderName.isBlank()) {
      throw new IllegalArgumentException("elderName required");
    }
    ElderProfile elder = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getFullName, elderName)
        .last("LIMIT 1"));
    if (elder == null) {
      throw new IllegalArgumentException("elder not found");
    }
    return elder.getId();
  }

  private String resolveElderName(Long elderId, String fallback) {
    if (fallback != null && !fallback.isBlank()) {
      return fallback;
    }
    if (elderId == null) {
      return null;
    }
    ElderProfile elder = elderMapper.selectById(elderId);
    return elder == null ? null : elder.getFullName();
  }
}
