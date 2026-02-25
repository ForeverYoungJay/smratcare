package com.zhiyangyun.care.oa.model;

import lombok.Data;

@Data
public class OaTaskTrendItem {
  private String period;
  private Long totalCount;
  private Long doneCount;
  private Long openCount;
  private Long highPriorityCount;
}
