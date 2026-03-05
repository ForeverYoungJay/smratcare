package com.zhiyangyun.care.visit.model;

import lombok.Data;

@Data
public class VisitCheckInRequest {
  private Long orgId;
  private Long bookingId;
  private String visitCode;
  private String remark;
}
