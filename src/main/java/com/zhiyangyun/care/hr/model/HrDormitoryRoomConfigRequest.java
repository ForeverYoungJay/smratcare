package com.zhiyangyun.care.hr.model;

import lombok.Data;

@Data
public class HrDormitoryRoomConfigRequest {
  private Long id;
  private String building;
  private String floorLabel;
  private String roomNo;
  private Integer bedCapacity;
  private String status;
  private Integer sortNo;
  private String remark;
}
