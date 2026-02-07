package com.zhiyangyun.care.elder.model;

import lombok.Data;

@Data
public class RoomResponse {
  private Long id;
  private Long orgId;
  private String building;
  private String floorNo;
  private String roomNo;
  private String roomType;
  private Integer capacity;
  private Integer status;
}
