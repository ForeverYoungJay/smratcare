package com.zhiyangyun.care.asset.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.asset.model.FloorRequest;
import com.zhiyangyun.care.asset.model.FloorResponse;
import java.util.List;

public interface FloorService {
  FloorResponse create(FloorRequest request);
  FloorResponse update(Long id, FloorRequest request);
  FloorResponse get(Long id);
  IPage<FloorResponse> page(Long tenantId, long pageNo, long pageSize, Long buildingId, String keyword, Integer status);
  List<FloorResponse> list(Long tenantId, Long buildingId);
  void delete(Long id, Long tenantId);
}
