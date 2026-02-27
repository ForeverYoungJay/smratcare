package com.zhiyangyun.care.crm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.crm.model.MarketingPlanRequest;
import com.zhiyangyun.care.crm.model.MarketingPlanResponse;
import java.util.List;

public interface MarketingPlanService {
  IPage<MarketingPlanResponse> page(
      Long orgId,
      long pageNo,
      long pageSize,
      String moduleType,
      String status,
      String keyword);

  List<MarketingPlanResponse> listByModule(Long orgId, String moduleType);

  MarketingPlanResponse create(Long orgId, Long staffId, MarketingPlanRequest request);

  MarketingPlanResponse update(Long orgId, Long id, MarketingPlanRequest request);

  void remove(Long orgId, Long id);
}
