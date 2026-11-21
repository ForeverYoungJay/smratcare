package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import lombok.Data;

/** 押金汇总，供押金页统计卡与首页「押金预警」使用。 */
@Data
public class DepositSummary {
  private Integer accountCount = 0;
  /** 未缴清人数（应缴差额 > 0）。 */
  private Integer shortfallCount = 0;
  private BigDecimal shortfallAmount = BigDecimal.ZERO;
  private BigDecimal totalBalance = BigDecimal.ZERO;
  private BigDecimal totalPaid = BigDecimal.ZERO;
  private BigDecimal totalDeducted = BigDecimal.ZERO;
  private BigDecimal totalRefunded = BigDecimal.ZERO;
}
