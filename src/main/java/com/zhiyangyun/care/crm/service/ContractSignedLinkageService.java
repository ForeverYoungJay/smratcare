package com.zhiyangyun.care.crm.service;

import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.model.ContractSystemLinkageSummary;

public interface ContractSignedLinkageService {
  ContractSystemLinkageSummary ensureSignedLinkage(CrmContract contract, Long operatorId);
}
