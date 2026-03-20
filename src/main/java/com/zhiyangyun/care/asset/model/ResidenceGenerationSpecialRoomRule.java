package com.zhiyangyun.care.asset.model;

import lombok.Data;

@Data
public class ResidenceGenerationSpecialRoomRule {
  private Integer floorNo;
  private String roomNo;
  private String usageType;
  private String roomType;
  private Integer bedCount;
  private Boolean skipBedGeneration = false;
  private String genderLimit;
}
