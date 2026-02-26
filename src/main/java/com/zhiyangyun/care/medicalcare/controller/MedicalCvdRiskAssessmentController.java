package com.zhiyangyun.care.medicalcare.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthInspection;
import com.zhiyangyun.care.health.entity.HealthNursingLog;
import com.zhiyangyun.care.health.mapper.HealthInspectionMapper;
import com.zhiyangyun.care.health.mapper.HealthNursingLogMapper;
import com.zhiyangyun.care.health.support.ElderResolveSupport;
import com.zhiyangyun.care.medicalcare.entity.MedicalCvdRiskAssessment;
import com.zhiyangyun.care.medicalcare.mapper.MedicalCvdRiskAssessmentMapper;
import com.zhiyangyun.care.medicalcare.model.CvdPublishActionRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalCvdRiskAssessmentRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
@RequestMapping("/api/medical-care/cvd-assessments")
public class MedicalCvdRiskAssessmentController {
  private final MedicalCvdRiskAssessmentMapper mapper;
  private final ElderResolveSupport elderResolveSupport;
  private final HealthInspectionMapper inspectionMapper;
  private final HealthNursingLogMapper nursingLogMapper;

  public MedicalCvdRiskAssessmentController(
      MedicalCvdRiskAssessmentMapper mapper,
      ElderResolveSupport elderResolveSupport,
      HealthInspectionMapper inspectionMapper,
      HealthNursingLogMapper nursingLogMapper) {
    this.mapper = mapper;
    this.elderResolveSupport = elderResolveSupport;
    this.inspectionMapper = inspectionMapper;
    this.nursingLogMapper = nursingLogMapper;
  }

  @GetMapping("/page")
  public Result<IPage<MedicalCvdRiskAssessment>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String riskLevel,
      @RequestParam(required = false) Integer needFollowup,
      @RequestParam(required = false) String assessorName,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = buildQuery(orgId, elderId, riskLevel, needFollowup, assessorName, status, dateFrom, dateTo, keyword);
    wrapper.orderByDesc(MedicalCvdRiskAssessment::getAssessmentDate).orderByDesc(MedicalCvdRiskAssessment::getUpdateTime);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<MedicalCvdRiskAssessment> create(@Valid @RequestBody MedicalCvdRiskAssessmentRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    MedicalCvdRiskAssessment item = new MedicalCvdRiskAssessment();
    fillFromRequest(item, request, elderId);
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setCreatedBy(AuthContext.getStaffId());
    item.setStatus(item.getStatus() == null || item.getStatus().isBlank() ? "DRAFT" : item.getStatus());
    item.setIsDeleted(0);
    mapper.insert(item);
    return Result.ok(item);
  }

