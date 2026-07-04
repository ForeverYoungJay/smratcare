package com.zhiyangyun.care.elder.model;

import lombok.Data;

@Data
public class FamilyElderItem {
  private Long elderId;
  private String elderName;
  private String relation;
  private Integer isPrimary;
  private Integer elderStatus;
  private String careLevel;
  private String admissionDate;
}
