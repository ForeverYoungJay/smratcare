package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OaDocumentFolderRequest {
  @NotBlank
  private String name;

  private Long parentId;

  private Integer sortNo;

  private String status;

  private String remark;
}
