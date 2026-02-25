package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DiningPrepZoneRequest {
  @NotBlank
  private String zoneCode;

  @NotBlank
  private String zoneName;

  private String kitchenArea;

  private Integer capacity;

  private String managerName;

  private String status;

  private String remark;
}
