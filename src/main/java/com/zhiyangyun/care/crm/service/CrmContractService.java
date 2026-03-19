package com.zhiyangyun.care.crm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.crm.model.CrmContractRequest;
import com.zhiyangyun.care.crm.model.CrmContractResponse;
import java.util.List;

public interface CrmContractService {
  CrmContractResponse create(Long tenantId, Long orgId, Long staffId, CrmContractRequest request);

  CrmContractResponse update(Long tenantId, Long orgId, Long id, CrmContractRequest request);

  CrmContractResponse get(Long tenantId, Long id);

  IPage<CrmContractResponse> page(
      Long tenantId,
      long pageNo,
      long pageSize,
      String keyword,
      String contractNo,
      String elderName,
      String elderPhone,
      String marketerName,
      String orgName,
      String flowStage,
      String contractStatus,
      String status,
      String changeWorkflowStatus,
      Boolean overdueOnly,
      Boolean sortByOverdue,
      String currentOwnerDept);

  int deleteBatch(Long tenantId, List<Long> ids, List<String> contractNos);

  CrmContractResponse handoffToAssessment(Long tenantId, Long id);

  CrmContractResponse moveToBedSelect(Long tenantId, Long id);

  CrmContractResponse moveToPendingSign(Long tenantId, Long id);

  CrmContractResponse startChange(Long tenantId, Long id, String remark);

  CrmContractResponse submitChange(Long tenantId, Long id, String remark);

  CrmContractResponse approveChange(Long tenantId, Long id, String remark);

  CrmContractResponse rejectChange(Long tenantId, Long id, String remark);

  CrmContractResponse approve(Long tenantId, Long id, String remark);

  CrmContractResponse reject(Long tenantId, Long id, String remark);

  CrmContractResponse voidContract(Long tenantId, Long id, String remark);

  CrmContractResponse finalizeSign(Long tenantId, Long operatorId, Long id, String remark);
}
