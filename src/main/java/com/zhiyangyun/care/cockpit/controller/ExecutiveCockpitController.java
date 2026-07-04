package com.zhiyangyun.care.cockpit.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.cockpit.model.CockpitOverviewResponse;
import com.zhiyangyun.care.cockpit.service.CockpitMetricService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 经营驾驶舱接口。仅管理层可见（DIRECTOR/SYS_ADMIN 及各部门部长）。
 */
@RestController
@RequestMapping("/stats/executive")
public class ExecutiveCockpitController {

  private final CockpitMetricService cockpitMetricService;

  public ExecutiveCockpitController(CockpitMetricService cockpitMetricService) {
    this.cockpitMetricService = cockpitMetricService;
  }

  /** 经营总览：当日 KPI + 近 trendDays 日趋势。orgId 缺省取当前登录机构。 */
  @GetMapping("/overview")
  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN','MEDICAL_MINISTER','NURSING_MINISTER','FINANCE_MINISTER','LOGISTICS_MINISTER','MARKETING_MINISTER','HR_MINISTER')")
  public Result<CockpitOverviewResponse> overview(
      @RequestParam(required = false) Long orgId,
      @RequestParam(defaultValue = "30") int trendDays) {
    Long scopedOrgId = resolveOrgId(orgId);
    return Result.ok(cockpitMetricService.overview(scopedOrgId, trendDays));
  }

  /** 手动触发当日快照重算（数据补录/校准用）。 */
  @PostMapping("/snapshot/refresh")
  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<CockpitOverviewResponse> refresh(@RequestParam(required = false) Long orgId,
      @RequestParam(defaultValue = "30") int trendDays) {
    Long scopedOrgId = resolveOrgId(orgId);
    cockpitMetricService.snapshot(scopedOrgId, java.time.LocalDate.now());
    return Result.ok(cockpitMetricService.overview(scopedOrgId, trendDays));
  }

  private Long resolveOrgId(Long orgId) {
    Long current = AuthContext.getOrgId();
    // 非超管仅能查看本机构；超管可指定 orgId 做跨机构查看。
    if (AuthContext.hasRole("SYS_ADMIN") || AuthContext.hasRole("DIRECTOR")) {
      return orgId != null ? orgId : current;
    }
    return current;
  }
}
