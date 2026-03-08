package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

@Data
public class OaQuickChatFanoutRequest {
  @NotBlank
  private String stateJson;

  private List<Long> targetStaffIds;
}
