package com.zhiyangyun.care.auth.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentRequest {
  private Long orgId;
  private Long parentId;
  @NotBlank
  private String deptName;
  private String deptCode;
  private Integer sortNo = 0;
  private Integer status = 1;
}
