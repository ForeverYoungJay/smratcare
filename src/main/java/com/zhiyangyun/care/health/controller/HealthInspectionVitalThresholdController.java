package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.model.HealthInspectionVitalThresholdUpsertRequest;
import com.zhiyangyun.care.vital.entity.VitalThresholdConfig;
import com.zhiyangyun.care.vital.mapper.VitalThresholdConfigMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health/inspection/vital-threshold")
public class HealthInspectionVitalThresholdController {
  private final VitalThresholdConfigMapper thresholdConfigMapper;

  public HealthInspectionVitalThresholdController(VitalThresholdConfigMapper thresholdConfigMapper) {
    this.thresholdConfigMapper = thresholdConfigMapper;
  }

  @GetMapping("/list")
  public Result<List<VitalThresholdConfig>> list() {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(thresholdConfigMapper.selectList(Wrappers.lambdaQuery(VitalThresholdConfig.class)
        .eq(VitalThresholdConfig::getIsDeleted, 0)
        .eq(orgId != null, VitalThresholdConfig::getOrgId, orgId)
        .orderByAsc(VitalThresholdConfig::getType)
        .orderByAsc(VitalThresholdConfig::getMetricCode)));
  }

  @PostMapping("/upsert")
  public Result<VitalThresholdConfig> upsert(@Valid @RequestBody HealthInspectionVitalThresholdUpsertRequest request) {
    Long orgId = AuthContext.getOrgId();
    String type = normalizeCode(request.getType());
    String metricCode = normalizeMetricCode(request.getMetricCode());
    VitalThresholdConfig existing = thresholdConfigMapper.selectOne(Wrappers.lambdaQuery(VitalThresholdConfig.class)
        .eq(VitalThresholdConfig::getIsDeleted, 0)
        .eq(orgId != null, VitalThresholdConfig::getOrgId, orgId)
        .eq(VitalThresholdConfig::getType, type)
        .eq(metricCode != null, VitalThresholdConfig::getMetricCode, metricCode)
        .isNull(metricCode == null, VitalThresholdConfig::getMetricCode)
        .last("LIMIT 1"));
    VitalThresholdConfig config = existing == null ? new VitalThresholdConfig() : existing;
    if (existing == null) {
      config.setOrgId(orgId);
    }
    config.setType(type);
    config.setMetricCode(metricCode);
    config.setMinValue(request.getMinValue());
    config.setMaxValue(request.getMaxValue());
    config.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    config.setRemark(normalizeRemark(request.getRemark()));
    if (existing == null) {
      thresholdConfigMapper.insert(config);
    } else {
      thresholdConfigMapper.updateById(config);
    }
    return Result.ok(config);
  }

  private String normalizeCode(String value) {
    return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
  }

  private String normalizeMetricCode(String value) {
    String normalized = normalizeCode(value);
    return normalized == null || normalized.isBlank() ? null : normalized;
  }

  private String normalizeRemark(String value) {
    if (value == null) {
      return null;
    }
    String text = value.trim();
    return text.isEmpty() ? null : text;
  }
}
