package com.zhiyangyun.care.govreport.adapter;

import com.zhiyangyun.care.govreport.entity.GovChannelConfig;
import com.zhiyangyun.care.govreport.entity.GovReportTask;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 民政"金民工程"全国养老服务信息系统上报适配器（沙箱实现）。
 *
 * <p>正式环境应在此发起对民政上报接口的真实调用（HTTP/加签）；当前实现校验配置有效性后
 * 返回同步成功回执，用于打通链路与联调。
 */
@Component
public class JinminMzAdapter implements ReportChannelAdapter {

  public static final String CHANNEL = "MZ_JINMIN";

  @Override
  public String channel() {
    return CHANNEL;
  }

  @Override
  public ReportSendResult send(GovReportTask task, String payloadJson, GovChannelConfig config) {
    if (config == null || !StringUtils.hasText(config.getEndpoint())) {
      return ReportSendResult.fail("民政上报渠道未配置 endpoint");
    }
    if (config.getEnabled() == null || config.getEnabled() != 1) {
      return ReportSendResult.fail("民政上报渠道未启用");
    }
    // TODO 正式环境：按 config.endpoint/app_id/secret_ref 发起真实上报并加签。
    String receipt = "MZ-" + task.getId() + "-" + System.currentTimeMillis();
    return ReportSendResult.ok(receipt, true);
  }
}
