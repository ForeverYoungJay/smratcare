package com.zhiyangyun.care.store.model.admin;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProductTagRequest {
  private Long orgId;
  @Pattern(regexp = "^$|^[A-Za-z0-9_]+$", message = "标签编码仅允许字母/数字/下划线")
  private String tagCode;
  private String tagName;
  private String tagType;
  private Integer status;
}
