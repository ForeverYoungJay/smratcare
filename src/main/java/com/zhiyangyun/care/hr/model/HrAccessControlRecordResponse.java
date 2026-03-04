package com.zhiyangyun.care.hr.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrAccessControlRecordResponse {
  private Long id;
  private Long staffId;
  private String staffName;
  private LocalDateTime checkInTime;
  private LocalDateTime checkOutTime;
  private String attendanceStatus;
  private String accessResult;
}
