package com.zhiyangyun.care.asset.model;

import lombok.Data;

@Data
public class FloorResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long buildingId;
  private String floorNo;
  private String name;
  private Integer status;
  private Integer sortNo;
}
