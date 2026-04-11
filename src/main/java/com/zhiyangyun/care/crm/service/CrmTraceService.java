package com.zhiyangyun.care.crm.service;

import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.model.trace.CrmContractWorkflowLogResponse;
import com.zhiyangyun.care.crm.model.trace.CrmLeadAssignLogResponse;
import com.zhiyangyun.care.crm.model.trace.CrmSalesReportSnapshotResponse;
import com.zhiyangyun.care.crm.model.trace.CrmStageTransitionLogResponse;
import java.time.LocalDate;
import java.util.List;

public interface CrmTraceService {
  void recordLeadAssignment(String actionType, String detail, CrmLead before, CrmLead after, Object context);

  void recordLeadStageTransition(String actionType, String detail, CrmLead before, CrmLead after, Object context);

  void recordContractWorkflow(String actionType, String detail, CrmContract before, CrmContract after, Object context);

  void saveSnapshot(Long tenantId, Long orgId, String snapshotType, LocalDate windowFrom, LocalDate windowTo, String snapshotKey, Object metrics);

  List<CrmLeadAssignLogResponse> listLeadAssignments(Long tenantId, Long leadId, int limit);

  List<CrmStageTransitionLogResponse> listLeadStageTransitions(Long tenantId, Long leadId, int limit);

  List<CrmContractWorkflowLogResponse> listContractWorkflowLogs(Long tenantId, Long contractId, int limit);

  List<CrmSalesReportSnapshotResponse> listSnapshots(Long tenantId, String snapshotType, int limit);
}
