package com.zhiyangyun.care.cockpit.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.cockpit.entity.StatsMetricDefinition;
import com.zhiyangyun.care.cockpit.model.CockpitBiDistributionResponse;
import com.zhiyangyun.care.cockpit.model.CockpitBiOrgCompareItem;
import com.zhiyangyun.care.cockpit.model.CockpitBiSummaryResponse;
import com.zhiyangyun.care.cockpit.model.CockpitBiTrendItem;
import com.zhiyangyun.care.cockpit.service.CockpitBiService;
import com.zhiyangyun.care.cockpit.service.CockpitDailyStatsService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 经营驾驶舱 BI 接口（领域D）：只查 stats_daily_* 预聚合汇总表，不直查业务库。
 * 仅院长/超管可见；机构对比跨机构下钻仅 SYS_ADMIN。
 */
@RestController
@RequestMapping("/stats/executive/bi")
public class CockpitBiController {

  private final CockpitBiService cockpitBiService;
  private final CockpitDailyStatsService cockpitDailyStatsService;

  public CockpitBiController(CockpitBiService cockpitBiService,
      CockpitDailyStatsService cockpitDailyStatsService) {
    this.cockpitBiService = cockpitBiService;
    this.cockpitDailyStatsService = cockpitDailyStatsService;
  }

  /** 总览卡片：本月关键指标 + 环比。orgId 缺省取当前登录机构（SYS_ADMIN 可指定）。 */
  @GetMapping("/summary")
  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN')")
  public Result<CockpitBiSummaryResponse> summary(@RequestParam(required = false) Long orgId) {
    return Result.ok(cockpitBiService.summary(resolveOrgId(orgId)));
  }

  /** 趋势折线：近 days 天（默认30，最大180）。 */
  @GetMapping("/trend")
  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN')")
  public Result<List<CockpitBiTrendItem>> trend(
      @RequestParam(required = false) Long orgId,
      @RequestParam(defaultValue = "30") int days) {
    return Result.ok(cockpitBiService.trend(resolveOrgId(orgId), days));
  }

  /** 分布：护理等级分布 + 费用科目结构（最新一日汇总）。 */
  @GetMapping("/distribution")
  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN')")
  public Result<CockpitBiDistributionResponse> distribution(
      @RequestParam(required = false) Long orgId) {
    return Result.ok(cockpitBiService.distribution(resolveOrgId(orgId)));
  }

  /** 机构对比：指定月各机构关键指标（跨机构下钻，仅超管）。month 格式 yyyy-MM，缺省当月。 */
  @GetMapping("/org-compare")
  @PreAuthorize("hasRole('SYS_ADMIN')")
  public Result<List<CockpitBiOrgCompareItem>> orgCompare(
      @RequestParam(required = false) String month) {
    YearMonth target = month == null || month.isBlank() ? null : YearMonth.parse(month);
    return Result.ok(cockpitBiService.orgCompare(target));
  }

  /** 指标定义登记列表（口径治理）。 */
  @GetMapping("/metric-definitions")
  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN')")
  public Result<List<StatsMetricDefinition>> metricDefinitions() {
    return Result.ok(cockpitBiService.metricDefinitions());
  }

  /** 手动触发重算指定日期的三张每日汇总（数据补录/口径校准用）。缺省重算昨日。 */
  @PostMapping("/recalc")
  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN')")
  public Result<Integer> recalc(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false) Long orgId) {
    LocalDate target = date == null ? LocalDate.now().minusDays(1) : date;
    if (orgId == null && AuthContext.hasRole("SYS_ADMIN")) {
      return Result.ok(cockpitDailyStatsService.aggregateAllOrgs(target));
    }
    cockpitDailyStatsService.aggregate(resolveOrgId(orgId), target);
    return Result.ok(1);
  }

  private Long resolveOrgId(Long orgId) {
    Long current = AuthContext.getOrgId();
    // 超管可指定 orgId 做跨机构下钻；其余角色仅能查看本机构。
    if (AuthContext.hasRole("SYS_ADMIN")) {
      return orgId != null ? orgId : current;
    }
    return current;
  }
}
