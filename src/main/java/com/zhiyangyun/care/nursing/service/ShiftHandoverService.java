package com.zhiyangyun.care.nursing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.nursing.model.ShiftHandoverRequest;
import com.zhiyangyun.care.nursing.model.ShiftHandoverResponse;
import java.time.LocalDate;

public interface ShiftHandoverService {
  ShiftHandoverResponse create(ShiftHandoverRequest request);
  ShiftHandoverResponse update(Long id, ShiftHandoverRequest request);
  ShiftHandoverResponse get(Long id, Long tenantId);
  IPage<ShiftHandoverResponse> page(Long tenantId, long pageNo, long pageSize, LocalDate dateFrom,
      LocalDate dateTo, String shiftCode, String status);
  void delete(Long id, Long tenantId);
}
