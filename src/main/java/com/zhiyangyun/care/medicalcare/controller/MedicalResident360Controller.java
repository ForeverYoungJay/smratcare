package com.zhiyangyun.care.medicalcare.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.crm.entity.CrmContractAttachment;
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
      CrmContractAttachmentMapper contractAttachmentMapper) {
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

  @GetMapping("/overview")
  public Result<MedicalResidentOverviewResponse> overview(@RequestParam Long residentId) {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    ElderProfile elder = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getId, residentId)
        .last("LIMIT 1"));
    if (elder == null) {
      throw new IllegalArgumentException("长者不存在");
    }

    long pendingCareTaskCount = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getElderId, residentId)
        .eq(CareTaskDaily::getTaskDate, today)
        .in(CareTaskDaily::getStatus, "TODO", "PENDING", "ASSIGNED"));

    long doneCareTaskCount = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getElderId, residentId)
        .eq(CareTaskDaily::getTaskDate, today)
        .eq(CareTaskDaily::getStatus, "DONE"));

    long abnormalVitalCount = healthDataRecordMapper.selectCount(Wrappers.lambdaQuery(HealthDataRecord.class)
        .eq(HealthDataRecord::getIsDeleted, 0)
        .eq(orgId != null, HealthDataRecord::getOrgId, orgId)
        .eq(HealthDataRecord::getElderId, residentId)
        .eq(HealthDataRecord::getAbnormalFlag, 1)
        .ge(HealthDataRecord::getMeasuredAt, LocalDateTime.now().minusHours(24)));

    long abnormalInspectionCount = healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(HealthInspection::getElderId, residentId)
        .in(HealthInspection::getStatus, "ABNORMAL", "FOLLOWING"));

    long openIncidentCount = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(IncidentReport::getElderId, residentId)
        .ne(IncidentReport::getStatus, "CLOSED"));

    long outingOpenCount = outingRecordMapper.selectCount(Wrappers.lambdaQuery(ElderOutingRecord.class)
        .eq(ElderOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderOutingRecord::getOrgId, orgId)
        .eq(ElderOutingRecord::getElderId, residentId)
        .eq(ElderOutingRecord::getStatus, "OUT"));
    long medicalOutingOpenCount = medicalOutingRecordMapper.selectCount(Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
        .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderMedicalOutingRecord::getOrgId, orgId)
        .eq(ElderMedicalOutingRecord::getElderId, residentId)
        .eq(ElderMedicalOutingRecord::getStatus, "OUT"));
    long dischargePendingCount = dischargeApplyMapper.selectCount(Wrappers.lambdaQuery(ElderDischargeApply.class)
        .eq(ElderDischargeApply::getIsDeleted, 0)
        .eq(orgId != null, ElderDischargeApply::getOrgId, orgId)
        .eq(ElderDischargeApply::getElderId, residentId)
        .eq(ElderDischargeApply::getStatus, "PENDING"));

    ElderAccount account = elderAccountMapper.selectOne(Wrappers.lambdaQuery(ElderAccount.class)
        .eq(ElderAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderAccount::getOrgId, orgId)
        .eq(ElderAccount::getElderId, residentId)
        .last("LIMIT 1"));
    BigDecimal balance = account == null || account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
    long latestTxnCount = elderAccountLogMapper.selectCount(Wrappers.lambdaQuery(ElderAccountLog.class)
        .eq(ElderAccountLog::getIsDeleted, 0)
        .eq(orgId != null, ElderAccountLog::getOrgId, orgId)
        .eq(ElderAccountLog::getElderId, residentId)
        .ge(ElderAccountLog::getCreateTime, LocalDateTime.now().minusDays(30)));

    long attachmentCount = contractAttachmentMapper.selectCount(Wrappers.lambdaQuery(CrmContractAttachment.class)
        .eq(CrmContractAttachment::getIsDeleted, 0)
        .eq(orgId != null, CrmContractAttachment::getOrgId, orgId));

    MedicalResidentRiskCardResponse risk = riskCard(residentId).getData();
    if (risk == null) {
      risk = new MedicalResidentRiskCardResponse();
    }
    List<MedicalResidentOverviewCardResponse> cards = new ArrayList<>();

    cards.add(card("status-event", "卡片1：状态与事件总览", "全院协同", "blue",
        List.of(
            "当前状态：" + mapElderStatus(elder.getStatus()),
            "待处理：外出未返院 " + (outingOpenCount + medicalOutingOpenCount) + "，未闭环事故 " + openIncidentCount + "，待审批退住 " + dischargePendingCount,
            "今日照护待执行：" + pendingCareTaskCount + "，已完成 " + doneCareTaskCount),
        List.of(
            action("发起状态变更", "/elder/status-change?residentId=" + residentId, true),
            action("查看事件中心", "/elder/change-log?residentId=" + residentId + "&filter=this_month", false),
            action("外出登记", "/elder/outing?elderId=" + residentId, false)),
        "状态事件统一入口"));

    cards.add(card("risk-level", "卡片2：风险预警与等级", "风险驱动", "red",
        List.of(
            "风险标签：心血管 " + riskLabel(risk.getLatestCvdRiskLevel()) + "，体质 " + empty(risk.getLatestTcmPrimary()),
            "风险处置任务：待办 " + pendingCareTaskCount + "，24h 异常体征 " + abnormalVitalCount,
            "巡检闭环：异常处理中 " + abnormalInspectionCount),
        List.of(
            action("做评估", "/assessment/ability/admission?residentId=" + residentId + "&template=GBT42195", true),
            action("中医体质评估", "/medical-care/assessment/tcm?residentId=" + residentId + "&from=resident360", false),
            action("心血管风险评估", "/medical-care/assessment/cvd?residentId=" + residentId + "&from=resident360", false)),
        "风险评估与跟进"));

    cards.add(card("care-execution", "卡片3：护理执行", "服务质量", "cyan",
        List.of(
            "今日任务：待执行 " + pendingCareTaskCount + " / 已完成 " + doneCareTaskCount,
            "健康异常：24h 体征异常 " + abnormalVitalCount + " 条",
            "事故未闭环：" + openIncidentCount + " 条"),
        List.of(
            action("查看今日任务", "/care/workbench/task-board?residentId=" + residentId + "&date=today", true),
            action("查看护理计划", "/care/workbench/plan?residentId=" + residentId, false),
            action("异常清单", "/care/workbench/task-board?residentId=" + residentId + "&filter=overdue_or_missed", false)),
        "照护执行看板"));

    cards.add(card("medical", "卡片4：医疗健康摘要", "医护入口", "magenta",
        List.of(
            "最新评估：体质 " + empty(risk.getLatestTcmPrimary()) + "，心血管 " + riskLabel(risk.getLatestCvdRiskLevel()),
            "体征异常：24h " + abnormalVitalCount + " 条，巡检异常 " + abnormalInspectionCount + " 条",
            "风险任务待办：" + pendingCareTaskCount + " 条"),
        List.of(
            action("进入医护工作站", "/life/health-basic?residentId=" + residentId, true),
            action("用药登记", "/health/medication-registration?elderId=" + residentId, false),
            action("外出就医登记", "/elder/medical-outing?elderId=" + residentId, false)),
        "医护联合视图"));

    cards.add(card("finance", "卡片5：费用与账户", "业财一体", "orange",
        List.of(
            "账户余额：" + balance.stripTrailingZeros().toPlainString() + " 元",
            "近30天流水：" + latestTxnCount + " 条",
            "合同/票据附件：" + attachmentCount + " 份"),
        List.of(
            action("查看账单", "/finance/resident-bill-payment?residentId=" + residentId, true),
            action("费用明细", "/finance/account-log?residentId=" + residentId, false),
            action("合同与票据", "/elder/contracts-invoices?residentId=" + residentId, false)),
        "费用与附件联动"));

    cards.add(card("service-package", "卡片6：服务与套餐", "非医疗", "geekblue",
        List.of(
            "护理任务：今日待执行 " + pendingCareTaskCount + " 条",
            "在院风险：未闭环事故 " + openIncidentCount + " 条",
            "外出状态：外出中 " + (outingOpenCount + medicalOutingOpenCount) + " 次"),
        List.of(
            action("增购服务", "/care/elder-packages?residentId=" + residentId, true),
            action("服务预约", "/care/workbench/task?residentId=" + residentId + "&mode=booking", false),
            action("在院服务总览", "/elder/in-hospital-overview?residentId=" + residentId, false)),
        "服务与套餐管理"));

    MedicalResidentOverviewResponse response = new MedicalResidentOverviewResponse();
    response.setElderId(residentId);
    response.setElderName(elder.getFullName());
    response.setCurrentStatus(mapElderStatusCode(elder.getStatus()));
    response.setHasUnclosedIncident(openIncidentCount > 0);
    response.setCards(cards);
    return Result.ok(response);
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

  private String mapElderStatus(Integer status) {
    if (status == null) return "未知";
    return switch (status) {
      case 1 -> "在住";
      case 2 -> "外出";
      case 3 -> "试住";
      case 4 -> "退住中";
      case 5 -> "已退住";
      case 6 -> "离世";
      default -> "未知";
    };
  }

  private String mapElderStatusCode(Integer status) {
    if (status == null) return "UNKNOWN";
    return switch (status) {
      case 1 -> "IN";
      case 2 -> "OUT";
      case 3 -> "TRIAL";
      case 4 -> "DISCHARGE_PENDING";
      case 5 -> "DISCHARGED";
      case 6 -> "DECEASED";
      default -> "UNKNOWN";
    };
  }
}
