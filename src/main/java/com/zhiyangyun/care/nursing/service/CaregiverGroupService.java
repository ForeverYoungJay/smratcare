package com.zhiyangyun.care.nursing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.nursing.model.CaregiverGroupRequest;
import com.zhiyangyun.care.nursing.model.CaregiverGroupResponse;
import java.util.List;

public interface CaregiverGroupService {
  CaregiverGroupResponse create(CaregiverGroupRequest request);
  CaregiverGroupResponse update(Long id, CaregiverGroupRequest request);
  CaregiverGroupResponse get(Long id, Long tenantId);
  IPage<CaregiverGroupResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer enabled);
  List<CaregiverGroupResponse> list(Long tenantId, Integer enabled);
  void delete(Long id, Long tenantId);
}
