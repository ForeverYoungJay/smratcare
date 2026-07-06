package com.zhiyangyun.care.medins.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.medins.entity.MedinsSettlementSheet;
import com.zhiyangyun.care.medins.entity.MedinsUploadTask;
import com.zhiyangyun.care.medins.model.MedinsReceiptCallbackRequest;

/** 医保结算清单与上报任务服务。 */
public interface MedinsSettlementService {

  /**
   * 从长护险结算单生成医保结算清单（草稿）。
   * ltciSettlementId 与 elderId+month 二选一：前者优先。
   */
  MedinsSettlementSheet generateSheet(Long ltciSettlementId, Long elderId, String month);

  IPage<MedinsSettlementSheet> pageSheets(long pageNo, long pageSize, Long elderId,
      String month, String status);

  MedinsSettlementSheet getSheet(Long sheetId);

  /** 上传结算清单：生成报文快照 + 上报任务，调用渠道适配器。 */
  MedinsUploadTask uploadSheet(Long sheetId, String channel);

  /** 重发失败任务（复用原报文快照）。 */
  MedinsUploadTask retryTask(Long taskId);

  /** 主动查询回执（异步渠道轮询终态）。 */
  MedinsUploadTask queryReceipt(Long taskId);

  /** 渠道回执回调。 */
  void receiveCallback(MedinsReceiptCallbackRequest request);

  /** 已回执清单对账完成：RECEIPTED → RECONCILED。 */
  void reconcileSheet(Long sheetId);

  IPage<MedinsUploadTask> pageTasks(long pageNo, long pageSize, Long sheetId, String status);
}
