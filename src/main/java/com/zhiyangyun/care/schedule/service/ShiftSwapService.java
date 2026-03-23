package com.zhiyangyun.care.schedule.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.schedule.model.ShiftSwapRequestCreateRequest;
import com.zhiyangyun.care.schedule.model.ShiftSwapRequestResponse;

public interface ShiftSwapService {
  IPage<ShiftSwapRequestResponse> page(
      Long orgId, Long currentStaffId, boolean adminView, long pageNo, long pageSize, String status, boolean mineOnly);

  ShiftSwapRequestResponse create(Long orgId, Long currentStaffId, ShiftSwapRequestCreateRequest request);

  ShiftSwapRequestResponse targetConfirm(Long orgId, Long currentStaffId, Long id, boolean agreed, String remark);
}
