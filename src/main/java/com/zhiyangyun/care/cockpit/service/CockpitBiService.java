package com.zhiyangyun.care.cockpit.service;

import com.zhiyangyun.care.cockpit.entity.StatsMetricDefinition;
import com.zhiyangyun.care.cockpit.model.CockpitBiDistributionResponse;
import com.zhiyangyun.care.cockpit.model.CockpitBiOrgCompareItem;
import com.zhiyangyun.care.cockpit.model.CockpitBiSummaryResponse;
import com.zhiyangyun.care.cockpit.model.CockpitBiTrendItem;
import java.time.YearMonth;
import java.util.List;

/**
 * 经营驾驶舱 BI 查询服务：只读三张每日汇总表（stats_daily_*），不直查业务库。
 */
public interface CockpitBiService {

  /** 总览卡片：本月关键指标 + 环比。 */
  CockpitBiSummaryResponse summary(Long orgId);

  /** 趋势：近 days 天（30/90）按日折线数据。 */
  List<CockpitBiTrendItem> trend(Long orgId, int days);

  /** 分布：护理等级分布 + 费用科目结构（最新一日汇总）。 */
  CockpitBiDistributionResponse distribution(Long orgId);

  /** 机构对比：指定月各机构关键指标（SYS_ADMIN 跨机构下钻）。 */
  List<CockpitBiOrgCompareItem> orgCompare(YearMonth month);

  /** 指标定义登记列表（口径治理）。 */
  List<StatsMetricDefinition> metricDefinitions();
}
