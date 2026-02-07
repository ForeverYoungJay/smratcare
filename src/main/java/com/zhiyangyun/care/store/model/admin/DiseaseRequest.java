package com.zhiyangyun.care.store.model.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DiseaseRequest {
  private Long orgId;
  @NotBlank
  private String diseaseCode;
  @NotBlank
  private String diseaseName;
  private String remark;
}
