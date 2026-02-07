package com.zhiyangyun.care.visit.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class VisitCheckInResponse {
  private Long bookingId;
  private Long guardStaffId;
  private LocalDateTime checkTime;
  private Integer status;
}
