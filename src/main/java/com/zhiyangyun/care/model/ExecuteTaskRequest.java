package com.zhiyangyun.care.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExecuteTaskRequest {
  @NotNull
  private Long taskDailyId;

  @NotNull
  private Long staffId;

  @NotBlank
  private String bedQrCode;

  private String remark;
}
