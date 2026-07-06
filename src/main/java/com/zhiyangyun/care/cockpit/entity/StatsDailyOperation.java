package com.zhiyangyun.care.cockpit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 经营驾驶舱 BI：每日运营汇总（按 org+日期预聚合）。
 */
@Data
@TableName("stats_daily_operation")
public class StatsDailyOperation {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private LocalDate statDate;

  private Integer residentCount;
  private Integer totalBeds;
  private Integer occupiedBeds;
  private BigDecimal occupancyRate;
  private BigDecimal bedTurnoverRate;
  private Integer admissions;
  private Integer discharges;
  private BigDecimal avgStayDays;
  /** 在住长者护理等级分布 JSON：{"一级":10,...} */
  private String careLevelJson;

  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private Integer isDeleted;
}
