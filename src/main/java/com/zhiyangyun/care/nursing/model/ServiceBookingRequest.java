package com.zhiyangyun.care.nursing.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ServiceBookingRequest {
  private Long tenantId;
  private Long orgId;
  @NotNull
  private Long elderId;
  private Long planId;
  @NotNull
  private Long serviceItemId;
  @NotNull
  private LocalDateTime bookingTime;
  private Integer expectedDuration;
  private Long assignedStaffId;
  private String source = "MANUAL";
  private String status = "BOOKED";
  private String cancelReason;
  private String remark;
  private Long createdBy;
}
