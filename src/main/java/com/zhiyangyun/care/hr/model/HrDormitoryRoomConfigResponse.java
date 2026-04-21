package com.zhiyangyun.care.hr.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrDormitoryRoomConfigResponse {
  private Long id;
  private String building;
  private String floorLabel;
  private String roomNo;
  private Integer bedCapacity;
  private String status;
  private Integer sortNo;
  private String remark;
  private LocalDateTime updateTime;
}
