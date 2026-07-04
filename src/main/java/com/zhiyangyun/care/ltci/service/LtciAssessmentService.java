package com.zhiyangyun.care.ltci.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.ltci.entity.LtciAssessment;
import com.zhiyangyun.care.ltci.model.LtciAcceptRequest;
import com.zhiyangyun.care.ltci.model.LtciApplyRequest;
import com.zhiyangyun.care.ltci.model.LtciDisputeRequest;
import com.zhiyangyun.care.ltci.model.LtciScoreRequest;

/** 长护险失能评估业务：申请→受理→打分判级→告知→争议复核→生效。 */
public interface LtciAssessmentService {

  /** 提交评估申请，返回申请 id。 */
  Long apply(LtciApplyRequest request);

  /** 受理申请并分配评估员。 */
  void accept(LtciAcceptRequest request);

  /** 提交逐项打分，系统按模板组合判级并落库，返回评估记录。 */
  LtciAssessment score(LtciScoreRequest request);

  /** 结果告知（JUDGED → NOTIFIED，并将评估记录置为生效）。 */
  void notifyResult(Long assessmentId);

  /** 发起争议复核，生成一条 DISPUTE 类型申请，返回新申请 id。 */
  Long dispute(LtciDisputeRequest request);

  /** 分页查询评估记录。 */
  IPage<LtciAssessment> pageAssessments(long pageNo, long pageSize, Long elderId, String status,
      Integer disabilityLevel);

  /** 查询长者当前生效的评估记录（无则返回 null）。 */
  LtciAssessment currentEffective(Long elderId);
}
