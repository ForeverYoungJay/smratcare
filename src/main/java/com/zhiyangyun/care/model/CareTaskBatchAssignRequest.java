package com.zhiyangyun.care.model;

import lombok.Data;

@Data
public class CareTaskBatchAssignRequest {
  private Long staffId;

  private String dateFrom;

  private String dateTo;

  private String roomNo;

  private String floorNo;

  private String building;

  private String careLevel;

  private String status;

  private Boolean force;
}
