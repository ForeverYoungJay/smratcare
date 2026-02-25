package com.zhiyangyun.care.life.service;

import com.zhiyangyun.care.life.model.DiningRiskCheckResponse;

public interface DiningRiskService {
  DiningRiskCheckResponse check(Long orgId, Long elderId, String elderName, String dishNames);
}
