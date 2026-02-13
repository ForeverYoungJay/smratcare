package com.zhiyangyun.care.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CareTaskCreateRequest {
  @NotNull
  private Long elderId;

  private Long templateId;

  private String taskName;

  @NotNull
  private String planTime;

  private Long staffId;

  private String status;
}
