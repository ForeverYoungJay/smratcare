package com.zhiyangyun.care.medicalcare.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.health.entity.HealthInspection;
import com.zhiyangyun.care.health.entity.HealthMedicationTask;
import com.zhiyangyun.care.health.mapper.HealthInspectionMapper;
import com.zhiyangyun.care.health.mapper.HealthMedicationTaskMapper;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.medicalcare.entity.MedicalCvdRiskAssessment;
import com.zhiyangyun.care.medicalcare.entity.MedicalTcmAssessment;
import com.zhiyangyun.care.medicalcare.mapper.MedicalCvdRiskAssessmentMapper;
import com.zhiyangyun.care.medicalcare.mapper.MedicalTcmAssessmentMapper;
import com.zhiyangyun.care.medicalcare.model.MedicalCareWorkbenchSummaryResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medical-care/workbench")
public class MedicalCareWorkbenchController {
  private final CareTaskDailyMapper careTaskDailyMapper;
  private final HealthInspectionMapper healthInspectionMapper;
  private final HealthMedicationTaskMapper healthMedicationTaskMapper;
  private final MedicalTcmAssessmentMapper tcmAssessmentMapper;
  private final MedicalCvdRiskAssessmentMapper cvdRiskAssessmentMapper;

  public MedicalCareWorkbenchController(
      CareTaskDailyMapper careTaskDailyMapper,
      HealthInspectionMapper healthInspectionMapper,
      HealthMedicationTaskMapper healthMedicationTaskMapper,
      MedicalTcmAssessmentMapper tcmAssessmentMapper,
      MedicalCvdRiskAssessmentMapper cvdRiskAssessmentMapper) {
    this.careTaskDailyMapper = careTaskDailyMapper;
    this.healthInspectionMapper = healthInspectionMapper;
    this.healthMedicationTaskMapper = healthMedicationTaskMapper;
    this.tcmAssessmentMapper = tcmAssessmentMapper;
    this.cvdRiskAssessmentMapper = cvdRiskAssessmentMapper;
  }

  @GetMapping("/summary")
  public Result<MedicalCareWorkbenchSummaryResponse> summary() {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();

    MedicalCareWorkbenchSummaryResponse response = new MedicalCareWorkbenchSummaryResponse();

    response.setPendingCareTaskCount(careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getTaskDate, today)
        .in(CareTaskDaily::getStatus, "TODO", "PENDING", "ASSIGNED")));

    response.setOverdueCareTaskCount(careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getTaskDate, today)
        .lt(CareTaskDaily::getPlanTime, now)
        .notIn(CareTaskDaily::getStatus, "DONE", "COMPLETED")));

    response.setTodayInspectionPendingCount(healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(HealthInspection::getInspectionDate, today)
        .in(HealthInspection::getStatus, "FOLLOWING", "ABNORMAL")));

    response.setTodayInspectionDoneCount(healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(HealthInspection::getInspectionDate, today)
        .in(HealthInspection::getStatus, "NORMAL", "CLOSED")));

    response.setAbnormalInspectionCount(healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(HealthInspection::getStatus, "ABNORMAL")));

    response.setTodayMedicationPendingCount(healthMedicationTaskMapper.selectCount(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
        .eq(HealthMedicationTask::getTaskDate, today)
        .eq(HealthMedicationTask::getStatus, "PENDING")));

    response.setTodayMedicationDoneCount(healthMedicationTaskMapper.selectCount(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
        .eq(HealthMedicationTask::getTaskDate, today)
        .eq(HealthMedicationTask::getStatus, "DONE")));

    response.setTcmPublishedCount(tcmAssessmentMapper.selectCount(Wrappers.lambdaQuery(MedicalTcmAssessment.class)
        .eq(MedicalTcmAssessment::getIsDeleted, 0)
        .eq(orgId != null, MedicalTcmAssessment::getOrgId, orgId)
        .eq(MedicalTcmAssessment::getStatus, "PUBLISHED")));

    response.setCvdHighRiskCount(cvdRiskAssessmentMapper.selectCount(Wrappers.lambdaQuery(MedicalCvdRiskAssessment.class)
        .eq(MedicalCvdRiskAssessment::getIsDeleted, 0)
        .eq(orgId != null, MedicalCvdRiskAssessment::getOrgId, orgId)
        .in(MedicalCvdRiskAssessment::getRiskLevel, "HIGH", "VERY_HIGH")));

    response.setCvdNeedFollowupCount(cvdRiskAssessmentMapper.selectCount(Wrappers.lambdaQuery(MedicalCvdRiskAssessment.class)
        .eq(MedicalCvdRiskAssessment::getIsDeleted, 0)
        .eq(orgId != null, MedicalCvdRiskAssessment::getOrgId, orgId)
        .eq(MedicalCvdRiskAssessment::getNeedFollowup, 1)));

    List<MedicalCvdRiskAssessment> topList = cvdRiskAssessmentMapper.selectList(Wrappers.lambdaQuery(MedicalCvdRiskAssessment.class)
        .eq(MedicalCvdRiskAssessment::getIsDeleted, 0)
        .eq(orgId != null, MedicalCvdRiskAssessment::getOrgId, orgId)
        .in(MedicalCvdRiskAssessment::getRiskLevel, "VERY_HIGH", "HIGH")
        .orderByDesc(MedicalCvdRiskAssessment::getAssessmentDate)
        .last("LIMIT 20"));

    Map<Long, MedicalCareWorkbenchSummaryResponse.ResidentRiskItem> deduplicated = new LinkedHashMap<>();
    for (MedicalCvdRiskAssessment item : topList) {
      Long elderId = item.getElderId();
      if (elderId == null || deduplicated.containsKey(elderId)) {
        continue;
      }
      MedicalCareWorkbenchSummaryResponse.ResidentRiskItem riskItem = new MedicalCareWorkbenchSummaryResponse.ResidentRiskItem();
      riskItem.setElderId(elderId);
      riskItem.setElderName(item.getElderName());
      riskItem.setRiskLevel(item.getRiskLevel());
      riskItem.setKeyRiskFactors(item.getKeyRiskFactors());
      riskItem.setAssessmentDate(item.getAssessmentDate() == null ? null : item.getAssessmentDate().toString());
      deduplicated.put(elderId, riskItem);
      if (deduplicated.size() >= 5) {
        break;
      }
    }
    response.setKeyResidents(deduplicated.values().stream().toList());

    return Result.ok(response);
  }
}
