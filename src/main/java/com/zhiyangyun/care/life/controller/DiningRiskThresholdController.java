package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningRiskThresholdConfig;
import com.zhiyangyun.care.life.mapper.DiningRiskThresholdConfigMapper;
import com.zhiyangyun.care.life.model.DiningRiskThresholdUpsertRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/life/dining/risk-threshold")
public class DiningRiskThresholdController {
  private final DiningRiskThresholdConfigMapper thresholdConfigMapper;

  public DiningRiskThresholdController(DiningRiskThresholdConfigMapper thresholdConfigMapper) {
    this.thresholdConfigMapper = thresholdConfigMapper;
  }

  @GetMapping("/list")
  public Result<List<DiningRiskThresholdConfig>> list() {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(thresholdConfigMapper.selectList(Wrappers.lambdaQuery(DiningRiskThresholdConfig.class)
        .eq(DiningRiskThresholdConfig::getIsDeleted, 0)
        .eq(orgId != null, DiningRiskThresholdConfig::getOrgId, orgId)
        .orderByAsc(DiningRiskThresholdConfig::getMetricCode)));
  }

  @PostMapping("/upsert")
  public Result<DiningRiskThresholdConfig> upsert(@Valid @RequestBody DiningRiskThresholdUpsertRequest request) {
    Long orgId = AuthContext.getOrgId();
    DiningRiskThresholdConfig existing = thresholdConfigMapper.selectOne(Wrappers.lambdaQuery(DiningRiskThresholdConfig.class)
        .eq(DiningRiskThresholdConfig::getIsDeleted, 0)
        .eq(orgId != null, DiningRiskThresholdConfig::getOrgId, orgId)
        .eq(DiningRiskThresholdConfig::getMetricCode, request.getMetricCode())
        .last("LIMIT 1"));
    DiningRiskThresholdConfig config = existing == null ? new DiningRiskThresholdConfig() : existing;
    if (existing == null) {
      config.setTenantId(orgId);
      config.setOrgId(orgId);
      config.setCreatedBy(AuthContext.getStaffId());
    }
    config.setMetricCode(request.getMetricCode().trim().toUpperCase());
    config.setMetricName(request.getMetricName());
    config.setThresholdValue(request.getThresholdValue());
    config.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
    config.setRemark(request.getRemark());
    if (existing == null) {
      thresholdConfigMapper.insert(config);
    } else {
      thresholdConfigMapper.updateById(config);
    }
    return Result.ok(config);
  }
}
