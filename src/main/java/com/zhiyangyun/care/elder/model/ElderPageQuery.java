package com.zhiyangyun.care.elder.model;

import lombok.Data;

@Data
public class ElderPageQuery {
  private Long tenantId;
  private long pageNo = 1;
  private long pageSize = 20;
  private String keyword;
}
