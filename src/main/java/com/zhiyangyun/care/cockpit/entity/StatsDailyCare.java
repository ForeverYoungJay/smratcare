package com.zhiyangyun.care.cockpit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 经营驾驶舱 BI：每日护理与安全汇总（按 org+日期预聚合）。
 */
@Data
@TableName("stats_daily_care")
public class StatsDailyCare {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private LocalDate statDate;

  private Integer careTaskTotal;
  private Integer careTaskDone;
  private BigDecimal careTaskCompletionRate;

  private Integer scheduledStaffCount;
  private BigDecimal scheduledHours;
  /** 人效工时 = 排班总工时 / 排班员工数 */
  private BigDecimal hoursPerStaff;

  private Integer alertTotal;
  /** 已响应且未超时升级的告警数 */
  private Integer alertOnTime;
  private BigDecimal alertOnTimeRate;

  private BigDecimal satisfactionScore;
  private Integer satisfactionCount;

  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private Integer isDeleted;
}
