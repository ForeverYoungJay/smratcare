package com.zhiyangyun.care.auth.model;

import lombok.Data;

@Data
public class StaffInfo {
  private Long id;
  private Long orgId;
  private Long departmentId;
  private String staffNo;
  private String username;
  private String realName;
  private String phone;
  private Long directLeaderId;
  private Long indirectLeaderId;
  private Integer status;
}
