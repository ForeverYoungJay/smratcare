package com.zhiyangyun.care.asset.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.asset.model.BuildingRequest;
import com.zhiyangyun.care.asset.model.BuildingResponse;
import java.util.List;

public interface BuildingService {
  BuildingResponse create(BuildingRequest request);
  BuildingResponse update(Long id, BuildingRequest request);
  BuildingResponse get(Long id);
  IPage<BuildingResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer status);
  List<BuildingResponse> list(Long tenantId);
  void delete(Long id, Long tenantId);
}
