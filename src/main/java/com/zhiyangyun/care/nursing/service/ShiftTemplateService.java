package com.zhiyangyun.care.nursing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.nursing.model.ShiftTemplateApplyRequest;
import com.zhiyangyun.care.nursing.model.ShiftTemplateRequest;
import com.zhiyangyun.care.nursing.model.ShiftTemplateResponse;
import java.util.List;

public interface ShiftTemplateService {
  ShiftTemplateResponse create(ShiftTemplateRequest request);
  ShiftTemplateResponse update(Long id, ShiftTemplateRequest request);
  ShiftTemplateResponse get(Long id, Long tenantId);
  IPage<ShiftTemplateResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer enabled);
  List<ShiftTemplateResponse> list(Long tenantId, Integer enabled);
  int apply(Long id, Long tenantId, Long operatorId, ShiftTemplateApplyRequest request);
  void delete(Long id, Long tenantId);
}
