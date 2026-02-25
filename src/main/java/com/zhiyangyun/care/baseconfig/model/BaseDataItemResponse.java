package com.zhiyangyun.care.baseconfig.model;

import lombok.Data;

@Data
public class BaseDataItemResponse {
  private Long id;
  private String configGroup;
  private String itemCode;
  private String itemName;
  private Integer status;
  private Integer sortNo;
  private String remark;
}
