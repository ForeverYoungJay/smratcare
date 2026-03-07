package com.zhiyangyun.care.hr.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrAttendanceRecordResponse {
  private Long id;
  private Long staffId;
  private String staffName;
  private LocalDateTime checkInTime;
  private LocalDateTime checkOutTime;
  private String status;
  private Boolean abnormal;
  private Integer reviewed;
  private String reviewRemark;
  private LocalDateTime reviewedAt;
}
