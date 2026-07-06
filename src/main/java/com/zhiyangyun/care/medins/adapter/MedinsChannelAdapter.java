package com.zhiyangyun.care.medins.adapter;

import com.zhiyangyun.care.medins.entity.MedinsChannelConfig;
import com.zhiyangyun.care.medins.entity.MedinsEvoucher;
import com.zhiyangyun.care.medins.entity.MedinsSettlementSheet;
import com.zhiyangyun.care.medins.entity.MedinsUploadTask;

/**
 * 医保平台渠道适配器。各地医保平台（结算清单上传、回执查询、电子凭证校验）差异
 * 由各自适配器实现封装，上层服务只依赖本接口 + medins_channel_config 配置，
 * 便于跨地区/跨供应商差异化接入。
 */
public interface MedinsChannelAdapter {

  /** 渠道编码，如 MOCK / 各地医保平台编码。 */
  String channel();

  /** 上传医保结算清单，返回渠道受理结果（部分渠道异步回执）。 */
  MedinsChannelResult uploadSettlementSheet(MedinsSettlementSheet sheet, String payloadJson,
      MedinsChannelConfig config);

  /** 主动查询上报任务回执（异步渠道轮询终态）。 */
  MedinsChannelResult queryReceipt(MedinsUploadTask task, MedinsChannelConfig config);

  /** 校验长者医保电子凭证有效性。 */
  MedinsChannelResult verifyEvoucher(MedinsEvoucher evoucher, MedinsChannelConfig config);
}
