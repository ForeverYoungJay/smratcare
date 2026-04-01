package com.zhiyangyun.care.auth.model;

import lombok.Data;

@Data
public class RoleRequest {
  private Long orgId;
  private Long departmentId;
  private Long superiorRoleId;
  private String roleName;
  private String roleCode;
  private String routePermissionsJson;
  private Integer status = 1;
}
