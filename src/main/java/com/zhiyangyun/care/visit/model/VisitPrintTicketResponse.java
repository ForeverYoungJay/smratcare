package com.zhiyangyun.care.visit.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class VisitPrintTicketResponse {
  private Long bookingId;
  private String ticketNo;
  private String elderName;
  private String familyName;
  private String floorNo;
  private String roomNo;
  private LocalDateTime visitTime;
  private Integer visitorCount;
  private String carPlate;
  private String statusText;
  private LocalDateTime generatedAt;
}
