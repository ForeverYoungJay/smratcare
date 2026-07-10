package com.zhiyangyun.care.operations.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.crm.model.report.MarketingChannelReportItem;
import com.zhiyangyun.care.crm.model.report.MarketingConversionReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingDataQualityResponse;
import com.zhiyangyun.care.crm.model.report.MarketingWorkbenchSummaryResponse;
import com.zhiyangyun.care.crm.service.MarketingReportService;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.family.config.FamilyPortalProperties;
import com.zhiyangyun.care.family.entity.FamilyNotifyLog;
import com.zhiyangyun.care.family.entity.FamilyPortalState;
import com.zhiyangyun.care.family.entity.FamilyRechargeOrder;
import com.zhiyangyun.care.family.mapper.FamilyNotifyLogMapper;
import com.zhiyangyun.care.family.mapper.FamilyPortalStateMapper;
import com.zhiyangyun.care.family.mapper.FamilyRechargeOrderMapper;
import com.zhiyangyun.care.fire.entity.FireSafetyRecord;
import com.zhiyangyun.care.fire.mapper.FireSafetyRecordMapper;
import com.zhiyangyun.care.health.entity.HealthInspection;
import com.zhiyangyun.care.health.entity.HealthMedicationTask;
import com.zhiyangyun.care.health.mapper.HealthInspectionMapper;
import com.zhiyangyun.care.health.mapper.HealthMedicationTaskMapper;
import com.zhiyangyun.care.hr.entity.StaffProfile;
import com.zhiyangyun.care.hr.entity.StaffRewardPunishment;
import com.zhiyangyun.care.hr.entity.StaffTrainingRecord;
import com.zhiyangyun.care.hr.mapper.StaffProfileMapper;
import com.zhiyangyun.care.hr.mapper.StaffRewardPunishmentMapper;
import com.zhiyangyun.care.hr.mapper.StaffTrainingRecordMapper;
import com.zhiyangyun.care.life.entity.IncidentReport;
import com.zhiyangyun.care.life.entity.DiningMealOrder;
import com.zhiyangyun.care.life.entity.MaintenanceRequest;
import com.zhiyangyun.care.life.entity.RoomCleaningTask;
import com.zhiyangyun.care.life.mapper.DiningMealOrderMapper;
import com.zhiyangyun.care.life.mapper.IncidentReportMapper;
import com.zhiyangyun.care.life.mapper.MaintenanceRequestMapper;
import com.zhiyangyun.care.life.mapper.RoomCleaningTaskMapper;
import com.zhiyangyun.care.logistics.entity.LogisticsEquipmentArchive;
import com.zhiyangyun.care.logistics.mapper.LogisticsEquipmentArchiveMapper;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.operations.entity.StaffMobileHandoverRecord;
import com.zhiyangyun.care.operations.entity.StaffMobilePatrolScanRecord;
import com.zhiyangyun.care.operations.entity.StaffMobileTaskReceipt;
import com.zhiyangyun.care.operations.mapper.StaffMobileHandoverRecordMapper;
import com.zhiyangyun.care.operations.mapper.StaffMobilePatrolScanRecordMapper;
import com.zhiyangyun.care.operations.mapper.StaffMobileTaskReceiptMapper;
import com.zhiyangyun.care.operations.model.OperationsCapabilityMapResponse;
import com.zhiyangyun.care.operations.model.OperationsCapabilityMapResponse.CapabilitySummary;
import com.zhiyangyun.care.operations.model.OperationsCapabilityMapResponse.DomainCapability;
import com.zhiyangyun.care.operations.model.OperationsCapabilityMapResponse.IntelligenceCapability;
import com.zhiyangyun.care.operations.model.OperationsCapabilityMapResponse.MetricCapability;
import com.zhiyangyun.care.operations.model.OperationsFamilyServiceResponse;
import com.zhiyangyun.care.operations.model.OperationsFamilyServiceResponse.FamilyServiceAction;
import com.zhiyangyun.care.operations.model.OperationsFamilyServiceResponse.FamilyServiceFlow;
import com.zhiyangyun.care.operations.model.OperationsFamilyServiceResponse.FamilyServiceMetric;
import com.zhiyangyun.care.operations.model.OperationsFamilyServiceResponse.FamilyRuntimeCard;
import com.zhiyangyun.care.operations.model.OperationsFamilyServiceResponse.FamilyVisibleData;
import com.zhiyangyun.care.operations.model.OperationsIntelligenceResponse;
import com.zhiyangyun.care.operations.model.OperationsIntelligenceResponse.IntelligenceAction;
import com.zhiyangyun.care.operations.model.OperationsIntelligenceResponse.IntelligenceDataSource;
import com.zhiyangyun.care.operations.model.OperationsIntelligenceResponse.IntelligenceMetric;
import com.zhiyangyun.care.operations.model.OperationsIntelligenceResponse.IntelligenceScenario;
import com.zhiyangyun.care.operations.model.OperationsLogisticsResponse;
import com.zhiyangyun.care.operations.model.OperationsLogisticsResponse.LogisticsAction;
import com.zhiyangyun.care.operations.model.OperationsLogisticsResponse.LogisticsFlow;
import com.zhiyangyun.care.operations.model.OperationsLogisticsResponse.LogisticsMetric;
import com.zhiyangyun.care.operations.model.OperationsLogisticsResponse.LogisticsRisk;
import com.zhiyangyun.care.operations.model.OperationsMarketingResponse;
import com.zhiyangyun.care.operations.model.OperationsMarketingResponse.MarketingAction;
import com.zhiyangyun.care.operations.model.OperationsMarketingResponse.MarketingChannel;
import com.zhiyangyun.care.operations.model.OperationsMarketingResponse.MarketingFlow;
import com.zhiyangyun.care.operations.model.OperationsMarketingResponse.MarketingFunnelStage;
import com.zhiyangyun.care.operations.model.OperationsMarketingResponse.MarketingMetric;
import com.zhiyangyun.care.operations.model.OperationsMarketingResponse.MarketingRisk;
import com.zhiyangyun.care.operations.model.OperationsSafetyRiskResponse;
import com.zhiyangyun.care.operations.model.OperationsSafetyRiskResponse.EmergencyFlow;
import com.zhiyangyun.care.operations.model.OperationsSafetyRiskResponse.SafetyAction;
import com.zhiyangyun.care.operations.model.OperationsSafetyRiskResponse.SafetyMetric;
import com.zhiyangyun.care.operations.model.OperationsSafetyRiskResponse.SafetyRecentEvent;
import com.zhiyangyun.care.operations.model.OperationsSafetyRiskResponse.SafetyRiskSource;
import com.zhiyangyun.care.operations.model.OperationsServiceReportResponse;
import com.zhiyangyun.care.operations.model.OperationsServiceReportResponse.ComplianceArchive;
import com.zhiyangyun.care.operations.model.OperationsServiceReportResponse.QualityDomain;
import com.zhiyangyun.care.operations.model.OperationsServiceReportResponse.ReportAction;
import com.zhiyangyun.care.operations.model.OperationsServiceReportResponse.ReportMetric;
import com.zhiyangyun.care.operations.model.OperationsServiceReportResponse.StandardProcess;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileAction;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileCompleteTaskRequest;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileHandover;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileHandoverRequest;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileIncidentRequest;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileIncidentView;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileMetric;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobilePatrolPoint;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobilePatrolScanRequest;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileReceipt;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileRoleCard;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileRuntimeCard;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileSchedule;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileTask;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileTaskReceiptView;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileWorkflow;
import com.zhiyangyun.care.operations.model.OperationsWorkforceResponse;
import com.zhiyangyun.care.operations.model.OperationsWorkforceResponse.WorkforceAction;
import com.zhiyangyun.care.operations.model.OperationsWorkforceResponse.WorkforceFlow;
import com.zhiyangyun.care.operations.model.OperationsWorkforceResponse.WorkforceMetric;
import com.zhiyangyun.care.operations.model.OperationsWorkforceResponse.WorkforceRisk;
import com.zhiyangyun.care.operations.model.OperationsWorkforceResponse.WorkforceRoleLoad;
import com.zhiyangyun.care.operations.service.OperationsCapabilityService;
import com.zhiyangyun.care.schedule.entity.AttendanceRecord;
import com.zhiyangyun.care.schedule.entity.StaffSchedule;
import com.zhiyangyun.care.schedule.mapper.AttendanceRecordMapper;
import com.zhiyangyun.care.schedule.mapper.StaffScheduleMapper;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.mapper.SmartAlertMapper;
import com.zhiyangyun.care.store.entity.MaterialPurchaseOrder;
import com.zhiyangyun.care.store.mapper.MaterialPurchaseOrderMapper;
import com.zhiyangyun.care.store.service.InventoryService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OperationsCapabilityServiceImpl implements OperationsCapabilityService {
  private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final String VERSION = "2026.06-operations";
  private static final String FAMILY_STATE_COMMUNICATION = "COMMUNICATION";
  /** 员工移动端可见模块全集。 */
  private static final Set<String> STAFF_MOBILE_ALL_MODULES =
      Set.of("CARE", "MEDICATION", "INSPECTION", "LOGISTICS", "MEAL");

  /** 员工移动端列表/详情为空时是否返回演示兜底数据；生产环境默认关闭。 */
  @Value("${zhiyangyun.staff-mobile.demo-fallback:false}")
  private boolean staffMobileDemoFallback;

  /** 开启后，员工必须完成今日上班打卡才能提交任务回执；默认关闭仅做标记。 */
  @Value("${zhiyangyun.staff-mobile.require-on-duty:false}")
  private boolean staffMobileRequireOnDuty;

  private final IncidentReportMapper incidentReportMapper;
  private final FamilyRechargeOrderMapper familyRechargeOrderMapper;
  private final FamilyNotifyLogMapper familyNotifyLogMapper;
  private final FamilyPortalStateMapper familyPortalStateMapper;
  private final FamilyPortalProperties familyPortalProperties;
  private final SmartAlertMapper smartAlertMapper;
  private final FireSafetyRecordMapper fireSafetyRecordMapper;
  private final StaffMapper staffMapper;
  private final StaffProfileMapper staffProfileMapper;
  private final StaffTrainingRecordMapper staffTrainingRecordMapper;
  private final StaffRewardPunishmentMapper staffRewardPunishmentMapper;
  private final StaffScheduleMapper staffScheduleMapper;
  private final AttendanceRecordMapper attendanceRecordMapper;
  private final CareTaskDailyMapper careTaskDailyMapper;
  private final HealthInspectionMapper healthInspectionMapper;
  private final HealthMedicationTaskMapper healthMedicationTaskMapper;
  private final InventoryService inventoryService;
  private final MaterialPurchaseOrderMapper materialPurchaseOrderMapper;
  private final MaintenanceRequestMapper maintenanceRequestMapper;
  private final RoomCleaningTaskMapper roomCleaningTaskMapper;
  private final DiningMealOrderMapper diningMealOrderMapper;
  private final LogisticsEquipmentArchiveMapper logisticsEquipmentArchiveMapper;
  private final MarketingReportService marketingReportService;
  private final StaffMobileHandoverRecordMapper staffMobileHandoverRecordMapper;
  private final StaffMobilePatrolScanRecordMapper staffMobilePatrolScanRecordMapper;
  private final StaffMobileTaskReceiptMapper staffMobileTaskReceiptMapper;

  public OperationsCapabilityServiceImpl(
      IncidentReportMapper incidentReportMapper,
      FamilyRechargeOrderMapper familyRechargeOrderMapper,
      FamilyNotifyLogMapper familyNotifyLogMapper,
      FamilyPortalStateMapper familyPortalStateMapper,
      FamilyPortalProperties familyPortalProperties,
      SmartAlertMapper smartAlertMapper,
      FireSafetyRecordMapper fireSafetyRecordMapper,
      StaffMapper staffMapper,
      StaffProfileMapper staffProfileMapper,
      StaffTrainingRecordMapper staffTrainingRecordMapper,
      StaffRewardPunishmentMapper staffRewardPunishmentMapper,
      StaffScheduleMapper staffScheduleMapper,
      AttendanceRecordMapper attendanceRecordMapper,
      CareTaskDailyMapper careTaskDailyMapper,
      HealthInspectionMapper healthInspectionMapper,
      HealthMedicationTaskMapper healthMedicationTaskMapper,
      InventoryService inventoryService,
      MaterialPurchaseOrderMapper materialPurchaseOrderMapper,
      MaintenanceRequestMapper maintenanceRequestMapper,
      RoomCleaningTaskMapper roomCleaningTaskMapper,
      DiningMealOrderMapper diningMealOrderMapper,
      LogisticsEquipmentArchiveMapper logisticsEquipmentArchiveMapper,
      MarketingReportService marketingReportService,
      StaffMobileHandoverRecordMapper staffMobileHandoverRecordMapper,
      StaffMobilePatrolScanRecordMapper staffMobilePatrolScanRecordMapper,
      StaffMobileTaskReceiptMapper staffMobileTaskReceiptMapper) {
    this.incidentReportMapper = incidentReportMapper;
    this.familyRechargeOrderMapper = familyRechargeOrderMapper;
    this.familyNotifyLogMapper = familyNotifyLogMapper;
    this.familyPortalStateMapper = familyPortalStateMapper;
    this.familyPortalProperties = familyPortalProperties;
    this.smartAlertMapper = smartAlertMapper;
    this.fireSafetyRecordMapper = fireSafetyRecordMapper;
    this.staffMapper = staffMapper;
    this.staffProfileMapper = staffProfileMapper;
    this.staffTrainingRecordMapper = staffTrainingRecordMapper;
    this.staffRewardPunishmentMapper = staffRewardPunishmentMapper;
    this.staffScheduleMapper = staffScheduleMapper;
    this.attendanceRecordMapper = attendanceRecordMapper;
    this.careTaskDailyMapper = careTaskDailyMapper;
    this.healthInspectionMapper = healthInspectionMapper;
    this.healthMedicationTaskMapper = healthMedicationTaskMapper;
    this.inventoryService = inventoryService;
    this.materialPurchaseOrderMapper = materialPurchaseOrderMapper;
    this.maintenanceRequestMapper = maintenanceRequestMapper;
    this.roomCleaningTaskMapper = roomCleaningTaskMapper;
    this.diningMealOrderMapper = diningMealOrderMapper;
    this.logisticsEquipmentArchiveMapper = logisticsEquipmentArchiveMapper;
    this.marketingReportService = marketingReportService;
    this.staffMobileHandoverRecordMapper = staffMobileHandoverRecordMapper;
    this.staffMobilePatrolScanRecordMapper = staffMobilePatrolScanRecordMapper;
    this.staffMobileTaskReceiptMapper = staffMobileTaskReceiptMapper;
  }

  @Override
  public OperationsCapabilityMapResponse capabilityMap() {
    List<DomainCapability> domains = List.of(
        domain("lifecycle", "老人全生命周期管理", "入住评估、试住、正式入住、转房、退住、离院和归档形成统一生命周期。",
            "READY", "/elder/resident-360",
            List.of("入住评估", "试住登记", "外出请假", "退住申请", "退住结算", "离院/离世登记", "状态变更日志"),
            List.of("把护理等级调整记录沉淀到长者 360 画像", "补齐风险告知书与知情同意书模板"),
            List.of("/elder/assessment/ability/admission", "/elder/status-change", "/elder/change-log")),
        domain("care", "护理服务闭环", "护理计划、每日任务、扫码执行、异常上报、质检复盘和交接班可串联执行。",
            "READY", "/medical-care/care-task-board",
            List.of("护理计划", "任务派发", "扫码执行", "异常任务", "护理质检", "交接班"),
            List.of("为执行记录补充照片强校验场景", "把质检结果反写到护理员绩效"),
            List.of("/care/workbench/plan", "/medical-care/care-task-board", "/medical-care/nursing-quality")),
        domain("health", "健康与用药管理", "生命体征、慢病、医嘱、发药、剩余用药、巡检和异常规则集中到医护健康服务。",
            "READY", "/medical-care/center",
            List.of("健康档案", "健康数据", "慢病维护", "就医记录", "检查报告", "康复记录", "用药任务", "趋势预警"),
            List.of("把检查报告附件统一归入合规档案包", "把趋势预警处置结果反写到护理观察任务"),
            List.of("/health/management/archive", "/health/management/data", "/medical-care/orders", "/medical-care/smart-alerts")),
        domain("staff-mobile", "员工移动端 / 小程序", "护理员、医生和后勤的一线手机端承接护理执行、查房、用药、巡检、维修和送餐闭环。",
            "READY", "/stats/staff-mobile",
            List.of("护理执行", "查房巡诊", "用药确认", "巡检异常", "维修派单", "餐食配送"),
            List.of("深化离线暂存", "按角色推送待办提醒"),
            List.of("/stats/staff-mobile", "/medical-care/care-task-board", "/medical-care/inspection")),
        domain("safety", "安全风险管理", "跌倒、离床、事故、夜间巡查、设备告警和应急处置在安全风险中心统一闭环。",
            "READY", "/stats/operations?tab=safety",
            List.of("事故登记", "应急预案", "现场处置", "家属告知", "整改复盘", "消防夜间巡查", "智慧设备告警", "异常规则配置"),
            List.of("深化门禁/定位设备实时状态", "生成风险事件统计月报"),
            List.of("/stats/operations?tab=safety", "/life/incident", "/fire/night-patrol", "/medical-care/smart-alerts")),
        domain("family", "家属端与服务连接", "家属可围绕动态、缴费、探访、沟通、投诉建议和生日关怀形成服务触点。",
            "PARTIAL", "/oa/family-service",
            List.of("家属账号", "在线充值/缴费", "微信通知", "家属沟通", "投诉建议", "生日关怀"),
            List.of("补齐小程序护理记录/健康数据可见范围", "新增探访预约与外出申请家属端闭环"),
            List.of("/oa/work-execution/family-communication", "/finance/payments/cashier-desk", "/oa/life/birthday")),
        domain("bed-finance", "床位、合同与收费", "床态、房间、合同、押金、账单、欠费、退住结算和票据已具备运营主链路。",
            "READY", "/finance/workbench",
            List.of("床态全景", "房间楼栋", "合同管理", "押金账户", "周期账单", "欠费催缴", "发票收据", "退住结算"),
            List.of("完善多套餐价格模拟", "把合同变更与护理等级变更自动联动账单"),
            List.of("/logistics/assets/room-state-map", "/logistics/assets/bed-management", "/marketing/contracts", "/finance/bills/in-resident")),
        domain("staff", "员工与排班管理", "员工档案、资质、培训、考勤、排班、绩效和证照提醒支撑护理人力调度。",
            "READY", "/stats/operations?tab=workforce",
            List.of("员工档案", "护理员分组", "排班日历", "请假调班", "培训记录", "证照提醒", "绩效"),
            List.of("接入智能排班建议", "按护理等级自动核算班次缺口"),
            List.of("/hr/profile/basic", "/hr/development/certificate-reminders", "/care/scheduling/shift-calendar", "/hr/development/records")),
        domain("logistics", "物资、餐饮与后勤", "药品耗材、出入库、库存预警、设备维护、保洁洗衣、维修工单和食谱餐饮已分域管理。",
            "READY", "/stats/operations?tab=logistics",
            List.of("物资主数据", "出入库", "长者耗材归集", "库存预警", "设备台账", "维修工单", "保洁记录", "食谱与点餐"),
            List.of("把生日/活动/护理耗材自动生成备料单"),
            List.of("/logistics/storage/outbound", "/logistics/storage/stock-query", "/logistics/task-center", "/logistics/dining/recipe")),
        domain("marketing", "营销与客户管理", "线索、咨询、参观预约、销售跟进、漏斗、渠道和转介绍可支撑入住转化。",
            "READY", "/stats/operations?tab=marketing",
            List.of("线索池", "咨询记录", "跟进任务", "参观预约", "合同签约", "转化漏斗", "渠道报表"),
            List.of("把营销承诺同步到入住交接清单", "补齐老客户转介绍奖励配置"),
            List.of("/marketing/leads/all", "/marketing/funnel/consultation", "/marketing/reports/conversion")),
        domain("quality-compliance", "服务质量与合规档案", "质检、审批、合同票据、风险报告、政府报表和操作日志需要统一进入合规视角。",
            "PARTIAL", "/stats/operations?tab=quality",
            List.of("护理完成率", "异常处理", "护理月报", "审批流程", "合同票据", "事故报告", "操作日志", "问卷满意度"),
            List.of("建设合规档案包下载", "把政府监管报表做成固定导出模板"),
            List.of("/care/service/nursing-reports", "/survey/stats", "/stats/org/monthly-operation"))
    );

    OperationsCapabilityMapResponse response = new OperationsCapabilityMapResponse();
    response.setVersion(VERSION);
    response.setGeneratedAt(LocalDateTime.now().format(DATETIME_FMT));
    response.setDomains(domains);
    response.setMetrics(buildMetrics());
    response.setIntelligence(buildIntelligence());
    response.setSummary(buildSummary(domains));
    response.setOverallStatus(resolveOverallStatus(domains));
    return response;
  }

  @Override
  public OperationsServiceReportResponse serviceReport(String month) {
    YearMonth reportMonth = parseMonth(month);
    OperationsServiceReportResponse response = new OperationsServiceReportResponse();
    response.setMonth(reportMonth.toString());
    response.setGeneratedAt(LocalDateTime.now().format(DATETIME_FMT));
    response.setReportTitle(reportMonth + " 综合运营服务质量与合规报告");
    response.setStatus("LANDING");
    response.setMetrics(buildReportMetrics());
    response.setQualityDomains(buildQualityDomains());
    response.setComplianceArchives(buildComplianceArchives());
    response.setStandardProcesses(buildStandardProcesses());
    response.setMonthlyActions(buildMonthlyActions());
    return response;
  }

  @Override
  public OperationsFamilyServiceResponse familyService() {
    Long orgId = AuthContext.getOrgId();
    OperationsFamilyServiceResponse response = new OperationsFamilyServiceResponse();
    response.setGeneratedAt(LocalDateTime.now().format(DATETIME_FMT));
    response.setStatus("LANDING");
    response.setMetrics(buildFamilyMetrics());
    response.setRuntimeCards(buildFamilyRuntimeCards(orgId));
    response.setFlows(buildFamilyFlows());
    response.setVisibleData(buildFamilyVisibleData());
    response.setActions(buildFamilyActions());
    return response;
  }

  @Override
  public OperationsIntelligenceResponse intelligence() {
    OperationsIntelligenceResponse response = new OperationsIntelligenceResponse();
    response.setGeneratedAt(LocalDateTime.now().format(DATETIME_FMT));
    response.setStatus("LANDING");
    response.setMetrics(buildIntelligenceMetrics());
    response.setScenarios(buildIntelligenceScenarios());
    response.setDataSources(buildIntelligenceDataSources());
    response.setActions(buildIntelligenceActions());
    return response;
  }

  @Override
  public OperationsSafetyRiskResponse safetyRisk() {
    Long orgId = AuthContext.getOrgId();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

    long openIncidentCount = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(IncidentReport::getStatus, "OPEN"));
    long monthIncidentCount = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .ge(IncidentReport::getIncidentTime, monthStart));
    long majorIncidentCount = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(IncidentReport::getLevel, "MAJOR")
        .ge(IncidentReport::getIncidentTime, monthStart));
    long openAlertCount = smartAlertMapper.selectCount(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(orgId != null, SmartAlert::getOrgId, orgId)
        .ne(SmartAlert::getStatus, "RESOLVED"));
    long criticalAlertCount = smartAlertMapper.selectCount(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(orgId != null, SmartAlert::getOrgId, orgId)
        .eq(SmartAlert::getLevel, "CRITICAL")
        .ne(SmartAlert::getStatus, "RESOLVED"));
    long nightPatrolOpenCount = fireSafetyRecordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .eq(FireSafetyRecord::getRecordType, "NIGHT_PATROL")
        .ne(FireSafetyRecord::getStatus, "CLOSED"));
    long fireOpenCount = fireSafetyRecordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .ne(FireSafetyRecord::getStatus, "CLOSED"));
    long overdueFireCount = fireSafetyRecordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .ne(FireSafetyRecord::getStatus, "CLOSED")
        .isNotNull(FireSafetyRecord::getCheckTime)
        .lt(FireSafetyRecord::getCheckTime, now.minusHours(12)));
    // 与首页「未闭环风险」卡片同口径：体检异常/跟进中 + 维修超时工单
    long inspectionOpenCount = healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .in(HealthInspection::getStatus, List.of("ABNORMAL", "FOLLOWING")));
    long maintenanceOverdueCount = maintenanceRequestMapper.selectCount(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getIsDeleted, 0)
        .eq(orgId != null, MaintenanceRequest::getOrgId, orgId)
        .in(MaintenanceRequest::getStatus, List.of("OPEN", "PROCESSING"))
        .lt(MaintenanceRequest::getReportedAt, now.minusDays(2)));

    int riskIndex = (int) Math.min(100,
        openIncidentCount * 12
            + monthIncidentCount * 4
            + majorIncidentCount * 18
            + openAlertCount * 6
            + criticalAlertCount * 18
            + nightPatrolOpenCount * 8
            + overdueFireCount * 10
            + inspectionOpenCount * 6
            + maintenanceOverdueCount * 8);

    OperationsSafetyRiskResponse response = new OperationsSafetyRiskResponse();
    response.setGeneratedAt(now.format(DATETIME_FMT));
    response.setStatus("LANDING");
    response.setRiskIndex(riskIndex);
    response.setRiskLevel(resolveSafetyRiskLevel(riskIndex));
    response.setMetrics(List.of(
        safetyMetric("open_incident", "未闭环事故", openIncidentCount + " 起", "事故登记中仍处于处理中状态", openIncidentCount > 0 ? "danger" : "normal", "/life/incident?status=OPEN"),
        safetyMetric("month_incident", "本月事故", monthIncidentCount + " 起", "含一般/重大事故，用于安全复盘和监管留痕", monthIncidentCount > 0 ? "warning" : "normal", "/life/incident"),
        safetyMetric("smart_alert", "设备未闭环告警", openAlertCount + " 条", "呼叫器、床垫、定位/门禁等设备告警", openAlertCount > 0 ? "danger" : "normal", "/medical-care/smart-alerts?status=OPEN"),
        safetyMetric("critical_alert", "紧急设备告警", criticalAlertCount + " 条", "需要优先确认和处置的紧急告警", criticalAlertCount > 0 ? "danger" : "normal", "/medical-care/smart-alerts?level=CRITICAL"),
        safetyMetric("night_patrol", "夜间巡查未闭环", nightPatrolOpenCount + " 项", "消防夜间巡查、交接和扫码记录", nightPatrolOpenCount > 0 ? "warning" : "normal", "/fire/night-patrol?status=OPEN"),
        safetyMetric("fire_open", "消防安全待办", fireOpenCount + " 项", "设施、巡查、维护、故障处理未闭环", fireOpenCount > 0 ? "warning" : "normal", "/fire/data-stats"),
        safetyMetric("inspection_abnormal", "体检异常未闭环", inspectionOpenCount + " 条", "健康巡检异常与跟进中记录，需医护复核闭环", inspectionOpenCount > 0 ? "danger" : "normal", "/health/inspection?status=OPEN"),
        safetyMetric("maintenance_overdue", "维修超时工单", maintenanceOverdueCount + " 单", "报修超过 2 天未完成的后勤工单", maintenanceOverdueCount > 0 ? "warning" : "normal", "/logistics/assets/maintenance-record")
    ));
    response.setRiskSources(buildSafetyRiskSources(openIncidentCount, majorIncidentCount, openAlertCount, criticalAlertCount, nightPatrolOpenCount, fireOpenCount));
    response.setEmergencyFlows(buildEmergencyFlows());
    response.setActions(buildSafetyActions(openIncidentCount, openAlertCount, criticalAlertCount, nightPatrolOpenCount, overdueFireCount));
    response.setRecentEvents(buildSafetyRecentEvents(orgId));
    return response;
  }

  @Override
  public OperationsWorkforceResponse workforce() {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    LocalDate monthStart = today.withDayOfMonth(1);
    LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

    long activeStaffCount = staffMapper.selectCount(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .eq(StaffAccount::getStatus, 1));
    long profileCount = staffProfileMapper.selectCount(Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getIsDeleted, 0)
        .eq(orgId != null, StaffProfile::getOrgId, orgId));
    long certificateMissingCount = staffProfileMapper.selectCount(Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getIsDeleted, 0)
        .eq(orgId != null, StaffProfile::getOrgId, orgId)
        .and(w -> w.isNull(StaffProfile::getCertificateNo).or().eq(StaffProfile::getCertificateNo, "")));
    long contractExpiringCount = staffProfileMapper.selectCount(Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getIsDeleted, 0)
        .eq(orgId != null, StaffProfile::getOrgId, orgId)
        .isNotNull(StaffProfile::getContractEndDate)
        .between(StaffProfile::getContractEndDate, today, today.plusDays(30)));
    long todayScheduleCount = staffScheduleMapper.selectCount(Wrappers.lambdaQuery(StaffSchedule.class)
        .eq(StaffSchedule::getIsDeleted, 0)
        .eq(orgId != null, StaffSchedule::getOrgId, orgId)
        .eq(StaffSchedule::getDutyDate, today));
    long todayAttendanceCount = attendanceRecordMapper.selectCount(Wrappers.lambdaQuery(AttendanceRecord.class)
        .eq(AttendanceRecord::getIsDeleted, 0)
        .eq(orgId != null, AttendanceRecord::getOrgId, orgId)
        .eq(AttendanceRecord::getWorkDate, today)
        .isNotNull(AttendanceRecord::getCheckInTime));
    long todayLeaveCount = attendanceRecordMapper.selectCount(Wrappers.lambdaQuery(AttendanceRecord.class)
        .eq(AttendanceRecord::getIsDeleted, 0)
        .eq(orgId != null, AttendanceRecord::getOrgId, orgId)
        .eq(AttendanceRecord::getWorkDate, today)
        .eq(AttendanceRecord::getStatus, "ON_LEAVE"));
    long unreviewedAttendanceCount = attendanceRecordMapper.selectCount(Wrappers.lambdaQuery(AttendanceRecord.class)
        .eq(AttendanceRecord::getIsDeleted, 0)
        .eq(orgId != null, AttendanceRecord::getOrgId, orgId)
        .between(AttendanceRecord::getWorkDate, monthStart, monthEnd)
        .and(w -> w.isNull(AttendanceRecord::getReviewed).or().eq(AttendanceRecord::getReviewed, 0)));
    long monthTrainingCount = staffTrainingRecordMapper.selectCount(Wrappers.lambdaQuery(StaffTrainingRecord.class)
        .eq(StaffTrainingRecord::getIsDeleted, 0)
        .eq(orgId != null, StaffTrainingRecord::getOrgId, orgId)
        .ge(StaffTrainingRecord::getStartDate, monthStart)
        .le(StaffTrainingRecord::getStartDate, monthEnd));
    long trainingAbsentCount = staffTrainingRecordMapper.selectCount(Wrappers.lambdaQuery(StaffTrainingRecord.class)
        .eq(StaffTrainingRecord::getIsDeleted, 0)
        .eq(orgId != null, StaffTrainingRecord::getOrgId, orgId)
        .ge(StaffTrainingRecord::getStartDate, monthStart)
        .le(StaffTrainingRecord::getStartDate, monthEnd)
        .eq(StaffTrainingRecord::getAttendanceStatus, "ABSENT"));
    long monthRewardPunishmentCount = staffRewardPunishmentMapper.selectCount(Wrappers.lambdaQuery(StaffRewardPunishment.class)
        .eq(StaffRewardPunishment::getIsDeleted, 0)
        .eq(orgId != null, StaffRewardPunishment::getOrgId, orgId)
        .ge(StaffRewardPunishment::getOccurredDate, monthStart)
        .le(StaffRewardPunishment::getOccurredDate, monthEnd));
    long todayCareTaskCount = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getTaskDate, today));
    long pendingCareTaskCount = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getTaskDate, today)
        .ne(CareTaskDaily::getStatus, "DONE"));

    int workforceIndex = resolveWorkforceIndex(activeStaffCount, todayScheduleCount, todayAttendanceCount,
        todayLeaveCount, certificateMissingCount, contractExpiringCount, unreviewedAttendanceCount, pendingCareTaskCount);

    OperationsWorkforceResponse response = new OperationsWorkforceResponse();
    response.setGeneratedAt(LocalDateTime.now().format(DATETIME_FMT));
    response.setStatus("LANDING");
    response.setWorkforceIndex(workforceIndex);
    response.setWorkforceLevel(resolveWorkforceLevel(workforceIndex));
    response.setMetrics(List.of(
        workforceMetric("active_staff", "在职员工", activeStaffCount + " 人", "已启用员工账号，作为排班与绩效基础", activeStaffCount > 0 ? "normal" : "warning", "/hr/profile/basic"),
        workforceMetric("today_schedule", "今日排班", todayScheduleCount + " 人次", "当天已实施排班记录", todayScheduleCount > 0 ? "normal" : "warning", "/care/scheduling/shift-calendar"),
        workforceMetric("today_attendance", "今日在岗", todayAttendanceCount + " 人", "当天已打卡员工数", todayAttendanceCount >= todayScheduleCount && todayScheduleCount > 0 ? "normal" : "warning", "/hr/attendance/records"),
        workforceMetric("leave", "请假/离岗", todayLeaveCount + " 人", "当天请假员工，影响班次补位", todayLeaveCount > 0 ? "warning" : "normal", "/hr/attendance/leave-approval"),
        workforceMetric("certificate", "证照缺口", certificateMissingCount + " 人", "员工档案缺少证照编号，需要补齐资质", certificateMissingCount > 0 ? "danger" : "normal", "/hr/development/certificate-reminders"),
        workforceMetric("care_load", "护理任务负荷", pendingCareTaskCount + "/" + todayCareTaskCount, "今日未完成/总护理任务", pendingCareTaskCount > 0 ? "warning" : "normal", "/medical-care/care-task-board")
    ));
    response.setRisks(buildWorkforceRisks(certificateMissingCount, contractExpiringCount, unreviewedAttendanceCount,
        trainingAbsentCount, pendingCareTaskCount, todayScheduleCount, todayAttendanceCount));
    response.setRoleLoads(buildWorkforceRoleLoads(activeStaffCount, profileCount, todayScheduleCount,
        todayAttendanceCount, todayCareTaskCount, pendingCareTaskCount, monthTrainingCount, monthRewardPunishmentCount));
    response.setActions(buildWorkforceActions(certificateMissingCount, contractExpiringCount, unreviewedAttendanceCount,
        trainingAbsentCount, pendingCareTaskCount, todayScheduleCount, todayAttendanceCount));
    response.setFlows(buildWorkforceFlows());
    return response;
  }

  @Override
  public OperationsStaffMobileResponse staffMobile() {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();

    long careTaskTotalCount = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getTaskDate, today));
    long careTaskPendingCount = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getTaskDate, today)
        .ne(CareTaskDaily::getStatus, "DONE"));
    long medicationPendingCount = healthMedicationTaskMapper.selectCount(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
        .eq(HealthMedicationTask::getTaskDate, today)
        .eq(HealthMedicationTask::getStatus, "PENDING"));
    long inspectionOpenCount = healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(HealthInspection::getInspectionDate, today)
        .in(HealthInspection::getStatus, List.of("ABNORMAL", "FOLLOWING")));
    long maintenanceOpenCount = maintenanceRequestMapper.selectCount(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getIsDeleted, 0)
        .eq(orgId != null, MaintenanceRequest::getOrgId, orgId)
        .in(MaintenanceRequest::getStatus, List.of("OPEN", "PROCESSING")));
    List<DiningMealOrder> todayMealOrders = diningMealOrderMapper.selectList(Wrappers.lambdaQuery(DiningMealOrder.class)
        .eq(DiningMealOrder::getIsDeleted, 0)
        .eq(orgId != null, DiningMealOrder::getOrgId, orgId)
        .eq(DiningMealOrder::getOrderDate, today));
    long mealPendingCount = todayMealOrders.stream()
        .filter(item -> !"DELIVERED".equals(item.getStatus()))
        .count();
    long cleaningPendingCount = roomCleaningTaskMapper.selectCount(Wrappers.lambdaQuery(RoomCleaningTask.class)
        .eq(RoomCleaningTask::getIsDeleted, 0)
        .eq(orgId != null, RoomCleaningTask::getOrgId, orgId)
        .eq(RoomCleaningTask::getPlanDate, today)
        .ne(RoomCleaningTask::getStatus, "DONE"));

    int mobileIndex = resolveStaffMobileIndex(careTaskPendingCount, medicationPendingCount, inspectionOpenCount,
        maintenanceOpenCount, mealPendingCount, cleaningPendingCount);

    OperationsStaffMobileResponse response = new OperationsStaffMobileResponse();
    response.setGeneratedAt(now.format(DATETIME_FMT));
    response.setStatus("READY");
    response.setMobileIndex(mobileIndex);
    response.setMobileLevel(resolveStaffMobileLevel(mobileIndex));
    response.setMetrics(List.of(
        staffMobileMetric("care", "护理执行", careTaskPendingCount + "/" + careTaskTotalCount,
            "护理执行、补录与交接需要手机端随身处理", careTaskPendingCount > 0 ? "warning" : "normal", "/medical-care/care-task-board"),
        staffMobileMetric("medication", "用药任务", medicationPendingCount + " 项",
            "医护需要按时确认发药、追踪漏服与补记", medicationPendingCount > 0 ? "danger" : "normal", "/health/medication/medication-registration?date=today"),
        staffMobileMetric("inspection", "巡房巡检", inspectionOpenCount + " 项",
            "异常巡检必须第一时间拍照、记录和闭环", inspectionOpenCount > 0 ? "danger" : "normal", "/medical-care/inspection"),
        staffMobileMetric("maintenance", "维修派单", maintenanceOpenCount + " 单",
            "后勤在现场接单、到场确认和验收关闭", maintenanceOpenCount > 0 ? "warning" : "normal", "/logistics/task-center"),
        staffMobileMetric("meal", "餐食配送", mealPendingCount + " 单",
            "送餐、特殊餐、补送和签收都依赖移动操作", mealPendingCount > 0 ? "warning" : "normal", "/logistics/dining/order"),
        staffMobileMetric("cleaning", "巡检保洁", cleaningPendingCount + " 项",
            "保洁、洗衣、巡查和异常照片需要现场留痕", cleaningPendingCount > 0 ? "warning" : "normal", "/logistics/task-center")
    ));
    response.setRuntimeCards(buildStaffMobileRuntimeCards(orgId));
    response.setRoleCards(List.of(
        staffMobileRoleCard("caregiver", "护理员", "一线执行", "READY", "/medical-care/care-task-board",
            List.of("护理执行", "体征补录", "交接班", "异常上报"),
            List.of("扫码即办", "拍照留痕", "语音转写", "离线暂存")),
        staffMobileRoleCard("doctor", "医生 / 医护", "医嘱管理", "READY", "/medical-care/center",
            List.of("查房巡诊", "用药确认", "异常巡检", "医嘱跟进"),
            List.of("患者卡片", "快捷批注", "一键确认", "异常提醒")),
        staffMobileRoleCard("logistics", "后勤人员", "保障执行", "READY", "/logistics/task-center",
            List.of("维修派单", "餐食配送", "保洁巡检", "物资补给"),
            List.of("到场确认", "图片回执", "工单关闭", "扫码流转"))
    ));
    response.setWorkflows(List.of(
        staffMobileWorkflow("care-flow", "护理执行闭环", "READY", "/medical-care/care-task-board",
            List.of("接收今日任务", "现场执行并拍照", "补录体征/异常", "提交交接说明")),
        staffMobileWorkflow("round-flow", "查房与巡诊闭环", "READY", "/medical-care/inspection",
            List.of("查看巡房清单", "记录异常情况", "联动医嘱与用药", "完成复核关闭")),
        staffMobileWorkflow("delivery-flow", "送餐与保障闭环", "READY", "/logistics/dining/order",
            List.of("接单", "按餐型备餐", "送达签收", "异常回单"))
    ));
    response.setActions(buildStaffMobileActions(careTaskPendingCount, medicationPendingCount, inspectionOpenCount,
        maintenanceOpenCount, mealPendingCount, cleaningPendingCount));
    return response;
  }

  private List<FamilyRuntimeCard> buildFamilyRuntimeCards(Long orgId) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
    long pendingRechargeCount = familyRechargeOrderMapper.selectCount(Wrappers.lambdaQuery(FamilyRechargeOrder.class)
        .eq(FamilyRechargeOrder::getIsDeleted, 0)
        .eq(orgId != null, FamilyRechargeOrder::getOrgId, orgId)
        .in(FamilyRechargeOrder::getStatus, "INIT", "PREPAY_CREATED"));
    long abnormalRechargeCount = familyRechargeOrderMapper.selectCount(Wrappers.lambdaQuery(FamilyRechargeOrder.class)
        .eq(FamilyRechargeOrder::getIsDeleted, 0)
        .eq(orgId != null, FamilyRechargeOrder::getOrgId, orgId)
        .in(FamilyRechargeOrder::getStatus, "FAILED", "CLOSED"));
    long paidTodayCount = familyRechargeOrderMapper.selectCount(Wrappers.lambdaQuery(FamilyRechargeOrder.class)
        .eq(FamilyRechargeOrder::getIsDeleted, 0)
        .eq(orgId != null, FamilyRechargeOrder::getOrgId, orgId)
        .eq(FamilyRechargeOrder::getStatus, "PAID")
        .ge(FamilyRechargeOrder::getPaidAt, todayStart));
    long notifyPendingCount = familyNotifyLogMapper.selectCount(Wrappers.lambdaQuery(FamilyNotifyLog.class)
        .eq(FamilyNotifyLog::getIsDeleted, 0)
        .eq(orgId != null, FamilyNotifyLog::getOrgId, orgId)
        .eq(FamilyNotifyLog::getStatus, "PENDING"));
    long notifyFailedCount = familyNotifyLogMapper.selectCount(Wrappers.lambdaQuery(FamilyNotifyLog.class)
        .eq(FamilyNotifyLog::getIsDeleted, 0)
        .eq(orgId != null, FamilyNotifyLog::getOrgId, orgId)
        .eq(FamilyNotifyLog::getStatus, "FAILED"));
    long communicationCount7d = familyPortalStateMapper.selectCount(Wrappers.lambdaQuery(FamilyPortalState.class)
        .eq(FamilyPortalState::getIsDeleted, 0)
        .eq(orgId != null, FamilyPortalState::getOrgId, orgId)
        .eq(FamilyPortalState::getCategory, FAMILY_STATE_COMMUNICATION)
        .ge(FamilyPortalState::getUpdateTime, now.minusDays(7)));

    FamilyPortalProperties.SmsCode smsCode = familyPortalProperties.getSmsCode() == null
        ? new FamilyPortalProperties.SmsCode()
        : familyPortalProperties.getSmsCode();
    FamilyPortalProperties.WechatNotify notify = familyPortalProperties.getWechatNotify() == null
        ? new FamilyPortalProperties.WechatNotify()
        : familyPortalProperties.getWechatNotify();
    FamilyPortalProperties.WechatPay pay = familyPortalProperties.getWechatPay() == null
        ? new FamilyPortalProperties.WechatPay()
        : familyPortalProperties.getWechatPay();

    return List.of(
        familyRuntimeCard(
            "sms-runtime",
            "短信验证码",
            smsCode.isEnabled() ? defaultText(smsCode.getProvider(), "unknown").toUpperCase(Locale.ROOT) : "OFF",
            smsCode.isEnabled() && !"mock".equalsIgnoreCase(defaultText(smsCode.getProvider(), "")) && !smsCode.isDebugReturnCode() ? "READY" : "WARNING",
            smsCode.isEnabled()
                ? "冷却 " + smsCode.getCooldownSeconds() + " 秒，" + (smsCode.isDebugReturnCode() ? "当前仍返回调试验证码" : "正式验证码链路")
                : "当前未启用短信验证码",
            "/oa/family-service-health"),
        familyRuntimeCard(
            "notify-runtime",
            "微信通知",
            "待重试 " + notifyPendingCount + " · 失败 " + notifyFailedCount,
            notify.isEnabled() && notifyFailedCount == 0 ? "READY" : "WARNING",
            notify.isEnabled()
                ? "模板页 " + defaultText(notify.getPage(), "pages/messages/index")
                : "订阅通知尚未启用",
            "/oa/family-service-health"),
        familyRuntimeCard(
            "pay-runtime",
            "充值与支付",
            "待支付 " + pendingRechargeCount + " · 异常 " + abnormalRechargeCount,
            pay.isEnabled() && abnormalRechargeCount == 0 && !pay.isSkipNotifySignatureVerify() ? "READY" : "WARNING",
            "今日到账 " + paidTodayCount + " 笔" + (pay.isSkipNotifySignatureVerify() ? "，当前仍跳过回调验签" : "，回调验签开启"),
            "/oa/family-service-health"),
        familyRuntimeCard(
            "communication-runtime",
            "家属沟通",
            "近 7 天 " + communicationCount7d + " 条",
            communicationCount7d > 0 ? "READY" : "PARTIAL",
            "含留言、语音与探视预约等家属互动记录",
            "/oa/work-execution/family-communication")
    );
  }

  private List<StaffMobileRuntimeCard> buildStaffMobileRuntimeCards(Long orgId) {
    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
    long receiptTodayCount = staffMobileTaskReceiptMapper.selectCount(Wrappers.lambdaQuery(StaffMobileTaskReceipt.class)
        .eq(StaffMobileTaskReceipt::getIsDeleted, 0)
        .eq(orgId != null, StaffMobileTaskReceipt::getOrgId, orgId)
        .ge(StaffMobileTaskReceipt::getReceiptTime, todayStart));
    long photoEvidenceCount = staffMobileTaskReceiptMapper.selectCount(Wrappers.lambdaQuery(StaffMobileTaskReceipt.class)
        .eq(StaffMobileTaskReceipt::getIsDeleted, 0)
        .eq(orgId != null, StaffMobileTaskReceipt::getOrgId, orgId)
        .ge(StaffMobileTaskReceipt::getReceiptTime, todayStart)
        .and(w -> w.isNotNull(StaffMobileTaskReceipt::getPhotoUrls).ne(StaffMobileTaskReceipt::getPhotoUrls, "")));
    long voiceEvidenceCount = staffMobileTaskReceiptMapper.selectCount(Wrappers.lambdaQuery(StaffMobileTaskReceipt.class)
        .eq(StaffMobileTaskReceipt::getIsDeleted, 0)
        .eq(orgId != null, StaffMobileTaskReceipt::getOrgId, orgId)
        .ge(StaffMobileTaskReceipt::getReceiptTime, todayStart)
        .and(w -> w.isNotNull(StaffMobileTaskReceipt::getVoiceUrl).ne(StaffMobileTaskReceipt::getVoiceUrl, "")));
    long handoverTodayCount = staffMobileHandoverRecordMapper.selectCount(Wrappers.lambdaQuery(StaffMobileHandoverRecord.class)
        .eq(StaffMobileHandoverRecord::getIsDeleted, 0)
        .eq(orgId != null, StaffMobileHandoverRecord::getOrgId, orgId)
        .ge(StaffMobileHandoverRecord::getHandoverTime, todayStart));
    long incidentOpenCount = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(IncidentReport::getStatus, "OPEN")
        .ge(IncidentReport::getCreateTime, todayStart));
    long incidentEvidenceCount = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .ge(IncidentReport::getCreateTime, todayStart)
        .and(w -> w.isNotNull(IncidentReport::getAttachmentUrls).ne(IncidentReport::getAttachmentUrls, "")
            .or().isNotNull(IncidentReport::getVoiceUrl).ne(IncidentReport::getVoiceUrl, "")));

    return List.of(
        staffMobileRuntimeCard(
            "receipt-runtime",
            "今日现场回执",
            String.valueOf(receiptTodayCount),
            receiptTodayCount > 0 ? "READY" : "WARNING",
            "护理、用药、巡检、维修、送餐统一回执条数",
            "/stats/staff-mobile-ledger"),
        staffMobileRuntimeCard(
            "evidence-runtime",
            "证据留痕",
            "照片 " + photoEvidenceCount + " · 语音 " + voiceEvidenceCount,
            receiptTodayCount == 0 || (photoEvidenceCount == 0 && voiceEvidenceCount == 0) ? "WARNING" : "READY",
            "统计今日回执中已上传的照片与语音证据",
            "/stats/staff-mobile-ledger"),
        staffMobileRuntimeCard(
            "handover-runtime",
            "班次交接",
            String.valueOf(handoverTodayCount),
            handoverTodayCount > 0 ? "READY" : "WARNING",
            "今日移动端提交的交接记录数量",
            "/stats/staff-mobile-ledger"),
        staffMobileRuntimeCard(
            "incident-runtime",
            "现场异常",
            "开放 " + incidentOpenCount + " · 留痕 " + incidentEvidenceCount,
            incidentOpenCount == 0 ? "READY" : "WARNING",
            "今日员工端异常上报及照片/语音取证情况",
            "/stats/staff-mobile-ledger")
    );
  }

  @Override
  public List<StaffMobileTask> staffMobileTasks(String module) {
    String normalizedModule = normalizeModule(module);
    Set<String> allowedModules = allowedModulesForCurrentStaff();
    List<StaffMobileTask> tasks = new ArrayList<>();
    if (staffMobileModuleVisible("CARE", normalizedModule, allowedModules)) {
      tasks.addAll(buildCareMobileTasks());
    }
    if (staffMobileModuleVisible("MEDICATION", normalizedModule, allowedModules)) {
      tasks.addAll(buildMedicationMobileTasks());
    }
    if (staffMobileModuleVisible("INSPECTION", normalizedModule, allowedModules)) {
      tasks.addAll(buildInspectionMobileTasks());
    }
    if (staffMobileModuleVisible("LOGISTICS", normalizedModule, allowedModules)) {
      tasks.addAll(buildMaintenanceMobileTasks());
    }
    if (staffMobileModuleVisible("MEAL", normalizedModule, allowedModules)) {
      tasks.addAll(buildMealMobileTasks());
    }
    return tasks.stream()
        .sorted(Comparator.comparing(StaffMobileTask::getTime, Comparator.nullsLast(String::compareTo)))
        .limit(50)
        .toList();
  }

  private boolean staffMobileModuleVisible(String targetModule, String requestedModule, Set<String> allowedModules) {
    if (requestedModule != null && !requestedModule.equals(targetModule)) {
      return false;
    }
    return allowedModules.contains(targetModule);
  }

  /**
   * 依据当前员工角色限定移动端可见模块。角色编码来源于 V147 演示数据与 RBAC 矩阵
   * （NURSING_EMPLOYEE/NURSING_MINISTER、MEDICAL_EMPLOYEE/MEDICAL_MINISTER、
   * LOGISTICS_EMPLOYEE/LOGISTICS_MINISTER 等）：
   * <ul>
   *   <li>护理类（NURSING_*）→ 护理执行 CARE + 餐食配送 MEAL；巡检（HealthInspection）
   *       属于医护健康域（巡房巡诊），因此不划入护理类；</li>
   *   <li>医护类（MEDICAL_*）→ 用药确认 MEDICATION + 巡房巡检 INSPECTION；</li>
   *   <li>后勤类（LOGISTICS_*）→ 维修工单 LOGISTICS + 餐食配送 MEAL。</li>
   * </ul>
   * admin（ADMIN/SYS_ADMIN/DIRECTOR）或未匹配到任何已知一线角色
   * （如 FINANCE_、HR_、MARKETING_ 前缀角色、STAFF 或无角色）时返回全部模块，
   * 保持向后兼容、避免误伤存量账号。
   */
  private Set<String> allowedModulesForCurrentStaff() {
    if (AuthContext.isAdmin()) {
      return STAFF_MOBILE_ALL_MODULES;
    }
    Set<String> allowed = new LinkedHashSet<>();
    for (String roleCode : AuthContext.getRoleCodes()) {
      String normalized = roleCode == null ? "" : roleCode.trim().toUpperCase(Locale.ROOT);
      if (normalized.startsWith("NURSING_")) {
        allowed.add("CARE");
        allowed.add("MEAL");
      } else if (normalized.startsWith("MEDICAL_")) {
        allowed.add("MEDICATION");
        allowed.add("INSPECTION");
      } else if (normalized.startsWith("LOGISTICS_")) {
        allowed.add("LOGISTICS");
        allowed.add("MEAL");
      }
    }
    return allowed.isEmpty() ? STAFF_MOBILE_ALL_MODULES : allowed;
  }

  @Override
  public StaffMobileTask staffMobileTaskDetail(String id) {
    return staffMobileTasks(null).stream()
        .filter(item -> id != null && id.equals(item.getId()))
        .findFirst()
        .orElseGet(() -> {
          if (staffMobileDemoFallback) {
            // 演示/联调模式下保留 demo 兜底数据
            return fallbackStaffMobileTasks().get(0);
          }
          throw new IllegalArgumentException("任务不存在或已完成");
        });
  }

  @Override
  public StaffMobileReceipt completeStaffMobileTask(String id, StaffMobileCompleteTaskRequest request) {
    StaffMobileCompleteTaskRequest safeRequest = request == null ? new StaffMobileCompleteTaskRequest() : request;
    boolean onDuty = isCurrentStaffOnDutyToday();
    if (staffMobileRequireOnDuty && !onDuty) {
      throw new IllegalStateException("请先完成上班打卡后再提交任务回执");
    }
    if (!onDuty) {
      // 未开启强制在班校验时不拦截，但在回执与业务单备注中追加标记，方便主管稽核
      safeRequest.setRemark(appendOffDutyMark(safeRequest.getRemark()));
    }
    String remark = safeRequest.getRemark();
    LocalDateTime now = LocalDateTime.now();
    saveStaffMobileTaskReceipt(id, safeRequest, now);
    if (id != null && id.startsWith("care-")) {
      Long entityId = parseMobileId(id, "care-");
      CareTaskDaily entity = entityId == null ? null : careTaskDailyMapper.selectById(entityId);
      if (entity != null) {
        entity.setStatus("DONE");
        entity.setUpdateTime(now);
        careTaskDailyMapper.updateById(entity);
      }
    } else if (id != null && id.startsWith("med-")) {
      Long entityId = parseMobileId(id, "med-");
      HealthMedicationTask entity = entityId == null ? null : healthMedicationTaskMapper.selectById(entityId);
      if (entity != null) {
        entity.setStatus("DONE");
        entity.setDoneTime(now);
        entity.setRemark(mergeMobileRemark(remark, safeRequest.getScanText()));
        entity.setUpdateTime(now);
        healthMedicationTaskMapper.updateById(entity);
      }
    } else if (id != null && id.startsWith("inspect-")) {
      Long entityId = parseMobileId(id, "inspect-");
      HealthInspection entity = entityId == null ? null : healthInspectionMapper.selectById(entityId);
      if (entity != null) {
        entity.setStatus("DONE");
        entity.setRemark(mergeMobileRemark(remark, safeRequest.getScanText()));
        entity.setAttachmentUrls(String.join(",", safeRequest.getPhotos() == null ? List.of() : safeRequest.getPhotos()));
        entity.setUpdateTime(now);
        healthInspectionMapper.updateById(entity);
      }
    } else if (id != null && id.startsWith("repair-")) {
      Long entityId = parseMobileId(id, "repair-");
      MaintenanceRequest entity = entityId == null ? null : maintenanceRequestMapper.selectById(entityId);
      if (entity != null) {
        entity.setStatus("DONE");
        entity.setCompletedAt(now);
        entity.setRemark(mergeMobileRemark(remark, safeRequest.getScanText()));
        entity.setUpdateTime(now);
        maintenanceRequestMapper.updateById(entity);
      }
    } else if (id != null && id.startsWith("meal-")) {
      Long entityId = parseMobileId(id, "meal-");
      DiningMealOrder entity = entityId == null ? null : diningMealOrderMapper.selectById(entityId);
      if (entity != null) {
        entity.setStatus("DELIVERED");
        entity.setRemark(mergeMobileRemark(remark, safeRequest.getScanText()));
        entity.setUpdateTime(now);
        diningMealOrderMapper.updateById(entity);
      }
    }
    return staffMobileReceipt(id, "DONE", "现场回执已提交");
  }

  @Override
  public List<StaffMobileTaskReceiptView> staffMobileTaskReceipts(String module) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.isAdmin() ? null : AuthContext.getStaffId();
    String normalizedModule = normalizeModule(module);
    List<StaffMobileTaskReceipt> receipts = staffMobileTaskReceiptMapper.selectList(Wrappers.lambdaQuery(StaffMobileTaskReceipt.class)
        .eq(StaffMobileTaskReceipt::getIsDeleted, 0)
        .eq(orgId != null, StaffMobileTaskReceipt::getOrgId, orgId)
        .eq(staffId != null, StaffMobileTaskReceipt::getStaffId, staffId)
        .eq(normalizedModule != null, StaffMobileTaskReceipt::getTaskModule, normalizedModule)
        .orderByDesc(StaffMobileTaskReceipt::getReceiptTime)
        .last("limit 50"));
    return receipts.stream().map(this::toMobileTaskReceiptView).toList();
  }

  @Override
  public List<StaffMobileSchedule> staffMobileSchedule() {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    LocalDate today = LocalDate.now();
    List<StaffSchedule> schedules = staffScheduleMapper.selectList(Wrappers.lambdaQuery(StaffSchedule.class)
        .eq(StaffSchedule::getIsDeleted, 0)
        .eq(orgId != null, StaffSchedule::getOrgId, orgId)
        .eq(staffId != null, StaffSchedule::getStaffId, staffId)
        .ge(StaffSchedule::getDutyDate, today)
        .le(StaffSchedule::getDutyDate, today.plusDays(6))
        .orderByAsc(StaffSchedule::getDutyDate, StaffSchedule::getStartTime)
        .last("limit 20"));
    List<StaffMobileSchedule> result = schedules.stream().map(this::toMobileSchedule).toList();
    return result.isEmpty() ? fallbackStaffMobileSchedule() : result;
  }

  @Override
  public List<StaffMobileHandover> staffMobileHandovers() {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.isAdmin() ? null : AuthContext.getStaffId();
    List<StaffMobileHandoverRecord> records = staffMobileHandoverRecordMapper.selectList(Wrappers.lambdaQuery(StaffMobileHandoverRecord.class)
        .eq(StaffMobileHandoverRecord::getIsDeleted, 0)
        .eq(orgId != null, StaffMobileHandoverRecord::getOrgId, orgId)
        .and(staffId != null, wrapper -> wrapper
            .eq(StaffMobileHandoverRecord::getStaffId, staffId)
            .or()
            .isNull(StaffMobileHandoverRecord::getStaffId))
        .orderByDesc(StaffMobileHandoverRecord::getHandoverTime)
        .last("limit 20"));
    if (!records.isEmpty()) {
      return records.stream().map(this::toMobileHandover).toList();
    }
    return List.of(
        staffMobileHandover("handover-care", "07:30", "早班接班重点", "护理主管",
            "请优先关注高风险老人、夜间起夜记录和未完成用药任务。"),
        staffMobileHandover("handover-logistics", "15:20", "后勤交接重点", "后勤主管",
            "维修、送餐、保洁和物资补给异常需在员工端提交照片和处理说明。")
    );
  }

  @Override
  public StaffMobileHandover submitStaffMobileHandover(StaffMobileHandoverRequest request) {
    StaffMobileHandoverRequest safeRequest = request == null ? new StaffMobileHandoverRequest() : request;
    LocalDateTime now = LocalDateTime.now();
    StaffMobileHandoverRecord record = new StaffMobileHandoverRecord();
    record.setOrgId(AuthContext.getOrgId());
    record.setStaffId(AuthContext.getStaffId());
    record.setTitle(blankToDefault(safeRequest.getTitle(), "移动端交接补充"));
    record.setOwner(blankToDefault(safeRequest.getOwner(), blankToDefault(AuthContext.getUsername(), "当前员工")));
    record.setContent(blankToDefault(safeRequest.getContent(), "无补充内容"));
    record.setHandoverTime(now);
    record.setCreatedBy(AuthContext.getStaffId());
    record.setCreateTime(now);
    record.setUpdateTime(now);
    record.setIsDeleted(0);
    staffMobileHandoverRecordMapper.insert(record);
    return toMobileHandover(record);
  }

  @Override
  public List<StaffMobilePatrolPoint> staffMobilePatrolPoints() {
    List<StaffMobilePatrolPoint> points = List.of(
        staffMobilePatrolPoint("P-CARE-STATION", "护理站巡检点", "护理站", "待扫码"),
        staffMobilePatrolPoint("P-MEDICINE", "药房核验点", "医护站 / 药房", "待扫码"),
        staffMobilePatrolPoint("P-DINING", "餐车交接点", "餐梯口 / 备餐间", "待扫码"),
        staffMobilePatrolPoint("P-FIRE", "消防巡查点", "楼层消防通道", "待扫码")
    );
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    List<StaffMobilePatrolScanRecord> scans = staffMobilePatrolScanRecordMapper.selectList(Wrappers.lambdaQuery(StaffMobilePatrolScanRecord.class)
        .eq(StaffMobilePatrolScanRecord::getIsDeleted, 0)
        .eq(orgId != null, StaffMobilePatrolScanRecord::getOrgId, orgId)
        .ge(StaffMobilePatrolScanRecord::getScanTime, today.atStartOfDay())
        .lt(StaffMobilePatrolScanRecord::getScanTime, today.plusDays(1).atStartOfDay()));
    if (scans.isEmpty()) {
      return points;
    }
    List<String> donePointIds = scans.stream().map(StaffMobilePatrolScanRecord::getPointId).toList();
    points.forEach(point -> {
      if (donePointIds.contains(point.getId())) {
        point.setStatus("已扫码");
      }
    });
    return points;
  }

  @Override
  public StaffMobileReceipt submitStaffMobilePatrolScan(StaffMobilePatrolScanRequest request) {
    StaffMobilePatrolScanRequest safeRequest = request == null ? new StaffMobilePatrolScanRequest() : request;
    String pointId = blankToDefault(safeRequest.getPointId(), safeRequest.getScanText());
    StaffMobilePatrolPoint point = staffMobilePatrolPoints().stream()
        .filter(item -> pointId != null && (pointId.equals(item.getId()) || pointId.contains(item.getId())))
        .findFirst()
        .orElse(null);
    LocalDateTime now = LocalDateTime.now();
    StaffMobilePatrolScanRecord record = new StaffMobilePatrolScanRecord();
    record.setOrgId(AuthContext.getOrgId());
    record.setStaffId(AuthContext.getStaffId());
    record.setPointId(point == null ? blankToDefault(pointId, "UNKNOWN") : point.getId());
    record.setPointName(point == null ? null : point.getName());
    record.setLocation(point == null ? null : point.getLocation());
    record.setScanText(safeRequest.getScanText());
    record.setRemark(safeRequest.getRemark());
    record.setScanTime(now);
    record.setStatus("DONE");
    record.setCreatedBy(AuthContext.getStaffId());
    record.setCreateTime(now);
    record.setUpdateTime(now);
    record.setIsDeleted(0);
    staffMobilePatrolScanRecordMapper.insert(record);
    return staffMobileReceipt(String.valueOf(record.getId()), "DONE", "巡检扫码已记录");
  }

  @Override
  public StaffMobileReceipt submitStaffMobileIncident(StaffMobileIncidentRequest request) {
    StaffMobileIncidentRequest safeRequest = request == null ? new StaffMobileIncidentRequest() : request;
    IncidentReport incident = new IncidentReport();
    incident.setOrgId(AuthContext.getOrgId());
    incident.setReporterName(blankToDefault(AuthContext.getUsername(), "员工移动端"));
    incident.setIncidentTime(LocalDateTime.now());
    incident.setIncidentType(blankToDefault(safeRequest.getIncidentType(), "OTHER"));
    incident.setLevel(blankToDefault(safeRequest.getLevel(), "一般"));
    incident.setElderName(blankToDefault(safeRequest.getResident(), safeRequest.getRoom()));
    incident.setLocation(safeRequest.getRoom());
    incident.setScanText(safeRequest.getScanText());
    incident.setAttachmentUrls(String.join(",", safeRequest.getPhotos() == null ? List.of() : safeRequest.getPhotos()));
    incident.setVoiceUrl(safeRequest.getVoiceUrl());
    incident.setVoiceDurationSec(safeRequest.getVoiceDurationSec());
    incident.setDescription(blankToDefault(safeRequest.getDescription(), "员工端异常上报"));
    incident.setActionTaken("员工端已上报，待主管跟进");
    incident.setStatus("OPEN");
    incident.setCreatedBy(AuthContext.getStaffId());
    incident.setCreateTime(LocalDateTime.now());
    incident.setUpdateTime(LocalDateTime.now());
    incident.setIsDeleted(0);
    incidentReportMapper.insert(incident);
    return staffMobileReceipt(String.valueOf(incident.getId()), "OPEN", "异常事件已上报");
  }

  @Override
  public List<StaffMobileIncidentView> staffMobileIncidents(String status, String level) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.isAdmin() ? null : AuthContext.getStaffId();
    List<IncidentReport> incidents = incidentReportMapper.selectList(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(staffId != null, IncidentReport::getCreatedBy, staffId)
        .eq(status != null && !status.isBlank(), IncidentReport::getStatus, status)
        .eq(level != null && !level.isBlank(), IncidentReport::getLevel, level)
        .orderByDesc(IncidentReport::getIncidentTime)
        .last("limit 50"));
    return incidents.stream().map(this::toMobileIncidentView).toList();
  }

  private List<StaffMobileTask> buildCareMobileTasks() {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.isAdmin() ? null : AuthContext.getStaffId();
    LocalDate today = LocalDate.now();
    List<CareTaskDaily> items = careTaskDailyMapper.selectList(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getTaskDate, today)
        .ne(CareTaskDaily::getStatus, "DONE")
        // 非 admin 仅可见分配给自己的任务 + 未分配的公共任务池（仍可领取执行）
        .and(staffId != null, wrapper -> wrapper
            .eq(CareTaskDaily::getAssignedStaffId, staffId)
            .or()
            .isNull(CareTaskDaily::getAssignedStaffId))
        .orderByAsc(CareTaskDaily::getPlanTime)
        .last("limit 20"));
    List<StaffMobileTask> result = items.stream()
        .map(item -> staffMobileTask(
            "care-" + item.getId(),
            "CARE",
            "护理执行任务",
            item.getElderId() == null ? "老人" : "老人 #" + item.getElderId(),
            item.getBedId() == null ? "床位待确认" : "床位 #" + item.getBedId(),
            formatTime(item.getPlanTime()),
            item.getStatus(),
            "MEDIUM",
            "开始执行",
            true,
            List.of("核对床号和姓名", "完成护理操作", "记录异常和交接事项")))
        .toList();
    return result.isEmpty() && staffMobileDemoFallback ? fallbackStaffMobileTasksByModule("CARE") : result;
  }

  private List<StaffMobileTask> buildMedicationMobileTasks() {
    // HealthMedicationTask 没有分配人字段（仅 createdBy 表示登记人），保持 org 级过滤
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    List<HealthMedicationTask> items = healthMedicationTaskMapper.selectList(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
        .eq(HealthMedicationTask::getTaskDate, today)
        .eq(HealthMedicationTask::getStatus, "PENDING")
        .orderByAsc(HealthMedicationTask::getPlannedTime)
        .last("limit 20"));
    List<StaffMobileTask> result = items.stream()
        .map(item -> staffMobileTask(
            "med-" + item.getId(),
            "MEDICATION",
            blankToDefault(item.getDrugName(), "用药确认"),
            blankToDefault(item.getElderName(), "老人"),
            "医护站",
            formatTime(item.getPlannedTime()),
            item.getStatus(),
            "HIGH",
            "确认发药",
            false,
            List.of("核对老人身份", "核对药品和剂量", "记录服药情况")))
        .toList();
    return result.isEmpty() && staffMobileDemoFallback ? fallbackStaffMobileTasksByModule("MEDICATION") : result;
  }

  private List<StaffMobileTask> buildInspectionMobileTasks() {
    // HealthInspection 没有分配人字段（仅 inspectorName 文本），保持 org 级过滤
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    List<HealthInspection> items = healthInspectionMapper.selectList(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .eq(HealthInspection::getInspectionDate, today)
        .in(HealthInspection::getStatus, List.of("ABNORMAL", "FOLLOWING", "PENDING"))
        .orderByDesc(HealthInspection::getUpdateTime)
        .last("limit 20"));
    List<StaffMobileTask> result = items.stream()
        .map(item -> staffMobileTask(
            "inspect-" + item.getId(),
            "INSPECTION",
            blankToDefault(item.getInspectionItem(), "巡房巡检复核"),
            blankToDefault(item.getElderName(), "老人"),
            "房间/巡检点",
            "今日",
            item.getStatus(),
            "HIGH",
            "记录跟进",
            true,
            List.of("复核异常情况", "拍照或记录说明", "必要时同步责任医生")))
        .toList();
    return result.isEmpty() && staffMobileDemoFallback ? fallbackStaffMobileTasksByModule("INSPECTION") : result;
  }

  private List<StaffMobileTask> buildMaintenanceMobileTasks() {
    // MaintenanceRequest 仅有 assigneeName 文本字段、没有 staffId 外键，无法可靠匹配当前员工，保持 org 级过滤
    Long orgId = AuthContext.getOrgId();
    List<MaintenanceRequest> items = maintenanceRequestMapper.selectList(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getIsDeleted, 0)
        .eq(orgId != null, MaintenanceRequest::getOrgId, orgId)
        .in(MaintenanceRequest::getStatus, List.of("OPEN", "PROCESSING"))
        .orderByDesc(MaintenanceRequest::getReportedAt)
        .last("limit 20"));
    List<StaffMobileTask> result = items.stream()
        .map(item -> staffMobileTask(
            "repair-" + item.getId(),
            "LOGISTICS",
            blankToDefault(item.getIssueType(), "维修工单"),
            "公共区域",
            blankToDefault(item.getRoomNo(), "位置待确认"),
            formatTime(item.getReportedAt()),
            item.getStatus(),
            blankToDefault(item.getPriority(), "MEDIUM"),
            "到场处理",
            true,
            List.of("到场确认位置", "记录处理材料", "拍照验收")))
        .toList();
    return result.isEmpty() && staffMobileDemoFallback ? fallbackStaffMobileTasksByModule("LOGISTICS") : result;
  }

  private List<StaffMobileTask> buildMealMobileTasks() {
    // DiningMealOrder 没有配送员/分配人字段，保持 org 级过滤
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    List<DiningMealOrder> items = diningMealOrderMapper.selectList(Wrappers.lambdaQuery(DiningMealOrder.class)
        .eq(DiningMealOrder::getIsDeleted, 0)
        .eq(orgId != null, DiningMealOrder::getOrgId, orgId)
        .eq(DiningMealOrder::getOrderDate, today)
        .ne(DiningMealOrder::getStatus, "DELIVERED")
        .orderByAsc(DiningMealOrder::getExpectedDeliveryTime)
        .last("limit 20"));
    List<StaffMobileTask> result = items.stream()
        .map(item -> staffMobileTask(
            "meal-" + item.getId(),
            "MEAL",
            blankToDefault(item.getMealType(), "餐食配送"),
            blankToDefault(item.getElderName(), "老人"),
            blankToDefault(item.getDeliveryAreaName(), "配送区域"),
            formatTime(item.getExpectedDeliveryTime()),
            item.getStatus(),
            "MEDIUM",
            "确认送达",
            false,
            List.of("核对餐别", "确认禁忌标签", "送达签收")))
        .toList();
    return result.isEmpty() && staffMobileDemoFallback ? fallbackStaffMobileTasksByModule("MEAL") : result;
  }

  private StaffMobileTask staffMobileTask(
      String id,
      String module,
      String title,
      String resident,
      String room,
      String time,
      String status,
      String priority,
      String actionText,
      boolean evidenceRequired,
      List<String> checklist) {
    StaffMobileTask task = new StaffMobileTask();
    task.setId(id);
    task.setModule(module);
    task.setTitle(title);
    task.setResident(resident);
    task.setRoom(room);
    task.setTime(blankToDefault(time, "待安排"));
    task.setStatus(blankToDefault(status, "待处理"));
    task.setPriority(priority);
    task.setActionText(actionText);
    task.setEvidenceRequired(evidenceRequired);
    task.setChecklist(checklist);
    task.setRoute("/pages/staff-task-detail/index?id=" + id);
    return task;
  }

  private List<StaffMobileTask> fallbackStaffMobileTasksByModule(String module) {
    return fallbackStaffMobileTasks().stream()
        .filter(item -> module == null || module.equals(item.getModule()))
        .toList();
  }

  private List<StaffMobileTask> fallbackStaffMobileTasks() {
    return List.of(
        staffMobileTask("care-demo", "CARE", "协助王桂兰晨间洗漱", "王桂兰", "3F-302-1", "08:20",
            "待执行", "MEDIUM", "开始执行", true, List.of("核对床号和姓名", "完成晨间洗漱", "记录皮肤和精神状态")),
        staffMobileTask("med-demo", "MEDICATION", "张建国早餐后用药", "张建国", "2F-215-2", "08:40",
            "待确认", "HIGH", "确认发药", false, List.of("核对老人身份", "核对药品和剂量", "记录服药情况")),
        staffMobileTask("inspect-demo", "INSPECTION", "夜间异常巡检复核", "刘秀英", "4F-408-1", "09:10",
            "异常待跟进", "HIGH", "记录跟进", true, List.of("复测生命体征", "拍照或语音记录", "同步责任医生")),
        staffMobileTask("repair-demo", "LOGISTICS", "306 卫生间扶手松动", "公共区域", "3F-306", "09:30",
            "待到场", "MEDIUM", "到场处理", true, List.of("到场确认位置", "记录处理材料", "拍照验收")),
        staffMobileTask("meal-demo", "MEAL", "糖尿病软食餐配送", "陈阿姨", "2F-208-1", "11:20",
            "待配送", "MEDIUM", "确认送达", false, List.of("核对餐别", "确认禁忌标签", "送达签收"))
    );
  }

  private StaffMobileSchedule toMobileSchedule(StaffSchedule item) {
    StaffMobileSchedule schedule = new StaffMobileSchedule();
    schedule.setDate(formatScheduleDate(item.getDutyDate()));
    schedule.setShift(blankToDefault(item.getShiftCode(), "班次"));
    schedule.setTime(formatRange(item.getStartTime(), item.getEndTime()));
    schedule.setArea(blankToDefault(item.getSourceTemplateName(), "责任区域待确认"));
    schedule.setStatus(item.getStatus() != null && item.getStatus() == 1 ? "已排班" : "待确认");
    return schedule;
  }

  private List<StaffMobileSchedule> fallbackStaffMobileSchedule() {
    return List.of(
        staffMobileSchedule("今天", "早班", "07:30-15:30", "2F / 3F 护理区", "进行中"),
        staffMobileSchedule("明天", "中班", "15:30-22:00", "3F 护理区", "已排班"),
        staffMobileSchedule("后天", "休息", "-", "-", "休息")
    );
  }

  private StaffMobileSchedule staffMobileSchedule(String date, String shift, String time, String area, String status) {
    StaffMobileSchedule schedule = new StaffMobileSchedule();
    schedule.setDate(date);
    schedule.setShift(shift);
    schedule.setTime(time);
    schedule.setArea(area);
    schedule.setStatus(status);
    return schedule;
  }

  private StaffMobileHandover staffMobileHandover(String id, String time, String title, String owner, String content) {
    StaffMobileHandover handover = new StaffMobileHandover();
    handover.setId(id);
    handover.setTime(time);
    handover.setTitle(title);
    handover.setOwner(owner);
    handover.setContent(content);
    return handover;
  }

  private StaffMobileHandover toMobileHandover(StaffMobileHandoverRecord record) {
    return staffMobileHandover(
        String.valueOf(record.getId()),
        record.getHandoverTime() == null ? "" : record.getHandoverTime().format(DateTimeFormatter.ofPattern("HH:mm")),
        record.getTitle(),
        record.getOwner(),
        record.getContent()
    );
  }

  private StaffMobilePatrolPoint staffMobilePatrolPoint(String id, String name, String location, String status) {
    StaffMobilePatrolPoint point = new StaffMobilePatrolPoint();
    point.setId(id);
    point.setName(name);
    point.setLocation(location);
    point.setStatus(status);
    return point;
  }

  private StaffMobileReceipt staffMobileReceipt(String id, String status, String message) {
    StaffMobileReceipt receipt = new StaffMobileReceipt();
    receipt.setId(id);
    receipt.setStatus(status);
    receipt.setMessage(message);
    receipt.setSubmittedAt(LocalDateTime.now().format(DATETIME_FMT));
    return receipt;
  }

  private String normalizeModule(String module) {
    if (module == null || module.isBlank()) {
      return null;
    }
    return module.trim().toUpperCase(Locale.ROOT);
  }

  private Long parseMobileId(String id, String prefix) {
    if (id == null || !id.startsWith(prefix)) {
      return null;
    }
    try {
      return Long.valueOf(id.substring(prefix.length()));
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /**
   * 判断当前员工今日是否已完成上班打卡（与 AttendanceServiceImpl 的今日记录查询口径一致）。
   * 无法从令牌解析员工身份时不拦截，直接视为在岗。
   */
  private boolean isCurrentStaffOnDutyToday() {
    Long staffId = AuthContext.getStaffId();
    if (staffId == null) {
      return true;
    }
    Long orgId = AuthContext.getOrgId();
    AttendanceRecord record = attendanceRecordMapper.selectOne(Wrappers.lambdaQuery(AttendanceRecord.class)
        .eq(AttendanceRecord::getIsDeleted, 0)
        .eq(orgId != null, AttendanceRecord::getOrgId, orgId)
        .eq(AttendanceRecord::getStaffId, staffId)
        .eq(AttendanceRecord::getWorkDate, LocalDate.now())
        .orderByDesc(AttendanceRecord::getUpdateTime)
        .last("LIMIT 1"));
    return record != null && record.getCheckInTime() != null;
  }

  private String appendOffDutyMark(String remark) {
    String mark = "[未打卡提交]";
    if (remark == null || remark.isBlank()) {
      return mark;
    }
    return remark.contains(mark) ? remark : remark + " " + mark;
  }

  private void saveStaffMobileTaskReceipt(String id, StaffMobileCompleteTaskRequest request, LocalDateTime now) {
    StaffMobileTask taskSnapshot = findStaffMobileTaskSnapshot(id);
    StaffMobileTaskReceipt receipt = new StaffMobileTaskReceipt();
    receipt.setOrgId(AuthContext.getOrgId());
    receipt.setStaffId(AuthContext.getStaffId());
    receipt.setTaskId(blankToDefault(id, "unknown"));
    receipt.setTaskModule(resolveTaskModuleFromId(id));
    if (taskSnapshot != null) {
      receipt.setTaskTitle(taskSnapshot.getTitle());
      receipt.setResident(taskSnapshot.getResident());
      receipt.setRoom(taskSnapshot.getRoom());
    }
    receipt.setRemark(request.getRemark());
    receipt.setScanText(request.getScanText());
    receipt.setCheckedItems(formatCheckedItems(request.getCheckedMap()));
    receipt.setPhotoUrls(String.join(",", request.getPhotos() == null ? List.of() : request.getPhotos()));
    receipt.setVoiceUrl(request.getVoiceUrl());
    receipt.setVoiceDurationSec(request.getVoiceDurationSec());
    receipt.setReceiptTime(now);
    receipt.setStatus("DONE");
    receipt.setCreatedBy(AuthContext.getStaffId());
    receipt.setCreateTime(now);
    receipt.setUpdateTime(now);
    receipt.setIsDeleted(0);
    staffMobileTaskReceiptMapper.insert(receipt);
  }

  private StaffMobileTaskReceiptView toMobileTaskReceiptView(StaffMobileTaskReceipt receipt) {
    StaffMobileTaskReceiptView view = new StaffMobileTaskReceiptView();
    view.setId(String.valueOf(receipt.getId()));
    view.setTaskId(receipt.getTaskId());
    view.setModule(receipt.getTaskModule());
    view.setModuleText(moduleText(receipt.getTaskModule()));
    view.setTaskTitle(receipt.getTaskTitle());
    view.setResident(receipt.getResident());
    view.setRoom(receipt.getRoom());
    view.setRemark(receipt.getRemark());
    view.setScanText(receipt.getScanText());
    view.setCheckedItems(receipt.getCheckedItems());
    view.setPhotos(splitCsv(receipt.getPhotoUrls()));
    view.setVoiceUrl(receipt.getVoiceUrl());
    view.setVoiceDurationSec(receipt.getVoiceDurationSec());
    view.setReceiptTime(receipt.getReceiptTime() == null ? "" : receipt.getReceiptTime().format(DATETIME_FMT));
    view.setStatus(receipt.getStatus());
    view.setAdminRoute(resolveStaffMobileAdminRoute(receipt.getTaskModule(), receipt.getStatus()));
    view.setTaskRoute(receipt.getTaskId() == null || receipt.getTaskId().isBlank()
        ? resolveStaffMobileAdminRoute(receipt.getTaskModule(), receipt.getStatus())
        : "/stats/staff-mobile-ledger?tab=tasks&taskId=" + receipt.getTaskId());
    return view;
  }

  private StaffMobileIncidentView toMobileIncidentView(IncidentReport incident) {
    StaffMobileIncidentView view = new StaffMobileIncidentView();
    view.setId(String.valueOf(incident.getId()));
    view.setIncidentType(incident.getIncidentType());
    view.setLevel(incident.getLevel());
    view.setResident(incident.getElderName());
    view.setLocation(incident.getLocation());
    view.setDescription(incident.getDescription());
    view.setActionTaken(incident.getActionTaken());
    view.setScanText(incident.getScanText());
    view.setPhotos(splitCsv(incident.getAttachmentUrls()));
    view.setVoiceUrl(incident.getVoiceUrl());
    view.setVoiceDurationSec(incident.getVoiceDurationSec());
    view.setIncidentTime(incident.getIncidentTime() == null ? "" : incident.getIncidentTime().format(DATETIME_FMT));
    view.setStatus(incident.getStatus());
    view.setAdminRoute("/life/incident");
    return view;
  }

  private String resolveStaffMobileAdminRoute(String module, String status) {
    String normalizedModule = defaultText(module, "").toUpperCase(java.util.Locale.ROOT);
    String normalizedStatus = defaultText(status, "").toUpperCase(java.util.Locale.ROOT);
    if ("MEDICATION".equals(normalizedModule)) {
      return "/health/medication/medication-registration";
    }
    if ("INSPECTION".equals(normalizedModule)) {
      return "/health/inspection";
    }
    if ("LOGISTICS".equals(normalizedModule)) {
      if ("OPEN".equals(normalizedStatus)) {
        return "/logistics/maintenance/dispatch";
      }
      if ("PROCESSING".equals(normalizedStatus)) {
        return "/logistics/maintenance/progress";
      }
      return "/logistics/assets/maintenance-record";
    }
    if ("MEAL".equals(normalizedModule)) {
      return "/logistics/dining/delivery-plan";
    }
    return "/health/nursing-log";
  }

  private StaffMobileTask findStaffMobileTaskSnapshot(String id) {
    if (id == null || id.isBlank()) {
      return null;
    }
    return staffMobileTasks(null).stream()
        .filter(item -> id.equals(item.getId()))
        .findFirst()
        .orElse(null);
  }

  private List<String> splitCsv(String value) {
    if (value == null || value.isBlank()) {
      return List.of();
    }
    return java.util.Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(item -> !item.isBlank())
        .toList();
  }

  private String moduleText(String module) {
    if ("CARE".equals(module)) {
      return "护理执行";
    }
    if ("MEDICATION".equals(module)) {
      return "用药确认";
    }
    if ("INSPECTION".equals(module)) {
      return "巡房巡检";
    }
    if ("LOGISTICS".equals(module)) {
      return "后勤维修";
    }
    if ("MEAL".equals(module)) {
      return "餐食配送";
    }
    return "现场任务";
  }

  private String resolveTaskModuleFromId(String id) {
    if (id == null) {
      return null;
    }
    if (id.startsWith("care-")) {
      return "CARE";
    }
    if (id.startsWith("med-")) {
      return "MEDICATION";
    }
    if (id.startsWith("inspect-")) {
      return "INSPECTION";
    }
    if (id.startsWith("repair-")) {
      return "LOGISTICS";
    }
    if (id.startsWith("meal-")) {
      return "MEAL";
    }
    return null;
  }

  private String formatCheckedItems(java.util.Map<String, Boolean> checkedMap) {
    if (checkedMap == null || checkedMap.isEmpty()) {
      return "";
    }
    return checkedMap.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + Boolean.TRUE.equals(entry.getValue()))
        .reduce((left, right) -> left + ";" + right)
        .orElse("");
  }

  private String mergeMobileRemark(String remark, String scanText) {
    if (scanText == null || scanText.isBlank()) {
      return remark;
    }
    return blankToDefault(remark, "") + " 扫码结果：" + scanText;
  }

  private String blankToDefault(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
  }

  private String formatTime(LocalDateTime value) {
    return value == null ? null : value.format(DateTimeFormatter.ofPattern("HH:mm"));
  }

  private String formatRange(LocalDateTime start, LocalDateTime end) {
    if (start == null && end == null) {
      return "-";
    }
    return blankToDefault(formatTime(start), "-") + "-" + blankToDefault(formatTime(end), "-");
  }

  private String formatScheduleDate(LocalDate date) {
    if (date == null) {
      return "待定";
    }
    LocalDate today = LocalDate.now();
    if (date.equals(today)) {
      return "今天";
    }
    if (date.equals(today.plusDays(1))) {
      return "明天";
    }
    if (date.equals(today.plusDays(2))) {
      return "后天";
    }
    return date.format(DateTimeFormatter.ofPattern("MM-dd"));
  }

  @Override
  public OperationsLogisticsResponse logistics() {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime todayStart = today.atStartOfDay();
    LocalDateTime overdueCutoff = now.minusDays(2);

    long lowStockCount = inventoryService.lowStockAlerts(orgId).size();
    long expiringCount = inventoryService.expiryAlerts(orgId, 30).size();
    long purchaseDraftCount = materialPurchaseOrderMapper.selectCount(Wrappers.lambdaQuery(MaterialPurchaseOrder.class)
        .eq(MaterialPurchaseOrder::getIsDeleted, 0)
        .eq(orgId != null, MaterialPurchaseOrder::getOrgId, orgId)
        .eq(MaterialPurchaseOrder::getStatus, "DRAFT"));
    long purchaseApprovedCount = materialPurchaseOrderMapper.selectCount(Wrappers.lambdaQuery(MaterialPurchaseOrder.class)
        .eq(MaterialPurchaseOrder::getIsDeleted, 0)
        .eq(orgId != null, MaterialPurchaseOrder::getOrgId, orgId)
        .eq(MaterialPurchaseOrder::getStatus, "APPROVED"));
    long maintenanceOpenCount = maintenanceRequestMapper.selectCount(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getIsDeleted, 0)
        .eq(orgId != null, MaintenanceRequest::getOrgId, orgId)
        .in(MaintenanceRequest::getStatus, List.of("OPEN", "PROCESSING")));
    long maintenanceOverdueCount = maintenanceRequestMapper.selectCount(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getIsDeleted, 0)
        .eq(orgId != null, MaintenanceRequest::getOrgId, orgId)
        .in(MaintenanceRequest::getStatus, List.of("OPEN", "PROCESSING"))
        .lt(MaintenanceRequest::getReportedAt, overdueCutoff));
    long equipmentTotalCount = logisticsEquipmentArchiveMapper.selectCount(Wrappers.lambdaQuery(LogisticsEquipmentArchive.class)
        .eq(LogisticsEquipmentArchive::getIsDeleted, 0)
        .eq(orgId != null, LogisticsEquipmentArchive::getOrgId, orgId));
    long equipmentMaintenanceCount = logisticsEquipmentArchiveMapper.selectCount(Wrappers.lambdaQuery(LogisticsEquipmentArchive.class)
        .eq(LogisticsEquipmentArchive::getIsDeleted, 0)
        .eq(orgId != null, LogisticsEquipmentArchive::getOrgId, orgId)
        .eq(LogisticsEquipmentArchive::getStatus, "MAINTENANCE"));
    long equipmentDueSoonCount = logisticsEquipmentArchiveMapper.selectCount(Wrappers.lambdaQuery(LogisticsEquipmentArchive.class)
        .eq(LogisticsEquipmentArchive::getIsDeleted, 0)
        .eq(orgId != null, LogisticsEquipmentArchive::getOrgId, orgId)
        .isNotNull(LogisticsEquipmentArchive::getNextMaintainedAt)
        .between(LogisticsEquipmentArchive::getNextMaintainedAt, now, now.plusDays(30)));
    long todayCleaningCount = roomCleaningTaskMapper.selectCount(Wrappers.lambdaQuery(RoomCleaningTask.class)
        .eq(RoomCleaningTask::getIsDeleted, 0)
        .eq(orgId != null, RoomCleaningTask::getOrgId, orgId)
        .eq(RoomCleaningTask::getPlanDate, today));
    long todayCleaningOpenCount = roomCleaningTaskMapper.selectCount(Wrappers.lambdaQuery(RoomCleaningTask.class)
        .eq(RoomCleaningTask::getIsDeleted, 0)
        .eq(orgId != null, RoomCleaningTask::getOrgId, orgId)
        .eq(RoomCleaningTask::getPlanDate, today)
        .ne(RoomCleaningTask::getStatus, "DONE"));
    List<DiningMealOrder> todayMealOrders = diningMealOrderMapper.selectList(Wrappers.lambdaQuery(DiningMealOrder.class)
        .eq(DiningMealOrder::getIsDeleted, 0)
        .eq(orgId != null, DiningMealOrder::getOrgId, orgId)
        .eq(DiningMealOrder::getOrderDate, today));
    long deliveredMealCount = todayMealOrders.stream().filter(item -> "DELIVERED".equals(item.getStatus())).count();
    long undeliveredMealCount = Math.max(0, todayMealOrders.size() - deliveredMealCount);
    long specialMealCount = todayMealOrders.stream()
        .filter(item -> item.getOverrideId() != null
            || containsAny(item.getDishNames(), "糖尿", "低盐", "软食", "流食", "营养", "特殊")
            || containsAny(item.getRemark(), "过敏", "禁忌"))
        .count();
    long todayMaintenanceCount = maintenanceRequestMapper.selectCount(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getIsDeleted, 0)
        .eq(orgId != null, MaintenanceRequest::getOrgId, orgId)
        .ge(MaintenanceRequest::getReportedAt, todayStart));

    int logisticsIndex = resolveLogisticsIndex(lowStockCount, expiringCount, purchaseApprovedCount,
        maintenanceOverdueCount, equipmentDueSoonCount, undeliveredMealCount, todayCleaningOpenCount);

    OperationsLogisticsResponse response = new OperationsLogisticsResponse();
    response.setGeneratedAt(now.format(DATETIME_FMT));
    response.setStatus("LANDING");
    response.setLogisticsIndex(logisticsIndex);
    response.setLogisticsLevel(resolveLogisticsLevel(logisticsIndex));
    response.setMetrics(List.of(
        logisticsMetric("inventory", "库存风险", (lowStockCount + expiringCount) + " 项",
            "低库存 " + lowStockCount + " / 临期 " + expiringCount, lowStockCount + expiringCount > 0 ? "danger" : "normal", "/logistics/storage/alerts"),
        logisticsMetric("purchase", "采购待办", (purchaseDraftCount + purchaseApprovedCount) + " 单",
            "草稿待审 " + purchaseDraftCount + " / 已批待到货 " + purchaseApprovedCount, purchaseApprovedCount > 0 ? "warning" : "normal", "/logistics/storage/purchase"),
        logisticsMetric("maintenance", "维修未闭环", maintenanceOpenCount + " 单",
            "超时 " + maintenanceOverdueCount + " 单，今日新增 " + todayMaintenanceCount, maintenanceOverdueCount > 0 ? "danger" : "normal", "/logistics/assets/maintenance-record"),
        logisticsMetric("equipment", "设备维保", equipmentDueSoonCount + " 台",
            "设备总数 " + equipmentTotalCount + " / 维修中 " + equipmentMaintenanceCount, equipmentDueSoonCount > 0 ? "warning" : "normal", "/logistics/maintenance/assets"),
        logisticsMetric("cleaning", "今日保洁", (todayCleaningCount - todayCleaningOpenCount) + "/" + todayCleaningCount,
            "未完成保洁 " + todayCleaningOpenCount + " 项", todayCleaningOpenCount > 0 ? "warning" : "normal", "/logistics/task-center"),
        logisticsMetric("dining", "餐饮送达", deliveredMealCount + "/" + todayMealOrders.size(),
            "未送达 " + undeliveredMealCount + " / 特殊餐 " + specialMealCount, undeliveredMealCount > 0 ? "warning" : "normal", "/logistics/dining/order")
    ));
    response.setRisks(buildLogisticsRisks(lowStockCount, expiringCount, purchaseDraftCount, purchaseApprovedCount,
        maintenanceOpenCount, maintenanceOverdueCount, equipmentDueSoonCount, todayCleaningOpenCount,
        undeliveredMealCount, specialMealCount));
    response.setActions(buildLogisticsActions(lowStockCount, expiringCount, purchaseApprovedCount,
        maintenanceOverdueCount, equipmentDueSoonCount, todayCleaningOpenCount, undeliveredMealCount));
    response.setFlows(buildLogisticsFlows());
    return response;
  }

  @Override
  public OperationsMarketingResponse marketing() {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    String monthStart = today.withDayOfMonth(1).toString();
    String todayText = today.toString();
    MarketingWorkbenchSummaryResponse summary = marketingReportService.workbenchSummary(orgId, monthStart, todayText);
    MarketingConversionReportResponse conversion = marketingReportService.conversion(orgId, monthStart, todayText, null, null);
    MarketingDataQualityResponse dataQuality = marketingReportService.dataQuality(orgId);
    List<MarketingChannelReportItem> channelItems = marketingReportService.channel(orgId, monthStart, todayText, null);

    long overdueFollowup = summary.getFollowup().getOverdue();
    long pendingSign = summary.getFunnel().getPendingSignCount();
    long pendingAdmission = summary.getFunnel().getPendingAdmissionCount();
    long unknownChannel = summary.getChannelUnknownCount() + dataQuality.getMissingSourceCount();
    long highIntentNoEval = summary.getRisk().getHighIntentNoEvalCount();
    long lockUnsigned = summary.getRisk().getLockUnsignedCount();
    int conversionIndex = resolveMarketingIndex(conversion.getContractRate(), overdueFollowup, pendingSign,
        pendingAdmission, unknownChannel, highIntentNoEval, lockUnsigned);

    OperationsMarketingResponse response = new OperationsMarketingResponse();
    response.setGeneratedAt(LocalDateTime.now().format(DATETIME_FMT));
    response.setStatus("LANDING");
    response.setConversionIndex(conversionIndex);
    response.setConversionLevel(resolveMarketingLevel(conversionIndex));
    response.setMetrics(List.of(
        marketingMetric("leads", "本月线索", conversion.getTotalLeads() + " 条",
            "今日咨询 " + summary.getFunnel().getTodayConsultCount() + "，高意向 " + summary.getFollowup().getHighIntentCount(), conversion.getTotalLeads() > 0 ? "normal" : "warning", "/marketing/leads/all"),
        marketingMetric("conversion", "签约转化率", formatRate(conversion.getContractRate()),
            "本月签约 " + conversion.getContractCount() + " / 预约 " + conversion.getReservationCount(), conversion.getContractRate() >= 20 ? "normal" : "warning", "/marketing/reports/conversion"),
        marketingMetric("followup", "跟进风险", overdueFollowup + " 条",
            "今日待跟进 " + summary.getFollowup().getTodayDue(), overdueFollowup > 0 ? "danger" : "normal", "/marketing/interactions/overdue"),
        marketingMetric("contract", "签约待办", pendingSign + " 单",
            "待入住办理 " + pendingAdmission + " 单", pendingSign + pendingAdmission > 0 ? "warning" : "normal", "/marketing/funnel/signing"),
        marketingMetric("channel", "渠道质量", unknownChannel + " 条",
            "渠道缺失/不规范，影响渠道 ROI", unknownChannel > 0 ? "warning" : "normal", "/marketing/leads/unknown-source"),
        marketingMetric("callback", "回访评分", String.format("%.1f", summary.getCallback().getScore()),
            "入住/试住/退住回访纳入服务口碑", summary.getCallback().getScore() >= 4 ? "normal" : "warning", "/marketing/reports/callback")
    ));
    response.setFunnelStages(buildMarketingFunnelStages(summary, conversion));
    response.setRisks(buildMarketingRisks(summary, dataQuality, conversion));
    response.setChannels(buildMarketingChannels(channelItems));
    response.setActions(buildMarketingActions(overdueFollowup, pendingSign, pendingAdmission, unknownChannel,
        highIntentNoEval, lockUnsigned, conversion.getContractRate()));
    response.setFlows(buildMarketingFlows());
    return response;
  }

  private int resolveStaffMobileIndex(
      long careTaskPendingCount,
      long medicationPendingCount,
      long inspectionOpenCount,
      long maintenanceOpenCount,
      long mealPendingCount,
      long cleaningPendingCount) {
    return (int) Math.min(100,
        careTaskPendingCount * 8
            + medicationPendingCount * 9
            + inspectionOpenCount * 8
            + maintenanceOpenCount * 5
            + mealPendingCount * 4
            + cleaningPendingCount * 3);
  }

  private String resolveStaffMobileLevel(int mobileIndex) {
    if (mobileIndex >= 75) return "CRITICAL";
    if (mobileIndex >= 50) return "HIGH";
    if (mobileIndex >= 25) return "MEDIUM";
    return "LOW";
  }

  private List<StaffMobileAction> buildStaffMobileActions(
      long careTaskPendingCount,
      long medicationPendingCount,
      long inspectionOpenCount,
      long maintenanceOpenCount,
      long mealPendingCount,
      long cleaningPendingCount) {
    List<StaffMobileAction> actions = new ArrayList<>();
    if (careTaskPendingCount > 0) {
      actions.add(staffMobileAction("优先处理护理待办", "护理员 / 护理主管", "HIGH", "/medical-care/care-task-board",
          "把今日护理执行、补录和交接统一放到手机端，减少来回切页。"));
    }
    if (medicationPendingCount > 0) {
      actions.add(staffMobileAction("补齐用药确认", "医生 / 医护", "HIGH", "/health/medication/medication-registration?date=today",
          "用药任务需要按时间快速确认，异常和漏服必须立刻处理。"));
    }
    if (inspectionOpenCount > 0) {
      actions.add(staffMobileAction("闭环巡检异常", "医生 / 医护", "HIGH", "/medical-care/inspection",
          "异常巡检应支持拍照、备注和跟进动作，避免挂起。"));
    }
    if (maintenanceOpenCount > 0 || mealPendingCount > 0) {
      actions.add(staffMobileAction("处理后勤派单和配送", "后勤人员", "MEDIUM", "/logistics/task-center",
          "维修、保洁、送餐都需要现场接单和签收回执。"));
    }
    if (cleaningPendingCount > 0) {
      actions.add(staffMobileAction("补齐巡检保洁记录", "后勤 / 保洁", "MEDIUM", "/logistics/task-center",
          "巡检保洁的照片、时间和地点需要在移动端即时留痕。"));
    }
    if (actions.isEmpty()) {
      actions.add(staffMobileAction("保持移动端待命", "护理员 / 医护 / 后勤", "LOW", "/stats/staff-mobile",
          "当前任务压力平稳，但手机端仍应保留扫码、拍照和快速回执能力。"));
    }
    return actions;
  }

  private String resolveSafetyRiskLevel(int riskIndex) {
    if (riskIndex >= 75) return "CRITICAL";
    if (riskIndex >= 50) return "HIGH";
    if (riskIndex >= 25) return "MEDIUM";
    return "LOW";
  }

  private int resolveWorkforceIndex(
      long activeStaffCount,
      long todayScheduleCount,
      long todayAttendanceCount,
      long todayLeaveCount,
      long certificateMissingCount,
      long contractExpiringCount,
      long unreviewedAttendanceCount,
      long pendingCareTaskCount) {
    long gap = Math.max(0, todayScheduleCount - todayAttendanceCount);
    long loadRisk = activeStaffCount == 0 ? 30 : Math.min(30, pendingCareTaskCount / Math.max(1, activeStaffCount));
    return (int) Math.min(100,
        gap * 18
            + todayLeaveCount * 8
            + certificateMissingCount * 10
            + contractExpiringCount * 6
            + unreviewedAttendanceCount * 2
            + loadRisk);
  }

  private String resolveWorkforceLevel(int workforceIndex) {
    if (workforceIndex >= 75) return "CRITICAL";
    if (workforceIndex >= 50) return "HIGH";
    if (workforceIndex >= 25) return "MEDIUM";
    return "LOW";
  }

  private int resolveLogisticsIndex(
      long lowStockCount,
      long expiringCount,
      long purchaseApprovedCount,
      long maintenanceOverdueCount,
      long equipmentDueSoonCount,
      long undeliveredMealCount,
      long todayCleaningOpenCount) {
    return (int) Math.min(100,
        lowStockCount * 8
            + expiringCount * 6
            + purchaseApprovedCount * 4
            + maintenanceOverdueCount * 12
            + equipmentDueSoonCount * 6
            + undeliveredMealCount * 5
            + todayCleaningOpenCount * 3);
  }

  private String resolveLogisticsLevel(int logisticsIndex) {
    if (logisticsIndex >= 75) return "CRITICAL";
    if (logisticsIndex >= 50) return "HIGH";
    if (logisticsIndex >= 25) return "MEDIUM";
    return "LOW";
  }

  private int resolveMarketingIndex(
      double contractRate,
      long overdueFollowup,
      long pendingSign,
      long pendingAdmission,
      long unknownChannel,
      long highIntentNoEval,
      long lockUnsigned) {
    int lowConversionPenalty = contractRate >= 25 ? 0 : contractRate >= 15 ? 8 : contractRate >= 8 ? 16 : 24;
    return (int) Math.min(100,
        lowConversionPenalty
            + overdueFollowup * 8
            + pendingSign * 5
            + pendingAdmission * 4
            + unknownChannel * 3
            + highIntentNoEval * 7
            + lockUnsigned * 6);
  }

  private String resolveMarketingLevel(int conversionIndex) {
    if (conversionIndex >= 75) return "CRITICAL";
    if (conversionIndex >= 50) return "HIGH";
    if (conversionIndex >= 25) return "MEDIUM";
    return "LOW";
  }

  private List<MarketingFunnelStage> buildMarketingFunnelStages(
      MarketingWorkbenchSummaryResponse summary,
      MarketingConversionReportResponse conversion) {
    return List.of(
        marketingStage("consult", "咨询线索", conversion.getConsultCount() + " 条", "READY", "/marketing/funnel/consultation",
            List.of("今日咨询 " + summary.getFunnel().getTodayConsultCount(), "总线索 " + conversion.getTotalLeads())),
        marketingStage("intent", "意向评估", conversion.getIntentCount() + " 条", "READY", "/marketing/funnel/evaluation",
            List.of("意向率 " + formatRate(conversion.getIntentRate()), "高意向 " + summary.getFollowup().getHighIntentCount())),
        marketingStage("reservation", "预约锁床", conversion.getReservationCount() + " 条", "READY", "/marketing/reservation/records",
            List.of("预约率 " + formatRate(conversion.getReservationRate()), "锁床临期 " + summary.getFollowup().getLockExpiringCount())),
        marketingStage("sign", "签约入住", conversion.getContractCount() + " 单", summary.getFunnel().getPendingSignCount() > 0 ? "WARNING" : "READY", "/marketing/funnel/signing",
            List.of("签约率 " + formatRate(conversion.getContractRate()), "待签约 " + summary.getFunnel().getPendingSignCount(), "待入住 " + summary.getFunnel().getPendingAdmissionCount())),
        marketingStage("lost", "流失复盘", conversion.getInvalidCount() + " 条", conversion.getInvalidCount() > 0 ? "WARNING" : "READY", "/marketing/funnel/lost",
            List.of("流失客户归因", "二次激活池"))
    );
  }

  private List<MarketingRisk> buildMarketingRisks(
      MarketingWorkbenchSummaryResponse summary,
      MarketingDataQualityResponse dataQuality,
      MarketingConversionReportResponse conversion) {
    long unknownChannel = summary.getChannelUnknownCount() + dataQuality.getMissingSourceCount();
    return List.of(
        marketingRisk("followup", "销售跟进掉线", "营销主管",
            summary.getFollowup().getOverdue() > 0 ? "HIGH" : "LOW", "/marketing/interactions/overdue",
            List.of("逾期跟进 " + summary.getFollowup().getOverdue() + " 条", "今日待跟进 " + summary.getFollowup().getTodayDue() + " 条"),
            List.of("补齐回访计划", "复核跟进结果", "高意向客户转评估")),
        marketingRisk("contract", "签约/入住卡点", "营销/评估/院办",
            summary.getFunnel().getPendingSignCount() + summary.getFunnel().getPendingAdmissionCount() > 0 ? "MEDIUM" : "LOW",
            "/marketing/funnel/signing",
            List.of("待签约 " + summary.getFunnel().getPendingSignCount() + " 单", "待入住 " + summary.getFunnel().getPendingAdmissionCount() + " 单"),
            List.of("合同资料补齐", "评估报告联动", "床位与账单交接")),
        marketingRisk("channel", "渠道归因不清", "营销/运营",
            unknownChannel > 0 ? "MEDIUM" : "LOW", "/marketing/leads/unknown-source",
            List.of("渠道缺失/不规范 " + unknownChannel + " 条", "非标准来源 " + dataQuality.getNonStandardSourceCount() + " 条"),
            List.of("渠道字典归一", "补齐来源标签", "复盘渠道转化率")),
        marketingRisk("conversion", "转化率偏低", "院长/营销主管",
            conversion.getContractRate() < 15 ? "HIGH" : "LOW", "/marketing/reports/conversion",
            List.of("签约转化率 " + formatRate(conversion.getContractRate()), "本月签约 " + conversion.getContractCount() + " 单"),
            List.of("复盘咨询话术", "优化参观动线", "分析失效原因"))
    );
  }

  private List<MarketingChannel> buildMarketingChannels(List<MarketingChannelReportItem> items) {
    return items.stream()
        .sorted(Comparator.comparingLong(MarketingChannelReportItem::getContractCount).reversed()
            .thenComparing(Comparator.comparingLong(MarketingChannelReportItem::getLeadCount).reversed()))
        .limit(5)
        .map(item -> {
          MarketingChannel channel = new MarketingChannel();
          channel.setSource(defaultText(item.getSource(), "未标记渠道"));
          channel.setLeadCount(item.getLeadCount() + " 条");
          channel.setContractRate(formatRate(item.getLeadCount() <= 0 ? 0D : item.getContractCount() * 100D / item.getLeadCount()));
          channel.setRoutePath("/marketing/reports/channel-rank");
          return channel;
        })
        .toList();
  }

  private List<MarketingAction> buildMarketingActions(
      long overdueFollowup,
      long pendingSign,
      long pendingAdmission,
      long unknownChannel,
      long highIntentNoEval,
      long lockUnsigned,
      double contractRate) {
    List<MarketingAction> actions = new ArrayList<>();
    if (overdueFollowup > 0) {
      actions.add(marketingAction("追回逾期未跟进客户", "营销主管", "HIGH", "/marketing/interactions/overdue",
          "逾期跟进会直接影响咨询到评估转化，需要当日补齐回访结果。"));
    }
    if (pendingSign > 0 || pendingAdmission > 0) {
      actions.add(marketingAction("推进待签约/待入住合同", "营销/院办", "HIGH", "/marketing/funnel/signing",
          "签约和入住阶段需要合同、评估、床位和账单联动，避免成交卡在交接处。"));
    }
    if (highIntentNoEval > 0 || lockUnsigned > 0) {
      actions.add(marketingAction("处理高意向和锁床风险", "营销/评估", "MEDIUM", "/marketing/funnel/evaluation",
          "高意向未评估、锁床未签约会占用床位资源，需要明确下一步。"));
    }
    if (unknownChannel > 0) {
      actions.add(marketingAction("补齐渠道来源", "营销运营", "MEDIUM", "/marketing/leads/unknown-source",
          "渠道缺失会影响投放复盘和转介绍奖励，需要统一来源口径。"));
    }
    if (contractRate < 15) {
      actions.add(marketingAction("复盘低转化渠道与话术", "院长/营销主管", "MEDIUM", "/marketing/reports/conversion",
          "本月签约转化率偏低，建议按渠道、顾问和参观环节拆解原因。"));
    }
    if (actions.isEmpty()) {
      actions.add(marketingAction("执行本周营销复盘", "院长/营销主管", "LOW", "/stats/operations?tab=marketing",
          "当前转化风险平稳，建议持续复盘渠道、跟进及时率和签约入住交接。"));
    }
    return actions;
  }

  private List<MarketingFlow> buildMarketingFlows() {
    return List.of(
        marketingFlow("lead", "线索进入与分配", "READY", "/marketing/leads/all",
            List.of("渠道登记", "顾问分配", "咨询记录", "客户标签", "下一次跟进")),
        marketingFlow("visit", "咨询参观到评估", "READY", "/marketing/funnel/consultation",
            List.of("电话咨询", "参观预约", "到院接待", "入住评估", "高意向推进")),
        marketingFlow("contract", "签约入住交接", "READY", "/marketing/funnel/signing",
            List.of("合同草拟", "评估报告", "选床锁床", "账单押金", "正式入住")),
        marketingFlow("review", "渠道与转介绍复盘", "PARTIAL", "/marketing/reports/channel-rank",
            List.of("渠道来源归一", "转化率统计", "老客户转介绍", "投放 ROI", "复盘行动"))
    );
  }

  private List<LogisticsRisk> buildLogisticsRisks(
      long lowStockCount,
      long expiringCount,
      long purchaseDraftCount,
      long purchaseApprovedCount,
      long maintenanceOpenCount,
      long maintenanceOverdueCount,
      long equipmentDueSoonCount,
      long todayCleaningOpenCount,
      long undeliveredMealCount,
      long specialMealCount) {
    return List.of(
        logisticsRisk("inventory", "药品耗材与库存安全", "仓库/后勤",
            lowStockCount + expiringCount > 0 ? "HIGH" : "LOW", "/logistics/storage/alerts",
            List.of("低库存 " + lowStockCount + " 项", "30天内临期 " + expiringCount + " 项"),
            List.of("生成采购单", "核对安全库存", "优先处理药品/耗材/尿不湿/床品")),
        logisticsRisk("purchase", "采购与到货闭环", "采购/财务",
            purchaseApprovedCount > 0 ? "MEDIUM" : "LOW", "/logistics/storage/purchase",
            List.of("草稿待审 " + purchaseDraftCount + " 单", "已批待到货 " + purchaseApprovedCount + " 单"),
            List.of("审批采购预算", "跟踪供应商到货", "入库验收并回写库存")),
        logisticsRisk("maintenance", "维修与设备维保", "后勤主管",
            maintenanceOverdueCount > 0 ? "HIGH" : "LOW", "/logistics/assets/maintenance-record",
            List.of("维修未闭环 " + maintenanceOpenCount + " 单", "维修超时 " + maintenanceOverdueCount + " 单", "维保临期 " + equipmentDueSoonCount + " 台"),
            List.of("派单到责任人", "核对成本材料", "维保待办自动生成", "关闭维修记录")),
        logisticsRisk("service", "保洁洗衣与餐饮保障", "后勤/食堂",
            todayCleaningOpenCount + undeliveredMealCount > 0 ? "MEDIUM" : "LOW", "/logistics/task-center",
            List.of("未完成保洁 " + todayCleaningOpenCount + " 项", "未送达餐 " + undeliveredMealCount + " 份", "特殊餐 " + specialMealCount + " 份"),
            List.of("保洁任务打卡", "送餐记录核对", "特殊餐/禁忌提示", "与护理交接同步"))
    );
  }

  private List<LogisticsAction> buildLogisticsActions(
      long lowStockCount,
      long expiringCount,
      long purchaseApprovedCount,
      long maintenanceOverdueCount,
      long equipmentDueSoonCount,
      long todayCleaningOpenCount,
      long undeliveredMealCount) {
    List<LogisticsAction> actions = new ArrayList<>();
    if (lowStockCount > 0 || expiringCount > 0) {
      actions.add(logisticsAction("处理库存低/临期预警", "仓库/后勤", "HIGH", "/logistics/storage/alerts",
          "药品、耗材、餐饮物资和床品库存异常会影响照护连续性，需要先确认采购或调拨。"));
    }
    if (maintenanceOverdueCount > 0 || equipmentDueSoonCount > 0) {
      actions.add(logisticsAction("关闭维修与维保风险", "后勤主管", "HIGH", "/logistics/assets/maintenance-record",
          "维修超时和设备维保临期会影响安全责任留痕，需要派单、验收并关闭记录。"));
    }
    if (purchaseApprovedCount > 0) {
      actions.add(logisticsAction("跟进已批采购到货", "采购/仓库", "MEDIUM", "/logistics/storage/purchase",
          "已审批采购单需要进入到货、入库和账务核对，避免库存预警长期挂起。"));
    }
    if (todayCleaningOpenCount > 0 || undeliveredMealCount > 0) {
      actions.add(logisticsAction("补齐今日保障任务", "后勤/食堂", "MEDIUM", "/logistics/task-center",
          "保洁、洗衣、维修、送餐等任务需要当日闭环，避免影响入住体验。"));
    }
    if (actions.isEmpty()) {
      actions.add(logisticsAction("执行后勤周复盘", "院长/后勤主管", "LOW", "/stats/operations?tab=logistics",
          "当前后勤风险平稳，建议复盘库存金额、维修成本、设备维保和餐饮保障。"));
    }
    return actions;
  }

  private List<LogisticsFlow> buildLogisticsFlows() {
    return List.of(
        logisticsFlow("stock", "药品耗材库存闭环", "READY", "/logistics/storage/outbound",
            List.of("安全库存配置", "低库存/临期预警", "采购或调拨", "入库验收", "长者领用归集", "护理/餐饮消耗归集")),
        logisticsFlow("maintenance", "设备维修维保闭环", "READY", "/logistics/maintenance/assets",
            List.of("设备建档", "维保计划", "自动待办", "维修派单", "验收关闭")),
        logisticsFlow("service-task", "保洁洗衣维修工单", "READY", "/logistics/task-center",
            List.of("任务发起", "派单执行", "现场备注", "成本记录", "服务验收")),
        logisticsFlow("dining", "食堂营养餐保障", "READY", "/logistics/dining/recipe",
            List.of("菜品营养维护", "周菜谱排班", "特殊餐识别", "备料采购", "送餐记录"))
    );
  }

  private List<WorkforceRisk> buildWorkforceRisks(
      long certificateMissingCount,
      long contractExpiringCount,
      long unreviewedAttendanceCount,
      long trainingAbsentCount,
      long pendingCareTaskCount,
      long todayScheduleCount,
      long todayAttendanceCount) {
    return List.of(
        workforceRisk("schedule", "排班与在岗缺口", "人事/护理主管",
            todayAttendanceCount < todayScheduleCount ? "HIGH" : "LOW",
            "/care/scheduling/shift-calendar",
            List.of("今日排班 " + todayScheduleCount + " 人次", "今日在岗 " + todayAttendanceCount + " 人"),
            List.of("确认未打卡人员", "安排临时补位", "同步交接班重点风险")),
        workforceRisk("qualification", "资质证照完整性", "人事主管",
            certificateMissingCount > 0 ? "HIGH" : "LOW",
            "/hr/development/certificate-reminders",
            List.of("缺少证照编号 " + certificateMissingCount + " 人", "30天内合同到期 " + contractExpiringCount + " 人"),
            List.of("补齐护理员/医护资质", "证照到期提醒", "续签与离职交接")),
        workforceRisk("attendance", "考勤审核与请假调班", "人事/部门主管",
            unreviewedAttendanceCount > 0 ? "MEDIUM" : "LOW",
            "/hr/attendance/records",
            List.of("本月未审核考勤 " + unreviewedAttendanceCount + " 条", "培训缺勤 " + trainingAbsentCount + " 次"),
            List.of("批量审核异常考勤", "关闭请假/调班审批", "沉淀工时统计")),
        workforceRisk("care-load", "护理任务人力负荷", "护理主管",
            pendingCareTaskCount > 0 ? "MEDIUM" : "LOW",
            "/medical-care/care-task-board",
            List.of("今日未完成护理任务 " + pendingCareTaskCount + " 项"),
            List.of("按护理等级复核任务", "关注超时和异常任务", "把质检结果回写绩效"))
    );
  }

  private List<WorkforceRoleLoad> buildWorkforceRoleLoads(
      long activeStaffCount,
      long profileCount,
      long todayScheduleCount,
      long todayAttendanceCount,
      long todayCareTaskCount,
      long pendingCareTaskCount,
      long monthTrainingCount,
      long monthRewardPunishmentCount) {
    long completedCareTaskCount = Math.max(0, todayCareTaskCount - pendingCareTaskCount);
    return List.of(
        workforceRoleLoad("hr", "人事基础盘", activeStaffCount + " 人在职 / " + profileCount + " 份档案",
            profileCount >= activeStaffCount ? "READY" : "WARNING", "/hr/workbench",
            List.of("员工档案", "劳动合同", "证照提醒", "社保与宿舍")),
        workforceRoleLoad("schedule", "排班出勤", todayAttendanceCount + "/" + todayScheduleCount,
            todayScheduleCount == 0 || todayAttendanceCount < todayScheduleCount ? "WARNING" : "READY",
            "/care/scheduling/shift-calendar",
            List.of("排班日历", "请假调班", "考勤审核", "工时统计")),
        workforceRoleLoad("care", "照护执行人效", completedCareTaskCount + "/" + todayCareTaskCount,
            pendingCareTaskCount > 0 ? "WARNING" : "READY", "/medical-care/care-task-board",
            List.of("护理任务", "异常上报", "拍照备注", "质检复盘")),
        workforceRoleLoad("growth", "培训与绩效", monthTrainingCount + " 场培训 / " + monthRewardPunishmentCount + " 条奖惩",
            "READY", "/hr/performance/reports",
            List.of("培训记录", "证照生成", "奖惩积分", "绩效榜"))
    );
  }

  private List<WorkforceAction> buildWorkforceActions(
      long certificateMissingCount,
      long contractExpiringCount,
      long unreviewedAttendanceCount,
      long trainingAbsentCount,
      long pendingCareTaskCount,
      long todayScheduleCount,
      long todayAttendanceCount) {
    List<WorkforceAction> actions = new ArrayList<>();
    if (todayAttendanceCount < todayScheduleCount) {
      actions.add(workforceAction("补齐今日排班在岗缺口", "人事/护理主管", "HIGH", "/care/scheduling/shift-calendar",
          "当前在岗人数低于排班人数，需要确认请假、漏打卡或临时补班。"));
    }
    if (certificateMissingCount > 0 || contractExpiringCount > 0) {
      actions.add(workforceAction("处理证照与合同提醒", "人事主管", "HIGH", "/hr/development/certificate-reminders",
          "证照缺失或合同临期会影响人员资质合规和监管检查。"));
    }
    if (pendingCareTaskCount > 0) {
      actions.add(workforceAction("复核护理任务负荷", "护理主管", "MEDIUM", "/medical-care/care-task-board",
          "今日仍有护理任务未完成，应按护理等级和班次重新分配。"));
    }
    if (unreviewedAttendanceCount > 0 || trainingAbsentCount > 0) {
      actions.add(workforceAction("关闭考勤培训异常", "人事/部门主管", "MEDIUM", "/hr/attendance/records",
          "考勤未审核和培训缺勤需要进入工时、绩效和复盘记录。"));
    }
    if (actions.isEmpty()) {
      actions.add(workforceAction("执行本周人效复盘", "院长/人事", "LOW", "/hr/performance/reports",
          "当前人力风险平稳，建议复盘护理任务、人力排班、培训和奖惩积分。"));
    }
    return actions;
  }

  private List<WorkforceFlow> buildWorkforceFlows() {
    return List.of(
        workforceFlow("profile", "员工档案与资质", "READY", "/hr/development/certificate-reminders",
            List.of("入职建档", "合同与证照登记", "证照到期提醒", "培训/奖惩归集", "离职归档")),
        workforceFlow("schedule", "排班、请假与调班", "READY", "/care/scheduling/shift-calendar",
            List.of("生成排班", "员工请假/调班", "主管审批", "同步 OA 日程", "考勤核对")),
        workforceFlow("performance", "服务绩效闭环", "PARTIAL", "/hr/performance/reports",
            List.of("护理任务执行", "异常与质检", "奖惩积分", "工时统计", "绩效报表")),
        workforceFlow("smart-scheduling", "智能排班准备", "PLANNED", "/stats/operations?tab=intelligence",
            List.of("采集护理等级", "采集资质与班次", "计算人力缺口", "生成可解释排班建议"))
    );
  }

  private List<SafetyRiskSource> buildSafetyRiskSources(
      long openIncidentCount,
      long majorIncidentCount,
      long openAlertCount,
      long criticalAlertCount,
      long nightPatrolOpenCount,
      long fireOpenCount) {
    return List.of(
        safetySource("incident", "事故事件", "护理主管/安全员", openIncidentCount > 0 || majorIncidentCount > 0 ? "WARNING" : "READY",
            "/life/incident?status=OPEN",
            List.of("未闭环事故 " + openIncidentCount + " 起", "本月重大事故 " + majorIncidentCount + " 起"),
            List.of("事故登记", "现场处理", "家属告知", "整改复盘", "关闭归档")),
        safetySource("smart-alert", "智慧设备告警", "医护/门岗", openAlertCount > 0 ? "WARNING" : "READY",
            "/medical-care/smart-alerts?status=OPEN",
            List.of("未闭环告警 " + openAlertCount + " 条", "紧急告警 " + criticalAlertCount + " 条"),
            List.of("告警确认", "到场核验", "处置说明", "必要时同步家属", "设备复位")),
        safetySource("night-patrol", "夜间巡房/防火巡查", "门岗/后勤", nightPatrolOpenCount > 0 ? "WARNING" : "READY",
            "/fire/night-patrol?status=OPEN",
            List.of("夜间巡查未闭环 " + nightPatrolOpenCount + " 项", "消防安全待办 " + fireOpenCount + " 项"),
            List.of("扫码巡查", "隐患记录", "交接打卡", "维修联动", "巡查报表")),
        safetySource("emergency", "应急预案", "院长/安全员", "READY",
            "/life/incident",
            List.of("事故登记已支持应急预案、现场处置、家属告知、整改复盘和监管上报"),
            List.of("触发识别", "预案启动", "现场处置", "通知链路", "复盘整改", "监管报备"))
    );
  }

  private List<EmergencyFlow> buildEmergencyFlows() {
    return List.of(
        emergencyFlow("fall", "跌倒/坠床应急", "设备跌倒告警、护理巡房发现、家属反馈", "/life/incident?incidentType=跌倒",
            List.of("启动应急预案", "现场评估意识和疼痛", "通知护士/医生/家属", "必要时转诊", "登记事故报告并复盘防跌倒措施")),
        emergencyFlow("lost", "走失/电子围栏应急", "门禁、定位手环或巡房未见", "/medical-care/smart-alerts?status=OPEN",
            List.of("门岗锁定出入口", "调取定位/监控", "分区搜寻", "通知家属与管理层", "形成走失复盘")),
        emergencyFlow("choking", "噎食/窒息应急", "餐厅/护理员发现异常", "/life/incident?incidentType=噎食",
            List.of("立即海姆立克/呼叫医护", "记录餐食与体征", "必要时急救转运", "通知家属", "复盘餐饮风险并登记监管上报情况")),
        emergencyFlow("infection", "感染/发热聚集应急", "生命体征异常或多人相似症状", "/health/management/data",
            List.of("隔离观察", "体温/血氧复测", "上报院感责任人", "环境消杀", "形成监管记录")),
        emergencyFlow("pressure", "压疮风险处置", "评估高风险、床垫告警或皮肤异常", "/medical-care/care-task-board",
            List.of("翻身计划加密", "拍照记录", "调整床垫/护理垫", "护理质检跟踪", "家属同步"))
    );
  }

  private List<SafetyAction> buildSafetyActions(
      long openIncidentCount,
      long openAlertCount,
      long criticalAlertCount,
      long nightPatrolOpenCount,
      long overdueFireCount) {
    List<SafetyAction> actions = new ArrayList<>();
    if (criticalAlertCount > 0) {
      actions.add(safetyAction("优先处理紧急设备告警", "HIGH", "医护/门岗", "/medical-care/smart-alerts?level=CRITICAL",
          "存在紧急设备告警，需要先确认到场、处置说明和是否通知家属。"));
    }
    if (openIncidentCount > 0) {
      actions.add(safetyAction("关闭未闭环事故", "HIGH", "护理主管/安全员", "/life/incident?status=OPEN",
          "事故需要补齐现场处理、整改措施、复盘结论和关闭状态。"));
    }
    if (nightPatrolOpenCount > 0 || overdueFireCount > 0) {
      actions.add(safetyAction("补齐夜间巡查与消防待办", "MEDIUM", "门岗/后勤", "/fire/night-patrol?status=OPEN",
          "夜间巡查和消防记录未闭环会影响安全责任留痕。"));
    }
    if (openAlertCount > criticalAlertCount) {
      actions.add(safetyAction("复核设备在线与告警质量", "MEDIUM", "系统管理员/医护", "/medical-care/smart-alerts?status=OPEN",
          "排查重复告警、离线设备和位置绑定，避免真实风险被噪声淹没。"));
    }
    if (actions.isEmpty()) {
      actions.add(safetyAction("执行安全例行复盘", "LOW", "院长/安全员", "/stats/operations?tab=safety",
          "当前安全风险平稳，建议按周复盘事故、巡查、设备告警和应急演练记录。"));
    }
    return actions;
  }

  private List<SafetyRecentEvent> buildSafetyRecentEvents(Long orgId) {
    List<SafetyRecentEvent> events = new ArrayList<>();
    List<IncidentReport> incidents = incidentReportMapper.selectList(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .orderByDesc(IncidentReport::getIncidentTime)
        .last("LIMIT 5"));
    for (IncidentReport item : incidents) {
      events.add(safetyEvent(item.getId(), "事故", defaultText(item.getIncidentType(), "事故事件") + " / " + defaultText(item.getElderName(), "未关联长者"),
          defaultText(item.getLevel(), "NORMAL"), defaultText(item.getStatus(), "OPEN"),
          item.getIncidentTime() == null ? "" : item.getIncidentTime().format(DATETIME_FMT),
          "/life/incident"));
    }
    List<SmartAlert> alerts = smartAlertMapper.selectList(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(orgId != null, SmartAlert::getOrgId, orgId)
        .orderByDesc(SmartAlert::getLatestTriggeredAt)
        .last("LIMIT 5"));
    for (SmartAlert item : alerts) {
      events.add(safetyEvent(item.getId(), "设备告警", defaultText(item.getTitle(), item.getAlertType()),
          defaultText(item.getLevel(), "WARNING"), defaultText(item.getStatus(), "OPEN"),
          item.getLatestTriggeredAt() == null ? "" : item.getLatestTriggeredAt().format(DATETIME_FMT),
          "/medical-care/smart-alerts"));
    }
    List<FireSafetyRecord> fireRecords = fireSafetyRecordMapper.selectList(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .orderByDesc(FireSafetyRecord::getCheckTime)
        .last("LIMIT 5"));
    for (FireSafetyRecord item : fireRecords) {
      events.add(safetyEvent(item.getId(), "消防巡查", defaultText(item.getTitle(), item.getRecordType()),
          "NORMAL", defaultText(item.getStatus(), "OPEN"),
          item.getCheckTime() == null ? "" : item.getCheckTime().format(DATETIME_FMT),
          "NIGHT_PATROL".equals(item.getRecordType()) ? "/fire/night-patrol" : "/fire/data-stats"));
    }
    return events.stream()
        .sorted(Comparator.comparing(OperationsSafetyRiskResponse.SafetyRecentEvent::getOccurredAt, Comparator.nullsLast(String::compareTo)).reversed())
        .limit(10)
        .toList();
  }

  private List<MetricCapability> buildMetrics() {
    return List.of(
        metric("in_hospital_count", "当前入住人数", "/elder/in-hospital-overview", "DashboardSummary.inHospitalCount", "判断当前服务规模和人力需求"),
        metric("bed_occupancy", "床位入住率", "/logistics/assets/room-state-map", "DashboardSummary.bedOccupancyRate", "识别空床转化和房态调度压力"),
        metric("monthly_revenue", "月收入", "/finance/workbench", "MonthlyRevenueStats", "判断现金流、收费进度与收入结构"),
        metric("unpaid_bills", "欠费金额/账单", "/finance/bills/follow-up", "BillMonthly unpaid", "驱动催缴、自动扣费和账务复核"),
        metric("care_completion", "护理任务完成率", "/medical-care/care-task-board", "CareTaskDaily", "识别照护执行是否掉线"),
        metric("risk_elder_count", "风险老人数量", "/elder/resident-360", "OaPortalSummary.elderAbnormalCount", "安排巡视、交接班和风险告知"),
        metric("incident_count", "本月事故数量", "/life/incident", "IncidentReport", "用于安全复盘与监管留痕"),
        metric("family_satisfaction", "家属满意度", "/survey/stats", "Survey statistics", "衡量服务体验与投诉闭环"),
        metric("staff_efficiency", "员工人效", "/stats/operations?tab=workforce", "Hr + Care task", "评估排班、工时和护理任务负荷"),
        metric("health_alert_trend", "老人健康异常趋势", "/medical-care/smart-alerts", "SmartAlert DERIVED_HEALTH_TREND", "识别连续异常、复测缺口和干预任务"),
        metric("marketing_conversion", "营销线索转化率", "/stats/operations?tab=marketing", "Marketing funnel", "判断获客渠道和入住转化质量")
    );
  }

  private List<IntelligenceCapability> buildIntelligence() {
    return List.of(
        intelligence("risk_prediction", "智能风险预警", "PARTIAL", "/stats/operations?tab=intelligence",
            List.of("已具备异常规则配置和连续健康趋势预警", "下一步识别夜间频繁呼叫和活动下降趋势", "再联动统一任务中心自动派单")),
        intelligence("ai_care_summary", "AI护理摘要/月报", "READY", "/care/service/nursing-reports",
            List.of("沉淀护理任务、质检、异常和交接班", "生成月度照护摘要", "输出家属端可读版本")),
        intelligence("voice_recording", "语音录入护理记录", "PLANNED", "/stats/operations?tab=intelligence",
            List.of("先统一护理日志字段", "再接入语音转写", "最后进入质检与交接班")),
        intelligence("iot_devices", "IoT设备接入", "READY", "/stats/operations?tab=intelligence",
            List.of("已预留智慧设备告警入口", "接入床垫、手环、门禁、呼叫器", "按设备类型配置处置 SLA")),
        intelligence("smart_scheduling", "智能排班", "PLANNED", "/stats/operations?tab=intelligence",
            List.of("采集护理等级、人力资质和工时", "预测班次缺口", "推荐可解释排班方案")),
        intelligence("family_qa", "家属智能问答", "PLANNED", "/stats/operations?tab=intelligence",
            List.of("限定可回答数据范围", "接入老人动态、费用和探访规则", "保留人工转接与审计日志"))
    );
  }

  private List<ReportMetric> buildReportMetrics() {
    return List.of(
        reportMetric("care_completion", "护理任务完成率", "看板追踪", "按今日/本月护理任务统计完成、超时和异常", "READY", "/medical-care/care-task-board"),
        reportMetric("patrol_completion", "巡房完成率", "驾驶舱追踪", "夜间巡房、健康巡检和消防巡查统一纳入闭环", "READY", "/stats/operations?tab=safety"),
        reportMetric("abnormal_sla", "异常处理时效", "SLA追踪", "异常任务中心跟踪护理异常处理、质检复盘和关闭留痕", "READY", "/care/exception"),
        reportMetric("family_satisfaction", "家属满意度", "问卷统计", "满意度问卷、投诉建议和回访评分合并查看", "READY", "/survey/stats"),
        reportMetric("complaint_closure", "投诉闭环", "协同待办", "投诉建议转任务、审批、回访和复盘", "PARTIAL", "/oa/work-execution/task"),
        reportMetric("monthly_report", "月度服务报告", "护理报表生成", "护理执行、质检复盘、异常闭环和交接班形成月报摘要", "READY", "/care/service/nursing-reports")
    );
  }

  private List<QualityDomain> buildQualityDomains() {
    return List.of(
        qualityDomain("nursing", "护理服务质检", "护理主管", "READY", "/care/service/nursing-reports",
            List.of("护理任务完成率", "扫码执行记录", "异常任务复核", "护理计划覆盖", "护理质检复盘", "月度服务报告"),
            List.of("/medical-care/care-task-board", "/medical-care/nursing-quality", "/care/service/nursing-reports")),
        qualityDomain("health", "健康与用药质控", "医护主管", "READY", "/medical-care/center",
            List.of("生命体征异常", "慢病档案", "医嘱执行", "任务确认发药", "漏服/拒服提醒"),
            List.of("/health/management/data", "/medical-care/orders", "/health/medication/medication-registration")),
        qualityDomain("safety", "安全事件质控", "院长/安全员", "READY", "/stats/operations?tab=safety",
            List.of("事故上报", "应急处置", "夜间巡房", "消防巡查", "设备告警"),
            List.of("/stats/operations?tab=safety", "/life/incident", "/fire/night-patrol", "/medical-care/smart-alerts")),
        qualityDomain("family", "家属体验质控", "社工/客服", "PARTIAL", "/oa/work-execution/family-communication",
            List.of("家属沟通", "投诉建议", "探访/外出", "生日关怀", "满意度调查"),
            List.of("/oa/work-execution/family-communication", "/survey/stats", "/oa/life/birthday")),
        qualityDomain("finance", "收费合同质控", "财务主管", "READY", "/finance/workbench",
            List.of("合同归档", "账单核销", "欠费催缴", "发票收据", "退住结算"),
            List.of("/finance/bills/in-resident", "/finance/bills/follow-up", "/finance/discharge/settlement"))
    );
  }

  private List<ComplianceArchive> buildComplianceArchives() {
    return List.of(
        archive("elder-agreement", "长者入住合规档案", "PARTIAL", "/oa/document?compliance=1&archive=elder-agreement",
            List.of("入住协议", "知情同意书", "风险告知书", "护理等级确认", "家属联系人授权"),
            List.of("统一档案包下载", "风险告知模板电子签")),
        archive("medical-care", "医疗护理记录档案", "READY", "/oa/document?compliance=1&archive=medical-care",
            List.of("护理记录", "医嘱记录", "用药确认", "生命体征", "交接班记录"),
            List.of("健康档案已补充就医、检查报告和康复记录摘要", "检查报告附件进入文档中心归档")),
        archive("incident", "事故与应急档案", "READY", "/oa/document?compliance=1&archive=incident",
            List.of("事故报告", "现场处理", "家属告知", "整改措施", "复盘结论"),
            List.of("监管报表固定模板", "演练记录归档")),
        archive("government", "监管与经营报表", "PARTIAL", "/oa/document?compliance=1&archive=government",
            List.of("入住人数", "床位使用率", "月收入", "事故数量", "服务质量"),
            List.of("政府监管导出模板", "月度服务报告 PDF/Excel")),
        archive("audit", "权限与操作日志", "READY", "/oa/document?compliance=1&archive=audit",
            List.of("角色权限", "页面权限", "操作日志", "制度更新", "员工培训记录"),
            List.of("关键业务操作审计看板"))
    );
  }

  private List<StandardProcess> buildStandardProcesses() {
    return List.of(
        process("admission", "入住流程标准化", "READY", "/elder/admission-processing",
            List.of("咨询转入", "入住评估", "合同签约", "床位分配", "费用审核", "护理计划创建")),
        process("care", "护理流程标准化", "READY", "/medical-care/care-task-board",
            List.of("护理计划", "任务生成", "扫码执行", "异常上报", "质检复盘", "交接班")),
        process("incident", "事故处理流程标准化", "READY", "/life/incident",
            List.of("事故上报", "应急预案", "现场处置", "通知家属", "整改措施", "复盘结论", "监管报告")),
        process("discharge", "退住流程标准化", "READY", "/finance/discharge/settlement",
            List.of("退住申请", "费用审核", "物品/药品结清", "退费结算", "离院归档")),
        process("handover", "员工交接流程标准化", "READY", "/medical-care/handovers",
            List.of("班次交接", "风险长者", "未完成任务", "异常事件", "物资/药品交接"))
    );
  }

  private List<ReportAction> buildMonthlyActions() {
    return List.of(
        action("生成本月服务质量报告", "HIGH", "院长/运营", "/stats/operations?tab=quality", "月末前"),
        action("补齐高风险长者风险告知书", "HIGH", "护理主管", "/elder/resident-360", "3天内"),
        action("复盘异常任务和超时工单", "MEDIUM", "医护/后勤主管", "/medical-care/unified-task-center", "本周内"),
        action("导出满意度与投诉闭环清单", "MEDIUM", "社工/客服", "/survey/stats", "本周内"),
        action("抽查合同、票据和退住结算档案", "MEDIUM", "财务主管", "/finance/workbench", "月结前")
    );
  }

  private List<FamilyServiceMetric> buildFamilyMetrics() {
    return List.of(
        familyMetric("visible_data", "家属可见数据", "7类", "动态、护理、健康、报告、餐食、缴费、相册", "/oa/family-users"),
        familyMetric("visit_booking", "探访预约", "已接入", "家属预约、门岗核验、访客小票", "/elder/status-change/visit-register"),
        familyMetric("outing", "请假外出", "已接入", "家属申请、机构审批、外出登记、返院回填", "/elder/status-change/outing?status=APPLIED"),
        familyMetric("payment", "在线缴费", "已接入", "充值、微信预支付、订单查询、自动扣费提醒", "/finance/payments/cashier-desk"),
        familyMetric("communication", "反馈与沟通", "闭环中", "家属留言、评价、投诉建议、处理反馈", "/oa/work-execution/family-communication")
    );
  }

  private List<FamilyServiceFlow> buildFamilyFlows() {
    return List.of(
        familyFlow("visit", "探访预约闭环", "READY", "/elder/status-change/visit-register",
            "/api/family/visit/book / /api/family/visit/my", "/api/guard/visit/today",
            List.of("家属提交预约", "生成 visitCode/verifyCode", "门岗查看今日预约", "到访核验签到", "探访记录归档")),
        familyFlow("outing", "请假外出闭环", "READY", "/elder/status-change/outing?status=APPLIED",
            "/api/family/outing-records / /api/family/outing-applications", "/api/elder/lifecycle/outing/{id}/approve",
            List.of("家属提交申请", "机构审核批准", "老人状态转外出中", "返院时间回填", "申请可取消/记录可查")),
        familyFlow("feedback", "投诉建议闭环", "READY", "/oa/work-execution/family-communication",
            "/api/family/feedback", "/api/oa/suggestion/{id}/handle",
            List.of("家属提交评价/投诉", "生成 OA 建议处理单", "客服填写处理反馈", "处理完成后家属可查回复")),
        familyFlow("payment", "在线缴费闭环", "READY", "/finance/workbench",
            "/api/family/payment/summary", "/finance/payments/cashier-desk",
            List.of("家属查看账单", "充值/微信预支付", "财务收款核销", "异常订单生成待办")),
        familyFlow("communication", "家属沟通闭环", "READY", "/oa/work-execution/family-communication",
            "/api/family/communication/messages", "/oa/work-execution/family-communication",
            List.of("文字/语音/素材留言", "机构端处理", "状态同步", "必要时转投诉处理单"))
    );
  }

  private List<FamilyVisibleData> buildFamilyVisibleData() {
    return List.of(
        visibleData("dynamic", "老人动态", "READY", "/api/family/dashboard/home", "/oa/activity-center/records",
            List.of("动态摘要", "近期活动", "机构通知", "重点事件")),
        visibleData("care", "护理记录", "READY", "/api/family/care-logs", "/medical-care/care-task-board",
            List.of("护理项目", "执行时间", "护理员", "备注", "照片")),
        visibleData("health", "健康数据", "READY", "/api/family/health/trend", "/health/management/data",
            List.of("血压", "血糖", "体温", "心率", "异常提示")),
        visibleData("assessment", "评估报告", "READY", "/api/family/assessment-reports", "/elder/assessment/ability/archive",
            List.of("入住评估", "持续评估", "认知/自理报告", "AI摘要")),
        visibleData("medical", "就医与医嘱", "READY", "/api/family/medical-records", "/medical-care/orders",
            List.of("就医记录", "诊断", "建议", "用药")),
        visibleData("payment", "费用与缴费", "READY", "/api/family/payment/summary", "/finance/bills/in-resident",
            List.of("账单", "已缴", "欠费", "余额", "缴费历史")),
        visibleData("album", "照片视频", "READY", "/api/family/activity-albums", "/oa/activity-center/records",
            List.of("活动相册", "评论", "点赞", "可见范围"))
    );
  }

  private List<FamilyServiceAction> buildFamilyActions() {
    return List.of(
        familyAction("配置外出申请审批提醒", "护理/客服", "MEDIUM", "/elder/status-change/outing?status=APPLIED",
            "家属端申请、取消与机构审批已接入，后续可把待审批、逾期未归推送到工作台和微信通知。"),
        familyAction("固化家属可见范围规则", "院长/系统管理员", "HIGH", "/oa/family-users",
            "护理、健康、相册、费用等敏感数据需要按关系、授权和安全密码控制。"),
        familyAction("把探访预约接入首页待办", "门岗/客服", "MEDIUM", "/elder/status-change/visit-register",
            "今日预约、待签到、已过期预约需要进入工作台。"),
        familyAction("投诉建议处理后自动回访", "社工/客服", "MEDIUM", "/oa/work-execution/family-communication",
            "处理完成后生成回访任务并沉淀满意度。")
    );
  }

  private List<IntelligenceMetric> buildIntelligenceMetrics() {
    return List.of(
        intelligenceMetric("risk_rules", "风险规则", "已接入", "异常规则配置、风险时间线、统一任务中心", "/medical-care/alert-rules"),
        intelligenceMetric("iot_alerts", "IoT告警", "已接入", "设备档案、事件上报、告警确认与处置", "/medical-care/smart-alerts"),
        intelligenceMetric("ai_reports", "AI护理摘要", "已接入", "月度服务摘要、风险提醒、家属可读摘要、下月建议", "/care/service/nursing-reports"),
        intelligenceMetric("schedule", "智能排班", "规划中", "先接排班日历、班组和护理等级，再做推荐", "/care/scheduling/shift-calendar"),
        intelligenceMetric("family_qa", "家属问答", "规划中", "基于家属可见范围、费用、探访规则和常见问题", "/oa/family-users")
    );
  }

  private List<IntelligenceScenario> buildIntelligenceScenarios() {
    return List.of(
        intelligenceScenario("risk_prediction", "智能风险预警", "PARTIAL", "/medical-care/smart-alerts", "医护主管",
            List.of("异常规则配置", "连续健康趋势预警", "风险时间线", "健康巡检异常"),
            List.of("识别夜间频繁呼叫", "把活动下降转成护理观察任务", "把趋势预警处置同步到统一任务中心")),
        intelligenceScenario("ai_care_summary", "AI护理摘要", "READY", "/care/service/nursing-reports", "医护主管",
            List.of("月度照护摘要", "照护亮点", "风险提醒", "家属可读摘要", "下月建议"),
            List.of("接入生命体征趋势", "发布为家属端月报", "支持一键转随访任务")),
        intelligenceScenario("voice_recording", "语音录入护理记录", "PLANNED", "/medical-care/nursing-log", "护理主管",
            List.of("护理日志字段", "家属语音/沟通媒体上传能力"),
            List.of("接入护理员语音转写", "结构化抽取护理项目和异常", "纳入护理质检复核")),
        intelligenceScenario("iot_devices", "IoT设备接入", "READY", "/medical-care/smart-alerts", "设备/医护",
            List.of("设备档案", "事件上报", "告警确认", "告警处置", "通知家属选项"),
            List.of("补齐床垫、手环、门禁、呼叫器设备模板", "按设备类型配置SLA", "联动夜间巡房")),
        intelligenceScenario("smart_scheduling", "智能排班", "PLANNED", "/care/scheduling/shift-calendar", "护理/人事",
            List.of("排班方案", "排班日历", "调班申请", "护理员分组"),
            List.of("按护理等级与任务量预测人力缺口", "推荐班次与备班", "把证照资质纳入约束")),
        intelligenceScenario("family_qa", "家属智能问答", "PLANNED", "/oa/family-users", "客服/系统管理员",
            List.of("家属服务中心", "帮助FAQ", "沟通模板", "家属可见数据范围"),
            List.of("限定问答数据范围", "支持费用/探访/老人状态查询", "低置信度转人工沟通"))
    );
  }

  private List<IntelligenceDataSource> buildIntelligenceDataSources() {
    return List.of(
        intelligenceDataSource("vitals", "健康生命体征", "READY", "/health/management/data",
            List.of("血压", "血糖", "体温", "血氧/心率", "健康巡检异常")),
        intelligenceDataSource("care", "护理执行记录", "READY", "/medical-care/care-task-board",
            List.of("护理任务", "扫码执行", "异常任务", "交接班", "护理日志")),
        intelligenceDataSource("device", "智慧设备事件", "READY", "/medical-care/smart-alerts",
            List.of("SOS", "跌倒", "电子围栏", "生命体征设备", "设备在线状态")),
        intelligenceDataSource("family", "家属服务数据", "PARTIAL", "/oa/family-users",
            List.of("沟通消息", "投诉建议", "探访预约", "缴费状态", "满意度")),
        intelligenceDataSource("hr", "人力排班数据", "PARTIAL", "/care/scheduling/shift-calendar",
            List.of("班次", "护理员分组", "请假调班", "证照资质", "培训记录"))
    );
  }

  private List<IntelligenceAction> buildIntelligenceActions() {
    return List.of(
        intelligenceAction("完善多源风险预警规则", "医护主管", "HIGH", "/medical-care/smart-alerts",
            "在连续血压/血糖异常基础上，把夜间呼叫和活动下降纳入预警规则。"),
        intelligenceAction("补齐设备类型和SLA模板", "设备/后勤", "HIGH", "/medical-care/smart-alerts",
            "床垫、手环、门禁、呼叫器按类型配置处置时限和责任岗位。"),
        intelligenceAction("发布本月AI护理摘要模板", "医护主管", "MEDIUM", "/care/service/nursing-reports",
            "把护理执行、质检、异常和交接班摘要整理为可发布的月度照护报告。"),
        intelligenceAction("建立智能排班输入清单", "护理/人事", "MEDIUM", "/care/scheduling/shift-calendar",
            "确认护理等级、任务量、人员资质、请假和工时作为推荐排班输入。"),
        intelligenceAction("定义家属问答安全边界", "系统管理员", "MEDIUM", "/oa/family-users",
            "限定可回答字段、敏感数据校验、人工转接和审计日志。")
    );
  }

  private List<CapabilitySummary> buildSummary(List<DomainCapability> domains) {
    int ready = (int) domains.stream().filter(item -> "READY".equals(item.getStatus())).count();
    int partial = (int) domains.stream().filter(item -> "PARTIAL".equals(item.getStatus())).count();
    int planned = (int) domains.stream().filter(item -> "PLANNED".equals(item.getStatus())).count();
    CapabilitySummary summary = new CapabilitySummary();
    summary.setLabel("综合运营平台能力");
    summary.setTotal(domains.size());
    summary.setReady(ready);
    summary.setPartial(partial);
    summary.setPlanned(planned);
    return List.of(summary);
  }

  private String resolveOverallStatus(List<DomainCapability> domains) {
    boolean hasPartial = domains.stream().anyMatch(item -> "PARTIAL".equals(item.getStatus()));
    boolean hasPlanned = domains.stream().anyMatch(item -> "PLANNED".equals(item.getStatus()));
    if (hasPartial || hasPlanned) {
      return "LANDING";
    }
    return "READY";
  }

  private DomainCapability domain(String key, String title, String description, String status, String routePath,
      List<String> completed, List<String> nextActions, List<String> evidenceRoutes) {
    DomainCapability item = new DomainCapability();
    item.setKey(key);
    item.setTitle(title);
    item.setDescription(description);
    item.setStatus(status);
    item.setStatusText(statusText(status));
    item.setRoutePath(routePath);
    item.setCompleted(completed);
    item.setNextActions(nextActions);
    item.setEvidenceRoutes(evidenceRoutes);
    return item;
  }

  private MetricCapability metric(String key, String name, String routePath, String source, String decisionValue) {
    MetricCapability item = new MetricCapability();
    item.setKey(key);
    item.setName(name);
    item.setRoutePath(routePath);
    item.setSource(source);
    item.setDecisionValue(decisionValue);
    return item;
  }

  private IntelligenceCapability intelligence(String key, String title, String status, String routePath,
      List<String> landingSteps) {
    IntelligenceCapability item = new IntelligenceCapability();
    item.setKey(key);
    item.setTitle(title);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setLandingSteps(landingSteps);
    return item;
  }

  private String statusText(String status) {
    if ("READY".equals(status)) {
      return "已落地";
    }
    if ("PARTIAL".equals(status)) {
      return "增强中";
    }
    return "待建设";
  }

  private YearMonth parseMonth(String month) {
    if (month == null || month.isBlank()) {
      return YearMonth.now();
    }
    try {
      return YearMonth.parse(month);
    } catch (DateTimeParseException ex) {
      return YearMonth.now();
    }
  }

  private ReportMetric reportMetric(String key, String label, String value, String helper, String status,
      String routePath) {
    ReportMetric item = new ReportMetric();
    item.setKey(key);
    item.setLabel(label);
    item.setValue(value);
    item.setHelper(helper);
    item.setStatus(status);
    item.setRoutePath(routePath);
    return item;
  }

  private QualityDomain qualityDomain(String key, String title, String owner, String status, String routePath,
      List<String> checkpoints, List<String> evidence) {
    QualityDomain item = new QualityDomain();
    item.setKey(key);
    item.setTitle(title);
    item.setOwner(owner);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setCheckpoints(checkpoints);
    item.setEvidence(evidence);
    return item;
  }

  private ComplianceArchive archive(String key, String title, String status, String routePath,
      List<String> requiredDocuments, List<String> missingOrNext) {
    ComplianceArchive item = new ComplianceArchive();
    item.setKey(key);
    item.setTitle(title);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setRequiredDocuments(requiredDocuments);
    item.setMissingOrNext(missingOrNext);
    return item;
  }

  private StandardProcess process(String key, String title, String status, String routePath, List<String> steps) {
    StandardProcess item = new StandardProcess();
    item.setKey(key);
    item.setTitle(title);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setSteps(steps);
    return item;
  }

  private ReportAction action(String title, String priority, String owner, String routePath, String dueHint) {
    ReportAction item = new ReportAction();
    item.setTitle(title);
    item.setPriority(priority);
    item.setOwner(owner);
    item.setRoutePath(routePath);
    item.setDueHint(dueHint);
    return item;
  }

  private FamilyServiceMetric familyMetric(String key, String label, String value, String helper, String routePath) {
    FamilyServiceMetric item = new FamilyServiceMetric();
    item.setKey(key);
    item.setLabel(label);
    item.setValue(value);
    item.setHelper(helper);
    item.setRoutePath(routePath);
    return item;
  }

  private FamilyServiceFlow familyFlow(String key, String title, String status, String routePath, String familyApi,
      String adminRoute, List<String> steps) {
    FamilyServiceFlow item = new FamilyServiceFlow();
    item.setKey(key);
    item.setTitle(title);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setFamilyApi(familyApi);
    item.setAdminRoute(adminRoute);
    item.setSteps(steps);
    return item;
  }

  private FamilyVisibleData visibleData(String key, String title, String status, String familyApi, String adminRoute,
      List<String> fields) {
    FamilyVisibleData item = new FamilyVisibleData();
    item.setKey(key);
    item.setTitle(title);
    item.setStatus(status);
    item.setFamilyApi(familyApi);
    item.setAdminRoute(adminRoute);
    item.setFields(fields);
    return item;
  }

  private FamilyServiceAction familyAction(String title, String owner, String priority, String routePath,
      String description) {
    FamilyServiceAction item = new FamilyServiceAction();
    item.setTitle(title);
    item.setOwner(owner);
    item.setPriority(priority);
    item.setRoutePath(routePath);
    item.setDescription(description);
    return item;
  }

  private IntelligenceMetric intelligenceMetric(String key, String label, String value, String helper,
      String routePath) {
    IntelligenceMetric item = new IntelligenceMetric();
    item.setKey(key);
    item.setLabel(label);
    item.setValue(value);
    item.setHelper(helper);
    item.setRoutePath(routePath);
    return item;
  }

  private IntelligenceScenario intelligenceScenario(String key, String title, String status, String routePath,
      String owner, List<String> landed, List<String> nextSteps) {
    IntelligenceScenario item = new IntelligenceScenario();
    item.setKey(key);
    item.setTitle(title);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setOwner(owner);
    item.setLanded(landed);
    item.setNextSteps(nextSteps);
    return item;
  }

  private IntelligenceDataSource intelligenceDataSource(String key, String title, String status, String routePath,
      List<String> signals) {
    IntelligenceDataSource item = new IntelligenceDataSource();
    item.setKey(key);
    item.setTitle(title);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setSignals(signals);
    return item;
  }

  private IntelligenceAction intelligenceAction(String title, String owner, String priority, String routePath,
      String description) {
    IntelligenceAction item = new IntelligenceAction();
    item.setTitle(title);
    item.setOwner(owner);
    item.setPriority(priority);
    item.setRoutePath(routePath);
    item.setDescription(description);
    return item;
  }

  private SafetyMetric safetyMetric(String key, String label, String value, String helper, String tone,
      String routePath) {
    SafetyMetric item = new SafetyMetric();
    item.setKey(key);
    item.setLabel(label);
    item.setValue(value);
    item.setHelper(helper);
    item.setTone(tone);
    item.setRoutePath(routePath);
    return item;
  }

  private SafetyRiskSource safetySource(String key, String title, String owner, String status, String routePath,
      List<String> signals, List<String> controls) {
    SafetyRiskSource item = new SafetyRiskSource();
    item.setKey(key);
    item.setTitle(title);
    item.setOwner(owner);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setSignals(signals);
    item.setControls(controls);
    return item;
  }

  private EmergencyFlow emergencyFlow(String key, String title, String trigger, String routePath, List<String> steps) {
    EmergencyFlow item = new EmergencyFlow();
    item.setKey(key);
    item.setTitle(title);
    item.setTrigger(trigger);
    item.setRoutePath(routePath);
    item.setSteps(steps);
    return item;
  }

  private SafetyAction safetyAction(String title, String priority, String owner, String routePath,
      String description) {
    SafetyAction item = new SafetyAction();
    item.setTitle(title);
    item.setPriority(priority);
    item.setOwner(owner);
    item.setRoutePath(routePath);
    item.setDescription(description);
    return item;
  }

  private SafetyRecentEvent safetyEvent(Long id, String source, String title, String level, String status,
      String occurredAt, String routePath) {
    SafetyRecentEvent item = new SafetyRecentEvent();
    item.setId(id);
    item.setSource(source);
    item.setTitle(title);
    item.setLevel(level);
    item.setStatus(status);
    item.setOccurredAt(occurredAt);
    item.setRoutePath(routePath);
    return item;
  }

  private WorkforceMetric workforceMetric(String key, String label, String value, String helper, String tone,
      String routePath) {
    WorkforceMetric item = new WorkforceMetric();
    item.setKey(key);
    item.setLabel(label);
    item.setValue(value);
    item.setHelper(helper);
    item.setTone(tone);
    item.setRoutePath(routePath);
    return item;
  }

  private WorkforceRisk workforceRisk(String key, String title, String owner, String level, String routePath,
      List<String> signals, List<String> controls) {
    WorkforceRisk item = new WorkforceRisk();
    item.setKey(key);
    item.setTitle(title);
    item.setOwner(owner);
    item.setLevel(level);
    item.setRoutePath(routePath);
    item.setSignals(signals);
    item.setControls(controls);
    return item;
  }

  private WorkforceRoleLoad workforceRoleLoad(String key, String title, String coverage, String status,
      String routePath, List<String> signals) {
    WorkforceRoleLoad item = new WorkforceRoleLoad();
    item.setKey(key);
    item.setTitle(title);
    item.setCoverage(coverage);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setSignals(signals);
    return item;
  }

  private WorkforceAction workforceAction(String title, String owner, String priority, String routePath,
      String description) {
    WorkforceAction item = new WorkforceAction();
    item.setTitle(title);
    item.setOwner(owner);
    item.setPriority(priority);
    item.setRoutePath(routePath);
    item.setDescription(description);
    return item;
  }

  private WorkforceFlow workforceFlow(String key, String title, String status, String routePath, List<String> steps) {
    WorkforceFlow item = new WorkforceFlow();
    item.setKey(key);
    item.setTitle(title);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setSteps(steps);
    return item;
  }

  private StaffMobileMetric staffMobileMetric(String key, String label, String value, String helper, String tone,
      String routePath) {
    StaffMobileMetric item = new StaffMobileMetric();
    item.setKey(key);
    item.setLabel(label);
    item.setValue(value);
    item.setHelper(helper);
    item.setTone(tone);
    item.setRoutePath(routePath);
    return item;
  }

  private FamilyRuntimeCard familyRuntimeCard(String key, String title, String value, String status, String helper,
      String routePath) {
    FamilyRuntimeCard item = new FamilyRuntimeCard();
    item.setKey(key);
    item.setTitle(title);
    item.setValue(value);
    item.setStatus(status);
    item.setHelper(helper);
    item.setRoutePath(routePath);
    return item;
  }

  private StaffMobileRuntimeCard staffMobileRuntimeCard(String key, String title, String value, String status,
      String helper, String routePath) {
    StaffMobileRuntimeCard item = new StaffMobileRuntimeCard();
    item.setKey(key);
    item.setTitle(title);
    item.setValue(value);
    item.setStatus(status);
    item.setHelper(helper);
    item.setRoutePath(routePath);
    return item;
  }

  private StaffMobileRoleCard staffMobileRoleCard(String key, String title, String owner, String status,
      String routePath, List<String> scenarios, List<String> mobileNeeds) {
    StaffMobileRoleCard item = new StaffMobileRoleCard();
    item.setKey(key);
    item.setTitle(title);
    item.setOwner(owner);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setScenarios(scenarios);
    item.setMobileNeeds(mobileNeeds);
    return item;
  }

  private StaffMobileWorkflow staffMobileWorkflow(String key, String title, String status, String routePath,
      List<String> steps) {
    StaffMobileWorkflow item = new StaffMobileWorkflow();
    item.setKey(key);
    item.setTitle(title);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setSteps(steps);
    return item;
  }

  private StaffMobileAction staffMobileAction(String title, String owner, String priority, String routePath,
      String description) {
    StaffMobileAction item = new StaffMobileAction();
    item.setTitle(title);
    item.setOwner(owner);
    item.setPriority(priority);
    item.setRoutePath(routePath);
    item.setDescription(description);
    return item;
  }

  private LogisticsMetric logisticsMetric(String key, String label, String value, String helper, String tone,
      String routePath) {
    LogisticsMetric item = new LogisticsMetric();
    item.setKey(key);
    item.setLabel(label);
    item.setValue(value);
    item.setHelper(helper);
    item.setTone(tone);
    item.setRoutePath(routePath);
    return item;
  }

  private LogisticsRisk logisticsRisk(String key, String title, String owner, String level, String routePath,
      List<String> signals, List<String> controls) {
    LogisticsRisk item = new LogisticsRisk();
    item.setKey(key);
    item.setTitle(title);
    item.setOwner(owner);
    item.setLevel(level);
    item.setRoutePath(routePath);
    item.setSignals(signals);
    item.setControls(controls);
    return item;
  }

  private LogisticsAction logisticsAction(String title, String owner, String priority, String routePath,
      String description) {
    LogisticsAction item = new LogisticsAction();
    item.setTitle(title);
    item.setOwner(owner);
    item.setPriority(priority);
    item.setRoutePath(routePath);
    item.setDescription(description);
    return item;
  }

  private LogisticsFlow logisticsFlow(String key, String title, String status, String routePath, List<String> steps) {
    LogisticsFlow item = new LogisticsFlow();
    item.setKey(key);
    item.setTitle(title);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setSteps(steps);
    return item;
  }

  private MarketingMetric marketingMetric(String key, String label, String value, String helper, String tone,
      String routePath) {
    MarketingMetric item = new MarketingMetric();
    item.setKey(key);
    item.setLabel(label);
    item.setValue(value);
    item.setHelper(helper);
    item.setTone(tone);
    item.setRoutePath(routePath);
    return item;
  }

  private MarketingFunnelStage marketingStage(String key, String title, String value, String status, String routePath,
      List<String> signals) {
    MarketingFunnelStage item = new MarketingFunnelStage();
    item.setKey(key);
    item.setTitle(title);
    item.setValue(value);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setSignals(signals);
    return item;
  }

  private MarketingRisk marketingRisk(String key, String title, String owner, String level, String routePath,
      List<String> signals, List<String> controls) {
    MarketingRisk item = new MarketingRisk();
    item.setKey(key);
    item.setTitle(title);
    item.setOwner(owner);
    item.setLevel(level);
    item.setRoutePath(routePath);
    item.setSignals(signals);
    item.setControls(controls);
    return item;
  }

  private MarketingAction marketingAction(String title, String owner, String priority, String routePath,
      String description) {
    MarketingAction item = new MarketingAction();
    item.setTitle(title);
    item.setOwner(owner);
    item.setPriority(priority);
    item.setRoutePath(routePath);
    item.setDescription(description);
    return item;
  }

  private MarketingFlow marketingFlow(String key, String title, String status, String routePath, List<String> steps) {
    MarketingFlow item = new MarketingFlow();
    item.setKey(key);
    item.setTitle(title);
    item.setStatus(status);
    item.setRoutePath(routePath);
    item.setSteps(steps);
    return item;
  }

  private String formatRate(double value) {
    return String.format("%.1f%%", value);
  }

  private String defaultText(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value.trim();
  }

  private boolean containsAny(String text, String... keywords) {
    if (text == null || text.isBlank()) {
      return false;
    }
    String upper = text.toUpperCase();
    for (String keyword : keywords) {
      if (upper.contains(keyword.toUpperCase())) {
        return true;
      }
    }
    return false;
  }
}
