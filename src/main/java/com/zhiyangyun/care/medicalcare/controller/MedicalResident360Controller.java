package com.zhiyangyun.care.medicalcare.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.entity.CrmContractAttachment;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.mapper.CrmContractAttachmentMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischargeApply;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderMedicalOutingRecord;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderOutingRecord;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeApplyMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderMedicalOutingRecordMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderOutingRecordMapper;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.finance.entity.ElderAccount;
import com.zhiyangyun.care.finance.entity.ElderAccountLog;
import com.zhiyangyun.care.finance.mapper.ElderAccountLogMapper;
import com.zhiyangyun.care.finance.mapper.ElderAccountMapper;
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
import com.zhiyangyun.care.medicalcare.model.MedicalResidentOverviewActionResponse;
import com.zhiyangyun.care.medicalcare.model.MedicalResidentOverviewCardResponse;
import com.zhiyangyun.care.medicalcare.model.MedicalResidentOverviewResponse;
import com.zhiyangyun.care.medicalcare.model.MedicalResidentRiskCardResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
  private final ElderMapper elderMapper;
  private final ElderOutingRecordMapper outingRecordMapper;
  private final ElderMedicalOutingRecordMapper medicalOutingRecordMapper;
  private final ElderDischargeApplyMapper dischargeApplyMapper;
  private final ElderAccountMapper elderAccountMapper;
  private final ElderAccountLogMapper elderAccountLogMapper;
  private final CrmContractAttachmentMapper contractAttachmentMapper;
  private final CrmContractMapper crmContractMapper;

  public MedicalResident360Controller(
      MedicalTcmAssessmentMapper tcmAssessmentMapper,
      MedicalCvdRiskAssessmentMapper cvdRiskAssessmentMapper,
      HealthDataRecordMapper healthDataRecordMapper,
      HealthInspectionMapper healthInspectionMapper,
      IncidentReportMapper incidentReportMapper,
      CareTaskDailyMapper careTaskDailyMapper,
      ElderMapper elderMapper,
      ElderOutingRecordMapper outingRecordMapper,
      ElderMedicalOutingRecordMapper medicalOutingRecordMapper,
      ElderDischargeApplyMapper dischargeApplyMapper,
      ElderAccountMapper elderAccountMapper,
      ElderAccountLogMapper elderAccountLogMapper,
      CrmContractAttachmentMapper contractAttachmentMapper,
      CrmContractMapper crmContractMapper) {
    this.tcmAssessmentMapper = tcmAssessmentMapper;
    this.cvdRiskAssessmentMapper = cvdRiskAssessmentMapper;
    this.healthDataRecordMapper = healthDataRecordMapper;
    this.healthInspectionMapper = healthInspectionMapper;
    this.incidentReportMapper = incidentReportMapper;
    this.careTaskDailyMapper = careTaskDailyMapper;
    this.elderMapper = elderMapper;
    this.outingRecordMapper = outingRecordMapper;
    this.medicalOutingRecordMapper = medicalOutingRecordMapper;
    this.dischargeApplyMapper = dischargeApplyMapper;
    this.elderAccountMapper = elderAccountMapper;
    this.elderAccountLogMapper = elderAccountLogMapper;
    this.contractAttachmentMapper = contractAttachmentMapper;
    this.crmContractMapper = crmContractMapper;
  }

  @GetMapping("/risk-card")
  public Result<MedicalResidentRiskCardResponse> riskCard(@RequestParam Long residentId) {
    Long orgId = AuthContext.getOrgId();
    Long resolvedResidentId = resolveResidentId(orgId, residentId);
    LocalDateTime now = LocalDateTime.now();

    MedicalResidentRiskCardResponse response = new MedicalResidentRiskCardResponse();
    response.setElderId(resolvedResidentId == null ? residentId : resolvedResidentId);
    if (resolvedResidentId == null) {
      response.setElderName("未匹配长者");
      return Result.ok(response);
    }

    MedicalTcmAssessment latestTcm = tcmAssessmentMapper.selectOne(Wrappers.lambdaQuery(MedicalTcmAssessment.class)
        .eq(MedicalTcmAssessment::getIsDeleted, 0)
        .eq(orgId != null, MedicalTcmAssessment::getOrgId, orgId)
        .eq(MedicalTcmAssessment::getElderId, resolvedResidentId)
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
        .eq(MedicalCvdRiskAssessment::getElderId, resolvedResidentId)
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
        .eq(HealthDataRecord::getElderId, resolvedResidentId)
        .eq(HealthDataRecord::getAbnormalFlag, 1)
        .ge(HealthDataRecord::getMeasuredAt, now.minusHours(24))));

    response.setAbnormalInspectionOpenCount(healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(HealthInspection::getElderId, resolvedResidentId)
        .in(HealthInspection::getStatus, "ABNORMAL", "FOLLOWING")));

    response.setOpenIncidentCount(incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(IncidentReport::getElderId, resolvedResidentId)
        .ne(IncidentReport::getStatus, "CLOSED")));

    response.setPendingCareTaskCount(careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getElderId, resolvedResidentId)
        .eq(CareTaskDaily::getTaskDate, LocalDate.now())
        .in(CareTaskDaily::getStatus, "TODO", "PENDING", "ASSIGNED")));

    response.setTcmAssessmentRoute("/medical-care/assessment/tcm?residentId=" + resolvedResidentId + "&from=resident360");
    response.setCvdAssessmentRoute("/medical-care/assessment/cvd?residentId=" + resolvedResidentId + "&from=resident360");

    return Result.ok(response);
  }

  @GetMapping("/overview")
  public Result<MedicalResidentOverviewResponse> overview(@RequestParam Long residentId) {
    Long orgId = AuthContext.getOrgId();
    Long resolvedResidentId = resolveResidentId(orgId, residentId);
    LocalDate today = LocalDate.now();
    if (resolvedResidentId == null) {
      MedicalResidentOverviewResponse emptyResponse = new MedicalResidentOverviewResponse();
      emptyResponse.setElderId(residentId);
      emptyResponse.setElderName("未匹配长者");
      emptyResponse.setCurrentStatus("UNKNOWN");
      emptyResponse.setHasUnclosedIncident(false);
      emptyResponse.setAlertCardCount(0);
      emptyResponse.setAlertTotalCount(0);
      emptyResponse.setCards(List.of());
      return Result.ok(emptyResponse);
    }
    ElderProfile elder = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getId, resolvedResidentId)
        .last("LIMIT 1"));
    if (elder == null) {
      MedicalResidentOverviewResponse emptyResponse = new MedicalResidentOverviewResponse();
      emptyResponse.setElderId(resolvedResidentId);
      emptyResponse.setElderName("未匹配长者");
      emptyResponse.setCurrentStatus("UNKNOWN");
      emptyResponse.setHasUnclosedIncident(false);
      emptyResponse.setAlertCardCount(0);
      emptyResponse.setAlertTotalCount(0);
      emptyResponse.setCards(List.of());
      return Result.ok(emptyResponse);
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDate monthStartDate = today.withDayOfMonth(1);
    LocalDateTime monthStartAt = monthStartDate.atStartOfDay();

    long pendingCareTaskCount = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getElderId, resolvedResidentId)
        .eq(CareTaskDaily::getTaskDate, today)
        .in(CareTaskDaily::getStatus, "TODO", "PENDING", "ASSIGNED"));

    long doneCareTaskCount = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getElderId, resolvedResidentId)
        .eq(CareTaskDaily::getTaskDate, today)
        .eq(CareTaskDaily::getStatus, "DONE"));

    long abnormalVitalCount = healthDataRecordMapper.selectCount(Wrappers.lambdaQuery(HealthDataRecord.class)
        .eq(HealthDataRecord::getIsDeleted, 0)
        .eq(orgId != null, HealthDataRecord::getOrgId, orgId)
        .eq(HealthDataRecord::getElderId, resolvedResidentId)
        .eq(HealthDataRecord::getAbnormalFlag, 1)
        .ge(HealthDataRecord::getMeasuredAt, LocalDateTime.now().minusHours(24)));

    long abnormalInspectionCount = healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(HealthInspection::getElderId, resolvedResidentId)
        .in(HealthInspection::getStatus, "ABNORMAL", "FOLLOWING"));

    long openIncidentCount = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(IncidentReport::getElderId, resolvedResidentId)
        .ne(IncidentReport::getStatus, "CLOSED"));

    long outingOpenCount = outingRecordMapper.selectCount(Wrappers.lambdaQuery(ElderOutingRecord.class)
        .eq(ElderOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderOutingRecord::getOrgId, orgId)
        .eq(ElderOutingRecord::getElderId, resolvedResidentId)
        .eq(ElderOutingRecord::getStatus, "OUT"));
    long medicalOutingOpenCount = medicalOutingRecordMapper.selectCount(Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
        .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderMedicalOutingRecord::getOrgId, orgId)
        .eq(ElderMedicalOutingRecord::getElderId, resolvedResidentId)
        .eq(ElderMedicalOutingRecord::getStatus, "OUT"));
    long dischargePendingCount = dischargeApplyMapper.selectCount(Wrappers.lambdaQuery(ElderDischargeApply.class)
        .eq(ElderDischargeApply::getIsDeleted, 0)
        .eq(orgId != null, ElderDischargeApply::getOrgId, orgId)
        .eq(ElderDischargeApply::getElderId, resolvedResidentId)
        .eq(ElderDischargeApply::getStatus, "PENDING"));

    long overdueOutingCount = outingRecordMapper.selectCount(Wrappers.lambdaQuery(ElderOutingRecord.class)
        .eq(ElderOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderOutingRecord::getOrgId, orgId)
        .eq(ElderOutingRecord::getElderId, resolvedResidentId)
        .eq(ElderOutingRecord::getStatus, "OUT")
        .isNotNull(ElderOutingRecord::getExpectedReturnTime)
        .lt(ElderOutingRecord::getExpectedReturnTime, now))
        + medicalOutingRecordMapper.selectCount(Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
        .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderMedicalOutingRecord::getOrgId, orgId)
        .eq(ElderMedicalOutingRecord::getElderId, resolvedResidentId)
        .eq(ElderMedicalOutingRecord::getStatus, "OUT")
        .isNotNull(ElderMedicalOutingRecord::getExpectedReturnTime)
        .lt(ElderMedicalOutingRecord::getExpectedReturnTime, now));

    long monthOutingCount = outingRecordMapper.selectCount(Wrappers.lambdaQuery(ElderOutingRecord.class)
        .eq(ElderOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderOutingRecord::getOrgId, orgId)
        .eq(ElderOutingRecord::getElderId, resolvedResidentId)
        .ge(ElderOutingRecord::getCreateTime, monthStartAt));
    long monthMedicalOutingCount = medicalOutingRecordMapper.selectCount(Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
        .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderMedicalOutingRecord::getOrgId, orgId)
        .eq(ElderMedicalOutingRecord::getElderId, resolvedResidentId)
        .ge(ElderMedicalOutingRecord::getCreateTime, monthStartAt));
    long monthIncidentCount = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(IncidentReport::getElderId, resolvedResidentId)
        .ge(IncidentReport::getCreateTime, monthStartAt));
    long monthDischargeCount = dischargeApplyMapper.selectCount(Wrappers.lambdaQuery(ElderDischargeApply.class)
        .eq(ElderDischargeApply::getIsDeleted, 0)
        .eq(orgId != null, ElderDischargeApply::getOrgId, orgId)
        .eq(ElderDischargeApply::getElderId, resolvedResidentId)
        .ge(ElderDischargeApply::getCreateTime, monthStartAt));

    long missedTaskCount = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getElderId, resolvedResidentId)
        .eq(CareTaskDaily::getTaskDate, today)
        .in(CareTaskDaily::getStatus, "MISSED", "OVERDUE"));
    long overtimeTaskCount = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getElderId, resolvedResidentId)
        .eq(CareTaskDaily::getTaskDate, today)
        .eq(CareTaskDaily::getStatus, "OVERDUE"));

    ElderAccount account = elderAccountMapper.selectOne(Wrappers.lambdaQuery(ElderAccount.class)
        .eq(ElderAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderAccount::getOrgId, orgId)
        .eq(ElderAccount::getElderId, resolvedResidentId)
        .last("LIMIT 1"));
    BigDecimal balance = account == null || account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
    long latestTxnCount = elderAccountLogMapper.selectCount(Wrappers.lambdaQuery(ElderAccountLog.class)
        .eq(ElderAccountLog::getIsDeleted, 0)
        .eq(orgId != null, ElderAccountLog::getOrgId, orgId)
        .eq(ElderAccountLog::getElderId, resolvedResidentId)
        .ge(ElderAccountLog::getCreateTime, LocalDateTime.now().minusDays(30)));

    CrmContract latestContract = crmContractMapper.selectOne(Wrappers.lambdaQuery(CrmContract.class)
        .eq(CrmContract::getIsDeleted, 0)
        .eq(orgId != null, CrmContract::getOrgId, orgId)
        .eq(CrmContract::getElderId, resolvedResidentId)
        .orderByDesc(CrmContract::getUpdateTime)
        .last("LIMIT 1"));

    long attachmentCount = latestContract == null || latestContract.getId() == null
        ? 0
        : contractAttachmentMapper.selectCount(Wrappers.lambdaQuery(CrmContractAttachment.class)
            .eq(CrmContractAttachment::getIsDeleted, 0)
            .eq(orgId != null, CrmContractAttachment::getOrgId, orgId)
            .eq(CrmContractAttachment::getContractId, latestContract.getId()));

    long assessmentReportCount = latestContract == null || latestContract.getId() == null
        ? 0
        : contractAttachmentMapper.selectCount(Wrappers.lambdaQuery(CrmContractAttachment.class)
            .eq(CrmContractAttachment::getIsDeleted, 0)
            .eq(orgId != null, CrmContractAttachment::getOrgId, orgId)
            .eq(CrmContractAttachment::getContractId, latestContract.getId())
            .likeRight(CrmContractAttachment::getAttachmentType, "ASSESSMENT"));

    MedicalResidentRiskCardResponse risk = riskCard(resolvedResidentId).getData();
    if (risk == null) {
      risk = new MedicalResidentRiskCardResponse();
    }
    List<MedicalResidentOverviewCardResponse> cards = new ArrayList<>();
    String currentStatusText = mapElderStatus(elder.getStatus(), outingOpenCount, medicalOutingOpenCount);
    String latestEventSummary = buildLatestEventSummary(orgId, resolvedResidentId);
    String contractStatus = latestContract == null ? "未签" : emptyContractStatus(latestContract.getContractStatus(), latestContract.getStatus());
    String contractDateRange = latestContract == null
        ? "-"
        : (latestContract.getEffectiveFrom() == null ? "-" : latestContract.getEffectiveFrom()) + " ~ "
        + (latestContract.getEffectiveTo() == null ? "-" : latestContract.getEffectiveTo());
    long contractExpireDays = latestContract == null || latestContract.getEffectiveTo() == null
        ? -1
        : ChronoUnit.DAYS.between(today, latestContract.getEffectiveTo());

    List<MedicalResidentOverviewActionResponse> statusActions = new ArrayList<>(List.of(
        action("发起状态变更", "/elder/status-change/center?residentId=" + resolvedResidentId, true),
        action("查看事件中心", "/elder/change-log?residentId=" + resolvedResidentId + "&filter=this_month", false),
        action("最近一次事件", "/elder/change-log?residentId=" + resolvedResidentId + "&filter=latest", false),
        action("本月事件", "/elder/change-log?residentId=" + resolvedResidentId + "&filter=this_month", false)));
    if ("OUT".equals(mapElderStatusCode(elder.getStatus(), outingOpenCount, medicalOutingOpenCount))
        || "OUT_MEDICAL".equals(mapElderStatusCode(elder.getStatus(), outingOpenCount, medicalOutingOpenCount))) {
      statusActions.add(action("外出返院登记", "/elder/outing?mode=return&elderId=" + resolvedResidentId, false));
    }
    if (openIncidentCount > 0) {
      statusActions.add(action("事故结案", "/elder/incident?residentId=" + resolvedResidentId + "&tab=unclosed", false));
    }
    if (overdueOutingCount > 0) {
      statusActions.add(action("外出超时处理", "/elder/change-log?residentId=" + resolvedResidentId + "&filter=overdue", false));
    }
    cards.add(withAlert(card("status-event", "卡片1：状态与事件总览", "全院协同的指挥灯", "blue",
        List.of(
            "当前状态：" + currentStatusText,
            latestEventSummary,
            "本月事件：外出 " + monthOutingCount + " / 来访 0 / 事故 " + monthIncidentCount + " / 退住 " + monthDischargeCount + " / 就医 " + monthMedicalOutingCount,
            "待处理事件：未闭环事故 " + openIncidentCount + "、待审批退住 " + dischargePendingCount + "、外出超时未返院 " + overdueOutingCount),
        statusActions,
        "全院协同状态总览"), openIncidentCount + dischargePendingCount + overdueOutingCount, monthIncidentCount));

    cards.add(withAlert(card("risk-level", "卡片2：风险预警与等级", "风险驱动护理与管理", "red",
        List.of(
            "风险标签：跌倒/走失/压疮/吞咽/过敏/精神行为/感染（可配置）",
            "最新评估：" + (risk.getLatestCvdDate() == null ? "-" : risk.getLatestCvdDate()) + "，等级 " + riskLabel(risk.getLatestCvdRiskLevel()) + "，护理建议 " + empty(risk.getLatestTcmPrimary()),
            "风险处置：未完成任务 " + pendingCareTaskCount + "，复评提醒 " + abnormalInspectionCount + "，24h异常体征 " + abnormalVitalCount),
        List.of(
            action("做评估", "/elder/assessment/ability/admission?residentId=" + resolvedResidentId + "&template=GBT42195", true),
            action("创建风险处置任务", "/oa/work-execution/task?residentId=" + resolvedResidentId + "&category=risk", false),
            action("查看历史评估", "/elder/assessment/ability/admission?residentId=" + resolvedResidentId + "&tab=reports", false)),
        "评估完成自动回写风险标签、护理建议与计划"), abnormalVitalCount + abnormalInspectionCount, pendingCareTaskCount));

    cards.add(withAlert(card("care-execution", "卡片3：护理执行", "任务完成率=服务质量", "cyan",
        List.of(
            "今日任务：应做 " + (pendingCareTaskCount + doneCareTaskCount) + " / 已做 " + doneCareTaskCount + " / 超时 " + overtimeTaskCount,
            buildCareRateSparkline(orgId, resolvedResidentId, today),
            "异常：漏做 " + missedTaskCount + " / 超时 " + overtimeTaskCount + " / 投诉关联 0",
            "当前护理等级建议：" + empty(risk.getLatestTcmPrimary())),
        List.of(
            action("查看今日任务", "/care/workbench/task-board?residentId=" + resolvedResidentId + "&date=today", true),
            action("查看护理计划", "/care/workbench/plan?residentId=" + resolvedResidentId, false),
            action("异常清单", "/care/workbench/task-board?residentId=" + resolvedResidentId + "&filter=overdue_or_missed", false),
            action("扫码执行", "/care/workbench/qr?residentId=" + resolvedResidentId, false)),
        "状态变化自动暂停/新增/调整任务"), missedTaskCount + overtimeTaskCount, pendingCareTaskCount));

    cards.add(withAlert(card("handover-communication", "卡片4：沟通与交接", "跨班组协同", "purple",
        List.of(
            "今日交接重点：风险、注意事项、未完成事项（交接班记录查看）",
            "家属沟通摘要：最近3条沟通记录（待接入）",
            "重要提醒：禁忌/过敏/纠纷风险/重点关注（待接入）"),
        List.of(
            action("新增交接记录", "/medical-care/handovers?residentId=" + resolvedResidentId, true),
            action("新增沟通记录", "/elder/change-log?residentId=" + resolvedResidentId + "&mode=communication", false),
            action("查看全部记录", "/elder/change-log?residentId=" + resolvedResidentId, false)),
        "交接、沟通与投诉闭环协同"), 0, 0));

    long contractSoonExpireCount = (contractExpireDays >= 0 && contractExpireDays < 30) ? 1 : 0;
    long contractMissingCount = latestContract == null ? 1 : 0;
    cards.add(withAlert(card("admission-contract", "卡片5：入住/合同摘要", "入住链路门牌号", "geekblue",
        List.of(
            "入住日期：" + (elder.getAdmissionDate() == null ? "-" : elder.getAdmissionDate()) + "，合同状态：" + contractStatus,
            "合同周期：" + contractDateRange + (contractExpireDays >= 0 ? "，到期倒计时 " + contractExpireDays + " 天" : ""),
            "套餐摘要：护理/服务套餐请以套餐管理为准",
            "票据摘要：合同附件 " + attachmentCount + " 份，评估报告 " + assessmentReportCount + " 份"),
        List.of(
            action("查看合同", "/elder/contracts-invoices?residentId=" + resolvedResidentId, true),
            action("发起续签/变更", "/marketing/contract-signing?residentId=" + resolvedResidentId + "&type=renew_or_change", false),
            action("上传合同/票据", "/elder/contracts-invoices?residentId=" + resolvedResidentId + "&tab=attachments", false),
            action("入住办理", "/elder/admission-processing?residentId=" + resolvedResidentId, false)),
        "合同生效后联动账单、护理任务、床态"), contractMissingCount, contractSoonExpireCount));

    long negativeBalanceCount = balance.compareTo(BigDecimal.ZERO) < 0 ? 1 : 0;
    long lowBalanceCount = balance.compareTo(BigDecimal.valueOf(100)) < 0 ? 1 : 0;
    cards.add(withAlert(card("finance-account", "卡片6：费用与账户", "业财一体化驾驶盘", "orange",
        List.of(
            "账户余额/押金：" + balance.stripTrailingZeros().toPlainString() + " 元",
            "近3笔交易：请进入费用明细查看实时流水",
            "当月流水：" + latestTxnCount + " 条，自动催缴状态：以账单配置为准"),
        List.of(
            action("查看账单", "/finance/resident-bill-payment?residentId=" + resolvedResidentId + "&period=this_month", true),
            action("缴费/充值", "/finance/prepaid-recharge?residentId=" + resolvedResidentId, false),
            action("退住结算", "/finance/discharge-settlement?residentId=" + resolvedResidentId, false),
            action("费用明细", "/finance/account-log?residentId=" + resolvedResidentId, false)),
        "外出/退住事件可联动结算与扣费规则"), negativeBalanceCount, lowBalanceCount));

    cards.add(withAlert(card("medical-summary", "卡片7：医疗健康摘要", "医护端核心入口", "magenta",
        List.of(
            "关键体征：24h 异常 " + abnormalVitalCount + " 条（异常高亮）",
            "医嘱/巡检：异常巡检待处理 " + abnormalInspectionCount + " 条",
            "用药：今日应服/已服请见用药登记；剩余药量预警请见剩余用药",
            "最近就医：外出就医中 " + medicalOutingOpenCount + " 次"),
        List.of(
            action("进入医护工作站", "/medical-care/workbench?residentId=" + resolvedResidentId, true),
            action("查看医嘱", "/medical-care/inspection?residentId=" + resolvedResidentId, false),
            action("用药登记", "/medical-care/medication-registration?residentId=" + resolvedResidentId + "&date=today", false),
            action("外出就医登记", "/elder/medical-outing?elderId=" + resolvedResidentId, false)),
        "就医登记可触发随访任务与家属通知"), abnormalVitalCount + abnormalInspectionCount + medicalOutingOpenCount, pendingCareTaskCount));

    cards.add(withAlert(card("service-package", "卡片8：服务与套餐", "非医疗服务执行总控", "cyan",
        List.of(
            "已开通服务：护理/康复/陪诊/洗浴/理发（明细见服务计划）",
            "预约服务：待执行预约请查看服务预约",
            "套餐变更：最近一次以套餐记录为准"),
        List.of(
            action("增购服务", "/care/elder-packages?residentId=" + resolvedResidentId, true),
            action("服务预约", "/care/workbench/task?residentId=" + resolvedResidentId, false),
            action("查看服务计划", "/care/service/service-plans?residentId=" + resolvedResidentId, false),
            action("服务报表", "/care/service/nursing-reports?residentId=" + resolvedResidentId, false)),
        "服务变更自动联动任务与统计"), 0, pendingCareTaskCount));

    cards.add(withAlert(card("dining-order", "卡片9：膳食与点餐", "餐饮系统入口", "green",
        List.of(
            "当前膳食方案：请在膳食计划中维护（普食/低盐/糖尿病/软食）",
            "禁忌/过敏提示：请在健康档案与交接记录维护",
            "今日订餐状态与近7天异常：请见点餐与送餐记录"),
        List.of(
            action("查看今日订餐", "/dining/order?residentId=" + resolvedResidentId + "&date=today", true),
            action("修改膳食方案", "/life/meal-plan?residentId=" + resolvedResidentId, false),
            action("餐饮统计", "/dining/stats?residentId=" + resolvedResidentId + "&period=week", false)),
        "外出登记可联动暂停/恢复送餐"), 0, outingOpenCount + medicalOutingOpenCount));

    cards.add(withAlert(card("activity-album", "卡片10：活动与相册", "生活质量与展示", "lime",
        List.of(
            "本周活动参与：请查看活动管理记录",
            "最新照片：展示最近4张缩略图（相册模块）",
            "未来7天活动预告：请查看活动日历"),
        List.of(
            action("查看活动日历", "/oa/work-execution/calendar?residentId=" + resolvedResidentId, true),
            action("上传照片", "/oa/album?residentId=" + resolvedResidentId + "&mode=upload", false),
            action("查看相册", "/oa/album?residentId=" + resolvedResidentId, false)),
        "活动参与与家属可见相册联动"), 0, 0));

    cards.add(withAlert(card("family-feedback", "卡片11：家属互动与满意度", "外部反馈闭环", "gold",
        List.of(
            "最近问卷评分：护理/餐饮/环境/沟通（待接入）",
            "家属留言/投诉：最新1条请查看反馈工单",
            "处理状态：未处理/处理中/已结案"),
        List.of(
            action("发送问卷", "/survey/submit?residentId=" + resolvedResidentId, true),
            action("查看反馈", "/survey/stats?residentId=" + resolvedResidentId, false),
            action("创建改进任务", "/oa/work-execution/task?residentId=" + resolvedResidentId + "&source=survey", false)),
        "问卷与投诉自动形成改进任务"), 0, 0));

    long attachmentDangerCount = (latestContract != null && attachmentCount == 0) ? 1 : 0;
    long attachmentWarningCount = (latestContract != null && assessmentReportCount == 0) ? 1 : 0;
    cards.add(withAlert(card("docs-attachments", "卡片12：文档与附件", "全院追溯证据链", "volcano",
        List.of(
            "附件分类计数：合同/票据/评估报告/就医单据/告知书/其他",
            "当前合同附件：" + attachmentCount + " 份，评估报告附件：" + assessmentReportCount + " 份",
            "缺失提醒：合同未上传、发票未关联、特殊事件证明未上传（按流程核验）"),
        List.of(
            action("上传附件", "/elder/contracts-invoices?residentId=" + resolvedResidentId + "&tab=attachments", true),
            action("查看全部", "/elder/contracts-invoices?residentId=" + resolvedResidentId + "&tab=all", false),
            action("下载打包", "/elder/contracts-invoices?residentId=" + resolvedResidentId + "&action=download", false),
            action("关联到事件/合同/账单", "/elder/contracts-invoices?residentId=" + resolvedResidentId + "&mode=link", false)),
        "附件缺失可直达上传并预选类别"), attachmentDangerCount, attachmentWarningCount));

    int alertCardCount = (int) cards.stream().filter((item) -> item.getAlertCount() != null && item.getAlertCount() > 0).count();
    int alertTotalCount = cards.stream().mapToInt((item) -> item.getAlertCount() == null ? 0 : item.getAlertCount()).sum();

    MedicalResidentOverviewResponse response = new MedicalResidentOverviewResponse();
    response.setElderId(resolvedResidentId);
    response.setElderName(elder.getFullName());
    response.setCurrentStatus(mapElderStatusCode(elder.getStatus(), outingOpenCount, medicalOutingOpenCount));
    response.setHasUnclosedIncident(openIncidentCount > 0);
    response.setAlertCardCount(alertCardCount);
    response.setAlertTotalCount(alertTotalCount);
    response.setCards(cards);
    return Result.ok(response);
  }

  private Long resolveResidentId(Long orgId, Long residentId) {
    if (residentId == null) {
      return null;
    }
    ElderProfile elder = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getId, residentId)
        .last("LIMIT 1"));
    if (elder != null) {
      return elder.getId();
    }
    CrmContract byContractId = crmContractMapper.selectOne(Wrappers.lambdaQuery(CrmContract.class)
        .eq(CrmContract::getIsDeleted, 0)
        .eq(orgId != null, CrmContract::getOrgId, orgId)
        .eq(CrmContract::getId, residentId)
        .isNotNull(CrmContract::getElderId)
        .orderByDesc(CrmContract::getUpdateTime)
        .last("LIMIT 1"));
    if (byContractId != null && byContractId.getElderId() != null) {
      return byContractId.getElderId();
    }
    CrmContract byLeadId = crmContractMapper.selectOne(Wrappers.lambdaQuery(CrmContract.class)
        .eq(CrmContract::getIsDeleted, 0)
        .eq(orgId != null, CrmContract::getOrgId, orgId)
        .eq(CrmContract::getLeadId, residentId)
        .isNotNull(CrmContract::getElderId)
        .orderByDesc(CrmContract::getUpdateTime)
        .last("LIMIT 1"));
    if (byLeadId != null && byLeadId.getElderId() != null) {
      return byLeadId.getElderId();
    }
    return null;
  }

  private MedicalResidentOverviewCardResponse card(
      String key,
      String title,
      String tag,
      String tagColor,
      List<String> lines,
      List<MedicalResidentOverviewActionResponse> actions,
      String description) {
    MedicalResidentOverviewCardResponse card = new MedicalResidentOverviewCardResponse();
    card.setKey(key);
    card.setTitle(title);
    card.setTag(tag);
    card.setTagColor(tagColor);
    card.setLines(lines);
    card.setActions(actions);
    card.setDescription(description);
    return card;
  }

  private MedicalResidentOverviewActionResponse action(String label, String path, boolean primary) {
    MedicalResidentOverviewActionResponse action = new MedicalResidentOverviewActionResponse();
    action.setLabel(label);
    action.setPath(path);
    action.setPrimary(primary);
    return action;
  }

  private MedicalResidentOverviewCardResponse withAlert(
      MedicalResidentOverviewCardResponse card,
      long dangerCount,
      long warningCount) {
    int safeDangerCount = safeToInt(dangerCount);
    int safeWarningCount = safeToInt(warningCount);
    card.setDangerCount(safeDangerCount);
    card.setWarningCount(safeWarningCount);
    card.setAlertCount(safeDangerCount + safeWarningCount);
    return card;
  }

  private int safeToInt(long value) {
    if (value < 0) return 0;
    return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
  }

  private String riskLabel(String value) {
    if ("VERY_HIGH".equals(value)) return "极高风险";
    if ("HIGH".equals(value)) return "高风险";
    if ("MEDIUM".equals(value)) return "中风险";
    if ("LOW".equals(value)) return "低风险";
    return "未分层";
  }

  private String empty(String value) {
    return (value == null || value.isBlank()) ? "-" : value;
  }

  private String emptyContractStatus(String contractStatus, String status) {
    if (contractStatus != null && !contractStatus.isBlank()) {
      return contractStatus;
    }
    if (status != null && !status.isBlank()) {
      return status;
    }
    return "未签";
  }

  private String buildCareRateSparkline(Long orgId, Long residentId, LocalDate today) {
    List<String> points = new ArrayList<>();
    for (int i = 6; i >= 0; i--) {
      LocalDate day = today.minusDays(i);
      long dayTotal = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
          .eq(CareTaskDaily::getIsDeleted, 0)
          .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
          .eq(CareTaskDaily::getElderId, residentId)
          .eq(CareTaskDaily::getTaskDate, day));
      long dayDone = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
          .eq(CareTaskDaily::getIsDeleted, 0)
          .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
          .eq(CareTaskDaily::getElderId, residentId)
          .eq(CareTaskDaily::getTaskDate, day)
          .eq(CareTaskDaily::getStatus, "DONE"));
      int rate = dayTotal == 0 ? 100 : (int) Math.round((dayDone * 100.0) / dayTotal);
      points.add(rate + "%");
    }
    return "最近7天完成率：" + String.join(" · ", points);
  }

  private String buildLatestEventSummary(Long orgId, Long residentId) {
    LocalDateTime latestTime = null;
    String latestSummary = "最近一次事件：暂无（类型/时间/处理人/闭环）";

    IncidentReport latestIncident = incidentReportMapper.selectOne(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(IncidentReport::getElderId, residentId)
        .orderByDesc(IncidentReport::getIncidentTime)
        .last("LIMIT 1"));
    if (latestIncident != null) {
      LocalDateTime time = latestIncident.getIncidentTime() == null ? latestIncident.getUpdateTime() : latestIncident.getIncidentTime();
      latestTime = time;
      latestSummary = "最近一次事件：事故/" + (time == null ? "-" : time) + "/处理人 "
          + empty(latestIncident.getReporterName()) + "/" + ("CLOSED".equals(latestIncident.getStatus()) ? "已闭环" : "未闭环");
    }

    ElderDischargeApply latestDischarge = dischargeApplyMapper.selectOne(Wrappers.lambdaQuery(ElderDischargeApply.class)
        .eq(ElderDischargeApply::getIsDeleted, 0)
        .eq(orgId != null, ElderDischargeApply::getOrgId, orgId)
        .eq(ElderDischargeApply::getElderId, residentId)
        .orderByDesc(ElderDischargeApply::getUpdateTime)
        .last("LIMIT 1"));
    if (latestDischarge != null) {
      LocalDateTime time = latestDischarge.getUpdateTime();
      if (latestTime == null || (time != null && time.isAfter(latestTime))) {
        latestTime = time;
        latestSummary = "最近一次事件：退住/" + (time == null ? "-" : time) + "/处理人 "
            + empty(latestDischarge.getReviewedByName()) + "/" + ("APPROVED".equals(latestDischarge.getStatus()) ? "已闭环" : "未闭环");
      }
    }

    ElderMedicalOutingRecord latestMedicalOuting = medicalOutingRecordMapper.selectOne(Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
        .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderMedicalOutingRecord::getOrgId, orgId)
        .eq(ElderMedicalOutingRecord::getElderId, residentId)
        .orderByDesc(ElderMedicalOutingRecord::getUpdateTime)
        .last("LIMIT 1"));
    if (latestMedicalOuting != null) {
      LocalDateTime time = latestMedicalOuting.getUpdateTime();
      if (latestTime == null || (time != null && time.isAfter(latestTime))) {
        latestTime = time;
        latestSummary = "最近一次事件：外出就医/" + (time == null ? "-" : time) + "/处理人 "
            + empty(latestMedicalOuting.getCompanion()) + "/" + ("OUT".equals(latestMedicalOuting.getStatus()) ? "未闭环" : "已闭环");
      }
    }

    ElderOutingRecord latestOuting = outingRecordMapper.selectOne(Wrappers.lambdaQuery(ElderOutingRecord.class)
        .eq(ElderOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderOutingRecord::getOrgId, orgId)
        .eq(ElderOutingRecord::getElderId, residentId)
        .orderByDesc(ElderOutingRecord::getUpdateTime)
        .last("LIMIT 1"));
    if (latestOuting != null) {
      LocalDateTime time = latestOuting.getUpdateTime();
      if (latestTime == null || (time != null && time.isAfter(latestTime))) {
        latestSummary = "最近一次事件：外出/" + (time == null ? "-" : time) + "/处理人 "
            + empty(latestOuting.getCompanion()) + "/" + ("OUT".equals(latestOuting.getStatus()) ? "未闭环" : "已闭环");
      }
    }
    return latestSummary;
  }

  private String mapElderStatus(Integer status, long outingOpenCount, long medicalOutingOpenCount) {
    if (medicalOutingOpenCount > 0) return "外出就医";
    if (outingOpenCount > 0) return "外出";
    if (status == null) return "意向";
    return switch (status) {
      case 1 -> "在住";
      case 2 -> "外出";
      case 3 -> "试住";
      case 4 -> "退住办理中";
      case 5 -> "已退住";
      case 6 -> "死亡";
      default -> "意向";
    };
  }

  private String mapElderStatusCode(Integer status, long outingOpenCount, long medicalOutingOpenCount) {
    if (medicalOutingOpenCount > 0) return "OUT_MEDICAL";
    if (outingOpenCount > 0) return "OUT";
    if (status == null) return "INTENT";
    return switch (status) {
      case 1 -> "IN";
      case 2 -> "OUT";
      case 3 -> "TRIAL";
      case 4 -> "DISCHARGE_PENDING";
      case 5 -> "DISCHARGED";
      case 6 -> "DECEASED";
      default -> "INTENT";
    };
  }
}
