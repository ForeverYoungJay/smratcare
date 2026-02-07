package com.zhiyangyun.care.store.model.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductTagRequest {
  private Long orgId;
  @NotBlank
  private String tagCode;
  @NotBlank
  private String tagName;
  private String tagType;
}
