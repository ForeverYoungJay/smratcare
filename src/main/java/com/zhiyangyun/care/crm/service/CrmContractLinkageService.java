package com.zhiyangyun.care.crm.service;

import com.zhiyangyun.care.crm.model.CrmContractLinkageResponse;
import com.zhiyangyun.care.crm.model.CrmContractAssessmentOverviewResponse;

public interface CrmContractLinkageService {
  CrmContractLinkageResponse getByElderId(Long tenantId, Long elderId);

  CrmContractLinkageResponse getByLeadId(Long tenantId, Long leadId);

  CrmContractAssessmentOverviewResponse getAssessmentOverview(Long tenantId, Long elderId, Long leadId);
}
