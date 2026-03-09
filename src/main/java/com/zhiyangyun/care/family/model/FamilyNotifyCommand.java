package com.zhiyangyun.care.family.model;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

@Data
public class FamilyNotifyCommand {
  private Long orgId;
  private Long familyUserId;
  private Long elderId;
  private String eventType;
  private String level;
  private String title;
  private String content;
  private String pagePath;
  private Long createdBy;
  private Map<String, String> placeholders = new LinkedHashMap<>();
}
