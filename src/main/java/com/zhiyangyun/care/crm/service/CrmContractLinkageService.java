package com.zhiyangyun.care.crm.service;

import com.zhiyangyun.care.crm.model.CrmContractLinkageResponse;

public interface CrmContractLinkageService {
  CrmContractLinkageResponse getByElderId(Long tenantId, Long elderId);

  CrmContractLinkageResponse getByLeadId(Long tenantId, Long leadId);
}
