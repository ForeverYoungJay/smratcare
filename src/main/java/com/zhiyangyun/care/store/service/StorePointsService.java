package com.zhiyangyun.care.store.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.store.model.PointsAccountResponse;
import com.zhiyangyun.care.store.model.PointsAdjustRequest;
import com.zhiyangyun.care.store.model.PointsLogResponse;

public interface StorePointsService {
  IPage<PointsAccountResponse> page(Long orgId, long pageNo, long pageSize, String keyword, Integer status);

  IPage<PointsLogResponse> logPage(Long orgId, long pageNo, long pageSize, Long elderId, String keyword);

  void adjust(PointsAdjustRequest request);
}
