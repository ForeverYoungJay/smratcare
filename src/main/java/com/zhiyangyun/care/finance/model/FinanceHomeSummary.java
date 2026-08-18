package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** 工作台首页四张卡 + 最近操作记录。 */
@Data
public class FinanceHomeSummary {
  private String billMonth;

  /** 在住老人数。 */
  private Integer residentCount = 0;
  private Integer bedTotal = 0;
  private Integer occupiedBedCount = 0;

  /** 代养费结清率：按账单条数。 */
  private BigDecimal careFeeSettleRate = BigDecimal.ZERO;
  private Integer billCount = 0;
  private Integer settledBillCount = 0;
  /** 按金额的回款率，作为条数口径的补充。 */
  private BigDecimal careFeeAmountRate = BigDecimal.ZERO;
  private BigDecimal billTotalAmount = BigDecimal.ZERO;
  private BigDecimal billOutstandingAmount = BigDecimal.ZERO;

  /** 押金预警：未缴清人数与合计差额。 */
  private Integer depositShortfallCount = 0;
  private BigDecimal depositShortfallAmount = BigDecimal.ZERO;
  private BigDecimal depositTotalBalance = BigDecimal.ZERO;

  /** 电费未缴房间。 */
  private Integer electricityUnpaidRoomCount = 0;
  private BigDecimal electricityUnpaidFee = BigDecimal.ZERO;
  private Integer electricityUnrecordedRoomCount = 0;

  private List<RecentOperation> recentOperations = new ArrayList<>();

  @Data
  public static class RecentOperation {
    private Long id;
    private String actorName;
    private String actionType;
    private String actionTypeText;
    private String entityType;
    private String detail;
    private LocalDateTime createTime;
  }
}
