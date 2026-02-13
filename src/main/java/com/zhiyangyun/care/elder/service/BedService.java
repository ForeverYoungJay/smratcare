package com.zhiyangyun.care.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.elder.model.BedRequest;
import com.zhiyangyun.care.elder.model.BedResponse;

public interface BedService {
  BedResponse create(BedRequest request);

  BedResponse update(Long id, BedRequest request);

  BedResponse get(Long id, Long tenantId);

  IPage<BedResponse> page(Long orgId, long pageNo, long pageSize,
      String keyword, Integer status, String bedNo, String roomNo, String elderName);

  java.util.List<BedResponse> list(Long orgId);

  java.util.List<BedResponse> map(Long orgId);

  void delete(Long id, Long tenantId);
}
