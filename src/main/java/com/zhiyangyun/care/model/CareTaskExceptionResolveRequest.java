package com.zhiyangyun.care.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CareTaskExceptionResolveRequest {
  @NotNull
  private Long taskDailyId;

  private Long staffId;

  @Min(1)
  @Max(5)
  private Integer score = 5;

  @Size(max = 500, message = "处理说明不能超过500字")
  private String resolution;
}
