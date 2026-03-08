package com.zhiyangyun.care.oa.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class OaQuickChatEventBatchRequest {
  @Valid
  @NotEmpty
  private List<OaQuickChatEventItemRequest> events;
}
