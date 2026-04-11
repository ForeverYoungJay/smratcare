package com.zhiyangyun.care.crm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.crm.model.CrmLeadAssignRequest;
import com.zhiyangyun.care.crm.model.CrmLeadRequest;
import com.zhiyangyun.care.crm.model.CrmLeadResponse;

public interface CrmLeadService {
  CrmLeadResponse create(CrmLeadRequest request);
  CrmLeadResponse update(Long id, CrmLeadRequest request, Long currentStaffId, boolean adminView);
  CrmLeadResponse get(Long id, Long tenantId, Long currentStaffId, boolean adminView);
  IPage<CrmLeadResponse> page(Long tenantId, Long currentStaffId, boolean adminView, long pageNo, long pageSize, String keyword, Integer status, String source, String customerTag,
                              String consultantName, String consultantPhone, String elderName, String elderPhone,
                              String consultDateFrom, String consultDateTo, String consultType, String mediaChannel,
                              String infoSource, String marketerName, String followupStatus, String reservationChannel,
                              String contractNo, String contractStatus, String flowStage, String currentOwnerDept, String callbackType,
                              String followupDateFrom, String followupDateTo, Boolean followupDueOnly);
  CrmLeadResponse assign(Long id, Long tenantId, Long operatorId, boolean adminView, CrmLeadAssignRequest request);
  void delete(Long id, Long tenantId, Long currentStaffId, boolean adminView);
}
