package com.zhiyangyun.care.cockpit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 经营驾驶舱 BI：每日财务汇总（按 org+日期预聚合）。
 */
@Data
@TableName("stats_daily_finance")
public class StatsDailyFinance {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private LocalDate statDate;

  /** 当月账单应收累计（统计日所属账单月） */
  private BigDecimal revenueAmount;
  /** 当月已回款累计 */
  private BigDecimal paidAmount;
  /** 当日回款（payment_record.paid_at 落在统计日） */
  private BigDecimal paidDailyAmount;
  /** 当月欠费累计 */
  private BigDecimal outstandingAmount;
  private BigDecimal collectionRate;
  /** 当月费用科目结构 JSON：{"BED_FEE":12000.00,...} */
  private String costStructureJson;

  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private Integer isDeleted;
}
