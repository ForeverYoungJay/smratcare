package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DiningDeliveryAreaRequest {
  @NotBlank
  private String areaCode;

  @NotBlank
  private String areaName;

  private String buildingName;

  private String floorNo;

  private String roomScope;

  private String managerName;

  private String status;

  private String remark;
}
