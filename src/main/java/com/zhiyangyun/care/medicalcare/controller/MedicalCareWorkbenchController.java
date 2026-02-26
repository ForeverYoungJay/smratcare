package com.zhiyangyun.care.medicalcare.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.entity.CareTaskExecuteLog;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.health.entity.HealthDataRecord;
import com.zhiyangyun.care.health.entity.HealthInspection;
import com.zhiyangyun.care.health.entity.HealthMedicationTask;
import com.zhiyangyun.care.health.entity.HealthNursingLog;
import com.zhiyangyun.care.health.mapper.HealthDataRecordMapper;
import com.zhiyangyun.care.health.mapper.HealthInspectionMapper;
import com.zhiyangyun.care.health.mapper.HealthMedicationTaskMapper;
import com.zhiyangyun.care.health.mapper.HealthNursingLogMapper;
import com.zhiyangyun.care.life.entity.IncidentReport;
import com.zhiyangyun.care.life.mapper.IncidentReportMapper;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.mapper.CareTaskExecuteLogMapper;
import com.zhiyangyun.care.medicalcare.entity.MedicalCvdRiskAssessment;
import com.zhiyangyun.care.medicalcare.entity.MedicalTcmAssessment;
import com.zhiyangyun.care.medicalcare.mapper.MedicalCvdRiskAssessmentMapper;
import com.zhiyangyun.care.medicalcare.mapper.MedicalTcmAssessmentMapper;
import com.zhiyangyun.care.medicalcare.model.MedicalCareWorkbenchSummaryResponse;
import com.zhiyangyun.care.nursing.entity.ShiftHandover;
import com.zhiyangyun.care.nursing.mapper.ShiftHandoverMapper;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.survey.entity.SurveySubmission;
import com.zhiyangyun.care.survey.mapper.SurveySubmissionMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
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
  private final CareTaskExecuteLogMapper careTaskExecuteLogMapper;
  private final ElderMapper elderMapper;
  private final HealthDataRecordMapper healthDataRecordMapper;
  private final HealthInspectionMapper healthInspectionMapper;
  private final HealthMedicationTaskMapper healthMedicationTaskMapper;
  private final HealthNursingLogMapper healthNursingLogMapper;
  private final ShiftHandoverMapper shiftHandoverMapper;
  private final IncidentReportMapper incidentReportMapper;
  private final SurveySubmissionMapper surveySubmissionMapper;
  private final OaTaskMapper oaTaskMapper;
  private final MedicalTcmAssessmentMapper tcmAssessmentMapper;
  private final MedicalCvdRiskAssessmentMapper cvdRiskAssessmentMapper;

  public MedicalCareWorkbenchController(
      CareTaskDailyMapper careTaskDailyMapper,
      CareTaskExecuteLogMapper careTaskExecuteLogMapper,
      ElderMapper elderMapper,
      HealthDataRecordMapper healthDataRecordMapper,
      HealthInspectionMapper healthInspectionMapper,
      HealthMedicationTaskMapper healthMedicationTaskMapper,
      HealthNursingLogMapper healthNursingLogMapper,
      ShiftHandoverMapper shiftHandoverMapper,
      IncidentReportMapper incidentReportMapper,
      SurveySubmissionMapper surveySubmissionMapper,
      OaTaskMapper oaTaskMapper,
      MedicalTcmAssessmentMapper tcmAssessmentMapper,
      MedicalCvdRiskAssessmentMapper cvdRiskAssessmentMapper) {
    this.careTaskDailyMapper = careTaskDailyMapper;
    this.careTaskExecuteLogMapper = careTaskExecuteLogMapper;
    this.elderMapper = elderMapper;
    this.healthDataRecordMapper = healthDataRecordMapper;
    this.healthInspectionMapper = healthInspectionMapper;
    this.healthMedicationTaskMapper = healthMedicationTaskMapper;
    this.healthNursingLogMapper = healthNursingLogMapper;
    this.shiftHandoverMapper = shiftHandoverMapper;
    this.incidentReportMapper = incidentReportMapper;
    this.surveySubmissionMapper = surveySubmissionMapper;
    this.oaTaskMapper = oaTaskMapper;
    this.tcmAssessmentMapper = tcmAssessmentMapper;
    this.cvdRiskAssessmentMapper = cvdRiskAssessmentMapper;
  }

  @GetMapping("/summary")
  public Result<MedicalCareWorkbenchSummaryResponse> summary() {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
    LocalDateTime startOf30Days = now.minusDays(30);

    MedicalCareWorkbenchSummaryResponse response = new MedicalCareWorkbenchSummaryResponse();

    Long pendingCare = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getTaskDate, today)
        .in(CareTaskDaily::getStatus, "TODO", "PENDING", "ASSIGNED"));
    response.setPendingCareTaskCount(pendingCare);

    Long overdueCare = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getTaskDate, today)
        .lt(CareTaskDaily::getPlanTime, now)
        .notIn(CareTaskDaily::getStatus, "DONE", "COMPLETED"));
    response.setOverdueCareTaskCount(overdueCare);

    Long inspectionPending = healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(HealthInspection::getInspectionDate, today)
        .in(HealthInspection::getStatus, "FOLLOWING", "ABNORMAL"));
    response.setTodayInspectionPendingCount(inspectionPending);
    response.setTodayInspectionTodoCount(inspectionPending);

    Long inspectionDone = healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(HealthInspection::getInspectionDate, today)
        .in(HealthInspection::getStatus, "NORMAL", "CLOSED"));
    response.setTodayInspectionDoneCount(inspectionDone);
    response.setTodayInspectionPlanCount(inspectionPending + inspectionDone);

    Long abnormalInspection = healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(HealthInspection::getStatus, "ABNORMAL"));
    response.setAbnormalInspectionCount(abnormalInspection);

    Long medicationPending = healthMedicationTaskMapper.selectCount(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
        .eq(HealthMedicationTask::getTaskDate, today)
        .eq(HealthMedicationTask::getStatus, "PENDING"));
    response.setTodayMedicationPendingCount(medicationPending);

    Long medicationDone = healthMedicationTaskMapper.selectCount(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
        .eq(HealthMedicationTask::getTaskDate, today)
        .eq(HealthMedicationTask::getStatus, "DONE"));
    response.setTodayMedicationDoneCount(medicationDone);

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

    // Card A
    response.setPendingMedicalOrderCount(medicationPending);
    response.setPendingReviewCount(0L);
    response.setPendingAuditCount(0L);
    Long unclosedAbnormal = abnormalInspection + incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .ne(IncidentReport::getStatus, "CLOSED"));
    response.setUnclosedAbnormalCount(unclosedAbnormal);

    // Card B
    Long abnormalVital24h = healthDataRecordMapper.selectCount(Wrappers.lambdaQuery(HealthDataRecord.class)
        .eq(HealthDataRecord::getIsDeleted, 0)
        .eq(orgId != null, HealthDataRecord::getOrgId, orgId)
        .eq(HealthDataRecord::getAbnormalFlag, 1)
        .ge(HealthDataRecord::getMeasuredAt, now.minusHours(24)));
    response.setAbnormalVital24hCount(abnormalVital24h);
    Long abnormalEvent24h = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .ge(IncidentReport::getIncidentTime, now.minusHours(24)));
    response.setAbnormalEvent24hCount(abnormalEvent24h);

    // Card C/D
    Long medShould = medicationPending + medicationDone;
    response.setMedicalOrderShouldCount(medShould);
    response.setMedicalOrderDoneCount(medicationDone);
    response.setMedicalOrderPendingCount(medicationPending);
    Long medAbnormal = healthMedicationTaskMapper.selectCount(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
        .eq(HealthMedicationTask::getTaskDate, today)
        .notIn(HealthMedicationTask::getStatus, "PENDING", "DONE"));
    response.setMedicalOrderAbnormalCount(medAbnormal);
    response.setOrderCheckRate(medShould == 0 ? 0D : (double) medicationDone * 100D / medShould);

    response.setMedicationShouldCount(medShould);
    response.setMedicationDoneCount(medicationDone);
    response.setMedicationUndoneCount(medicationPending);
    response.setMedicationLowStockCount(0L);
    response.setMedicationRequestPendingCount(0L);

    // Card E
    Long careDone = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getTaskDate, today)
        .in(CareTaskDaily::getStatus, "DONE", "COMPLETED"));
    Long careShould = pendingCare + careDone;
    response.setCareTaskShouldCount(careShould);
    response.setCareTaskDoneCount(careDone);
    response.setCareTaskOverdueCount(overdueCare);
    Long scanDone = careTaskExecuteLogMapper.selectCount(Wrappers.lambdaQuery(CareTaskExecuteLog.class)
        .eq(CareTaskExecuteLog::getIsDeleted, 0)
        .eq(orgId != null, CareTaskExecuteLog::getOrgId, orgId)
        .eq(CareTaskExecuteLog::getResultStatus, 1)
        .isNotNull(CareTaskExecuteLog::getBedQrCode)
        .ge(CareTaskExecuteLog::getExecuteTime, startOfDay)
        .lt(CareTaskExecuteLog::getExecuteTime, startOfTomorrow));
    response.setScanExecuteRate(careDone == 0 ? 0D : (double) scanDone * 100D / careDone);

    // Card F
    Long nursingLogPending = healthNursingLogMapper.selectCount(Wrappers.lambdaQuery(HealthNursingLog.class)
        .eq(HealthNursingLog::getIsDeleted, 0)
        .eq(orgId != null, HealthNursingLog::getOrgId, orgId)
        .eq(HealthNursingLog::getStatus, "PENDING"));
    response.setNursingLogPendingCount(nursingLogPending);

    // Card G
    Long handoverPending = shiftHandoverMapper.selectCount(Wrappers.lambdaQuery(ShiftHandover.class)
        .eq(ShiftHandover::getIsDeleted, 0)
        .eq(orgId != null, ShiftHandover::getOrgId, orgId)
        .eq(ShiftHandover::getDutyDate, today)
        .ne(ShiftHandover::getStatus, "CONFIRMED"));
    Long handoverDone = shiftHandoverMapper.selectCount(Wrappers.lambdaQuery(ShiftHandover.class)
        .eq(ShiftHandover::getIsDeleted, 0)
        .eq(orgId != null, ShiftHandover::getOrgId, orgId)
        .eq(ShiftHandover::getDutyDate, today)
        .eq(ShiftHandover::getStatus, "CONFIRMED"));
    Long handoverRisk = shiftHandoverMapper.selectCount(Wrappers.lambdaQuery(ShiftHandover.class)
        .eq(ShiftHandover::getIsDeleted, 0)
        .eq(orgId != null, ShiftHandover::getOrgId, orgId)
        .eq(ShiftHandover::getDutyDate, today)
        .isNotNull(ShiftHandover::getRiskNote));
    Long handoverTodo = shiftHandoverMapper.selectCount(Wrappers.lambdaQuery(ShiftHandover.class)
        .eq(ShiftHandover::getIsDeleted, 0)
        .eq(orgId != null, ShiftHandover::getOrgId, orgId)
        .eq(ShiftHandover::getDutyDate, today)
        .isNotNull(ShiftHandover::getTodoNote));
    response.setHandoverPendingCount(handoverPending);
    response.setHandoverDoneCount(handoverDone);
    response.setHandoverRiskCount(handoverRisk);
    response.setHandoverTodoCount(handoverTodo);

    // Card H
    Long incidentOpen = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .ne(IncidentReport::getStatus, "CLOSED"));
    Long incident30d = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .ge(IncidentReport::getIncidentTime, startOf30Days));
    Long inHospital = elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getStatus, 1));
    Long lowScoreSurvey = surveySubmissionMapper.selectCount(Wrappers.lambdaQuery(SurveySubmission.class)
        .eq(SurveySubmission::getIsDeleted, 0)
        .eq(orgId != null, SurveySubmission::getOrgId, orgId)
        .lt(SurveySubmission::getScoreEffective, 60));
    Long rectifyInProgress = oaTaskMapper.selectCount(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .like(OaTask::getTitle, "整改")
        .in(OaTask::getStatus, "TODO", "DOING"));
    Long rectifyOverdue = oaTaskMapper.selectCount(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .like(OaTask::getTitle, "整改")
        .in(OaTask::getStatus, "TODO", "DOING")
        .lt(OaTask::getEndTime, now));
    response.setIncidentOpenCount(incidentOpen);
    response.setIncident30dCount(incident30d);
    response.setIncident30dRate(inHospital == 0 ? 0D : (double) incident30d * 100D / inHospital);
    response.setLowScoreSurveyCount(lowScoreSurvey);
    response.setRectifyInProgressCount(rectifyInProgress);
    response.setRectifyOverdueCount(rectifyOverdue);

    // Card I
    Long tcmMonthCount = tcmAssessmentMapper.selectCount(Wrappers.lambdaQuery(MedicalTcmAssessment.class)
        .eq(MedicalTcmAssessment::getIsDeleted, 0)
        .eq(orgId != null, MedicalTcmAssessment::getOrgId, orgId)
        .ge(MedicalTcmAssessment::getAssessmentDate, today.withDayOfMonth(1)));
    Long cvdMonthCount = cvdRiskAssessmentMapper.selectCount(Wrappers.lambdaQuery(MedicalCvdRiskAssessment.class)
        .eq(MedicalCvdRiskAssessment::getIsDeleted, 0)
        .eq(orgId != null, MedicalCvdRiskAssessment::getOrgId, orgId)
        .ge(MedicalCvdRiskAssessment::getAssessmentDate, today.withDayOfMonth(1)));
    response.setAiReportGeneratedCount(tcmMonthCount + cvdMonthCount);
    response.setAiReportPublishedCount(response.getTcmPublishedCount() + response.getCvdHighRiskCount());

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
    response.setTopRiskResidentCount((long) response.getKeyResidents().size());

    return Result.ok(response);
  }
}
