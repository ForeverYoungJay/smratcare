package com.zhiyangyun.care.elder.service;

import com.zhiyangyun.care.elder.model.BedReleaseResult;
import java.time.LocalDate;

public interface ElderOccupancyService {
  BedReleaseResult releaseBedAndCloseRelation(
      Long tenantId,
      Long orgId,
      Long elderId,
      LocalDate endDate,
      String reason);
}
