package com.zhiyangyun.care.cockpit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 经营驾驶舱每日经营指标快照。
 */
@Data
@TableName("cockpit_metric_snapshot")
public class CockpitMetricSnapshot {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private LocalDate statDate;

  private Integer totalBeds;
  private Integer occupiedBeds;
  private Integer availableBeds;
  private BigDecimal occupancyRate;

  private Integer residentCount;
  private Integer admissions;
  private Integer discharges;
  private Integer netChange;

  private BigDecimal revenueAmount;
  private BigDecimal paidAmount;
  private BigDecimal outstandingAmount;
  private BigDecimal collectionRate;

  private Integer alertTotal;
  private Integer alertResolved;
  private Integer alertTimeout;
  private BigDecimal alertRespRate;

  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private Integer isDeleted;
}
