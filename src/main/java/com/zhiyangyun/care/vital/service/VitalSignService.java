package com.zhiyangyun.care.vital.service;

import com.zhiyangyun.care.vital.model.VitalRecordRequest;
import com.zhiyangyun.care.vital.model.VitalRecordResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface VitalSignService {
  VitalRecordResponse record(VitalRecordRequest request);

  List<VitalRecordResponse> listByElder(Long elderId, LocalDateTime from, LocalDateTime to, String type);

  VitalRecordResponse latest(Long elderId);

  List<VitalRecordResponse> abnormalToday();
}
