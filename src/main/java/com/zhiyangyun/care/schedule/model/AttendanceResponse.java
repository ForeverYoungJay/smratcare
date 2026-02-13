package com.zhiyangyun.care.schedule.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AttendanceResponse {
  private Long id;
  private Long staffId;
  private String staffName;
  private LocalDateTime checkInTime;
  private LocalDateTime checkOutTime;
  private String status;
}
