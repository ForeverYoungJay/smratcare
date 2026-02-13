package com.zhiyangyun.care.hr.model;

import lombok.Data;

@Data
public class StaffPointsAccountResponse {
  private Long staffId;
  private Integer pointsBalance;
  private Integer totalEarned;
  private Integer totalDeducted;
  private Integer status;
}
