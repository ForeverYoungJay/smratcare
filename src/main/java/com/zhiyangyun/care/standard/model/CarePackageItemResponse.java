package com.zhiyangyun.care.standard.model;

import lombok.Data;

@Data
public class CarePackageItemResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long packageId;
  private Long itemId;
  private Integer frequencyPerDay;
  private Integer enabled;
  private Integer sortNo;
  private String remark;
  private String itemName;
}
