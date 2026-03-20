package com.zhiyangyun.care.asset.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResidenceBatchCommitRequest {
  @NotBlank
  private String previewToken;
}
