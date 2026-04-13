package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.SupervisorRuleHelper;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.health.entity.HealthInspection;
import com.zhiyangyun.care.health.entity.HealthMedicationTask;
import com.zhiyangyun.care.health.mapper.HealthInspectionMapper;
import com.zhiyangyun.care.health.mapper.HealthMedicationTaskMapper;
import com.zhiyangyun.care.life.entity.IncidentReport;
import com.zhiyangyun.care.life.mapper.IncidentReportMapper;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.entity.OaDocument;
import com.zhiyangyun.care.oa.entity.OaNotice;
import com.zhiyangyun.care.oa.entity.OaSuggestion;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.entity.OaWorkReport;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import com.zhiyangyun.care.oa.mapper.OaDocumentMapper;
import com.zhiyangyun.care.oa.mapper.OaNoticeMapper;
import com.zhiyangyun.care.oa.mapper.OaSuggestionMapper;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.oa.mapper.OaWorkReportMapper;
import com.zhiyangyun.care.oa.model.OaPortalSummary;
import com.zhiyangyun.care.schedule.entity.AttendanceRecord;
import com.zhiyangyun.care.schedule.mapper.AttendanceRecordMapper;
import com.zhiyangyun.care.store.model.MaterialCenterOverviewResponse;
import com.zhiyangyun.care.store.service.MaterialCenterOverviewService;
import com.zhiyangyun.care.survey.entity.SurveySubmission;
import com.zhiyangyun.care.survey.entity.SurveyTemplate;
import com.zhiyangyun.care.survey.mapper.SurveySubmissionMapper;
import com.zhiyangyun.care.survey.mapper.SurveyTemplateMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oa/portal")
public class OaPortalController {
  private final OaNoticeMapper noticeMapper;
  private final OaTodoMapper todoMapper;
  private final OaApprovalMapper approvalMapper;
  private final OaTaskMapper taskMapper;
  private final OaWorkReportMapper workReportMapper;
  private final OaDocumentMapper documentMapper;
  private final OaSuggestionMapper suggestionMapper;
  private final AttendanceRecordMapper attendanceRecordMapper;
  private final IncidentReportMapper incidentReportMapper;
  private final MaterialCenterOverviewService materialCenterOverviewService;
  private final HealthInspectionMapper healthInspectionMapper;
  private final HealthMedicationTaskMapper healthMedicationTaskMapper;
  private final ElderMapper elderMapper;
  private final CrmContractMapper crmContractMapper;
  private final CrmLeadMapper crmLeadMapper;
  private final SurveyTemplateMapper surveyTemplateMapper;
  private final SurveySubmissionMapper surveySubmissionMapper;
  private final StaffMapper staffMapper;
  private final RoleMapper roleMapper;
  private final ObjectMapper objectMapper;

