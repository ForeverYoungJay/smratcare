package com.zhiyangyun.care.logistics.model;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VideoPolicyRequest {
  @NotNull @Pattern(regexp = "UNDECIDED|LOCAL|OSS")
  private String provider;
  @NotNull @Min(90) @Max(3650)
  private Integer retentionDays;
  @NotNull
  private Boolean autoCleanup;
  @Size(max = 255)
  private String basis;
}
