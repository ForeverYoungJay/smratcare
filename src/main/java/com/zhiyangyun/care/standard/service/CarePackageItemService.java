package com.zhiyangyun.care.standard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.standard.model.CarePackageItemRequest;
import com.zhiyangyun.care.standard.model.CarePackageItemResponse;
import java.util.List;

public interface CarePackageItemService {
  CarePackageItemResponse create(CarePackageItemRequest request);
  CarePackageItemResponse update(Long id, CarePackageItemRequest request);
  IPage<CarePackageItemResponse> page(Long tenantId, long pageNo, long pageSize, Long packageId);
  List<CarePackageItemResponse> list(Long tenantId, Long packageId);
  void delete(Long id, Long tenantId);
}
