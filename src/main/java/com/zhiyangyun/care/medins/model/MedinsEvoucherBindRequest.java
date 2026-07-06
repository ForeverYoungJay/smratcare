package com.zhiyangyun.care.medins.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 长者医保电子凭证绑定登记请求。 */
@Data
public class MedinsEvoucherBindRequest {
  @NotNull
  private Long elderId;
  private String insuredNo;
  private String idCard;
  /** 电子凭证授权令牌引用，不传敏感原文。 */
  private String ecardToken;
  /** 校验渠道编码，默认 MOCK。 */
  private String channel;
  private String remark;
}
