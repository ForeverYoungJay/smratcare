package com.zhiyangyun.care.standard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.standard.model.CarePackageRequest;
import com.zhiyangyun.care.standard.model.CarePackageResponse;
import java.util.List;

public interface CarePackageService {
  CarePackageResponse create(CarePackageRequest request);
  CarePackageResponse update(Long id, CarePackageRequest request);
  CarePackageResponse get(Long id, Long tenantId);
  IPage<CarePackageResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer enabled);
  List<CarePackageResponse> list(Long tenantId, Integer enabled);
  void delete(Long id, Long tenantId);
}
