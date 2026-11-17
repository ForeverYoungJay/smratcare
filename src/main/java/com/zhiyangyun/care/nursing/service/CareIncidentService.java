package com.zhiyangyun.care.nursing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.nursing.entity.CareIncident;
import com.zhiyangyun.care.nursing.model.CareIncidentActionRequest;
import com.zhiyangyun.care.nursing.model.CareIncidentDetailResponse;
import com.zhiyangyun.care.nursing.model.CareIncidentRequest;
import com.zhiyangyun.care.nursing.model.CareIncidentSummaryResponse;
import java.time.LocalDate;
import java.time.YearMonth;

public interface CareIncidentService {
  IPage<CareIncident> page(Long tenantId, long pageNo, long pageSize, String status, String incidentType,
      String severity, String keyword, LocalDate dateFrom, LocalDate dateTo, Long elderId, Long relatedTaskId);
  CareIncidentSummaryResponse summary(Long tenantId, YearMonth month);
  CareIncidentDetailResponse get(Long id, Long tenantId);
  CareIncidentDetailResponse create(Long tenantId, CareIncidentRequest request, Long operatorId, String operatorName);
  /** 状态不允许抛 IllegalStateException，权限不足抛 AccessDeniedException；记录不存在返回 null。 */
  CareIncidentDetailResponse act(Long id, Long tenantId, CareIncidentActionRequest request, Long operatorId,
      String operatorName, boolean manager);
}
