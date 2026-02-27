package com.zhiyangyun.care.store.service;

import com.zhiyangyun.care.store.model.MaterialCenterOverviewResponse;

public interface MaterialCenterOverviewService {
  MaterialCenterOverviewResponse overview(Long orgId, int expiryDays);
}
