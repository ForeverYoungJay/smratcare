package com.zhiyangyun.care.asset.model;

import lombok.Data;

@Data
public class BuildingResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String name;
  private String code;
  private Integer status;
  private Integer sortNo;
  private String remark;
}
