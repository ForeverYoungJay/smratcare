package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class OaQuickChatEventItemRequest {
  @NotBlank
  private String eventType;

  private String roomId;

  private Map<String, Object> room;

  private Map<String, Object> message;

  private Map<String, Object> meta;

  private List<String> targetStaffIds;
}
