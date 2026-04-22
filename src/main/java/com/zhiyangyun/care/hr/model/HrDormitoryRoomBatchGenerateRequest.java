package com.zhiyangyun.care.hr.model;

import lombok.Data;

@Data
public class HrDormitoryRoomBatchGenerateRequest {
  private String buildingPrefix;
  private String buildingNamingType;
  private Integer buildingStartNo;
  private Integer buildingCount;
  private Integer floorStartNo;
  private Integer floorCount;
  private Integer roomsPerFloor;
  private Integer roomStartNo;
  private Integer roomNoWidth;
  private Integer bedCapacity;
  private String roomNoSeparator;
  private String status;
  private String remark;
}
