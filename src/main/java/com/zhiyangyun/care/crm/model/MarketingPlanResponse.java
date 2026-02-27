package com.zhiyangyun.care.crm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MarketingPlanResponse {
  private Long id;
  private String moduleType;
  private String title;
  private String summary;
  private String content;
  private String quarterLabel;
  private String target;
  private String owner;
  private Integer priority;
  private String status;
  private LocalDate effectiveDate;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
