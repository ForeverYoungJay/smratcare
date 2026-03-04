package com.zhiyangyun.care.hr.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrPolicyAlertResponse {
  private Long documentId;
  private String name;
  private String folder;
  private String uploaderName;
  private LocalDateTime uploadedAt;
  private Long lastUpdatedDays;
  private String alertLevel;
}
