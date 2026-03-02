package com.zhiyangyun.care.crm.service;

import com.zhiyangyun.care.crm.model.action.CrmContractAttachmentRequest;
import com.zhiyangyun.care.crm.model.action.CrmContractAttachmentResponse;
import com.zhiyangyun.care.crm.model.action.CrmSmsTaskCreateRequest;
import com.zhiyangyun.care.crm.model.action.CrmSmsTaskResponse;
import java.util.List;

public interface CrmContractActionService {
  CrmContractAttachmentResponse createAttachment(
      Long tenantId, Long orgId, Long staffId, Long contractId, CrmContractAttachmentRequest request);

  List<CrmContractAttachmentResponse> listAttachment(Long tenantId, Long contractId);

  void deleteAttachment(Long tenantId, Long attachmentId);

  List<CrmSmsTaskResponse> createSmsTasks(Long tenantId, Long orgId, Long staffId, CrmSmsTaskCreateRequest request);

  List<CrmSmsTaskResponse> listSmsTasks(Long tenantId, Long contractId);

  CrmSmsTaskResponse sendSmsTask(Long tenantId, Long taskId);
}
