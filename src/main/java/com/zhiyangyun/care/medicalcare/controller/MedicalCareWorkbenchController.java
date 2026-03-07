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
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = parseDateOrDefault(date);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
    LocalDateTime startOf30Days = now.minusDays(30);
    boolean hasStatus = status != null && !status.isBlank();

    MedicalCareWorkbenchSummaryResponse response = new MedicalCareWorkbenchSummaryResponse();

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
        .lt(CareTaskDaily::getPlanTime, now)
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

    Long abnormalInspection = healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(elderId != null, HealthInspection::getElderId, elderId)
        .eq(HealthInspection::getStatus, "ABNORMAL"));
    response.setAbnormalInspectionCount(abnormalInspection);

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
        .eq(elderId != null, HealthDataRecord::getElderId, elderId)
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
        .eq(elderId != null, HealthMedicationTask::getElderId, elderId)
        .eq(HealthMedicationTask::getTaskDate, today)
        .eq(hasStatus, HealthMedicationTask::getStatus, status)
        .notIn(!hasStatus, HealthMedicationTask::getStatus, "PENDING", "DONE"));
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
        .eq(elderId != null, CareTaskDaily::getElderId, elderId)
        .eq(CareTaskDaily::getTaskDate, today)
        .eq(hasStatus, CareTaskDaily::getStatus, status)
        .in(!hasStatus, CareTaskDaily::getStatus, "DONE", "COMPLETED"));
    Long careShould = pendingCare + careDone;
    response.setCareTaskShouldCount(careShould);
    response.setCareTaskDoneCount(careDone);
    response.setCareTaskOverdueCount(overdueCare);
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
    Long incidentOpen = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .ne(IncidentReport::getStatus, "CLOSED"));
    Long incident30d = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(elderId != null, IncidentReport::getElderId, elderId)
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
        .in(MedicalCvdRiskAssessment::getRiskLevel, "VERY_HIGH", "HIGH")
        .orderByDesc(MedicalCvdRiskAssessment::getAssessmentDate)
        .last("LIMIT 20"));

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
      if (deduplicated.size() >= 5) {
        break;
      }
    }
    response.setKeyResidents(deduplicated.values().stream().toList());
      response.setTopRiskResidentCount((long) response.getKeyResidents().size());
    } catch (Exception exception) {
      log.error("Medical care summary fallback with default values. orgId={}, elderId={}, date={}, status={}",
          orgId, elderId, date, status, exception);
    }

    return Result.ok(response);
  }

  @GetMapping("/unified-tasks")
  public Result<IPage<MedicalUnifiedTaskItemResponse>> unifiedTasks(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String module,
      @RequestParam(required = false) String priority,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LocalDateTime now = LocalDateTime.now();
    MedicalAlertRuleConfig ruleConfig = medicalAlertRuleConfigMapper.selectOne(Wrappers.lambdaQuery(MedicalAlertRuleConfig.class)
        .eq(MedicalAlertRuleConfig::getIsDeleted, 0)
        .eq(orgId != null, MedicalAlertRuleConfig::getOrgId, orgId)
        .last("LIMIT 1"));
    int overdueHoursThreshold = ruleConfig == null || ruleConfig.getOverdueHoursThreshold() == null
        ? 12 : Math.max(ruleConfig.getOverdueHoursThreshold(), 1);
    LocalDateTime overdueCutoff = now.minusHours(overdueHoursThreshold);
    String moduleFilter = normalizeEnumFilter(module);
    String priorityFilter = normalizeEnumFilter(priority);
    String keywordFilter = normalizeText(keyword);
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
      rows.add(row);
    }

    List<MedicalUnifiedTaskItemResponse> filtered = rows.stream()
        .filter(item -> moduleFilter == null || moduleFilter.equalsIgnoreCase(item.getModule()))
        .filter(item -> priorityFilter == null || priorityFilter.equalsIgnoreCase(item.getPriority()))
        .filter(item -> matchKeyword(item, keywordFilter))
        .sorted(Comparator.comparing(MedicalUnifiedTaskItemResponse::getPlannedTime,
            Comparator.nullsLast(Comparator.naturalOrder())))
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
