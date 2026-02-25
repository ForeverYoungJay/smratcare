package com.zhiyangyun.care.nursing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.nursing.model.ServiceBookingRequest;
import com.zhiyangyun.care.nursing.model.ServiceBookingResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ServiceBookingService {
  ServiceBookingResponse create(ServiceBookingRequest request);
  ServiceBookingResponse update(Long id, ServiceBookingRequest request);
  ServiceBookingResponse get(Long id, Long tenantId);
  IPage<ServiceBookingResponse> page(Long tenantId, long pageNo, long pageSize, LocalDateTime timeFrom,
      LocalDateTime timeTo, String status, Long elderId);
  int generateFromPlans(Long tenantId, LocalDate date, boolean force);
  void delete(Long id, Long tenantId);
}
