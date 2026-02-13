package com.zhiyangyun.care.elder.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomRequest {
  private Long tenantId;
  private Long orgId;
  @NotNull
  private Long buildingId;
  @NotNull
  private Long floorId;
  private String building;
  private String floorNo;
  @NotBlank
  private String roomNo;
  private String roomType;
  private Integer capacity = 1;
  private Integer status = 1;
  private String roomQrCode;
  private Long createdBy;
}
