package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrStaffServicePlanResponse {
  private Long id;
  private Long staffId;
  private String staffNo;
  private String staffName;
  private Long departmentId;
  private String departmentName;
  private Integer breakfastEnabled;
  private BigDecimal breakfastUnitPrice;
  private Integer breakfastDaysPerMonth;
  private Integer lunchEnabled;
  private BigDecimal lunchUnitPrice;
  private Integer lunchDaysPerMonth;
  private Integer dinnerEnabled;
  private BigDecimal dinnerUnitPrice;
  private Integer dinnerDaysPerMonth;
  private Integer liveInDormitory;
  private String dormitoryBuilding;
  private String dormitoryRoomNo;
  private String dormitoryBedNo;
  private String meterNo;
  private String status;
  private String mealPlanSummary;
  private String remark;
  private LocalDateTime updateTime;
}
