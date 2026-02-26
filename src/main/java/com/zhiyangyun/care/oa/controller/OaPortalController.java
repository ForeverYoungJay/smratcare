package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.entity.ElderAccount;
import com.zhiyangyun.care.finance.entity.ElderAccountLog;
import com.zhiyangyun.care.finance.mapper.ElderAccountLogMapper;
import com.zhiyangyun.care.finance.mapper.ElderAccountMapper;
import com.zhiyangyun.care.health.entity.HealthInspection;
import com.zhiyangyun.care.health.mapper.HealthInspectionMapper;
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
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.entity.MaterialPurchaseOrder;
import com.zhiyangyun.care.store.entity.MaterialTransferOrder;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.MaterialPurchaseOrderMapper;
import com.zhiyangyun.care.store.mapper.MaterialTransferOrderMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
  private final ElderAccountMapper elderAccountMapper;
  private final ElderAccountLogMapper elderAccountLogMapper;
  private final IncidentReportMapper incidentReportMapper;
  private final MaterialTransferOrderMapper transferOrderMapper;
  private final MaterialPurchaseOrderMapper purchaseOrderMapper;
  private final ProductMapper productMapper;
  private final InventoryBatchMapper inventoryBatchMapper;
  private final HealthInspectionMapper healthInspectionMapper;
  private final ElderMapper elderMapper;
  private final CrmLeadMapper crmLeadMapper;

  public OaPortalController(
      OaNoticeMapper noticeMapper,
      OaTodoMapper todoMapper,
      OaApprovalMapper approvalMapper,
      OaTaskMapper taskMapper,
      OaWorkReportMapper workReportMapper,
      OaDocumentMapper documentMapper,
      OaSuggestionMapper suggestionMapper,
      AttendanceRecordMapper attendanceRecordMapper,
      ElderAccountMapper elderAccountMapper,
      ElderAccountLogMapper elderAccountLogMapper,
      IncidentReportMapper incidentReportMapper,
      MaterialTransferOrderMapper transferOrderMapper,
      MaterialPurchaseOrderMapper purchaseOrderMapper,
      ProductMapper productMapper,
      InventoryBatchMapper inventoryBatchMapper,
      HealthInspectionMapper healthInspectionMapper,
      ElderMapper elderMapper,
      CrmLeadMapper crmLeadMapper) {
    this.noticeMapper = noticeMapper;
    this.todoMapper = todoMapper;
    this.approvalMapper = approvalMapper;
    this.taskMapper = taskMapper;
    this.workReportMapper = workReportMapper;
    this.documentMapper = documentMapper;
    this.suggestionMapper = suggestionMapper;
    this.attendanceRecordMapper = attendanceRecordMapper;
    this.elderAccountMapper = elderAccountMapper;
    this.elderAccountLogMapper = elderAccountLogMapper;
    this.incidentReportMapper = incidentReportMapper;
    this.transferOrderMapper = transferOrderMapper;
    this.purchaseOrderMapper = purchaseOrderMapper;
    this.productMapper = productMapper;
    this.inventoryBatchMapper = inventoryBatchMapper;
    this.healthInspectionMapper = healthInspectionMapper;
    this.elderMapper = elderMapper;
    this.crmLeadMapper = crmLeadMapper;
  }

  @GetMapping("/summary")
  public Result<OaPortalSummary> summary() {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();

    List<OaNotice> notices = noticeMapper.selectList(Wrappers.lambdaQuery(OaNotice.class)
        .eq(OaNotice::getIsDeleted, 0)
        .eq(orgId != null, OaNotice::getOrgId, orgId)
        .orderByDesc(OaNotice::getPublishTime)
        .last("LIMIT 5"));
    List<OaTodo> todos = todoMapper.selectList(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(OaTodo::getStatus, "OPEN")
        .orderByAsc(OaTodo::getDueTime)
        .last("LIMIT 8"));

    Long openTodoCount = count(todoMapper.selectCount(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(OaTodo::getStatus, "OPEN")));
    Long overdueTodoCount = count(todoMapper.selectCount(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(OaTodo::getStatus, "OPEN")
        .lt(OaTodo::getDueTime, now)));
    Long pendingApprovalCount = count(approvalMapper.selectCount(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getStatus, "PENDING")));
    Long ongoingTaskCount = count(taskMapper.selectCount(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .in(OaTask::getStatus, "TODO", "DOING")));
    Long submittedReportCount = count(workReportMapper.selectCount(Wrappers.lambdaQuery(OaWorkReport.class)
        .eq(OaWorkReport::getIsDeleted, 0)
        .eq(orgId != null, OaWorkReport::getOrgId, orgId)
        .eq(OaWorkReport::getStatus, "SUBMITTED")
        .ge(OaWorkReport::getReportDate, today.minusDays(30))));

    Long todayScheduleCount = count(taskMapper.selectCount(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
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
    Long contractPendingCount = count(crmLeadMapper.selectCount(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(orgId != null, CrmLead::getOrgId, orgId)
        .and(w -> w.eq(CrmLead::getContractStatus, "未提交")
            .or().eq(CrmLead::getContractStatus, "待审批")
            .or().eq(CrmLead::getContractStatus, "审批中"))));

    Long myExpenseCount = count(elderAccountMapper.selectCount(Wrappers.lambdaQuery(ElderAccount.class)
        .eq(ElderAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderAccount::getOrgId, orgId)));
    Long deptExpenseCount = count(elderAccountLogMapper.selectCount(Wrappers.lambdaQuery(ElderAccountLog.class)
        .eq(ElderAccountLog::getIsDeleted, 0)
        .eq(orgId != null, ElderAccountLog::getOrgId, orgId)
        .ge(ElderAccountLog::getCreateTime, now.minusDays(30))));
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
    Long materialTransferDraftCount = count(transferOrderMapper.selectCount(Wrappers.lambdaQuery(MaterialTransferOrder.class)
        .eq(MaterialTransferOrder::getIsDeleted, 0)
        .eq(orgId != null, MaterialTransferOrder::getOrgId, orgId)
        .eq(MaterialTransferOrder::getStatus, "DRAFT")));
    Long materialPurchaseDraftCount = count(purchaseOrderMapper.selectCount(Wrappers.lambdaQuery(MaterialPurchaseOrder.class)
        .eq(MaterialPurchaseOrder::getIsDeleted, 0)
        .eq(orgId != null, MaterialPurchaseOrder::getOrgId, orgId)
        .eq(MaterialPurchaseOrder::getStatus, "DRAFT")));
    Long approvalTimeoutCount = count(approvalMapper.selectCount(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getStatus, "PENDING")
        .lt(OaApproval::getCreateTime, now.minusHours(48))));

    Long healthAbnormalCount = count(healthInspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .in(HealthInspection::getStatus, "ABNORMAL", "FOLLOWING")));
    Long inHospitalElderCount = count(elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getStatus, 1)));

    Long inventoryLowStockCount = inventoryAlertCount(orgId);

    List<OaPortalSummary.MarketingChannelItem> marketingChannels = queryMarketingChannels(orgId);
    List<OaPortalSummary.CollaborationGanttItem> collaborationGantt = queryCollaborationGantt(orgId);
    List<OaPortalSummary.WorkflowTodoItem> workflowTodos = buildWorkflowTodos(
        leavePendingCount, reimbursePendingCount, purchasePendingCount, contractPendingCount);
    List<OaPortalSummary.SuggestionItem> latestSuggestions = queryLatestSuggestions(orgId);
    Long suggestionCount = count(suggestionMapper.selectCount(Wrappers.lambdaQuery(OaSuggestion.class)
        .eq(OaSuggestion::getIsDeleted, 0)
        .eq(orgId != null, OaSuggestion::getOrgId, orgId)));

    OaPortalSummary summary = new OaPortalSummary();
    summary.setNotices(notices);
    summary.setTodos(todos);
    summary.setOpenTodoCount(openTodoCount);
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
    summary.setElderAbnormalCount(healthAbnormalCount);
    summary.setHealthAbnormalCount(healthAbnormalCount);
    summary.setIncidentOpenCount(incidentOpenCount);
    summary.setMaterialTransferDraftCount(materialTransferDraftCount);
    summary.setMaterialPurchaseDraftCount(materialPurchaseDraftCount);
    summary.setInHospitalElderCount(inHospitalElderCount);
    summary.setSuggestionCount(suggestionCount);
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

  private Long inventoryAlertCount(Long orgId) {
    List<Product> products = productMapper.selectList(Wrappers.lambdaQuery(Product.class)
        .eq(Product::getIsDeleted, 0)
        .eq(orgId != null, Product::getOrgId, orgId));
    if (products.isEmpty()) {
      return 0L;
    }
    List<InventoryBatch> batches = inventoryBatchMapper.selectList(Wrappers.lambdaQuery(InventoryBatch.class)
        .eq(InventoryBatch::getIsDeleted, 0)
        .eq(orgId != null, InventoryBatch::getOrgId, orgId));
    Map<Long, Integer> qtyByProduct = new HashMap<>();
    for (InventoryBatch batch : batches) {
      if (batch.getProductId() == null) {
        continue;
      }
      qtyByProduct.merge(batch.getProductId(), batch.getQuantity() == null ? 0 : batch.getQuantity(), Integer::sum);
    }
    long count = products.stream().filter(p -> {
      int current = qtyByProduct.getOrDefault(p.getId(), 0);
      int safety = p.getSafetyStock() == null ? 0 : p.getSafetyStock();
      return current < safety;
    }).count();
    return count < 0 ? 0L : count;
  }

  private List<OaPortalSummary.WorkflowTodoItem> buildWorkflowTodos(
      Long leavePendingCount,
      Long reimbursePendingCount,
      Long purchasePendingCount,
      Long contractPendingCount) {
    List<OaPortalSummary.WorkflowTodoItem> items = new ArrayList<>();
    items.add(workflow("LEAVE", "请假审批", leavePendingCount, "/oa/approval"));
    items.add(workflow("OVERTIME", "加班申请", 0L, "/oa/approval"));
    items.add(workflow("REIMBURSE", "报销审批", reimbursePendingCount, "/oa/approval"));
    items.add(workflow("PURCHASE", "采购审批", purchasePendingCount, "/oa/approval"));
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
}
