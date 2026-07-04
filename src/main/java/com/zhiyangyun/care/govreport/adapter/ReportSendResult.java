package com.zhiyangyun.care.govreport.adapter;

import lombok.Data;

/** 渠道发送结果。 */
@Data
public class ReportSendResult {
  private boolean success;
  private String receiptCode;
  private String errorDetail;
  /** 是否已同步拿到终态回执（部分渠道异步回调）。 */
  private boolean acked;

  public static ReportSendResult ok(String receiptCode, boolean acked) {
    ReportSendResult r = new ReportSendResult();
    r.success = true;
    r.receiptCode = receiptCode;
    r.acked = acked;
    return r;
  }

  public static ReportSendResult fail(String errorDetail) {
    ReportSendResult r = new ReportSendResult();
    r.success = false;
    r.errorDetail = errorDetail;
    return r;
  }
}
