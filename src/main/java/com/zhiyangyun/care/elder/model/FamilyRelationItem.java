package com.zhiyangyun.care.elder.model;

import lombok.Data;

@Data
public class FamilyRelationItem {
  private Long id;
  private Long familyUserId;
  private String realName;
  private String phone;
  private String relation;
  private Integer isPrimary;
}