  public OaPortalController(
      OaNoticeMapper noticeMapper,
      OaTodoMapper todoMapper,
      OaApprovalMapper approvalMapper,
      OaTaskMapper taskMapper,
      OaWorkReportMapper workReportMapper,
      OaDocumentMapper documentMapper,
      OaSuggestionMapper suggestionMapper,
      AttendanceRecordMapper attendanceRecordMapper,
      IncidentReportMapper incidentReportMapper,
      MaterialCenterOverviewService materialCenterOverviewService,
      HealthInspectionMapper healthInspectionMapper,
      HealthMedicationTaskMapper healthMedicationTaskMapper,
      ElderMapper elderMapper,
      CrmContractMapper crmContractMapper,
      CrmLeadMapper crmLeadMapper,
      SurveyTemplateMapper surveyTemplateMapper,
      SurveySubmissionMapper surveySubmissionMapper,
      StaffMapper staffMapper,
      RoleMapper roleMapper,
      ObjectMapper objectMapper) {
    this.noticeMapper = noticeMapper;
    this.todoMapper = todoMapper;
    this.approvalMapper = approvalMapper;
    this.taskMapper = taskMapper;
    this.workReportMapper = workReportMapper;
    this.documentMapper = documentMapper;
    this.suggestionMapper = suggestionMapper;
    this.attendanceRecordMapper = attendanceRecordMapper;
    this.incidentReportMapper = incidentReportMapper;
    this.materialCenterOverviewService = materialCenterOverviewService;
    this.healthInspectionMapper = healthInspectionMapper;
    this.healthMedicationTaskMapper = healthMedicationTaskMapper;
    this.elderMapper = elderMapper;
    this.crmContractMapper = crmContractMapper;
    this.crmLeadMapper = crmLeadMapper;
    this.surveyTemplateMapper = surveyTemplateMapper;
    this.surveySubmissionMapper = surveySubmissionMapper;
    this.staffMapper = staffMapper;
    this.roleMapper = roleMapper;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/summary")
  public Result<OaPortalSummary> summary(@RequestParam(required = false) String scope) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    String username = trimToNull(AuthContext.getUsername());
    boolean personalScope = "PERSONAL".equalsIgnoreCase(scope);
    List<String> roleCodes = AuthContext.getRoleCodes().stream()
        .map(item -> item == null ? null : item.trim().toUpperCase(Locale.ROOT))
        .filter(item -> item != null && !item.isBlank())
        .toList();
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
    LocalDateTime nextMonthStart = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();

    List<OaNotice> notices = noticeMapper.selectList(Wrappers.lambdaQuery(OaNotice.class)
        .eq(OaNotice::getIsDeleted, 0)
        .eq(orgId != null, OaNotice::getOrgId, orgId)
        .orderByDesc(OaNotice::getPublishTime)
        .last("LIMIT 5"));
    List<OaTodo> todos = listScopedTodos(orgId, personalScope, staffId, username, 8);

    Long openTodoCount = countScopedTodos(orgId, personalScope, staffId, username, false, false, now);
    Long birthdayTodoCount = countScopedTodos(orgId, personalScope, staffId, username, true, false, now);
    Long overdueTodoCount = countScopedTodos(orgId, personalScope, staffId, username, false, true, now);
    Long pendingApprovalCount = personalScope
        ? countPersonalPendingApprovals(orgId, staffId, roleCodes, false, now)
        : count(approvalMapper.selectCount(Wrappers.lambdaQuery(OaApproval.class)
            .eq(OaApproval::getIsDeleted, 0)
            .eq(orgId != null, OaApproval::getOrgId, orgId)
            .eq(OaApproval::getStatus, "PENDING")));
    Long ongoingTaskCount = count(taskMapper.selectCount(taskScopeQuery(orgId, personalScope, staffId, username)
        .eq(OaTask::getStatus, "OPEN")));
    Long submittedReportCount = personalScope && staffId == null
        ? 0L
        : count(workReportMapper.selectCount(Wrappers.lambdaQuery(OaWorkReport.class)
            .eq(OaWorkReport::getIsDeleted, 0)
            .eq(orgId != null, OaWorkReport::getOrgId, orgId)
            .eq(OaWorkReport::getStatus, "SUBMITTED")
            .eq(personalScope && staffId != null, OaWorkReport::getReporterId, staffId)
            .ge(OaWorkReport::getReportDate, today.minusDays(30))));

    Long todayScheduleCount = count(taskMapper.selectCount(taskScopeQuery(orgId, personalScope, staffId, username)
        .eq(OaTask::getStatus, "OPEN")
        .ge(OaTask::getStartTime, today.atStartOfDay())
        .lt(OaTask::getStartTime, today.plusDays(1).atStartOfDay())));
    Long attendanceAbnormalCount = count(attendanceRecordMapper.selectCount(Wrappers.lambdaQuery(AttendanceRecord.class)
        .eq(AttendanceRecord::getIsDeleted, 0)
        .eq(orgId != null, AttendanceRecord::getOrgId, orgId)
        .notIn(AttendanceRecord::getStatus, "NORMAL", "ON_TIME")
        .ge(AttendanceRecord::getCheckInTime, today.atStartOfDay())
        .lt(AttendanceRecord::getCheckInTime, today.plusDays(1).atStartOfDay())));

    Long leavePendingCount = countByApprovalType(orgId, "LEAVE");
    Long reimbursePendingCount = countByApprovalType(orgId, "REIMBURSE");
    Long purchasePendingCount = countByApprovalType(orgId, "PURCHASE");
    Long marketingPlanPendingCount = countByApprovalType(orgId, "MARKETING_PLAN");
    Long contractPendingCount = count(crmLeadMapper.selectCount(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(orgId != null, CrmLead::getOrgId, orgId)
        .and(w -> w.eq(CrmLead::getContractStatus, "未提交")
            .or().eq(CrmLead::getContractStatus, "待审批")
            .or().eq(CrmLead::getContractStatus, "审批中"))));

    Long myExpenseCount = sumApprovalAmount(orgId, staffId, List.of("REIMBURSE"), monthStart, nextMonthStart);
    Long deptExpenseCount = sumApprovalAmount(orgId, null, List.of("REIMBURSE", "PURCHASE"), monthStart, nextMonthStart);
    Long documentCount = count(documentMapper.selectCount(Wrappers.lambdaQuery(OaDocument.class)
        .eq(OaDocument::getIsDeleted, 0)
        .eq(orgId != null, OaDocument::getOrgId, orgId)));
    Long invoiceFolderCount = count(documentMapper.selectCount(Wrappers.lambdaQuery(OaDocument.class)
        .eq(OaDocument::getIsDeleted, 0)
        .eq(orgId != null, OaDocument::getOrgId, orgId)
        .like(OaDocument::getFolder, "发票")));

    Long incidentOpenCount = count(incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .ne(IncidentReport::getStatus, "CLOSED")));
    MaterialCenterOverviewResponse materialOverview = materialCenterOverviewService.overview(orgId, 30);
    Long materialTransferDraftCount = materialOverview.getMaterialTransferDraftCount();
    Long materialPurchaseDraftCount = materialOverview.getMaterialPurchaseDraftCount();
    Long approvalTimeoutCount = personalScope
        ? countPersonalPendingApprovals(orgId, staffId, roleCodes, true, now)
        : count(approvalMapper.selectCount(Wrappers.lambdaQuery(OaApproval.class)
            .eq(OaApproval::getIsDeleted, 0)
            .eq(orgId != null, OaApproval::getOrgId, orgId)
            .eq(OaApproval::getStatus, "PENDING")
            .lt(OaApproval::getCreateTime, now.minusHours(48))));
    Long pendingMedicalOrderCount = count(healthMedicationTaskMapper.selectCount(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
        .eq(HealthMedicationTask::getTaskDate, today)
        .eq(HealthMedicationTask::getStatus, "PENDING")));

