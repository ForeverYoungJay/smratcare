package com.zhiyangyun.care.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.ai.entity.AiRiskModel;
import com.zhiyangyun.care.ai.entity.AiRiskScore;
import com.zhiyangyun.care.ai.model.AiRiskSummaryResponse;
import com.zhiyangyun.care.ai.model.AiRiskTrendPointResponse;
import java.util.List;

/** 健康风险预测：模型配置 / 评分重算 / 看板查询。 */
public interface AiRiskService {

  /** 机构生效模型（机构自定义优先，否则全局默认）。 */
  List<AiRiskModel> listModels(Long orgId);

  /** 保存机构自定义模型（在全局默认基础上覆盖同 riskType）。 */
  AiRiskModel saveModel(Long orgId, Long staffId, AiRiskModel request);

  /** 重算：elderId 为空则全员；riskType 为空则全部启用类型。返回生成的评分条数。 */
  int recompute(Long orgId, Long staffId, Long elderId, String riskType);

  IPage<AiRiskScore> pageScores(Long orgId, long pageNo, long pageSize,
      String riskType, String riskLevel, Long elderId, String keyword);

  AiRiskSummaryResponse summary(Long orgId);

  List<AiRiskTrendPointResponse> trend(Long orgId, Long elderId, String riskType, int days);
}
