package com.zhiyangyun.care.medins.adapter;

import com.zhiyangyun.care.medins.entity.MedinsChannelConfig;
import com.zhiyangyun.care.medins.entity.MedinsEvoucher;
import com.zhiyangyun.care.medins.entity.MedinsSettlementSheet;
import com.zhiyangyun.care.medins.entity.MedinsUploadTask;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 医保 MOCK 适配器：本地联调与验收用。上传返回已受理未终态(acked=false)，
 * 模拟真实医保渠道的异步回执；终态由回执查询或 {@code /api/medins/receipts/callback} 回调置入。
 */
@Component
public class MedinsMockAdapter implements MedinsChannelAdapter {

  public static final String CHANNEL = "MOCK";

  @Override
  public String channel() {
    return CHANNEL;
  }

  @Override
  public MedinsChannelResult uploadSettlementSheet(MedinsSettlementSheet sheet, String payloadJson,
      MedinsChannelConfig config) {
    MedinsChannelResult precheck = checkConfig(config);
    if (precheck != null) {
      return precheck;
    }
    if (sheet == null || !StringUtils.hasText(payloadJson)) {
      return MedinsChannelResult.fail("结算清单报文为空，无法上传");
    }
    String receipt = "MI-" + sheet.getId() + "-" + System.currentTimeMillis();
    return MedinsChannelResult.ok(receipt, false);
  }

  @Override
  public MedinsChannelResult queryReceipt(MedinsUploadTask task, MedinsChannelConfig config) {
    MedinsChannelResult precheck = checkConfig(config);
    if (precheck != null) {
      return precheck;
    }
    if (task == null) {
      return MedinsChannelResult.fail("上报任务为空");
    }
    // MOCK：查询即视为医保端已受理终态。
    String code = StringUtils.hasText(task.getReceiptCode())
        ? task.getReceiptCode() : "MI-" + task.getId() + "-" + System.currentTimeMillis();
    String receiptJson = "{\"receiptCode\":\"" + code + "\",\"status\":\"ACKED\",\"channel\":\"MOCK\"}";
    return MedinsChannelResult.ok(code, true, receiptJson);
  }

  @Override
  public MedinsChannelResult verifyEvoucher(MedinsEvoucher evoucher, MedinsChannelConfig config) {
    MedinsChannelResult precheck = checkConfig(config);
    if (precheck != null) {
      return precheck;
    }
    if (evoucher == null || !StringUtils.hasText(evoucher.getEcardToken())) {
      return MedinsChannelResult.fail("电子凭证授权令牌为空");
    }
    if (!StringUtils.hasText(evoucher.getIdCard()) && !StringUtils.hasText(evoucher.getInsuredNo())) {
      return MedinsChannelResult.fail("身份证号与医保号均为空，无法核验凭证");
    }
    String code = "EV-" + evoucher.getElderId() + "-" + System.currentTimeMillis();
    return MedinsChannelResult.ok(code, true, "{\"result\":\"PASSED\",\"channel\":\"MOCK\"}");
  }

  private MedinsChannelResult checkConfig(MedinsChannelConfig config) {
    if (config == null || !StringUtils.hasText(config.getEndpoint())) {
      return MedinsChannelResult.fail("医保渠道未配置 endpoint");
    }
    if (config.getEnabled() == null || config.getEnabled() != 1) {
      return MedinsChannelResult.fail("医保渠道未启用");
    }
    return null;
  }
}
