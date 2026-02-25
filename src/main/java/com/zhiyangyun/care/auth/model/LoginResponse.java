package com.zhiyangyun.care.auth.model;

import java.util.List;
import lombok.Data;

@Data
public class LoginResponse {
  private String token;
  private StaffInfo staffInfo;
  private List<String> roles;
  private List<String> permissions;
}
