package com.zhiyangyun.care.smart.model;

import lombok.Data;

@Data
public class SmartAlertResolveRequest {
  private String resolutionNote;
  private Boolean notifyFamily;
}
