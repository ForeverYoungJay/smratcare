package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OaSuggestionHandleRequest {
  private String status;

  @Size(max = 500, message = "处理说明不能超过500字")
  private String reply;
}
