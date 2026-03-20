package com.zhiyangyun.care.asset.service;

import com.zhiyangyun.care.asset.model.ResidenceBatchCommitRequest;
import com.zhiyangyun.care.asset.model.ResidenceBatchCommitResponse;
import com.zhiyangyun.care.asset.model.ResidenceBatchGenerationRequest;
import com.zhiyangyun.care.asset.model.ResidenceBatchPreviewResponse;

public interface ResidenceBatchGenerationService {
  ResidenceBatchPreviewResponse preview(Long tenantId, Long operatorId, ResidenceBatchGenerationRequest request);

  ResidenceBatchCommitResponse commit(Long tenantId, Long operatorId, ResidenceBatchCommitRequest request);
}
