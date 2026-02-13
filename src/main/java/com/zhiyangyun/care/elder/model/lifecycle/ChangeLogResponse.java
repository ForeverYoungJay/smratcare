package com.zhiyangyun.care.elder.model.lifecycle;

import lombok.Data;

@Data
public class ChangeLogResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private String elderName;
  private String changeType;
  private String beforeValue;
  private String afterValue;
  private String reason;
  private String createTime;
}
