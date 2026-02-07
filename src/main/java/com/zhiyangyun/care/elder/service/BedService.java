package com.zhiyangyun.care.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.elder.model.BedRequest;
import com.zhiyangyun.care.elder.model.BedResponse;

public interface BedService {
  BedResponse create(BedRequest request);

  BedResponse update(Long id, BedRequest request);

  BedResponse get(Long id);

  IPage<BedResponse> page(Long orgId, long pageNo, long pageSize, String keyword, Integer status);

  java.util.List<BedResponse> list(Long orgId);

  void delete(Long id);
}
