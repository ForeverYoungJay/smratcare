package com.zhiyangyun.care.standard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.standard.model.ElderPackageRequest;
import com.zhiyangyun.care.standard.model.ElderPackageResponse;
import java.util.List;

public interface ElderPackageService {
  ElderPackageResponse create(ElderPackageRequest request);
  ElderPackageResponse update(Long id, ElderPackageRequest request);
  IPage<ElderPackageResponse> page(Long tenantId, long pageNo, long pageSize, Long elderId, Integer status);
  List<ElderPackageResponse> list(Long tenantId, Long elderId);
  void delete(Long id, Long tenantId);
}
