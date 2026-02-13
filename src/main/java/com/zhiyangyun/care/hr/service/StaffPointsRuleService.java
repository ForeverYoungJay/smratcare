package com.zhiyangyun.care.hr.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.hr.model.StaffPointsRuleRequest;
import com.zhiyangyun.care.hr.model.StaffPointsRuleResponse;

public interface StaffPointsRuleService {
  IPage<StaffPointsRuleResponse> page(Long orgId, long pageNo, long pageSize);

  StaffPointsRuleResponse upsert(Long orgId, StaffPointsRuleRequest request);

  void delete(Long orgId, Long id);
}
