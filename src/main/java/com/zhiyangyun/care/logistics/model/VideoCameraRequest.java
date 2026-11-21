package com.zhiyangyun.care.logistics.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class VideoCameraRequest {
  @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9_-]+")
  private String cameraCode;
  @NotBlank @Size(max = 128)
  private String cameraName;
  @NotBlank @Size(max = 128)
  private String location;
  @NotBlank @Size(max = 128) @Pattern(regexp = "[A-Za-z0-9_./-]+")
  private String channelRef;
  @NotNull @Pattern(regexp = "RTSP|ONVIF|GB28181|NVR")
  private String protocol;
  @Size(max = 64)
  private String responsibleName;
  @NotNull @Min(0) @Max(1)
  private Integer enabled;
  @NotNull @DecimalMin("0.1") @DecimalMax("100") @Digits(integer = 3, fraction = 2)
  private BigDecimal bitrateMbps;
}
