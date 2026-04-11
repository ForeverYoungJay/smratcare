package com.zhiyangyun.care.elder.service;

import com.zhiyangyun.care.elder.model.OccupancyHealthIssueResponse;
import com.zhiyangyun.care.elder.model.OccupancyRepairResponse;
import java.util.List;

public interface OccupancyConsistencyService {
  List<OccupancyHealthIssueResponse> inspect(Long tenantId, Long orgId, Long elderId, Long bedId);

  OccupancyRepairResponse repair(Long tenantId, Long orgId, Long elderId, Long bedId, String reason, Long operatorId);
}
