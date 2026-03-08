package com.zhiyangyun.care.auth.model;

import java.util.List;
import lombok.Data;

@Data
public class StaffOptionResponse {
  private Long id;
  private String username;
  private String realName;
  private Integer status;
  private Long departmentId;
  private Long orgId;
  private String staffNo;
  private String phone;
  private List<String> roleCodes;
}
