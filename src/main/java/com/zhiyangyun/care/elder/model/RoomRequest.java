package com.zhiyangyun.care.elder.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoomRequest {
  private Long orgId;
  private String building;
  private String floorNo;
  @NotBlank
  private String roomNo;
  private String roomType;
  private Integer capacity = 1;
  private Integer status = 1;
}
