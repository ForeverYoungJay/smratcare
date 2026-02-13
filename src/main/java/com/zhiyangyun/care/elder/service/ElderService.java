package com.zhiyangyun.care.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.elder.model.AssignBedRequest;
import com.zhiyangyun.care.elder.model.ElderCreateRequest;
import com.zhiyangyun.care.elder.model.ElderResponse;
import com.zhiyangyun.care.elder.model.ElderUpdateRequest;
import java.time.LocalDate;

public interface ElderService {
  ElderResponse create(ElderCreateRequest request);

  ElderResponse update(Long id, ElderUpdateRequest request);

  ElderResponse get(Long id, Long tenantId);

  IPage<ElderResponse> page(Long tenantId, long pageNo, long pageSize, String keyword);

  ElderResponse assignBed(Long elderId, AssignBedRequest request);

  ElderResponse unbindBed(Long elderId, LocalDate endDate, String reason, Long tenantId, Long createdBy);
}
