package com.zhiyangyun.care.crm.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CrmContractArchiveRuleResponse {
  private String ruleVersion;
  private String title;
  private String description;
  private List<String> requiredItems = new ArrayList<>();
  private List<String> stageNotes = new ArrayList<>();
  private LocalDateTime generatedAt;
}
