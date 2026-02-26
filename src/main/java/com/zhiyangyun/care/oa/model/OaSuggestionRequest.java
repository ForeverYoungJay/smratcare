package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OaSuggestionRequest {
  @NotBlank(message = "content 不能为空")
  @Size(max = 1000, message = "content 长度不能超过1000")
  private String content;

  @Size(max = 64, message = "proposerName 长度不能超过64")
  private String proposerName;

  @Size(max = 64, message = "contact 长度不能超过64")
  private String contact;
}
