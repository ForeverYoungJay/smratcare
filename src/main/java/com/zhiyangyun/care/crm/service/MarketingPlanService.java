package com.zhiyangyun.care.crm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.crm.model.MarketingPlanApprovalRequest;
import com.zhiyangyun.care.crm.model.MarketingPlanRequest;
import com.zhiyangyun.care.crm.model.MarketingPlanReadConfirmRequest;
import com.zhiyangyun.care.crm.model.MarketingPlanReceiptResponse;
import com.zhiyangyun.care.crm.model.MarketingPlanResponse;
import com.zhiyangyun.care.crm.model.MarketingPlanWorkflowSummaryResponse;
import java.util.List;

public interface MarketingPlanService {
  IPage<MarketingPlanResponse> page(
      Long orgId,
      long pageNo,
      long pageSize,
      String moduleType,
      String status,
      String keyword);

  List<MarketingPlanResponse> listByModule(Long orgId, String moduleType);

  MarketingPlanResponse detail(Long orgId, Long staffId, Long id);

  MarketingPlanResponse create(Long orgId, Long staffId, MarketingPlanRequest request);

  MarketingPlanResponse update(Long orgId, Long id, MarketingPlanRequest request);

  MarketingPlanResponse submit(Long orgId, Long staffId, Long id);

  MarketingPlanResponse approve(Long orgId, Long staffId, Long id, MarketingPlanApprovalRequest request);

  MarketingPlanResponse reject(Long orgId, Long staffId, Long id, MarketingPlanApprovalRequest request);

  MarketingPlanResponse publish(Long orgId, Long staffId, Long id);

  MarketingPlanResponse confirmRead(Long orgId, Long staffId, Long id, MarketingPlanReadConfirmRequest request);

  List<MarketingPlanReceiptResponse> listReceipts(Long orgId, Long id);

  MarketingPlanWorkflowSummaryResponse workflowSummary(Long orgId, Long id);

  void remove(Long orgId, Long id);
}
