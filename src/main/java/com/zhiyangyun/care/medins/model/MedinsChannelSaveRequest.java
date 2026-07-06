package com.zhiyangyun.care.medins.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 保存医保渠道配置请求（新增或编辑）。 */
@Data
public class MedinsChannelSaveRequest {
  /** 为空新增，否则编辑。 */
  private Long id;
  @NotBlank
  private String channel;
  private String regionCode;
  private String endpoint;
  private String appId;
  /** 密钥引用（密钥库键名），禁止填明文密钥。 */
  private String secretRef;
  private Integer enabled;
  private String remark;
}
