package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OaKnowledgeRequest {
  @NotBlank
  private String title;

  private String category;

  private String tags;

  @NotBlank
  private String content;

  private Long authorId;

  private String authorName;

  private String status;

  private String remark;
}
