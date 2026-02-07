package com.zhiyangyun.care.elder.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BedRequest {
  private Long orgId;
  @NotNull
  private Long roomId;
  @NotBlank
  private String bedNo;
  private Integer status = 1;
}
