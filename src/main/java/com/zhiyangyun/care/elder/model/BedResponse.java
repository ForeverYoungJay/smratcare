package com.zhiyangyun.care.elder.model;

import java.time.LocalDate;
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
  private String roomNo;
  private String building;
  private String floorNo;
  private String roomType;
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
