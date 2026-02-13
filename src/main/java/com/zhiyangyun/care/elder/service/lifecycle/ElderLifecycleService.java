package com.zhiyangyun.care.elder.service.lifecycle;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionRequest;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionResponse;
import com.zhiyangyun.care.elder.model.lifecycle.ChangeLogResponse;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeResponse;

public interface ElderLifecycleService {
  AdmissionResponse admit(AdmissionRequest request);
  DischargeResponse discharge(DischargeRequest request);
  IPage<ChangeLogResponse> changeLogs(Long tenantId, long pageNo, long pageSize, Long elderId);
}
