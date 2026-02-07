package com.zhiyangyun.care.store.model;

import lombok.Data;

@Data
public class ForbiddenReason {
  private Long diseaseId;
  private String diseaseName;
  private Long tagId;
  private String tagCode;
  private String tagName;
}
