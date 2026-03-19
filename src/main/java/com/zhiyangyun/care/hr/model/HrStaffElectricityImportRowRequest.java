package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class HrStaffElectricityImportRowRequest {
  private Long staffId;
  private String staffNo;
  private String staffName;
  private BigDecimal amount;
  private String dormitoryBuilding;
  private String dormitoryRoomNo;
  private String dormitoryBedNo;
  private String meterNo;
  private String remark;
}
