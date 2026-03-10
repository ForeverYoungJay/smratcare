package com.zhiyangyun.care.auth.model;

import lombok.Data;

@Data
public class FamilyAuthBootstrapResponse {
  private Long orgId;
  private String orgName;
  private String loginMode;
  private Boolean registerRequireSms;
  private Boolean resetPasswordRequireSms;
}