  @PutMapping("/{id}")
  public Result<MedicalCvdRiskAssessment> update(@PathVariable Long id, @Valid @RequestBody MedicalCvdRiskAssessmentRequest request) {
    MedicalCvdRiskAssessment item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item == null || (item.getIsDeleted() != null && item.getIsDeleted() == 1)
        || (orgId != null && !orgId.equals(item.getOrgId()))) {
      return Result.ok(null);
    }
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    fillFromRequest(item, request, elderId);
    mapper.updateById(item);
    return Result.ok(item);
  }

  @PostMapping("/{id}/publish")
  public Result<Map<String, Object>> publish(@PathVariable Long id, @RequestBody(required = false) CvdPublishActionRequest actions) {
    MedicalCvdRiskAssessment item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item == null || (item.getIsDeleted() != null && item.getIsDeleted() == 1)
        || (orgId != null && !orgId.equals(item.getOrgId()))) {
      return Result.ok(null);
    }
    item.setStatus("PUBLISHED");
    if (actions != null) {
      item.setGenerateInspectionPlan(actions.getGenerateInspectionPlan() == null ? 0 : actions.getGenerateInspectionPlan());
      item.setGenerateFollowupTask(actions.getGenerateFollowupTask() == null ? 0 : actions.getGenerateFollowupTask());
      item.setSuggestMedicalOrder(actions.getSuggestMedicalOrder() == null ? 0 : actions.getSuggestMedicalOrder());
    }
    mapper.updateById(item);

    Long inspectionId = null;
    Long nursingLogId = null;
    if (item.getGenerateInspectionPlan() != null && item.getGenerateInspectionPlan() == 1) {
      inspectionId = createInspectionPlan(item);
    }
    if (item.getGenerateFollowupTask() != null && item.getGenerateFollowupTask() == 1) {
      nursingLogId = createFollowupTask(item, inspectionId);
    }

    Map<String, Object> response = new HashMap<>();
    response.put("id", item.getId());
    response.put("status", item.getStatus());
    response.put("inspectionId", inspectionId);
    response.put("nursingLogId", nursingLogId);
    response.put("inspectionRoute", "/health/inspection?residentId=" + item.getElderId() + "&template=cvd_high_risk");
    response.put("medicalOrderRoute", "/life/health-basic?residentId=" + item.getElderId() + "&from=cvd_risk_assessment");
    return Result.ok(response);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    MedicalCvdRiskAssessment item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item != null && (item.getIsDeleted() == null || item.getIsDeleted() == 0)
        && (orgId == null || orgId.equals(item.getOrgId()))) {
      item.setIsDeleted(1);
      mapper.updateById(item);
    }
    return Result.ok(null);
  }

  private Long createInspectionPlan(MedicalCvdRiskAssessment item) {
    HealthInspection inspection = new HealthInspection();
    inspection.setTenantId(item.getTenantId());
    inspection.setOrgId(item.getOrgId());
    inspection.setElderId(item.getElderId());
    inspection.setElderName(item.getElderName());
    inspection.setInspectionDate(LocalDate.now());
    inspection.setInspectionItem("心血管高风险巡检计划（自动生成）");
    inspection.setResult(item.getConclusion());
    inspection.setStatus("FOLLOWING");
    inspection.setInspectorName(AuthContext.getUsername());
    inspection.setFollowUpAction(item.getNursingAdvice());
    inspection.setRemark("来源：心血管风险评估#" + item.getId());
    inspection.setCreatedBy(AuthContext.getStaffId());
    inspection.setIsDeleted(0);
    inspectionMapper.insert(inspection);
    return inspection.getId();
  }

  private Long createFollowupTask(MedicalCvdRiskAssessment item, Long inspectionId) {
    HealthNursingLog nursingLog = new HealthNursingLog();
    nursingLog.setTenantId(item.getTenantId());
    nursingLog.setOrgId(item.getOrgId());
    nursingLog.setElderId(item.getElderId());
    nursingLog.setElderName(item.getElderName());
    nursingLog.setSourceInspectionId(inspectionId);
    nursingLog.setLogTime(LocalDateTime.now());
    nursingLog.setLogType("医护随访");
    nursingLog.setContent(item.getMedicalAdvice() == null || item.getMedicalAdvice().isBlank()
        ? "请在随访周期内执行心血管高风险复核"
        : item.getMedicalAdvice());
    nursingLog.setStaffName(AuthContext.getUsername());
    nursingLog.setStatus("PENDING");
    nursingLog.setRemark("来源：心血管风险评估#" + item.getId());
    nursingLog.setCreatedBy(AuthContext.getStaffId());
    nursingLog.setIsDeleted(0);
    nursingLogMapper.insert(nursingLog);
    return nursingLog.getId();
  }

  private void fillFromRequest(MedicalCvdRiskAssessment item, MedicalCvdRiskAssessmentRequest request, Long elderId) {
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setAssessmentDate(request.getAssessmentDate());
    item.setAssessorId(request.getAssessorId() == null ? AuthContext.getStaffId() : request.getAssessorId());
    item.setAssessorName(request.getAssessorName() == null || request.getAssessorName().isBlank() ? AuthContext.getUsername()
        : request.getAssessorName());
    item.setRiskLevel(request.getRiskLevel());
    item.setKeyRiskFactors(request.getKeyRiskFactors());
    item.setLifestyleJson(request.getLifestyleJson());
    item.setFactorJson(request.getFactorJson());
    item.setConclusion(request.getConclusion());
    item.setMedicalAdvice(request.getMedicalAdvice());
    item.setNursingAdvice(request.getNursingAdvice());
    item.setFollowupDays(request.getFollowupDays());
    item.setNeedFollowup(request.getNeedFollowup() == null ? 0 : request.getNeedFollowup());
    item.setGenerateInspectionPlan(request.getGenerateInspectionPlan() == null ? 0 : request.getGenerateInspectionPlan());
    item.setGenerateFollowupTask(request.getGenerateFollowupTask() == null ? 0 : request.getGenerateFollowupTask());
    item.setSuggestMedicalOrder(request.getSuggestMedicalOrder() == null ? 0 : request.getSuggestMedicalOrder());
    item.setStatus(request.getStatus());
  }

  private LambdaQueryWrapper<MedicalCvdRiskAssessment> buildQuery(
      Long orgId,
      Long elderId,
      String riskLevel,
      Integer needFollowup,
      String assessorName,
      String status,
      String dateFrom,
      String dateTo,
      String keyword) {
    LocalDate from = parseDate(dateFrom);
    LocalDate to = parseDate(dateTo);
    LambdaQueryWrapper<MedicalCvdRiskAssessment> wrapper = Wrappers.lambdaQuery(MedicalCvdRiskAssessment.class)
        .eq(MedicalCvdRiskAssessment::getIsDeleted, 0)
        .eq(orgId != null, MedicalCvdRiskAssessment::getOrgId, orgId)
        .eq(elderId != null, MedicalCvdRiskAssessment::getElderId, elderId)
        .eq(riskLevel != null && !riskLevel.isBlank(), MedicalCvdRiskAssessment::getRiskLevel, riskLevel)
        .eq(needFollowup != null, MedicalCvdRiskAssessment::getNeedFollowup, needFollowup)
        .eq(assessorName != null && !assessorName.isBlank(), MedicalCvdRiskAssessment::getAssessorName, assessorName)
        .eq(status != null && !status.isBlank(), MedicalCvdRiskAssessment::getStatus, status)
        .ge(from != null, MedicalCvdRiskAssessment::getAssessmentDate, from)
        .le(to != null, MedicalCvdRiskAssessment::getAssessmentDate, to);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(MedicalCvdRiskAssessment::getElderName, keyword)
          .or().like(MedicalCvdRiskAssessment::getKeyRiskFactors, keyword)
          .or().like(MedicalCvdRiskAssessment::getConclusion, keyword)
          .or().like(MedicalCvdRiskAssessment::getAssessorName, keyword));
    }
    return wrapper;
  }

  private LocalDate parseDate(String date) {
    if (date == null || date.isBlank()) {
      return null;
    }
    return LocalDate.parse(date);
  }
}
