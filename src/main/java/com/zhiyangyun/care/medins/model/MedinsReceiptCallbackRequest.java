package com.zhiyangyun.care.medins.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 医保渠道回执回调请求。 */
@Data
public class MedinsReceiptCallbackRequest {
  @NotNull
  private Long taskId;
  private String receiptCode;
  /** ACKED 已回执 / REJECTED 驳回。 */
  private String receiptStatus;
  /** 渠道原始回执报文（JSON）。 */
  private String receiptJson;
  private String errorDetail;
  /** 渠道签名，用于回调校验。 */
  private String sign;
}
