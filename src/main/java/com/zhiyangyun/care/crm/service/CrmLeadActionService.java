package com.zhiyangyun.care.crm.service;

import com.zhiyangyun.care.crm.model.action.CrmCallbackPlanExecuteRequest;
import com.zhiyangyun.care.crm.model.action.CrmCallbackPlanRequest;
import com.zhiyangyun.care.crm.model.action.CrmCallbackPlanResponse;
import com.zhiyangyun.care.crm.model.action.CrmContractAttachmentRequest;
import com.zhiyangyun.care.crm.model.action.CrmContractAttachmentResponse;
import com.zhiyangyun.care.crm.model.action.CrmLeadBatchDeleteRequest;
import com.zhiyangyun.care.crm.model.action.CrmLeadBatchStatusRequest;
import com.zhiyangyun.care.crm.model.action.CrmSmsTaskCreateRequest;
import com.zhiyangyun.care.crm.model.action.CrmSmsTaskResponse;
import com.zhiyangyun.care.crm.model.CrmLeadResponse;
import java.util.List;

public interface CrmLeadActionService {
  int batchUpdateStatus(Long tenantId, CrmLeadBatchStatusRequest request);
  int batchDelete(Long tenantId, CrmLeadBatchDeleteRequest request);
  CrmLeadResponse handoffToAssessment(Long tenantId, String contractNo);

  CrmCallbackPlanResponse createCallbackPlan(Long tenantId, Long orgId, Long staffId, Long leadId, CrmCallbackPlanRequest request);
  List<CrmCallbackPlanResponse> listCallbackPlan(Long tenantId, Long leadId);
  CrmCallbackPlanResponse executeCallbackPlan(Long tenantId, Long planId, CrmCallbackPlanExecuteRequest request);

  CrmContractAttachmentResponse createAttachment(Long tenantId, Long orgId, Long staffId, Long leadId, CrmContractAttachmentRequest request);
  List<CrmContractAttachmentResponse> listAttachment(Long tenantId, Long leadId);
  void deleteAttachment(Long tenantId, Long attachmentId);

  List<CrmSmsTaskResponse> createSmsTasks(Long tenantId, Long orgId, Long staffId, CrmSmsTaskCreateRequest request);
  List<CrmSmsTaskResponse> listSmsTasks(Long tenantId, Long leadId);
  CrmSmsTaskResponse sendSmsTask(Long tenantId, Long taskId);
}
