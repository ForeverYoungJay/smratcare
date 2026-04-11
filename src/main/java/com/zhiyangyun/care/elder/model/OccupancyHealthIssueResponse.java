package com.zhiyangyun.care.elder.model;

import lombok.Data;

@Data
public class OccupancyHealthIssueResponse {
  private String issueType;
  private String severity;
  private Long elderId;
  private String elderName;
  private Long bedId;
  private String bedNo;
  private Long relationId;
  private String detail;
  private String suggestion;
}
