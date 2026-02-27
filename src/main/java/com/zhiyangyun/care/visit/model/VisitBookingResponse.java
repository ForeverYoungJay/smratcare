package com.zhiyangyun.care.visit.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class VisitBookingResponse {
  private Long id;
  private Long orgId;
  private Long elderId;
  private String elderName;
  private Long familyUserId;
  private String familyName;
  private String floorNo;
  private String roomNo;
  private LocalDate visitDate;
  private LocalDateTime visitTime;
  private String visitTimeSlot;
  private Integer visitorCount;
  private String carPlate;
  private Integer status;
  private String visitCode;
  private String remark;
}
