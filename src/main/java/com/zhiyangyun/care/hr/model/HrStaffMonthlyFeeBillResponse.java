package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrStaffMonthlyFeeBillResponse {
  private Long id;
  private Long staffId;
  private String staffNo;
  private String staffName;
  private Long departmentId;
  private String departmentName;
  private String feeMonth;
  private String feeType;
  private String title;
  private BigDecimal quantity;
  private BigDecimal unitPrice;
  private BigDecimal amount;
  private String status;
  private String financeSyncStatus;
  private Long financeSyncId;
  private LocalDateTime financeSyncAt;
  private String financeSyncByName;
  private String dormitoryBuilding;
  private String dormitoryRoomNo;
  private String dormitoryBedNo;
  private String meterNo;
  private String mealPlanSummary;
  private String detailSummary;
  private String remark;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
