package com.zhiyangyun.care.ltci.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.ltci.entity.LtciServicePackage;
import com.zhiyangyun.care.ltci.entity.LtciSettlement;
import com.zhiyangyun.care.ltci.model.LtciBenefitRequest;
import com.zhiyangyun.care.ltci.model.LtciInsuredRequest;
import com.zhiyangyun.care.ltci.model.LtciServiceRecordRequest;
import java.util.List;

/** 长护险待遇与结算业务。 */
public interface LtciBenefitService {

  /** 参保登记，返回参保 id。 */
  Long registerInsured(LtciInsuredRequest request);

  /** 依据评估等级核定待遇，返回待遇 id。 */
  Long grantBenefit(LtciBenefitRequest request);

  /** 录入护理服务记录，返回记录 id。 */
  Long addServiceRecord(LtciServiceRecordRequest request);

  /** 护理服务包列表（含全局默认与机构定制）。 */
  List<LtciServicePackage> listPackages(Long orgId);

  /** 生成/刷新指定长者某月度结算草稿，返回结算单。month 格式 YYYYMM。 */
  LtciSettlement generateSettlement(Long orgId, Long elderId, String month);

  /** 为机构下所有有效待遇长者批量生成某月草稿，返回生成条数（供定时任务调用）。 */
  int generateMonthlyDrafts(Long orgId, String month);

  /** 为所有机构批量生成某月结算草稿，返回生成条数（供定时任务调用）。 */
  int generateAllOrgMonthlyDrafts(String month);

  /** 提交结算单（DRAFT → SUBMITTED，并标记当月服务记录已结算）。 */
  void submitSettlement(Long settlementId);

  /** 分页查询结算单。 */
  IPage<LtciSettlement> pageSettlements(long pageNo, long pageSize, Long elderId, String month,
      String status);
}
