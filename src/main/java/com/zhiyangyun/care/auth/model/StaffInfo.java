package com.zhiyangyun.care.auth.model;

import lombok.Data;

@Data
public class StaffInfo {
  private Long id;
  private Long orgId;
  private Long departmentId;
  private String username;
  private String realName;
  private String phone;
  private Integer status;
}
