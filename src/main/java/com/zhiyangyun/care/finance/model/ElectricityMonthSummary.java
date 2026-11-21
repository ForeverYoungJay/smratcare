package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import lombok.Data;

/** 电费月度汇总，供电费页统计卡与首页「电费未缴房间」使用。 */
@Data
public class ElectricityMonthSummary {
  private String billMonth;
  private BigDecimal unitPrice = BigDecimal.ZERO;
  private Integer roomCount = 0;
  private Integer recordedRoomCount = 0;
  private Integer unrecordedRoomCount = 0;
  private Integer unpaidRoomCount = 0;
  private BigDecimal totalUsage = BigDecimal.ZERO;
  private BigDecimal totalFee = BigDecimal.ZERO;
  private BigDecimal unpaidFee = BigDecimal.ZERO;
}
