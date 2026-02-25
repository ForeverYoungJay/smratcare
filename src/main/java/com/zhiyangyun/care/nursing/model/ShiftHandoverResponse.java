package com.zhiyangyun.care.nursing.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ShiftHandoverResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private LocalDate dutyDate;
  private String shiftCode;
  private Long fromStaffId;
  private String fromStaffName;
  private Long toStaffId;
  private String toStaffName;
  private String summary;
  private String riskNote;
  private String todoNote;
  private String status;
  private LocalDateTime handoverTime;
  private LocalDateTime confirmTime;
}
