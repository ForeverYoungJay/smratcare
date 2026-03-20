package com.zhiyangyun.care.asset.model;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

@Data
public class ResidenceBatchPreviewItem {
  private String entityType;
  private String action;
  private String identifier;
  private String parentIdentifier;
  private String displayName;
  private String reason;
  private Boolean safeOverwrite = false;
  private Map<String, Object> payload = new LinkedHashMap<>();
}
