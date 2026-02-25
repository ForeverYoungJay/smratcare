package com.zhiyangyun.care.crm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.crm.model.CrmLeadRequest;
import com.zhiyangyun.care.crm.model.CrmLeadResponse;

public interface CrmLeadService {
  CrmLeadResponse create(CrmLeadRequest request);
  CrmLeadResponse update(Long id, CrmLeadRequest request);
  CrmLeadResponse get(Long id, Long tenantId);
  IPage<CrmLeadResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer status, String source, String customerTag);
  void delete(Long id, Long tenantId);
}
