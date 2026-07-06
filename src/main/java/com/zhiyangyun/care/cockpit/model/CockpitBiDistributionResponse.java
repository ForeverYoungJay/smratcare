package com.zhiyangyun.care.cockpit.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.Data;

/**
 * 经营驾驶舱 BI 分布：护理等级分布 + 费用科目结构（取最新一日汇总）。
 */
@Data
public class CockpitBiDistributionResponse {
  private Long orgId;
  private LocalDate statDate;
  /** 在住长者护理等级分布：等级 -> 人数 */
  private Map<String, Integer> careLevelDist;
  /** 当月费用科目结构：科目 -> 金额 */
  private Map<String, BigDecimal> costStructure;
}
