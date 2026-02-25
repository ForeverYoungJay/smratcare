package com.zhiyangyun.care.health.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HealthDrugDictionaryRequest {
  private String drugCode;

  @NotBlank
  private String drugName;

  private String specification;

  private String unit;

  private String manufacturer;

  private String category;

  private String remark;
}
