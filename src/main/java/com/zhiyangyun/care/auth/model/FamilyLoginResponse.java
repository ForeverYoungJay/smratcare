package com.zhiyangyun.care.auth.model;

import lombok.Data;

@Data
public class FamilyLoginResponse {
  private String token;
  private Long familyUserId;
  private Long orgId;
  private String phone;
  private String realName;
  /** 是否为本次登录自动创建的新账号（提示前端引导绑定长者）。 */
  private Boolean newAccount;
}
