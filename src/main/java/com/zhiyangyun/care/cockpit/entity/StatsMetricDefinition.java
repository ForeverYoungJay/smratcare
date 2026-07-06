package com.zhiyangyun.care.cockpit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 经营驾驶舱指标定义登记（口径治理）。org_id 为空表示平台级统一口径。
 */
@Data
@TableName("stats_metric_definition")
public class StatsMetricDefinition {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;

  private String metricCode;
  private String metricName;
  /** 指标分类：OPERATION运营 / FINANCE财务 / CARE护理安全 */
  private String metricCategory;
  private String unit;
  /** 口径说明 */
  private String caliberDesc;
  /** 计算SQL摘要（口径追溯用，非可执行SQL） */
  private String calcSqlSummary;
  private String sourceTable;
  private Integer status;

  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private Integer isDeleted;
}
