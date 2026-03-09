package com.zhiyangyun.care.family.sms;

import lombok.Data;

@Data
public class FamilySmsSendCommand {
  private Long orgId;
  private Long familyUserId;
  private String phone;
  private String scene;
  private String code;
}
