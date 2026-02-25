package com.zhiyangyun.care.baseconfig.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.baseconfig.model.BaseDataImportRequest;
import com.zhiyangyun.care.baseconfig.model.BaseDataImportResult;
import com.zhiyangyun.care.baseconfig.model.BaseDataItemRequest;
import com.zhiyangyun.care.baseconfig.model.BaseDataItemResponse;
import java.util.List;

public interface BaseDataItemService {
  BaseDataItemResponse create(BaseDataItemRequest request);
  BaseDataItemResponse update(Long id, BaseDataItemRequest request);
  IPage<BaseDataItemResponse> page(Long tenantId, long pageNo, long pageSize, String configGroup, String keyword, Integer status);
  List<BaseDataItemResponse> list(Long tenantId, String configGroup, Integer status);
  List<BaseDataItemResponse> list(Long tenantId, String configGroup, String keyword, Integer status);
  int batchUpdateStatus(Long tenantId, List<Long> ids, Integer status);
  BaseDataImportResult previewImport(Long tenantId, Long orgId, Long staffId, BaseDataImportRequest request);
  BaseDataImportResult importItems(Long tenantId, Long orgId, Long staffId, BaseDataImportRequest request);
  void delete(Long id, Long tenantId);
}
