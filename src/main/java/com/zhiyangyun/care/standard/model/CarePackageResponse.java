package com.zhiyangyun.care.standard.model;

import lombok.Data;

@Data
public class CarePackageResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String name;
  private String careLevel;
  private Integer cycleDays;
  private Integer enabled;
  private String remark;
}
