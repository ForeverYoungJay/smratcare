package com.zhiyangyun.care.auth.model;

import lombok.Data;

@Data
public class FamilyLoginResponse {
  private String token;
  private Long familyUserId;
  private Long orgId;
  private String phone;
  private String realName;
}
