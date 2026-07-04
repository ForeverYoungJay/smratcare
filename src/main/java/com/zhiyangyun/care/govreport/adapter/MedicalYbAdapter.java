package com.zhiyangyun.care.govreport.adapter;

import com.zhiyangyun.care.govreport.entity.GovChannelConfig;
import com.zhiyangyun.care.govreport.entity.GovReportTask;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 医保结算上报适配器（沙箱实现）。医保多为异步回执，send 返回已受理但未终态(acked=false)，
 * 终态由 {@code /api/govreport/receipts/callback} 回调置入。
 */
@Component
public class MedicalYbAdapter implements ReportChannelAdapter {

  public static final String CHANNEL = "YB_MEDICAL";

  @Override
  public String channel() {
    return CHANNEL;
  }

  @Override
  public ReportSendResult send(GovReportTask task, String payloadJson, GovChannelConfig config) {
    if (config == null || !StringUtils.hasText(config.getEndpoint())) {
      return ReportSendResult.fail("医保上报渠道未配置 endpoint");
    }
    if (config.getEnabled() == null || config.getEnabled() != 1) {
      return ReportSendResult.fail("医保上报渠道未启用");
    }
    // TODO 正式环境：调用当地医保结算接口（电子凭证/结算清单），等待异步回执。
    String receipt = "YB-" + task.getId() + "-" + System.currentTimeMillis();
    return ReportSendResult.ok(receipt, false);
  }
}
