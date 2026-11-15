package com.zhiyangyun.care.crm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.crm.model.visit.CrmVisitAppointmentCancelRequest;
import com.zhiyangyun.care.crm.model.visit.CrmVisitAppointmentCompleteRequest;
import com.zhiyangyun.care.crm.model.visit.CrmVisitAppointmentRequest;
import com.zhiyangyun.care.crm.model.visit.CrmVisitAppointmentResponse;

public interface CrmVisitAppointmentService {
  IPage<CrmVisitAppointmentResponse> page(Long tenantId, Long staffId, boolean manager, long pageNo, long pageSize,
      String keyword, String status, String dateFrom, String dateTo);
  CrmVisitAppointmentResponse create(Long tenantId, Long orgId, Long staffId, boolean manager,
      CrmVisitAppointmentRequest request);
  CrmVisitAppointmentResponse update(Long tenantId, Long staffId, boolean manager, Long id,
      CrmVisitAppointmentRequest request);
  CrmVisitAppointmentResponse arrive(Long tenantId, Long staffId, boolean manager, Long id);
  CrmVisitAppointmentResponse complete(Long tenantId, Long staffId, boolean manager, Long id,
      CrmVisitAppointmentCompleteRequest request);
  CrmVisitAppointmentResponse cancel(Long tenantId, Long staffId, boolean manager, Long id,
      CrmVisitAppointmentCancelRequest request);
}
