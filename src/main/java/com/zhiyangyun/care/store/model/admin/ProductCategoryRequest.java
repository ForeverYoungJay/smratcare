package com.zhiyangyun.care.store.model.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProductCategoryRequest {
  @NotBlank
  @Pattern(regexp = "^[A-Za-z0-9_\\-]+$", message = "分类编码仅允许字母/数字/下划线/中划线")
  private String categoryCode;

  @NotBlank
  private String categoryName;

  private Integer status;

  private String remark;
}
