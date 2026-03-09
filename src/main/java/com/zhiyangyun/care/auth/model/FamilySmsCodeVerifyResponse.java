package com.zhiyangyun.care.auth.model;

import lombok.Data;

@Data
public class FamilySmsCodeVerifyResponse {
  private Boolean passed;
  private Integer remainingAttempts;
  private String message;
}
