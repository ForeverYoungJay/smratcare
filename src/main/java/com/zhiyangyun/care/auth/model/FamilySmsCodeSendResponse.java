package com.zhiyangyun.care.auth.model;

import lombok.Data;

@Data
public class FamilySmsCodeSendResponse {
  private String bizNo;
  private Integer expireSeconds;
  private Integer retryAfterSeconds;
  private String provider;
  private String message;
  private String debugCode;
}
