package com.zhiyangyun.care.baseconfig.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.baseconfig.model.BaseDataItemRequest;
import com.zhiyangyun.care.baseconfig.model.BaseDataItemResponse;

public interface BaseDataItemService {
  BaseDataItemResponse create(BaseDataItemRequest request);
  BaseDataItemResponse update(Long id, BaseDataItemRequest request);
  IPage<BaseDataItemResponse> page(Long tenantId, long pageNo, long pageSize, String configGroup, String keyword, Integer status);
  java.util.List<BaseDataItemResponse> list(Long tenantId, String configGroup, Integer status);
  void delete(Long id, Long tenantId);
}
