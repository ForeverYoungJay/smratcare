package com.zhiyangyun.care.standard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.standard.model.ServiceItemRequest;
import com.zhiyangyun.care.standard.model.ServiceItemResponse;
import java.util.List;

public interface ServiceItemService {
  ServiceItemResponse create(ServiceItemRequest request);
  ServiceItemResponse update(Long id, ServiceItemRequest request);
  ServiceItemResponse get(Long id, Long tenantId);
  IPage<ServiceItemResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer enabled);
  List<ServiceItemResponse> list(Long tenantId, Integer enabled);
  void delete(Long id, Long tenantId);
}