    Long healthAbnormalCount = count(healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .in(HealthInspection::getStatus, "ABNORMAL", "FOLLOWING")));
    Long inHospitalElderCount = count(elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getStatus, 1)));

    Long inventoryLowStockCount = materialOverview.getLowStockCount();
    Long surveyDraftCount = count(surveyTemplateMapper.selectCount(Wrappers.lambdaQuery(SurveyTemplate.class)
        .eq(SurveyTemplate::getIsDeleted, 0)
        .eq(orgId != null, SurveyTemplate::getOrgId, orgId)
        .eq(SurveyTemplate::getStatus, 0)));
    Long surveyPublishedCount = count(surveyTemplateMapper.selectCount(Wrappers.lambdaQuery(SurveyTemplate.class)
        .eq(SurveyTemplate::getIsDeleted, 0)
        .eq(orgId != null, SurveyTemplate::getOrgId, orgId)
        .eq(SurveyTemplate::getStatus, 1)));
    Long surveyFamilyPublishedCount = count(surveyTemplateMapper.selectCount(Wrappers.lambdaQuery(SurveyTemplate.class)
        .eq(SurveyTemplate::getIsDeleted, 0)
        .eq(orgId != null, SurveyTemplate::getOrgId, orgId)
        .eq(SurveyTemplate::getStatus, 1)
        .eq(SurveyTemplate::getTargetType, "FAMILY")));
    Long surveyTodaySubmissionCount = count(surveySubmissionMapper.selectCount(Wrappers.lambdaQuery(SurveySubmission.class)
        .eq(SurveySubmission::getIsDeleted, 0)
        .eq(orgId != null, SurveySubmission::getOrgId, orgId)
        .ge(SurveySubmission::getCreateTime, today.atStartOfDay())
        .lt(SurveySubmission::getCreateTime, today.plusDays(1).atStartOfDay())));
    Long supervisorAnomalyCount = countSupervisorAnomalies(orgId);
    Long elderContractExpiringCount = count(crmContractMapper.selectCount(Wrappers.lambdaQuery(CrmContract.class)
        .eq(CrmContract::getIsDeleted, 0)
        .eq(orgId != null, CrmContract::getOrgId, orgId)
        .isNotNull(CrmContract::getElderId)
        .in(CrmContract::getStatus, "SIGNED", "EFFECTIVE")
        .isNotNull(CrmContract::getEffectiveTo)
        .le(CrmContract::getEffectiveTo, today.plusDays(30))));

    List<OaPortalSummary.MarketingChannelItem> marketingChannels = queryMarketingChannels(orgId);
    List<OaPortalSummary.CollaborationGanttItem> collaborationGantt = queryCollaborationGantt(orgId);
    List<OaPortalSummary.WorkflowTodoItem> workflowTodos = buildWorkflowTodos(
        leavePendingCount, reimbursePendingCount, purchasePendingCount, marketingPlanPendingCount, contractPendingCount);
    List<OaPortalSummary.SuggestionItem> latestSuggestions = queryLatestSuggestions(orgId);
    Long suggestionCount = count(suggestionMapper.selectCount(Wrappers.lambdaQuery(OaSuggestion.class)
        .eq(OaSuggestion::getIsDeleted, 0)
        .eq(orgId != null, OaSuggestion::getOrgId, orgId)));

    OaPortalSummary summary = new OaPortalSummary();
    summary.setNotices(notices);
    summary.setTodos(todos);
    summary.setOpenTodoCount(openTodoCount);
    summary.setBirthdayTodoCount(birthdayTodoCount);
    summary.setOverdueTodoCount(overdueTodoCount);
    summary.setPendingApprovalCount(pendingApprovalCount);
    summary.setOngoingTaskCount(ongoingTaskCount);
    summary.setSubmittedReportCount(submittedReportCount);
    summary.setTodayScheduleCount(todayScheduleCount);
    summary.setAttendanceAbnormalCount(attendanceAbnormalCount);
    summary.setLeavePendingCount(leavePendingCount);
    summary.setReimbursePendingCount(reimbursePendingCount);
    summary.setPurchasePendingCount(purchasePendingCount);
    summary.setContractPendingCount(contractPendingCount);
    summary.setMyExpenseCount(myExpenseCount);
    summary.setDeptExpenseCount(deptExpenseCount);
    summary.setInvoiceFolderCount(invoiceFolderCount);
    summary.setDocumentCount(documentCount);
    summary.setInventoryLowStockCount(inventoryLowStockCount);
    summary.setApprovalTimeoutCount(approvalTimeoutCount);
    summary.setPendingMedicalOrderCount(pendingMedicalOrderCount);
    summary.setElderAbnormalCount(0L);
    summary.setHealthAbnormalCount(healthAbnormalCount);
    summary.setElderContractExpiringCount(elderContractExpiringCount);
    summary.setIncidentOpenCount(incidentOpenCount);
    summary.setMaterialTransferDraftCount(materialTransferDraftCount);
    summary.setMaterialPurchaseDraftCount(materialPurchaseDraftCount);
    summary.setInHospitalElderCount(inHospitalElderCount);
    summary.setSuggestionCount(suggestionCount);
    summary.setSurveyDraftCount(surveyDraftCount);
    summary.setSurveyPublishedCount(surveyPublishedCount);
    summary.setSurveyTodaySubmissionCount(surveyTodaySubmissionCount);
    summary.setSurveyFamilyPublishedCount(surveyFamilyPublishedCount);
    summary.setSupervisorAnomalyCount(supervisorAnomalyCount);
    summary.setWorkflowTodos(workflowTodos);
    summary.setMarketingChannels(marketingChannels);
    summary.setCollaborationGantt(collaborationGantt);
    summary.setLatestSuggestions(latestSuggestions);
    return Result.ok(summary);
  }

  private Long countByApprovalType(Long orgId, String type) {
    return count(approvalMapper.selectCount(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getStatus, "PENDING")
        .eq(OaApproval::getApprovalType, type)));
  }

  private List<OaPortalSummary.WorkflowTodoItem> buildWorkflowTodos(
      Long leavePendingCount,
      Long reimbursePendingCount,
      Long purchasePendingCount,
      Long marketingPlanPendingCount,
      Long contractPendingCount) {
    List<OaPortalSummary.WorkflowTodoItem> items = new ArrayList<>();
    items.add(workflow("LEAVE", "请假审批", leavePendingCount, "/oa/approval"));
    items.add(workflow("OVERTIME", "加班申请", countByApprovalType(AuthContext.getOrgId(), "OVERTIME"), "/oa/approval"));
    items.add(workflow("REIMBURSE", "报销审批", reimbursePendingCount, "/oa/approval"));
    items.add(workflow("PURCHASE", "采购审批", purchasePendingCount, "/oa/approval"));
    items.add(workflow("MARKETING_PLAN", "营销方案审批", marketingPlanPendingCount, "/oa/approval?type=MARKETING_PLAN&status=PENDING"));
    items.add(workflow("CONTRACT", "合同审批", contractPendingCount, "/marketing/contract-signing"));
    return items;
  }

  private OaPortalSummary.WorkflowTodoItem workflow(String code, String name, Long count, String route) {
    OaPortalSummary.WorkflowTodoItem item = new OaPortalSummary.WorkflowTodoItem();
    item.setCode(code);
    item.setName(name);
    item.setCount(count(count));
    item.setRoute(route);
    return item;
  }

  private List<OaPortalSummary.MarketingChannelItem> queryMarketingChannels(Long orgId) {
    var wrapper = Wrappers.query(CrmLead.class)
        .select("COALESCE(source, '未标记') AS source",
            "COUNT(1) AS leadCount",
            "SUM(CASE WHEN contract_signed_flag = 1 THEN 1 ELSE 0 END) AS contractCount")
        .eq("is_deleted", 0)
        .eq(orgId != null, "org_id", orgId)
        .groupBy("COALESCE(source, '未标记')")
        .orderByDesc("leadCount")
        .last("LIMIT 5");
    return crmLeadMapper.selectMaps(wrapper).stream().map(row -> {
      OaPortalSummary.MarketingChannelItem item = new OaPortalSummary.MarketingChannelItem();
      item.setSource(str(row.get("source"), "未标记"));
      item.setLeadCount(number(row.get("leadCount")));
      item.setContractCount(number(row.get("contractCount")));
      return item;
    }).toList();
  }

  private List<OaPortalSummary.CollaborationGanttItem> queryCollaborationGantt(Long orgId) {
    List<OaTask> tasks = taskMapper.selectList(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .isNotNull(OaTask::getStartTime)
        .isNotNull(OaTask::getEndTime)
        .orderByDesc(OaTask::getCreateTime)
        .last("LIMIT 6"));
    return tasks.stream().map(task -> {
      OaPortalSummary.CollaborationGanttItem item = new OaPortalSummary.CollaborationGanttItem();
      item.setTaskId(task.getId());
      item.setTitle(task.getTitle());
      item.setStatus(task.getStatus());
      item.setStartTime(task.getStartTime());
      item.setEndTime(task.getEndTime());
      item.setProgress(progressByStatus(task.getStatus()));
      return item;
    }).toList();
  }

  private List<OaPortalSummary.SuggestionItem> queryLatestSuggestions(Long orgId) {
    List<OaSuggestion> suggestions = suggestionMapper.selectList(Wrappers.lambdaQuery(OaSuggestion.class)
        .eq(OaSuggestion::getIsDeleted, 0)
        .eq(orgId != null, OaSuggestion::getOrgId, orgId)
        .orderByDesc(OaSuggestion::getCreateTime)
        .last("LIMIT 6"));
    return suggestions.stream().map(suggestion -> {
      OaPortalSummary.SuggestionItem item = new OaPortalSummary.SuggestionItem();
      item.setId(suggestion.getId());
      item.setContent(suggestion.getContent());
      item.setProposerName(suggestion.getProposerName());
      item.setStatus(suggestion.getStatus());
      item.setCreateTime(suggestion.getCreateTime());
      return item;
    }).toList();
  }

  private int progressByStatus(String status) {
    if (status == null) {
      return 0;
    }
    return switch (status.trim().toUpperCase()) {
      case "DONE", "COMPLETED" -> 100;
      case "DOING", "PROCESSING" -> 60;
      case "TODO", "OPEN" -> 20;
      default -> 40;
    };
  }

  private Long count(Long value) {
    return value == null ? 0L : value;
  }

  private String str(Object value, String fallback) {
    if (value == null) {
      return fallback;
    }
    String val = String.valueOf(value).trim();
    return val.isEmpty() ? fallback : val;
  }

  private Long number(Object value) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    if (value == null) {
      return 0L;
    }
    try {
      return new BigDecimal(String.valueOf(value)).longValue();
    } catch (NumberFormatException e) {
      return 0L;
    }
  }

  private Long countSupervisorAnomalies(Long orgId) {
    List<StaffAccount> staffs = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId));
    if (staffs.isEmpty()) {
      return 0L;
    }
    Map<Long, StaffAccount> map = new LinkedHashMap<>();
    Map<Long, List<String>> roleMap = new LinkedHashMap<>();
    for (StaffAccount staff : staffs) {
      if (staff == null || staff.getId() == null) {
        continue;
      }
      map.put(staff.getId(), staff);
      roleMap.put(staff.getId(), SupervisorRuleHelper.normalizeRoleCodes(
          roleMapper.selectRoleCodesByStaff(staff.getId(), staff.getOrgId())));
    }
    long total = 0L;
    for (StaffAccount staff : staffs) {
      if (staff == null || staff.getId() == null) {
        continue;
      }
      if (hasSupervisorAnomaly(staff, map, roleMap)) {
        total++;
      }
    }
    return total;
  }

  private boolean hasSupervisorAnomaly(
      StaffAccount target,
      Map<Long, StaffAccount> staffMap,
      Map<Long, List<String>> roleMap) {
    Long targetId = target.getId();
    Long directLeaderId = target.getDirectLeaderId();
    Long indirectLeaderId = target.getIndirectLeaderId();
    if (targetId != null && targetId.equals(directLeaderId)) {
      return true;
    }
    if (targetId != null && targetId.equals(indirectLeaderId)) {
      return true;
    }
    if (directLeaderId != null && directLeaderId.equals(indirectLeaderId)) {
      return true;
    }
    SupervisorRuleHelper.Level targetLevel = SupervisorRuleHelper.resolveLevel(roleMap.get(targetId));
    if (directLeaderId != null) {
      StaffAccount directLeader = staffMap.get(directLeaderId);
      if (directLeader == null) {
        return true;
      }
      if (!SupervisorRuleHelper.canBeDirectLeader(
          targetLevel,
          target.getDepartmentId(),
          directLeader.getDepartmentId(),
          roleMap.get(directLeaderId))) {
        return true;
      }
    }
    if (indirectLeaderId != null) {
      StaffAccount indirectLeader = staffMap.get(indirectLeaderId);
      if (indirectLeader == null) {
        return true;
      }
      if (!SupervisorRuleHelper.canBeIndirectLeader(targetLevel, roleMap.get(indirectLeaderId))) {
        return true;
      }
    }
    return false;
  }

  private List<OaTodo> listScopedTodos(
      Long orgId,
      boolean personalScope,
      Long staffId,
      String username,
      int limit) {
    return todoMapper.selectList(todoScopeQuery(orgId, personalScope, staffId, username)
        .eq(OaTodo::getStatus, "OPEN")
        .orderByAsc(OaTodo::getDueTime)
        .last("LIMIT " + Math.max(limit, 1)));
  }

  private Long countScopedTodos(
      Long orgId,
      boolean personalScope,
      Long staffId,
      String username,
      boolean birthdayOnly,
      boolean overdueOnly,
      LocalDateTime now) {
    var wrapper = todoScopeQuery(orgId, personalScope, staffId, username)
        .eq(OaTodo::getStatus, "OPEN");
    if (birthdayOnly) {
      wrapper.like(OaTodo::getContent, "[BIRTHDAY_REMINDER:");
    }
    if (overdueOnly) {
      wrapper.isNotNull(OaTodo::getDueTime).lt(OaTodo::getDueTime, now);
    }
    return count(todoMapper.selectCount(wrapper));
  }

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OaTodo> todoScopeQuery(
      Long orgId,
      boolean personalScope,
      Long staffId,
      String username) {
    var wrapper = Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId);
    if (!personalScope) {
      return wrapper;
    }
    if (staffId == null) {
      wrapper.eq(OaTodo::getId, -1L);
      return wrapper;
    }
    wrapper.and(w -> w.eq(OaTodo::getAssigneeId, staffId)
        .or().eq(OaTodo::getCreatedBy, staffId)
        .or().eq(username != null, OaTodo::getAssigneeName, username));
    return wrapper;
  }

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OaTask> taskScopeQuery(
      Long orgId,
      boolean personalScope,
      Long staffId,
      String username) {
    var wrapper = Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId);
    if (!personalScope) {
      return wrapper;
    }
    if (staffId == null) {
      if (username == null) {
        wrapper.eq(OaTask::getId, -1L);
      } else {
        wrapper.eq(OaTask::getAssigneeName, username);
      }
      return wrapper;
    }
    wrapper.and(w -> w.eq(OaTask::getCreatedBy, staffId)
        .or().eq(OaTask::getAssigneeId, staffId)
        .or().eq(username != null, OaTask::getAssigneeName, username)
        .or().apply("FIND_IN_SET({0}, collaborator_ids)", staffId));
    return wrapper;
  }

  private Long countPersonalPendingApprovals(
      Long orgId,
      Long staffId,
      List<String> roleCodes,
      boolean overdueOnly,
      LocalDateTime now) {
    if (staffId == null) {
      return 0L;
    }
    var wrapper = Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getStatus, "PENDING");
    if (!AuthContext.isMinisterOrHigher()) {
      wrapper.eq(OaApproval::getApplicantId, staffId);
    }
    List<OaApproval> approvals = approvalMapper.selectList(wrapper);
    return approvals.stream()
        .filter(item -> !AuthContext.isMinisterOrHigher() || isPendingApprovalForCurrentUser(item, staffId, roleCodes))
        .filter(item -> !overdueOnly || isApprovalTimeout(item, now))
        .count();
  }

  private boolean isApprovalTimeout(OaApproval approval, LocalDateTime now) {
    if (approval == null || !"PENDING".equalsIgnoreCase(approval.getStatus())) {
      return false;
    }
    LocalDateTime activeSince = resolveApprovalActiveSince(approval);
    if (activeSince == null) {
      return false;
    }
    return activeSince.isBefore(now.minusHours(48));
  }

  private LocalDateTime resolveApprovalActiveSince(OaApproval approval) {
    Map<String, Object> formMap = readFormMap(approval == null ? null : approval.getFormData());
    LocalDateTime currentNodeEnteredAt = parseDateTime(formMap.get("currentNodeEnteredAt"));
    if (currentNodeEnteredAt != null) {
      return currentNodeEnteredAt;
    }
    Object rawNotifyHistory = formMap.get("notifyHistory");
    if (rawNotifyHistory instanceof List<?> list) {
      for (int i = list.size() - 1; i >= 0; i--) {
        Object item = list.get(i);
        if (item instanceof Map<?, ?> row) {
          LocalDateTime at = parseDateTime(row.get("at"));
          if (at != null) {
            return at;
          }
        }
      }
    }
    LocalDateTime submittedAt = parseDateTime(formMap.get("submittedAt"));
    if (submittedAt != null) {
      return submittedAt;
    }
    return approval == null ? null : approval.getCreateTime();
  }

  private boolean isPendingApprovalForCurrentUser(OaApproval approval, Long staffId, List<String> roleCodes) {
    if (approval == null || staffId == null) {
      return false;
    }
    Map<String, Object> formMap = readFormMap(approval.getFormData());
    Long approverId = parseLong(formMap.get("currentApproverId"));
    if (approverId != null) {
      return approverId.equals(staffId);
    }
    String approverRole = parseString(formMap.get("currentApproverRole"));
    if (approverRole == null) {
      return AuthContext.isMinisterOrHigher();
    }
    return matchesApproverRole(approverRole, roleCodes);
  }

  private boolean matchesApproverRole(String approverRole, List<String> roleCodes) {
    if (approverRole == null || approverRole.isBlank()) {
      return false;
    }
    String normalized = approverRole.trim().toUpperCase(Locale.ROOT);
    List<String> roles = roleCodes == null ? List.of() : roleCodes;
    if ("DEPT_MANAGER".equals(normalized) || "MANAGER".equals(normalized)) {
      return AuthContext.isMinisterOrHigher();
    }
    if ("DEAN".equals(normalized)) {
      return roles.contains("DEAN") || roles.contains("DIRECTOR") || AuthContext.isAdmin();
    }
    if ("HR".equals(normalized)) {
      return roles.stream().anyMatch(role -> role.contains("HR") || role.contains("PERSONNEL")) || AuthContext.isMinisterOrHigher();
    }
    if ("FINANCE".equals(normalized)) {
      return roles.stream().anyMatch(role -> role.contains("FINANCE") || role.contains("CASHIER") || role.contains("ACCOUNTANT"))
          || AuthContext.isMinisterOrHigher();
    }
    if ("WAREHOUSE".equals(normalized)) {
      return roles.stream().anyMatch(role -> role.contains("WAREHOUSE") || role.contains("STORE") || role.contains("LOGISTICS"))
          || AuthContext.isMinisterOrHigher();
    }
    if ("ADMIN_OFFICE".equals(normalized)) {
      return roles.stream().anyMatch(role -> role.contains("OFFICE") || role.equals("ADMIN") || role.contains("HR"))
          || AuthContext.isMinisterOrHigher();
    }
    if ("PURCHASING".equals(normalized)) {
      return roles.stream().anyMatch(role -> role.contains("PURCHASE") || role.contains("PROCUREMENT") || role.contains("LOGISTICS"))
          || AuthContext.isMinisterOrHigher();
    }
    return AuthContext.isMinisterOrHigher();
  }

  private Long sumApprovalAmount(
      Long orgId,
      Long applicantId,
      List<String> approvalTypes,
      LocalDateTime startTime,
      LocalDateTime endTime) {
    List<OaApproval> approvals = approvalMapper.selectList(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(applicantId != null, OaApproval::getApplicantId, applicantId)
        .eq(OaApproval::getStatus, "APPROVED")
        .in(approvalTypes != null && !approvalTypes.isEmpty(), OaApproval::getApprovalType, approvalTypes)
        .ge(startTime != null, OaApproval::getCreateTime, startTime)
        .lt(endTime != null, OaApproval::getCreateTime, endTime));
    BigDecimal total = BigDecimal.ZERO;
    for (OaApproval approval : approvals) {
      if (approval.getAmount() != null) {
        total = total.add(approval.getAmount());
      }
    }
    return total.longValue();
  }

  private Map<String, Object> readFormMap(String formData) {
    if (formData == null || formData.isBlank()) {
      return new LinkedHashMap<>();
    }
    try {
      return objectMapper.readValue(formData, new TypeReference<Map<String, Object>>() {});
    } catch (Exception ignore) {
      return new LinkedHashMap<>();
    }
  }

  private Long parseLong(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    try {
      return Long.parseLong(String.valueOf(value));
    } catch (Exception ignore) {
      return null;
    }
  }

  private String parseString(Object value) {
    if (value == null) {
      return null;
    }
    String text = String.valueOf(value).trim();
    return text.isEmpty() ? null : text;
  }

  private LocalDateTime parseDateTime(Object value) {
    String text = parseString(value);
    if (text == null) {
      return null;
    }
    try {
      return LocalDateTime.parse(text.replace(' ', 'T'));
    } catch (Exception ignore) {
      return null;
    }
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String text = value.trim();
    return text.isEmpty() ? null : text;
  }
}
