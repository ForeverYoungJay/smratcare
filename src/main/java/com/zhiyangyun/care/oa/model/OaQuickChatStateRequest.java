package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OaQuickChatStateRequest {
  @NotBlank
  private String stateJson;
}
