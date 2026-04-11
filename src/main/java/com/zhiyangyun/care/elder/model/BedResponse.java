package com.zhiyangyun.care.elder.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BedResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long roomId;
  private String bedNo;
  private String bedType;
  private String bedQrCode;
  private Integer status;
  private Long elderId;
  private String occupancySource;
  private String occupancyRefType;
  private Long occupancyRefId;
  private String occupancyRefNo;
  private LocalDateTime lockExpiresAt;
  private String occupancyNote;
  private String lastReleaseReason;
  private LocalDateTime lastReleasedAt;
  private String roomNo;
  private String building;
  private String buildingRemark;
  private String floorNo;
  private String roomType;
  private String roomRemark;
  private String areaCode;
  private String areaName;
  private String roomQrCode;
  private String elderName;
  private Integer elderGender;
  private String careLevel;
  private String riskLevel;
  private String riskLabel;
  private String riskSource;
  private Integer abnormalVital24hCount;
  private String latestAssessmentLevel;
  private LocalDate latestAssessmentDate;
}
