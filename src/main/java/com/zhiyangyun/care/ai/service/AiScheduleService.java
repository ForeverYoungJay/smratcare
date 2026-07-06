package com.zhiyangyun.care.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.ai.entity.AiScheduleConstraint;
import com.zhiyangyun.care.ai.model.AiScheduleAdoptResponse;
import com.zhiyangyun.care.ai.model.AiScheduleConstraintRequest;
import com.zhiyangyun.care.ai.model.AiScheduleGenerateRequest;
import com.zhiyangyun.care.ai.model.AiScheduleItemUpdateRequest;
import com.zhiyangyun.care.ai.model.AiScheduleProposalItemResponse;
import com.zhiyangyun.care.ai.model.AiScheduleProposalResponse;

/** 智能排班：方案生成 / 调整 / 采纳写回。 */
public interface AiScheduleService {

  AiScheduleProposalResponse generate(Long orgId, Long staffId, AiScheduleGenerateRequest request);

  IPage<AiScheduleProposalResponse> page(Long orgId, long pageNo, long pageSize, String status);

  AiScheduleProposalResponse detail(Long orgId, Long id);

  AiScheduleProposalItemResponse updateItem(
      Long orgId, Long staffId, Long proposalId, Long itemId, AiScheduleItemUpdateRequest request);

  void deleteItem(Long orgId, Long proposalId, Long itemId);

  AiScheduleAdoptResponse adopt(Long orgId, Long staffId, Long proposalId);

  void delete(Long orgId, Long id);

  AiScheduleConstraint getConstraint(Long orgId);

  AiScheduleConstraint saveConstraint(Long orgId, Long staffId, AiScheduleConstraintRequest request);
}
