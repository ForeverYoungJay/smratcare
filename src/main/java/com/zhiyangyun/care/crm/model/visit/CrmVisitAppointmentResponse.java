package com.zhiyangyun.care.crm.model.visit;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrmVisitAppointmentResponse {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long id;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long leadId;
  private String leadName;
  private String elderName;
  private LocalDateTime appointmentTime;
  private String visitorName;
  private String visitorPhone;
  private Integer partySize;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long ownerStaffId;
  private String ownerStaffName;
  private String status;
  private LocalDateTime arrivedAt;
  private LocalDateTime completedAt;
  private String visitResult;
  private String cancelReason;
  private LocalDate nextFollowDate;
  private String remark;
  private LocalDateTime createTime;
}
