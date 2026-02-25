package com.zhiyangyun.care.nursing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.nursing.model.CareLevelRequest;
import com.zhiyangyun.care.nursing.model.CareLevelResponse;
import java.util.List;

public interface CareLevelService {
  CareLevelResponse create(CareLevelRequest request);
  CareLevelResponse update(Long id, CareLevelRequest request);
  CareLevelResponse get(Long id, Long tenantId);
  IPage<CareLevelResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer enabled);
  List<CareLevelResponse> list(Long tenantId, Integer enabled);
  void delete(Long id, Long tenantId);
}
