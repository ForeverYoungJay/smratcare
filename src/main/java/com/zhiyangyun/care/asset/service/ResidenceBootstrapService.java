package com.zhiyangyun.care.asset.service;

import com.zhiyangyun.care.asset.model.ResidenceBootstrapRequest;
import com.zhiyangyun.care.asset.model.ResidenceBootstrapResponse;

public interface ResidenceBootstrapService {
  ResidenceBootstrapResponse bootstrap(Long tenantId, Long createdBy, ResidenceBootstrapRequest request);
}
