package com.zhiyangyun.care.smart.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SmartDeviceRequest {
  private Long elderId;
  @NotBlank
  private String deviceCode;
  @NotBlank
  private String deviceName;
  @NotBlank
  private String deviceType;
  private String vendor;
  private String model;
  private String location;
  private String protocol;
  private String bindStatus;
  private Integer enabled;
  private String remark;
}
