package com.zhiyangyun.care.hr.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrDormitoryStaffResponse {
  private Long staffId;
  private String staffNo;
  private String staffName;
  private Long departmentId;
  private String departmentName;
  private Integer status;
  private Integer liveInDormitory;
  private String dormitoryBuilding;
  private String dormitoryRoomNo;
  private String dormitoryBedNo;
  private String dormitoryLabel;
  private String meterNo;
  private String mealPlanSummary;
  private String remark;
  private Boolean occupancyConflict;
  private LocalDateTime updateTime;
}
