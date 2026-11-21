package com.zhiyangyun.care.nursing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.nursing.entity.NursingRectification;
import com.zhiyangyun.care.nursing.model.NursingRectificationRequest;
import com.zhiyangyun.care.nursing.model.NursingRectificationReviewRequest;

public interface NursingRectificationService {
  IPage<NursingRectification> page(Long tenantId, long pageNo, long pageSize, String status, String keyword,
      boolean overdue);
  NursingRectification create(Long tenantId, NursingRectificationRequest request, Long operatorId);
  /** 记录不存在返回 null；状态不允许抛 IllegalStateException；非负责人/管理者抛 AccessDeniedException。 */
  NursingRectification update(Long id, Long tenantId, NursingRectificationRequest request, Long operatorId,
      boolean manager);
  NursingRectification review(Long id, Long tenantId, NursingRectificationReviewRequest request, Long operatorId,
      String operatorName);
}
