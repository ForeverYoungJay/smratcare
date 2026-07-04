package com.zhiyangyun.care.govreport.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 渠道回执回调。 */
@Data
public class GovReceiptCallbackRequest {
  @NotNull
  private Long taskId;
  private String receiptCode;
  /** ACKED 成功 / FAILED 失败。 */
  private String receiptStatus;
  private String errorDetail;
  /** 渠道签名，用于回调校验。 */
  private String sign;
}
