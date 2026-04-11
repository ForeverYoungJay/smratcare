package com.zhiyangyun.care.elder.model;

import lombok.Data;

@Data
public class OccupancyRepairRequest {
  private Long elderId;
  private Long bedId;
  private String reason;
}
