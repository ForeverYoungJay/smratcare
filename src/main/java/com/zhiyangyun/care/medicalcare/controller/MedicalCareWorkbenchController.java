package com.zhiyangyun.care.medicalcare.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.zhiyangyun.care.medicalcare.entity.MedicalAlertRuleConfig;
import com.zhiyangyun.care.medicalcare.mapper.MedicalAlertRuleConfigMapper;
import com.zhiyangyun.care.medicalcare.mapper.MedicalCvdRiskAssessmentMapper;
import com.zhiyangyun.care.medicalcare.mapper.MedicalTcmAssessmentMapper;
import com.zhiyangyun.care.medicalcare.model.MedicalCareWorkbenchSummaryResponse;
import com.zhiyangyun.care.medicalcare.model.MedicalRiskTimelinePointResponse;
import com.zhiyangyun.care.medicalcare.model.MedicalUnifiedTaskItemResponse;
import com.zhiyangyun.care.nursing.entity.ShiftHandover;
import com.zhiyangyun.care.nursing.mapper.ShiftHandoverMapper;
import com.zhiyangyun.care.oa.entity.OaDocument;
import com.zhiyangyun.care.oa.mapper.OaDocumentMapper;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.survey.entity.SurveySubmission;
import com.zhiyangyun.care.survey.mapper.SurveySubmissionMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/medical-care/workbench", "/api/medical-care/center"})
public class MedicalCareWorkbenchController {
  private static final Logger log = LoggerFactory.getLogger(MedicalCareWorkbenchController.class);
  private static final int DEFAULT_INCIDENT_WINDOW_DAYS = 30;
  private static final int MIN_INCIDENT_WINDOW_DAYS = 1;
  private static final int MAX_INCIDENT_WINDOW_DAYS = 365;
  private static final int MIN_OVERDUE_HOURS = 1;
  private static final int MAX_OVERDUE_HOURS = 72;
  private static final int DEFAULT_TOP_RESIDENT_LIMIT = 5;
  private static final int MIN_TOP_RESIDENT_LIMIT = 1;
  private static final int MAX_TOP_RESIDENT_LIMIT = 20;
  private static final int DEFAULT_RISK_RESIDENT_LOOKBACK_DAYS = 180;
  private static final int MIN_RISK_RESIDENT_LOOKBACK_DAYS = 7;
  private static final int MAX_RISK_RESIDENT_LOOKBACK_DAYS = 730;
  private static final int DEFAULT_RISK_TIMELINE_DAYS = 14;
  private static final int MIN_RISK_TIMELINE_DAYS = 3;
  private static final int MAX_RISK_TIMELINE_DAYS = 60;

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
  private final OaDocumentMapper oaDocumentMapper;
  private final OaTaskMapper oaTaskMapper;
  private final MedicalTcmAssessmentMapper tcmAssessmentMapper;
  private final MedicalCvdRiskAssessmentMapper cvdRiskAssessmentMapper;
  private final MedicalAlertRuleConfigMapper medicalAlertRuleConfigMapper;

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
      OaDocumentMapper oaDocumentMapper,
      OaTaskMapper oaTaskMapper,
      MedicalTcmAssessmentMapper tcmAssessmentMapper,
      MedicalCvdRiskAssessmentMapper cvdRiskAssessmentMapper,
      MedicalAlertRuleConfigMapper medicalAlertRuleConfigMapper) {
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
    this.oaDocumentMapper = oaDocumentMapper;
    this.oaTaskMapper = oaTaskMapper;
    this.tcmAssessmentMapper = tcmAssessmentMapper;
    this.cvdRiskAssessmentMapper = cvdRiskAssessmentMapper;
    this.medicalAlertRuleConfigMapper = medicalAlertRuleConfigMapper;
  }

  @GetMapping("/summary")
  public Result<MedicalCareWorkbenchSummaryResponse> summary(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String date,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Integer incidentWindowDays,
      @RequestParam(required = false) Integer overdueHours,
      @RequestParam(required = false) Integer topResidentLimit,
      @RequestParam(required = false) Integer riskResidentLookbackDays) {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = parseDateOrDefault(date);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
    MedicalAlertRuleConfig ruleConfig = medicalAlertRuleConfigMapper.selectOne(
        Wrappers.lambdaQuery(MedicalAlertRuleConfig.class)
            .eq(MedicalAlertRuleConfig::getIsDeleted, 0)
            .eq(orgId != null, MedicalAlertRuleConfig::getOrgId, orgId)
            .last("LIMIT 1"));
    int defaultOverdueHours = ruleConfig == null || ruleConfig.getOverdueHoursThreshold() == null
        ? 12
        : Math.max(ruleConfig.getOverdueHoursThreshold(), MIN_OVERDUE_HOURS);
    int resolvedIncidentWindowDays = resolveRange(
        incidentWindowDays,
        DEFAULT_INCIDENT_WINDOW_DAYS,
        MIN_INCIDENT_WINDOW_DAYS,
        MAX_INCIDENT_WINDOW_DAYS);
    int resolvedOverdueHours = resolveRange(overdueHours, defaultOverdueHours, MIN_OVERDUE_HOURS, MAX_OVERDUE_HOURS);
    int resolvedTopResidentLimit = resolveRange(
        topResidentLimit,
        DEFAULT_TOP_RESIDENT_LIMIT,
        MIN_TOP_RESIDENT_LIMIT,
        MAX_TOP_RESIDENT_LIMIT);
    int resolvedRiskResidentLookbackDays = resolveRange(
        riskResidentLookbackDays,
        DEFAULT_RISK_RESIDENT_LOOKBACK_DAYS,
        MIN_RISK_RESIDENT_LOOKBACK_DAYS,
        MAX_RISK_RESIDENT_LOOKBACK_DAYS);

    LocalDateTime incidentWindowStartAt = now.minusDays(resolvedIncidentWindowDays);
    LocalDateTime careOverdueCutoff = now.minusHours(resolvedOverdueHours);
    LocalDate incidentWindowStartDate = today.minusDays(Math.max(0, resolvedIncidentWindowDays - 1L));
    LocalDate riskResidentStartDate = today.minusDays(Math.max(0, resolvedRiskResidentLookbackDays - 1L));
    boolean hasStatus = status != null && !status.isBlank();

    MedicalCareWorkbenchSummaryResponse response = new MedicalCareWorkbenchSummaryResponse();
    response.setConfiguredIncidentWindowDays(resolvedIncidentWindowDays);
    response.setConfiguredOverdueHours(resolvedOverdueHours);
    response.setConfiguredTopResidentLimit(resolvedTopResidentLimit);
    response.setConfiguredRiskResidentLookbackDays(resolvedRiskResidentLookbackDays);
    response.setSnapshotDate(today);
    response.setIncidentWindowStartDate(incidentWindowStartDate);
    response.setIncidentWindowEndDate(today);
    response.setGeneratedAt(now);

    try {
      Long pendingCare = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
          .eq(CareTaskDaily::getIsDeleted, 0)
          .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
          .eq(elderId != null, CareTaskDaily::getElderId, elderId)
          .eq(CareTaskDaily::getTaskDate, today)
          .eq(hasStatus, CareTaskDaily::getStatus, status)
          .in(!hasStatus, CareTaskDaily::getStatus, "TODO", "PENDING", "ASSIGNED"));
      response.setPendingCareTaskCount(pendingCare);

      Long overdueCare = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
          .eq(CareTaskDaily::getIsDeleted, 0)
          .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
          .eq(elderId != null, CareTaskDaily::getElderId, elderId)
          .eq(CareTaskDaily::getTaskDate, today)
          .lt(CareTaskDaily::getPlanTime, careOverdueCutoff)
          .eq(hasStatus, CareTaskDaily::getStatus, status)
          .notIn(!hasStatus, CareTaskDaily::getStatus, "DONE", "COMPLETED"));
      response.setOverdueCareTaskCount(overdueCare);

      Long inspectionPending = healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(elderId != null, HealthInspection::getElderId, elderId)
        .eq(HealthInspection::getInspectionDate, today)
        .eq(hasStatus, HealthInspection::getStatus, status)
        .in(!hasStatus, HealthInspection::getStatus, "FOLLOWING", "ABNORMAL"));
      response.setTodayInspectionPendingCount(inspectionPending);
      response.setTodayInspectionTodoCount(inspectionPending);

      Long inspectionDone = healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(elderId != null, HealthInspection::getElderId, elderId)
        .eq(HealthInspection::getInspectionDate, today)
        .eq(hasStatus, HealthInspection::getStatus, status)
        .in(!hasStatus, HealthInspection::getStatus, "NORMAL", "CLOSED"));
      response.setTodayInspectionDoneCount(inspectionDone);
      response.setTodayInspectionPlanCount(inspectionPending + inspectionDone);
      response.setInspectionCompletionRate(toPercent(inspectionDone, inspectionPending + inspectionDone));

      Long abnormalInspection = healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
          .eq(HealthInspection::getIsDeleted, 0)
          .eq(orgId != null, HealthInspection::getOrgId, orgId)
          .eq(elderId != null, HealthInspection::getElderId, elderId)
          .eq(HealthInspection::getInspectionDate, today)
          .eq(HealthInspection::getStatus, "ABNORMAL"));
      response.setAbnormalInspectionCount(abnormalInspection);
      Long abnormalInspectionOpen = healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
          .eq(HealthInspection::getIsDeleted, 0)
          .eq(orgId != null, HealthInspection::getOrgId, orgId)
          .eq(elderId != null, HealthInspection::getElderId, elderId)
          .in(HealthInspection::getStatus, "ABNORMAL", "FOLLOWING"));
      response.setAbnormalInspectionOpenCount(abnormalInspectionOpen);

      Long medicationPending = healthMedicationTaskMapper.selectCount(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
        .eq(elderId != null, HealthMedicationTask::getElderId, elderId)
        .eq(HealthMedicationTask::getTaskDate, today)
        .eq(hasStatus, HealthMedicationTask::getStatus, status)
        .eq(!hasStatus, HealthMedicationTask::getStatus, "PENDING"));
      response.setTodayMedicationPendingCount(medicationPending);

      Long medicationDone = healthMedicationTaskMapper.selectCount(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
        .eq(elderId != null, HealthMedicationTask::getElderId, elderId)
        .eq(HealthMedicationTask::getTaskDate, today)
        .eq(hasStatus, HealthMedicationTask::getStatus, status)
        .eq(!hasStatus, HealthMedicationTask::getStatus, "DONE"));
      response.setTodayMedicationDoneCount(medicationDone);

    Long incidentOpen = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(elderId != null, IncidentReport::getElderId, elderId)
        .ne(IncidentReport::getStatus, "CLOSED"));

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
    Long unclosedAbnormal = abnormalInspectionOpen + incidentOpen;
    response.setUnclosedAbnormalCount(unclosedAbnormal);

    // Card B
    Long abnormalVital24h = healthDataRecordMapper.selectCount(Wrappers.lambdaQuery(HealthDataRecord.class)
        .eq(HealthDataRecord::getIsDeleted, 0)
        .eq(orgId != null, HealthDataRecord::getOrgId, orgId)
        .eq(elderId != null, HealthDataRecord::getElderId, elderId)
        .eq(HealthDataRecord::getAbnormalFlag, 1)
        .ge(HealthDataRecord::getMeasuredAt, now.minusHours(24)));
    response.setAbnormalVital24hCount(abnormalVital24h);
    Long abnormalEvent24h = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(elderId != null, IncidentReport::getElderId, elderId)
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
        .eq(elderId != null, HealthMedicationTask::getElderId, elderId)
        .eq(HealthMedicationTask::getTaskDate, today)
        .eq(hasStatus, HealthMedicationTask::getStatus, status)
        .notIn(!hasStatus, HealthMedicationTask::getStatus, "PENDING", "DONE"));
    response.setMedicalOrderAbnormalCount(medAbnormal);
    response.setOrderCheckRate(medShould == 0 ? 0D : (double) medicationDone * 100D / medShould);
    response.setMedicalOrderExecutionRate(toPercent(medicationDone, medShould));

    response.setMedicationShouldCount(medShould);
    response.setMedicationDoneCount(medicationDone);
    response.setMedicationUndoneCount(medicationPending);
    response.setMedicationLowStockCount(0L);
    response.setMedicationRequestPendingCount(0L);

    // Card E
    Long careDone = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(elderId != null, CareTaskDaily::getElderId, elderId)
        .eq(CareTaskDaily::getTaskDate, today)
        .eq(hasStatus, CareTaskDaily::getStatus, status)
        .in(!hasStatus, CareTaskDaily::getStatus, "DONE", "COMPLETED"));
    Long careShould = pendingCare + careDone;
    response.setCareTaskShouldCount(careShould);
    response.setCareTaskDoneCount(careDone);
    response.setCareTaskOverdueCount(overdueCare);
    response.setCareTaskCompletionRate(toPercent(careDone, careShould));
    Long scanDone = careTaskExecuteLogMapper.selectCount(Wrappers.lambdaQuery(CareTaskExecuteLog.class)
        .eq(CareTaskExecuteLog::getIsDeleted, 0)
        .eq(orgId != null, CareTaskExecuteLog::getOrgId, orgId)
        .eq(elderId != null, CareTaskExecuteLog::getElderId, elderId)
        .eq(CareTaskExecuteLog::getResultStatus, 1)
        .isNotNull(CareTaskExecuteLog::getBedQrCode)
        .ge(CareTaskExecuteLog::getExecuteTime, startOfDay)
        .lt(CareTaskExecuteLog::getExecuteTime, startOfTomorrow));
    response.setScanExecuteRate(careDone == 0 ? 0D : (double) scanDone * 100D / careDone);

    // Card F
    Long nursingLogPending = healthNursingLogMapper.selectCount(Wrappers.lambdaQuery(HealthNursingLog.class)
        .eq(HealthNursingLog::getIsDeleted, 0)
        .eq(orgId != null, HealthNursingLog::getOrgId, orgId)
        .eq(elderId != null, HealthNursingLog::getElderId, elderId)
        .eq(hasStatus, HealthNursingLog::getStatus, status)
        .eq(!hasStatus, HealthNursingLog::getStatus, "PENDING"));
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
    Long incident30d = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(elderId != null, IncidentReport::getElderId, elderId)
        .ge(IncidentReport::getIncidentTime, incidentWindowStartAt));
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
    Long aiGeneratedCount = oaDocumentMapper.selectCount(Wrappers.lambdaQuery(OaDocument.class)
        .eq(OaDocument::getIsDeleted, 0)
        .eq(orgId != null, OaDocument::getOrgId, orgId)
        .eq(OaDocument::getFolder, "AI_HEALTH_REPORT"));
    Long aiPublishedCount = oaDocumentMapper.selectCount(Wrappers.lambdaQuery(OaDocument.class)
        .eq(OaDocument::getIsDeleted, 0)
        .eq(orgId != null, OaDocument::getOrgId, orgId)
        .eq(OaDocument::getFolder, "AI_HEALTH_REPORT")
        .like(OaDocument::getRemark, "\"status\":\"PUBLISHED\""));
    response.setAiReportGeneratedCount(aiGeneratedCount);
    response.setAiReportPublishedCount(aiPublishedCount);

    List<MedicalCvdRiskAssessment> topList = cvdRiskAssessmentMapper.selectList(Wrappers.lambdaQuery(MedicalCvdRiskAssessment.class)
        .eq(MedicalCvdRiskAssessment::getIsDeleted, 0)
        .eq(orgId != null, MedicalCvdRiskAssessment::getOrgId, orgId)
        .eq(elderId != null, MedicalCvdRiskAssessment::getElderId, elderId)
        .ge(MedicalCvdRiskAssessment::getAssessmentDate, riskResidentStartDate)
        .in(MedicalCvdRiskAssessment::getRiskLevel, "VERY_HIGH", "HIGH")
        .orderByDesc(MedicalCvdRiskAssessment::getAssessmentDate)
        .last("LIMIT " + Math.max(20, resolvedTopResidentLimit * 4)));

    Map<Long, MedicalCareWorkbenchSummaryResponse.ResidentRiskItem> deduplicated = new LinkedHashMap<>();
    for (MedicalCvdRiskAssessment item : topList) {
      Long riskElderId = item.getElderId();
      if (riskElderId == null || deduplicated.containsKey(riskElderId)) {
        continue;
      }
      MedicalCareWorkbenchSummaryResponse.ResidentRiskItem riskItem = new MedicalCareWorkbenchSummaryResponse.ResidentRiskItem();
      riskItem.setElderId(riskElderId);
      riskItem.setElderName(item.getElderName());
      riskItem.setRiskLevel(item.getRiskLevel());
      riskItem.setKeyRiskFactors(item.getKeyRiskFactors());
      riskItem.setAssessmentDate(item.getAssessmentDate() == null ? null : item.getAssessmentDate().toString());
      deduplicated.put(riskElderId, riskItem);
      if (deduplicated.size() >= resolvedTopResidentLimit) {
        break;
      }
    }
    response.setKeyResidents(deduplicated.values().stream().toList());
    response.setTopRiskResidentCount((long) response.getKeyResidents().size());

    response.setCareTaskOverdueRate(toPercent(response.getCareTaskOverdueCount(), response.getCareTaskShouldCount()));
    response.setMedicationPendingRate(toPercent(response.getMedicationUndoneCount(), response.getMedicationShouldCount()));
    response.setInspectionAbnormalRate(toPercent(response.getAbnormalInspectionCount(), response.getTodayInspectionPlanCount()));

    List<String> riskSignals = new ArrayList<>();
    int riskIndex = 0;

    int careOverdueScore = scoreByValue(response.getCareTaskOverdueRate(), 8, 15, 25, 35, 8, 16, 24, 32);
    riskIndex += careOverdueScore;
    if (careOverdueScore > 0) {
      riskSignals.add(String.format("护理任务超时率 %.2f%%（阈值 %d 小时）", response.getCareTaskOverdueRate(), resolvedOverdueHours));
    }

    int medicationPendingScore = scoreByValue(response.getMedicationPendingRate(), 10, 20, 35, 50, 6, 12, 18, 24);
    riskIndex += medicationPendingScore;
    if (medicationPendingScore > 0) {
      riskSignals.add(String.format("医嘱待执行率 %.2f%%（待执行 %d）", response.getMedicationPendingRate(), response.getMedicationUndoneCount()));
    }

    int unclosedAbnormalScore = scoreByValue(response.getUnclosedAbnormalCount(), 3, 8, 15, 25, 6, 12, 18, 24);
    riskIndex += unclosedAbnormalScore;
    if (unclosedAbnormalScore > 0) {
      riskSignals.add(String.format("未闭环异常 %d 条（巡检异常 + 事件）", response.getUnclosedAbnormalCount()));
    }

    int incidentScore = scoreByValue(response.getIncident30dRate(), 1.5, 3, 5, 8, 6, 12, 18, 24);
    riskIndex += incidentScore;
    if (incidentScore > 0) {
      riskSignals.add(String.format("近%d天事件发生率 %.2f%%", resolvedIncidentWindowDays, response.getIncident30dRate()));
    }

    if (response.getRectifyOverdueCount() != null && response.getRectifyOverdueCount() > 0) {
      int rectifyOverdueScore = (int) Math.min(12, response.getRectifyOverdueCount() * 3);
      riskIndex += rectifyOverdueScore;
      riskSignals.add(String.format("整改任务逾期 %d 条", response.getRectifyOverdueCount()));
    }

    if (response.getTopRiskResidentCount() != null && response.getTopRiskResidentCount() > 0) {
      int highRiskResidentScore = (int) Math.min(10, response.getTopRiskResidentCount() * 2);
      riskIndex += highRiskResidentScore;
      riskSignals.add(String.format("高风险长者 %d 人（近%d天）", response.getTopRiskResidentCount(), resolvedRiskResidentLookbackDays));
    }

    int safeRiskIndex = Math.max(0, Math.min(100, riskIndex));
    response.setRiskIndex(safeRiskIndex);
    response.setRiskLevel(resolveRiskLevel(safeRiskIndex));
    response.setRiskTriggeredCount((long) riskSignals.size());
    response.setRiskSignals(riskSignals);

    List<MedicalCareWorkbenchSummaryResponse.FocusActionItem> focusActions = new ArrayList<>();
    if (overdueCare > 0) {
      addFocusAction(
          focusActions,
          "care-overdue",
          "处理超时护理任务",
          "优先关闭已超过阈值的护理执行项，降低护理风险暴露。",
          "/medical-care/care-task-board?status=OVERDUE",
          "HIGH",
          overdueCare);
    }
    if (medicationPending > 0) {
      addFocusAction(
          focusActions,
          "order-pending",
          "推进待执行医嘱",
          "聚焦未执行医嘱，减少医嘱滞后和用药延迟。",
          "/medical-care/orders?status=PENDING",
          "HIGH",
          medicationPending);
    }
    if (abnormalInspectionOpen > 0) {
      addFocusAction(
          focusActions,
          "inspection-open",
          "闭环异常巡检",
          "跟进 ABNORMAL/FOLLOWING 巡检记录，避免异常长期挂起。",
          "/medical-care/inspection?status=ABNORMAL",
          "HIGH",
          abnormalInspectionOpen);
    }
    if (incidentOpen > 0) {
      addFocusAction(
          focusActions,
          "incident-open",
          "跟踪未结案事件",
          "事件工单仍有未闭环项，建议在质量中心尽快分派整改。",
          "/medical-care/nursing-quality?tab=quality",
          "MEDIUM",
          incidentOpen);
    }
    if (response.getTopRiskResidentCount() != null && response.getTopRiskResidentCount() > 0) {
      addFocusAction(
          focusActions,
          "resident-high-risk",
          "核查高风险长者",
          "结合风险评估结果核对重点人群的巡检和护理计划。",
          "/medical-care/residents?status=HIGH_RISK",
          "MEDIUM",
          response.getTopRiskResidentCount());
    }
    if (rectifyOverdue > 0) {
      addFocusAction(
          focusActions,
          "rectify-overdue",
          "处理逾期整改任务",
          "整改任务已逾期，建议同步 OA 任务节点与责任人。",
          "/oa/task?keyword=整改&status=TODO",
          "HIGH",
          rectifyOverdue);
    }
    if (focusActions.isEmpty()) {
      addFocusAction(
          focusActions,
          "routine-review",
          "查看统一任务中心",
          "当前核心风险指标平稳，建议按班次完成例行核对。",
          "/medical-care/unified-task-center",
          "LOW",
          0L);
    }
    response.setFocusActions(focusActions);
    response.setFocusActionCount(focusActions.size());
    } catch (Exception exception) {
      log.error(
          "Medical care summary fallback with default values. orgId={}, elderId={}, date={}, status={}, incidentWindowDays={}, overdueHours={}, topResidentLimit={}, riskResidentLookbackDays={}",
          orgId,
          elderId,
          date,
          status,
          incidentWindowDays,
          overdueHours,
          topResidentLimit,
          riskResidentLookbackDays,
          exception);
    }

    return Result.ok(response);
  }

  @GetMapping("/risk-timeline")
  public Result<List<MedicalRiskTimelinePointResponse>> riskTimeline(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String date,
      @RequestParam(required = false) Integer windowDays,
      @RequestParam(required = false) Integer overdueHours) {
    Long orgId = AuthContext.getOrgId();
    LocalDate endDate = parseDateOrDefault(date);
    int resolvedWindowDays = resolveRange(
        windowDays,
        DEFAULT_RISK_TIMELINE_DAYS,
        MIN_RISK_TIMELINE_DAYS,
        MAX_RISK_TIMELINE_DAYS);
    MedicalAlertRuleConfig ruleConfig = medicalAlertRuleConfigMapper.selectOne(
        Wrappers.lambdaQuery(MedicalAlertRuleConfig.class)
            .eq(MedicalAlertRuleConfig::getIsDeleted, 0)
            .eq(orgId != null, MedicalAlertRuleConfig::getOrgId, orgId)
            .last("LIMIT 1"));
    int defaultOverdueHours = ruleConfig == null || ruleConfig.getOverdueHoursThreshold() == null
        ? 12
        : Math.max(ruleConfig.getOverdueHoursThreshold(), MIN_OVERDUE_HOURS);
    int resolvedOverdueHours = resolveRange(overdueHours, defaultOverdueHours, MIN_OVERDUE_HOURS, MAX_OVERDUE_HOURS);

    LocalDate startDate = endDate.minusDays(Math.max(0, resolvedWindowDays - 1L));
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();
    List<MedicalRiskTimelinePointResponse> points = new ArrayList<>(resolvedWindowDays);
    for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
      LocalDateTime startOfDay = day.atStartOfDay();
      LocalDateTime startOfNextDay = day.plusDays(1).atStartOfDay();
      LocalDateTime timelineCutoff = day.isAfter(today)
          ? startOfDay
          : (day.isEqual(today) ? now : startOfNextDay);
      if (timelineCutoff.isBefore(startOfDay)) {
        timelineCutoff = startOfDay;
      }
      LocalDateTime dayOverdueCutoff = timelineCutoff.minusHours(resolvedOverdueHours);

      Long medicationPendingCount = healthMedicationTaskMapper.selectCount(Wrappers.lambdaQuery(HealthMedicationTask.class)
          .eq(HealthMedicationTask::getIsDeleted, 0)
          .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
          .eq(elderId != null, HealthMedicationTask::getElderId, elderId)
          .eq(HealthMedicationTask::getTaskDate, day)
          .eq(HealthMedicationTask::getStatus, "PENDING"));

      Long inspectionOpenCount = healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
          .eq(HealthInspection::getIsDeleted, 0)
          .eq(orgId != null, HealthInspection::getOrgId, orgId)
          .eq(elderId != null, HealthInspection::getElderId, elderId)
          .eq(HealthInspection::getInspectionDate, day)
          .in(HealthInspection::getStatus, "ABNORMAL", "FOLLOWING"));

      Long careOverdueCount = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
          .eq(CareTaskDaily::getIsDeleted, 0)
          .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
          .eq(elderId != null, CareTaskDaily::getElderId, elderId)
          .eq(CareTaskDaily::getTaskDate, day)
          .lt(CareTaskDaily::getPlanTime, dayOverdueCutoff)
          .notIn(CareTaskDaily::getStatus, "DONE", "COMPLETED"));

      Long incidentCount = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
          .eq(IncidentReport::getIsDeleted, 0)
          .eq(orgId != null, IncidentReport::getOrgId, orgId)
          .eq(elderId != null, IncidentReport::getElderId, elderId)
          .ge(IncidentReport::getIncidentTime, startOfDay)
          .lt(IncidentReport::getIncidentTime, startOfNextDay));

      Long rectifyOverdueCount = oaTaskMapper.selectCount(Wrappers.lambdaQuery(OaTask.class)
          .eq(OaTask::getIsDeleted, 0)
          .eq(orgId != null, OaTask::getOrgId, orgId)
          .like(OaTask::getTitle, "整改")
          .in(OaTask::getStatus, "TODO", "DOING")
          .isNotNull(OaTask::getEndTime)
          .ge(OaTask::getEndTime, startOfDay)
          .lt(OaTask::getEndTime, timelineCutoff));

      int riskScore = 0;
      riskScore += scoreByValue(medicationPendingCount, 4, 8, 15, 25, 8, 16, 24, 32);
      riskScore += scoreByValue(inspectionOpenCount, 2, 5, 10, 15, 8, 16, 24, 32);
      riskScore += scoreByValue(careOverdueCount, 3, 8, 15, 25, 8, 16, 24, 32);
      riskScore += scoreByValue(incidentCount, 1, 3, 5, 8, 8, 16, 24, 32);
      riskScore += scoreByValue(rectifyOverdueCount, 1, 3, 5, 8, 6, 12, 18, 24);
      int safeRiskScore = Math.max(0, Math.min(100, riskScore));

      MedicalRiskTimelinePointResponse point = new MedicalRiskTimelinePointResponse();
      point.setDate(day);
      point.setOverdueHours(resolvedOverdueHours);
      point.setMedicationPendingCount(medicationPendingCount);
      point.setInspectionOpenCount(inspectionOpenCount);
      point.setCareOverdueCount(careOverdueCount);
      point.setIncidentCount(incidentCount);
      point.setRectifyOverdueCount(rectifyOverdueCount);
      point.setRiskScore(safeRiskScore);
      point.setRiskLevel(resolveRiskLevel(safeRiskScore));
      points.add(point);
    }
    return Result.ok(points);
  }

  @GetMapping("/unified-tasks")
  public Result<IPage<MedicalUnifiedTaskItemResponse>> unifiedTasks(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String module,
      @RequestParam(required = false) String priority,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer overdueHours,
      @RequestParam(required = false) Boolean onlyOverdue,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDirection) {
    Long orgId = AuthContext.getOrgId();
    LocalDateTime now = LocalDateTime.now();
    MedicalAlertRuleConfig ruleConfig = medicalAlertRuleConfigMapper.selectOne(Wrappers.lambdaQuery(MedicalAlertRuleConfig.class)
        .eq(MedicalAlertRuleConfig::getIsDeleted, 0)
        .eq(orgId != null, MedicalAlertRuleConfig::getOrgId, orgId)
        .last("LIMIT 1"));
    int overdueHoursThreshold = ruleConfig == null || ruleConfig.getOverdueHoursThreshold() == null
        ? 12 : Math.max(ruleConfig.getOverdueHoursThreshold(), 1);
    int resolvedOverdueHours = resolveRange(overdueHours, overdueHoursThreshold, MIN_OVERDUE_HOURS, MAX_OVERDUE_HOURS);
    LocalDateTime overdueCutoff = now.minusHours(resolvedOverdueHours);
    String moduleFilter = normalizeEnumFilter(module);
    String priorityFilter = normalizeEnumFilter(priority);
    String statusFilter = normalizeEnumFilter(status);
    String keywordFilter = normalizeText(keyword);
    String sortByFilter = normalizeEnumFilter(sortBy);
    String sortDirectionFilter = normalizeEnumFilter(sortDirection);
    boolean onlyOverdueFilter = Boolean.TRUE.equals(onlyOverdue);
    boolean raiseTaskFromAbnormal = ruleConfig == null || !Integer.valueOf(0).equals(ruleConfig.getAutoRaiseTaskFromAbnormal());
    List<String> handoverRiskKeywords = parseKeywords(ruleConfig == null ? null : ruleConfig.getHandoverRiskKeywords());

    List<MedicalUnifiedTaskItemResponse> rows = new ArrayList<>();

    List<HealthMedicationTask> medicationTasks = healthMedicationTaskMapper.selectList(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
        .eq(elderId != null, HealthMedicationTask::getElderId, elderId)
        .eq(HealthMedicationTask::getStatus, "PENDING")
        .orderByAsc(HealthMedicationTask::getPlannedTime)
        .last("LIMIT 300"));
    for (HealthMedicationTask item : medicationTasks) {
      MedicalUnifiedTaskItemResponse row = new MedicalUnifiedTaskItemResponse();
      row.setId("ORDER_" + item.getId());
      row.setModule("ORDER");
      row.setResidentId(item.getElderId());
      row.setResidentName(item.getElderName());
      row.setTaskTitle("执行医嘱：" + safeText(item.getDrugName(), "未命名药品"));
      row.setAssignee(null);
      row.setPlannedTime(item.getPlannedTime());
      row.setPriority(item.getPlannedTime() != null && item.getPlannedTime().isBefore(overdueCutoff)
          ? "HIGH"
          : (raiseTaskFromAbnormal ? "MEDIUM" : "LOW"));
      row.setStatus(item.getStatus());
      row.setSourceId(item.getId());
      row.setSuggestedRoute(buildUnifiedTaskRoute(row.getModule(), row.getResidentId(), row.getSourceId(), row.getStatus()));
      enrichUnifiedTaskRow(row, now, overdueCutoff);
      rows.add(row);
    }

    List<HealthInspection> inspectionTasks = healthInspectionMapper.selectList(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(elderId != null, HealthInspection::getElderId, elderId)
        .in(HealthInspection::getStatus, "ABNORMAL", "FOLLOWING")
        .orderByDesc(HealthInspection::getInspectionDate)
        .last("LIMIT 300"));
    for (HealthInspection item : inspectionTasks) {
      MedicalUnifiedTaskItemResponse row = new MedicalUnifiedTaskItemResponse();
      row.setId("INSPECTION_" + item.getId());
      row.setModule("INSPECTION");
      row.setResidentId(item.getElderId());
      row.setResidentName(item.getElderName());
      row.setTaskTitle("巡检跟进：" + safeText(item.getInspectionItem(), "异常巡检"));
      row.setAssignee(item.getInspectorName());
      row.setPlannedTime(item.getInspectionDate() == null ? null : item.getInspectionDate().atStartOfDay().plusHours(9));
      row.setPriority("ABNORMAL".equals(item.getStatus()) && raiseTaskFromAbnormal ? "HIGH" : "MEDIUM");
      row.setStatus(item.getStatus());
      row.setSourceId(item.getId());
      row.setSuggestedRoute(buildUnifiedTaskRoute(row.getModule(), row.getResidentId(), row.getSourceId(), row.getStatus()));
      enrichUnifiedTaskRow(row, now, overdueCutoff);
      rows.add(row);
    }

    List<HealthNursingLog> nursingTasks = healthNursingLogMapper.selectList(Wrappers.lambdaQuery(HealthNursingLog.class)
        .eq(HealthNursingLog::getIsDeleted, 0)
        .eq(orgId != null, HealthNursingLog::getOrgId, orgId)
        .eq(elderId != null, HealthNursingLog::getElderId, elderId)
        .eq(HealthNursingLog::getStatus, "PENDING")
        .orderByDesc(HealthNursingLog::getLogTime)
        .last("LIMIT 300"));
    for (HealthNursingLog item : nursingTasks) {
      MedicalUnifiedTaskItemResponse row = new MedicalUnifiedTaskItemResponse();
      row.setId("NURSING_LOG_" + item.getId());
      row.setModule("NURSING_LOG");
      row.setResidentId(item.getElderId());
      row.setResidentName(item.getElderName());
      row.setTaskTitle("日志待处理：" + safeText(item.getContent(), "护理日志"));
      row.setAssignee(item.getStaffName());
      row.setPlannedTime(item.getLogTime());
      row.setPriority("INCIDENT".equals(item.getLogType()) && raiseTaskFromAbnormal ? "HIGH" : "MEDIUM");
      row.setStatus(item.getStatus());
      row.setSourceId(item.getSourceInspectionId());
      row.setSuggestedRoute(buildUnifiedTaskRoute(row.getModule(), row.getResidentId(), row.getSourceId(), row.getStatus()));
      enrichUnifiedTaskRow(row, now, overdueCutoff);
      rows.add(row);
    }

    List<ShiftHandover> handoverTasks = shiftHandoverMapper.selectList(Wrappers.lambdaQuery(ShiftHandover.class)
        .eq(ShiftHandover::getIsDeleted, 0)
        .eq(orgId != null, ShiftHandover::getOrgId, orgId)
        .in(ShiftHandover::getStatus, "DRAFT", "HANDED_OVER")
        .orderByDesc(ShiftHandover::getDutyDate)
        .last("LIMIT 300"));
    for (ShiftHandover item : handoverTasks) {
      MedicalUnifiedTaskItemResponse row = new MedicalUnifiedTaskItemResponse();
      row.setId("HANDOVER_" + item.getId());
      row.setModule("HANDOVER");
      row.setTaskTitle("交接待确认：" + safeText(item.getShiftCode(), "-"));
      row.setAssignee(item.getToStaffId() == null ? null : ("人员#" + item.getToStaffId()));
      String handoverRiskText = safeText(item.getRiskNote(), "") + " " + safeText(item.getTodoNote(), "");
      boolean matchedHandoverRisk = handoverRiskKeywords.stream().anyMatch(handoverRiskText::contains);
      row.setPlannedTime(item.getHandoverTime() != null
          ? item.getHandoverTime()
          : (item.getDutyDate() == null ? null : item.getDutyDate().atStartOfDay().plusHours(20)));
      row.setPriority(matchedHandoverRisk && raiseTaskFromAbnormal
          ? "HIGH"
          : (row.getPlannedTime() != null && row.getPlannedTime().isBefore(overdueCutoff)
          ? "HIGH"
          : ("HANDED_OVER".equals(item.getStatus()) ? "MEDIUM" : "LOW")));
      row.setStatus(item.getStatus());
      row.setSourceId(item.getId());
      row.setSuggestedRoute(buildUnifiedTaskRoute(row.getModule(), row.getResidentId(), row.getSourceId(), row.getStatus()));
      enrichUnifiedTaskRow(row, now, overdueCutoff);
      rows.add(row);
    }

    Comparator<MedicalUnifiedTaskItemResponse> comparator = resolveUnifiedTaskComparator(sortByFilter, sortDirectionFilter);
    List<MedicalUnifiedTaskItemResponse> filtered = rows.stream()
        .filter(item -> moduleFilter == null || moduleFilter.equalsIgnoreCase(item.getModule()))
        .filter(item -> priorityFilter == null || priorityFilter.equalsIgnoreCase(item.getPriority()))
        .filter(item -> statusFilter == null || statusFilter.equalsIgnoreCase(safeText(item.getStatus(), "")))
        .filter(item -> !onlyOverdueFilter || Boolean.TRUE.equals(item.getOverdue()))
        .filter(item -> matchKeyword(item, keywordFilter))
        .sorted(comparator)
        .toList();

    long safePageNo = Math.max(pageNo, 1);
    long safePageSize = Math.max(pageSize, 1);
    int from = (int) Math.min((safePageNo - 1) * safePageSize, filtered.size());
    int to = (int) Math.min(from + safePageSize, filtered.size());
    List<MedicalUnifiedTaskItemResponse> pageRecords = filtered.subList(from, to);
    IPage<MedicalUnifiedTaskItemResponse> page = new Page<>(safePageNo, safePageSize, filtered.size());
    page.setRecords(pageRecords);
    return Result.ok(page);
  }

  private Comparator<MedicalUnifiedTaskItemResponse> resolveUnifiedTaskComparator(String sortBy, String sortDirection) {
    Comparator<MedicalUnifiedTaskItemResponse> comparator;
    if ("PRIORITY".equals(sortBy)) {
      comparator = Comparator
          .comparing((MedicalUnifiedTaskItemResponse item) -> priorityWeight(item.getPriority()))
          .thenComparing(MedicalUnifiedTaskItemResponse::getPlannedTime, Comparator.nullsLast(Comparator.naturalOrder()));
    } else if ("RISK_SCORE".equals(sortBy) || "RISK".equals(sortBy)) {
      comparator = Comparator
          .comparing((MedicalUnifiedTaskItemResponse item) -> safeInt(item.getRiskScore()))
          .thenComparing(MedicalUnifiedTaskItemResponse::getPlannedTime, Comparator.nullsLast(Comparator.naturalOrder()));
    } else {
      comparator = Comparator
          .comparing(MedicalUnifiedTaskItemResponse::getPlannedTime, Comparator.nullsLast(Comparator.naturalOrder()))
          .thenComparing(
              (MedicalUnifiedTaskItemResponse item) -> priorityWeight(item.getPriority()),
              Comparator.reverseOrder());
    }
    boolean desc = "DESC".equals(sortDirection);
    if (sortDirection == null || sortDirection.isBlank()) {
      desc = "PRIORITY".equals(sortBy) || "RISK_SCORE".equals(sortBy) || "RISK".equals(sortBy);
    }
    return desc ? comparator.reversed() : comparator;
  }

  private void enrichUnifiedTaskRow(
      MedicalUnifiedTaskItemResponse row,
      LocalDateTime now,
      LocalDateTime overdueCutoff) {
    LocalDateTime plannedTime = row.getPlannedTime();
    boolean overdue = plannedTime != null && plannedTime.isBefore(now);
    long overdueMinutes = overdue ? Math.max(0L, Duration.between(plannedTime, now).toMinutes()) : 0L;
    row.setOverdue(overdue);
    row.setOverdueMinutes(overdueMinutes);
    int riskScore = resolveUnifiedTaskRiskScore(row, overdue, overdueCutoff);
    row.setRiskScore(riskScore);
    row.setRiskReason(resolveUnifiedTaskRiskReason(row, overdue, overdueMinutes, overdueCutoff));
  }

  private int resolveUnifiedTaskRiskScore(
      MedicalUnifiedTaskItemResponse row,
      boolean overdue,
      LocalDateTime overdueCutoff) {
    int score;
    String priority = safeText(row.getPriority(), "");
    if ("HIGH".equalsIgnoreCase(priority)) {
      score = 65;
    } else if ("MEDIUM".equalsIgnoreCase(priority)) {
      score = 45;
    } else {
      score = 25;
    }
    if (overdue) {
      score += 20;
    }
    if (row.getPlannedTime() != null && row.getPlannedTime().isBefore(overdueCutoff)) {
      score += 8;
    }
    if ("INSPECTION".equalsIgnoreCase(row.getModule()) && "ABNORMAL".equalsIgnoreCase(row.getStatus())) {
      score += 12;
    }
    if ("ORDER".equalsIgnoreCase(row.getModule()) && "PENDING".equalsIgnoreCase(row.getStatus())) {
      score += 5;
    }
    if ("HANDOVER".equalsIgnoreCase(row.getModule()) && !"CONFIRMED".equalsIgnoreCase(row.getStatus())) {
      score += 6;
    }
    return Math.max(0, Math.min(100, score));
  }

  private String resolveUnifiedTaskRiskReason(
      MedicalUnifiedTaskItemResponse row,
      boolean overdue,
      long overdueMinutes,
      LocalDateTime overdueCutoff) {
    List<String> reasons = new ArrayList<>();
    if ("HIGH".equalsIgnoreCase(row.getPriority())) {
      reasons.add("高优先级任务");
    } else if ("MEDIUM".equalsIgnoreCase(row.getPriority())) {
      reasons.add("中优先级任务");
    } else {
      reasons.add("低优先级任务");
    }
    if (overdue && overdueMinutes > 0) {
      reasons.add("已超时 " + overdueMinutes + " 分钟");
    }
    if (row.getPlannedTime() != null && row.getPlannedTime().isBefore(overdueCutoff)) {
      reasons.add("超过策略超时阈值");
    }
    if ("INSPECTION".equalsIgnoreCase(row.getModule()) && "ABNORMAL".equalsIgnoreCase(row.getStatus())) {
      reasons.add("异常巡检待闭环");
    }
    if ("ORDER".equalsIgnoreCase(row.getModule()) && "PENDING".equalsIgnoreCase(row.getStatus())) {
      reasons.add("医嘱仍待执行");
    }
    if ("HANDOVER".equalsIgnoreCase(row.getModule()) && !"CONFIRMED".equalsIgnoreCase(row.getStatus())) {
      reasons.add("交接待确认");
    }
    return String.join("；", reasons);
  }

  private String buildUnifiedTaskRoute(String module, Long residentId, Long sourceId, String status) {
    String route;
    if ("ORDER".equalsIgnoreCase(module)) {
      route = "/medical-care/orders?filter=to_execute";
      route = appendQueryParam(route, "elderId", residentId);
      return route;
    }
    if ("INSPECTION".equalsIgnoreCase(module)) {
      route = "/medical-care/inspection";
      route = appendQueryParam(route, "inspectionId", sourceId);
      route = appendQueryParam(route, "residentId", residentId);
      return route;
    }
    if ("NURSING_LOG".equalsIgnoreCase(module)) {
      route = "/medical-care/nursing-log";
      route = appendQueryParam(route, "sourceInspectionId", sourceId);
      route = appendQueryParam(route, "residentId", residentId);
      return route;
    }
    if ("HANDOVER".equalsIgnoreCase(module)) {
      route = "/medical-care/handovers";
      route = appendQueryParam(route, "id", sourceId);
      route = appendQueryParam(route, "status", normalizeText(status));
      return route;
    }
    return "/medical-care/unified-task-center";
  }

  private String appendQueryParam(String route, String key, Object value) {
    if (value == null) {
      return route;
    }
    String text = String.valueOf(value);
    if (text.isBlank()) {
      return route;
    }
    return route + (route.contains("?") ? "&" : "?") + key + "=" + text;
  }

  private int priorityWeight(String priority) {
    if ("HIGH".equalsIgnoreCase(priority)) {
      return 3;
    }
    if ("MEDIUM".equalsIgnoreCase(priority)) {
      return 2;
    }
    return 1;
  }

  private int safeInt(Integer value) {
    return value == null ? 0 : value;
  }

  private void addFocusAction(
      List<MedicalCareWorkbenchSummaryResponse.FocusActionItem> actions,
      String key,
      String title,
      String description,
      String route,
      String severity,
      Long count) {
    MedicalCareWorkbenchSummaryResponse.FocusActionItem item = new MedicalCareWorkbenchSummaryResponse.FocusActionItem();
    item.setKey(key);
    item.setTitle(title);
    item.setDescription(description);
    item.setRoute(route);
    item.setSeverity(severity);
    item.setCount(count == null ? 0L : Math.max(0L, count));
    actions.add(item);
  }

  private int resolveRange(Integer input, int defaultValue, int min, int max) {
    int raw = input == null ? defaultValue : input;
    if (raw < min) {
      return min;
    }
    if (raw > max) {
      return max;
    }
    return raw;
  }

  private double toPercent(Long numerator, Long denominator) {
    long safeNumerator = numerator == null ? 0L : numerator;
    long safeDenominator = denominator == null ? 0L : denominator;
    if (safeNumerator <= 0 || safeDenominator <= 0) {
      return 0D;
    }
    return BigDecimal.valueOf(safeNumerator)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(safeDenominator), 2, RoundingMode.HALF_UP)
        .doubleValue();
  }

  private int scoreByValue(
      Number value,
      double l1,
      double l2,
      double l3,
      double l4,
      int s1,
      int s2,
      int s3,
      int s4) {
    double current = value == null ? 0D : value.doubleValue();
    if (current >= l4) {
      return s4;
    }
    if (current >= l3) {
      return s3;
    }
    if (current >= l2) {
      return s2;
    }
    if (current >= l1) {
      return s1;
    }
    return 0;
  }

  private String resolveRiskLevel(int riskIndex) {
    if (riskIndex >= 75) {
      return "CRITICAL";
    }
    if (riskIndex >= 55) {
      return "HIGH";
    }
    if (riskIndex >= 30) {
      return "MEDIUM";
    }
    return "LOW";
  }

  private LocalDate parseDateOrDefault(String date) {
    if (date == null || date.isBlank()) {
      return LocalDate.now();
    }
    try {
      return LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    } catch (DateTimeParseException ignore) {
      return LocalDate.now();
    }
  }

  private String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String normalizeEnumFilter(String value) {
    String normalized = normalizeText(value);
    return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
  }

  private String safeText(String value, String fallback) {
    String normalized = normalizeText(value);
    return normalized == null ? fallback : normalized;
  }

  private boolean matchKeyword(MedicalUnifiedTaskItemResponse item, String keyword) {
    if (keyword == null) {
      return true;
    }
    String text = (safeText(item.getResidentName(), "") + " "
        + safeText(item.getTaskTitle(), "") + " "
        + safeText(item.getAssignee(), "")).toLowerCase();
    return text.contains(keyword.toLowerCase());
  }

  private List<String> parseKeywords(String rawText) {
    String text = normalizeText(rawText);
    if (text == null) {
      return List.of("异常", "风险", "事故", "上报");
    }
    List<String> list = List.of(text.split("[,，]")).stream()
        .map(this::normalizeText)
        .filter(value -> value != null && !value.isBlank())
        .toList();
    return list.isEmpty() ? List.of("异常", "风险", "事故", "上报") : list;
  }
}
