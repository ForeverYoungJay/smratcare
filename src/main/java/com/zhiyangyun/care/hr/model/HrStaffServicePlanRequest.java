package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class HrStaffServicePlanRequest {
  private Long staffId;
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
  private String remark;
}
