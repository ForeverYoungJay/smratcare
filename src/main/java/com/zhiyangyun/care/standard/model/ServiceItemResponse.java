package com.zhiyangyun.care.standard.model;

import lombok.Data;

@Data
public class ServiceItemResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String name;
  private String category;
  private Integer defaultDuration;
  private Integer defaultPoints;
  private Integer enabled;
  private String remark;
}
