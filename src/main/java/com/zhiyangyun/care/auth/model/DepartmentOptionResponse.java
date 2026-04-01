package com.zhiyangyun.care.auth.model;

import lombok.Data;

@Data
public class DepartmentOptionResponse {
  private Long id;
  private String deptName;
  private Integer sortNo;
  private Long orgId;
  private Integer status;
}
