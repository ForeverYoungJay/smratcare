package com.zhiyangyun.care.elder.model;

import com.zhiyangyun.care.compliance.annotation.SensitiveField;
import com.zhiyangyun.care.compliance.annotation.SensitiveType;
import lombok.Data;

@Data
public class FamilyRelationItem {
  private Long id;
  private Long familyUserId;
  private String realName;
  @SensitiveField(type = SensitiveType.PHONE)
  private String phone;
  @SensitiveField(type = SensitiveType.ID_CARD)
  private String idCardNo;
  private String relation;
  private Integer isPrimary;
}
