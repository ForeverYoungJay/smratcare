package com.zhiyangyun.care.govreport.adapter;

import com.zhiyangyun.care.govreport.entity.GovChannelConfig;
import com.zhiyangyun.care.govreport.entity.GovReportTask;

/**
 * 监管/医保上报渠道适配器。不同渠道（民政金民、医保等）实现各自的报文封装与发送，
 * 上层服务只依赖本接口，便于跨地区/跨供应商扩展。
 */
public interface ReportChannelAdapter {

  /** 渠道编码，如 MZ_JINMIN / YB_MEDICAL。 */
  String channel();

  /** 发送已构建的上报报文，返回发送结果。 */
  ReportSendResult send(GovReportTask task, String payloadJson, GovChannelConfig config);
}
