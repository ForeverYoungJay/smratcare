package com.zhiyangyun.care.auth.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StaffCreateRequest {
  private Long orgId;
  @NotNull
  private Long departmentId;
  @NotBlank
  private String staffNo;
  @NotBlank
  private String username;
  @NotBlank
  private String password;
  @NotBlank
  private String realName;
  private String phone;
  private String email;
  private Integer gender;
  private Integer status = 1;
}
