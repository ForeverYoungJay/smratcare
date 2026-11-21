package com.zhiyangyun.care.nursing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.nursing.entity.ServicePlanRevision;
import com.zhiyangyun.care.nursing.model.ServicePlanRequest;
import com.zhiyangyun.care.nursing.model.ServicePlanResponse;
import java.util.List;

public interface ServicePlanService {
  ServicePlanResponse create(ServicePlanRequest request);
  ServicePlanResponse update(Long id, ServicePlanRequest request, Long operatorId, String operatorName);
  /** 记录不存在返回 null；非待审核状态抛 IllegalStateException。 */
  ServicePlanResponse approve(Long id, Long tenantId, Long operatorId);
  ServicePlanResponse reject(Long id, Long tenantId, String reason);
  List<ServicePlanRevision> revisions(Long id, Long tenantId);
  ServicePlanResponse get(Long id, Long tenantId);
  IPage<ServicePlanResponse> page(Long tenantId, long pageNo, long pageSize, Long elderId, String status, String keyword);
  List<ServicePlanResponse> list(Long tenantId, String status);
  void delete(Long id, Long tenantId);
}
