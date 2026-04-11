package com.zhiyangyun.care.auth.model;

import lombok.Data;

@Data
public class RolePagePermissionSnapshot {
  private String roleCode;
  private String routePermissionsJson;
}
