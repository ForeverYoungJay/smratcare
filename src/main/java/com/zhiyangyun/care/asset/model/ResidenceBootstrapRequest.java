package com.zhiyangyun.care.asset.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ResidenceBootstrapRequest {
  @Min(1)
  @Max(20)
  private Integer buildingCount = 1;

  @Min(1)
  @Max(20)
  private Integer floorsPerBuilding = 3;

  @Min(1)
  @Max(200)
  private Integer roomsPerFloor = 10;

  @Min(1)
  @Max(12)
  private Integer bedsPerRoom = 2;

  @Min(1)
  @Max(9999)
  private Integer startNo;

  private String buildingPrefix = "楼栋";
  private String floorPrefix = "F";
  private String roomPrefix = "R";
  private String bedPrefix = "B";
  private String roomType;
  private String bedType;
  private String templateCode;
}
