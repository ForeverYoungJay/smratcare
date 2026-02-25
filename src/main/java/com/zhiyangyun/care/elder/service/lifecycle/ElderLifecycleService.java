package com.zhiyangyun.care.elder.service.lifecycle;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionRequest;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionRecordResponse;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionResponse;
import com.zhiyangyun.care.elder.model.lifecycle.ChangeLogResponse;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ElderLifecycleService {
  AdmissionResponse admit(AdmissionRequest request);
  DischargeResponse discharge(DischargeRequest request);
  IPage<ChangeLogResponse> changeLogs(
      Long tenantId,
      long pageNo,
      long pageSize,
      Long elderId,
      String changeType,
      String reason,
      LocalDateTime startTime,
      LocalDateTime endTime);
  IPage<AdmissionRecordResponse> admissionPage(
      Long tenantId,
      long pageNo,
      long pageSize,
      String keyword,
      String contractNo,
      Integer elderStatus,
      LocalDate admissionDateStart,
      LocalDate admissionDateEnd);
}
