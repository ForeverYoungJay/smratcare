package com.zhiyangyun.care.medicalcare.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.health.entity.HealthDataRecord;
import com.zhiyangyun.care.health.entity.HealthInspection;
import com.zhiyangyun.care.health.mapper.HealthDataRecordMapper;
import com.zhiyangyun.care.health.mapper.HealthInspectionMapper;
import com.zhiyangyun.care.life.entity.IncidentReport;
import com.zhiyangyun.care.life.mapper.IncidentReportMapper;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.medicalcare.entity.MedicalCvdRiskAssessment;
import com.zhiyangyun.care.medicalcare.entity.MedicalTcmAssessment;
import com.zhiyangyun.care.medicalcare.mapper.MedicalCvdRiskAssessmentMapper;
import com.zhiyangyun.care.medicalcare.mapper.MedicalTcmAssessmentMapper;
import com.zhiyangyun.care.medicalcare.model.MedicalResidentRiskCardResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medical-care/resident360")
public class MedicalResident360Controller {
  private final MedicalTcmAssessmentMapper tcmAssessmentMapper;
  private final MedicalCvdRiskAssessmentMapper cvdRiskAssessmentMapper;
  private final HealthDataRecordMapper healthDataRecordMapper;
  private final HealthInspectionMapper healthInspectionMapper;
  private final IncidentReportMapper incidentReportMapper;
  private final CareTaskDailyMapper careTaskDailyMapper;

  public MedicalResident360Controller(
      MedicalTcmAssessmentMapper tcmAssessmentMapper,
      MedicalCvdRiskAssessmentMapper cvdRiskAssessmentMapper,
      HealthDataRecordMapper healthDataRecordMapper,
      HealthInspectionMapper healthInspectionMapper,
      IncidentReportMapper incidentReportMapper,
      CareTaskDailyMapper careTaskDailyMapper) {
    this.tcmAssessmentMapper = tcmAssessmentMapper;
    this.cvdRiskAssessmentMapper = cvdRiskAssessmentMapper;
    this.healthDataRecordMapper = healthDataRecordMapper;
    this.healthInspectionMapper = healthInspectionMapper;
    this.incidentReportMapper = incidentReportMapper;
    this.careTaskDailyMapper = careTaskDailyMapper;
  }

  @GetMapping("/risk-card")
  public Result<MedicalResidentRiskCardResponse> riskCard(@RequestParam Long residentId) {
    Long orgId = AuthContext.getOrgId();
    LocalDateTime now = LocalDateTime.now();

    MedicalResidentRiskCardResponse response = new MedicalResidentRiskCardResponse();
    response.setElderId(residentId);

    MedicalTcmAssessment latestTcm = tcmAssessmentMapper.selectOne(Wrappers.lambdaQuery(MedicalTcmAssessment.class)
        .eq(MedicalTcmAssessment::getIsDeleted, 0)
        .eq(orgId != null, MedicalTcmAssessment::getOrgId, orgId)
        .eq(MedicalTcmAssessment::getElderId, residentId)
        .orderByDesc(MedicalTcmAssessment::getAssessmentDate)
        .last("LIMIT 1"));
    if (latestTcm != null) {
      response.setElderName(latestTcm.getElderName());
      response.setLatestTcmPrimary(latestTcm.getConstitutionPrimary());
      response.setLatestTcmSecondary(latestTcm.getConstitutionSecondary());
      response.setLatestTcmDate(latestTcm.getAssessmentDate() == null ? null : latestTcm.getAssessmentDate().toString());
      response.setLatestTcmSuggestion(latestTcm.getSuggestionPoints());
    }

    MedicalCvdRiskAssessment latestCvd = cvdRiskAssessmentMapper.selectOne(Wrappers.lambdaQuery(MedicalCvdRiskAssessment.class)
        .eq(MedicalCvdRiskAssessment::getIsDeleted, 0)
        .eq(orgId != null, MedicalCvdRiskAssessment::getOrgId, orgId)
        .eq(MedicalCvdRiskAssessment::getElderId, residentId)
        .orderByDesc(MedicalCvdRiskAssessment::getAssessmentDate)
        .last("LIMIT 1"));
    if (latestCvd != null) {
      if (response.getElderName() == null || response.getElderName().isBlank()) {
        response.setElderName(latestCvd.getElderName());
      }
      response.setLatestCvdRiskLevel(latestCvd.getRiskLevel());
      response.setLatestCvdDate(latestCvd.getAssessmentDate() == null ? null : latestCvd.getAssessmentDate().toString());
      response.setLatestCvdFactors(latestCvd.getKeyRiskFactors());
      response.setLatestCvdConclusion(latestCvd.getConclusion());
    }

    response.setAbnormalVital24hCount(healthDataRecordMapper.selectCount(Wrappers.lambdaQuery(HealthDataRecord.class)
        .eq(HealthDataRecord::getIsDeleted, 0)
        .eq(orgId != null, HealthDataRecord::getOrgId, orgId)
        .eq(HealthDataRecord::getElderId, residentId)
        .eq(HealthDataRecord::getAbnormalFlag, 1)
        .ge(HealthDataRecord::getMeasuredAt, now.minusHours(24))));

    response.setAbnormalInspectionOpenCount(healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(HealthInspection::getElderId, residentId)
        .in(HealthInspection::getStatus, "ABNORMAL", "FOLLOWING")));

    response.setOpenIncidentCount(incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(IncidentReport::getElderId, residentId)
        .ne(IncidentReport::getStatus, "CLOSED")));

    response.setPendingCareTaskCount(careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getElderId, residentId)
        .eq(CareTaskDaily::getTaskDate, LocalDate.now())
        .in(CareTaskDaily::getStatus, "TODO", "PENDING", "ASSIGNED")));

    response.setTcmAssessmentRoute("/medical-care/assessment/tcm?residentId=" + residentId + "&from=resident360");
    response.setCvdAssessmentRoute("/medical-care/assessment/cvd?residentId=" + residentId + "&from=resident360");

    return Result.ok(response);
  }
}
