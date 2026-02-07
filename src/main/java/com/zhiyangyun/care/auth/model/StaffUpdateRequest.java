package com.zhiyangyun.care.auth.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StaffUpdateRequest {
  @NotNull
  private Long id;
  private Long departmentId;
  private String password;
  private String realName;
  private String phone;
  private String email;
  private Integer gender;
  private Integer status;
}
