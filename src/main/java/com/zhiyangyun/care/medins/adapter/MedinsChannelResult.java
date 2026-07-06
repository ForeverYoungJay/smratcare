package com.zhiyangyun.care.medins.adapter;

import lombok.Data;

/** 医保渠道调用结果。 */
@Data
public class MedinsChannelResult {
  private boolean success;
  private String receiptCode;
  /** 渠道原始回执报文（JSON），留档核对。 */
  private String receiptJson;
  private String errorDetail;
  /** 是否已同步拿到终态回执（部分渠道异步回调/轮询）。 */
  private boolean acked;

  public static MedinsChannelResult ok(String receiptCode, boolean acked) {
    MedinsChannelResult r = new MedinsChannelResult();
    r.success = true;
    r.receiptCode = receiptCode;
    r.acked = acked;
    return r;
  }

  public static MedinsChannelResult ok(String receiptCode, boolean acked, String receiptJson) {
    MedinsChannelResult r = ok(receiptCode, acked);
    r.receiptJson = receiptJson;
    return r;
  }

  public static MedinsChannelResult fail(String errorDetail) {
    MedinsChannelResult r = new MedinsChannelResult();
    r.success = false;
    r.errorDetail = errorDetail;
    return r;
  }
}
