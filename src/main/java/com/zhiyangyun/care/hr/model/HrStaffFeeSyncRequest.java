package com.zhiyangyun.care.hr.model;

import java.util.List;
import lombok.Data;

@Data
public class HrStaffFeeSyncRequest {
  private String feeType;
  private String month;
  private List<Long> ids;
}
