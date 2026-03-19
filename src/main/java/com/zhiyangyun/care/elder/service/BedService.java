package com.zhiyangyun.care.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.elder.model.BedRequest;
import com.zhiyangyun.care.elder.model.BedResponse;

public interface BedService {
  BedResponse create(BedRequest request);

  BedResponse update(Long id, BedRequest request);

  BedResponse get(Long id, Long tenantId);

  IPage<BedResponse> page(Long orgId, long pageNo, long pageSize,
      String keyword, Integer status, String bedNo, String roomNo, String elderName, String roomType, String bedType);

  java.util.List<BedResponse> list(Long orgId);

  default java.util.List<BedResponse> map(Long orgId) {
    return map(orgId, true);
  }

  java.util.List<BedResponse> map(Long orgId, boolean includeRisk);

  void delete(Long id, Long tenantId);
}
