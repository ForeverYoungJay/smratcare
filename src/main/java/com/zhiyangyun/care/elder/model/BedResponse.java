package com.zhiyangyun.care.elder.model;

import lombok.Data;

@Data
public class BedResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long roomId;
  private String bedNo;
  private String bedQrCode;
  private Integer status;
  private Long elderId;
  private String roomNo;
  private String building;
  private String floorNo;
  private String roomQrCode;
  private String elderName;
  private String careLevel;
}
