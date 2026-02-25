package com.zhiyangyun.care.nursing.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ServiceBookingResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private String elderName;
  private Long planId;
  private String planName;
  private Long serviceItemId;
  private String serviceItemName;
  private LocalDateTime bookingTime;
  private Integer expectedDuration;
  private Long assignedStaffId;
  private String assignedStaffName;
  private String source;
  private String status;
  private String cancelReason;
  private String remark;
}
