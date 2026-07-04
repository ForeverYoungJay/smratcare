package com.zhiyangyun.care.smart.service;

import com.zhiyangyun.care.smart.entity.SmartAlertRule;
import com.zhiyangyun.care.smart.model.SmartRuleMatch;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 智慧安全场景规则引擎（纯逻辑）。给定一条设备事件与其指标，在候选规则中匹配并选出最应触发的规则：
 * 先比严重等级（CRITICAL &gt; HIGH &gt; WARNING），同等级再比优先级（priority 小者优先）。
 */
@Component
public class SmartAlertRuleEngine {

  /**
   * @param eventType 事件类型（如 FALL/SOS/BED_EXIT/VITAL）
   * @param deviceType 设备类型（可空）
   * @param metrics 事件携带指标（如 heartRate/spo2），可空
   * @param disabilityLevel 长者失能等级（可空，用于规则适用范围过滤）
   * @param rules 候选规则集
   */
  public SmartRuleMatch evaluate(String eventType, String deviceType, Map<String, Double> metrics,
      Integer disabilityLevel, List<SmartAlertRule> rules) {
    if (!StringUtils.hasText(eventType) || rules == null || rules.isEmpty()) {
      return SmartRuleMatch.noMatch();
    }
    SmartAlertRule best = null;
    for (SmartAlertRule rule : rules) {
      if (rule.getEnabled() != null && rule.getEnabled() == 0) {
        continue;
      }
      if (!eventType.equalsIgnoreCase(rule.getEventType())) {
        continue;
      }
      if (StringUtils.hasText(rule.getDeviceType())
          && !rule.getDeviceType().equalsIgnoreCase(deviceType)) {
        continue;
      }
      if (!scopeMatches(rule.getDisabilityLevelScope(), disabilityLevel)) {
        continue;
      }
      if (!conditionMatches(rule, metrics)) {
        continue;
      }
      if (best == null || isMorePreferred(rule, best)) {
        best = rule;
      }
    }
    if (best == null) {
      return SmartRuleMatch.noMatch();
    }
    SmartRuleMatch m = new SmartRuleMatch();
    m.setMatched(true);
    m.setRuleId(best.getId());
    m.setRuleCode(best.getRuleCode());
    m.setRuleName(best.getRuleName());
    m.setLevel(best.getLevel());
    m.setAutoDispatch(best.getAutoDispatch() == null || best.getAutoDispatch() == 1);
    m.setNotifyFamily(best.getNotifyFamily() != null && best.getNotifyFamily() == 1);
    m.setTitle(best.getRuleName());
    return m;
  }

  private boolean conditionMatches(SmartAlertRule rule, Map<String, Double> metrics) {
    String op = rule.getOperator() == null ? "PRESENT" : rule.getOperator().toUpperCase();
    if ("PRESENT".equals(op)) {
      return true;
    }
    if (!StringUtils.hasText(rule.getMetricKey()) || metrics == null) {
      return false;
    }
    Double value = metrics.get(rule.getMetricKey());
    if (value == null) {
      return false;
    }
    double v = value;
    double t = rule.getThreshold() == null ? 0d : rule.getThreshold().doubleValue();
    switch (op) {
      case "GT": return v > t;
      case "GE": return v >= t;
      case "LT": return v < t;
      case "LE": return v <= t;
      case "EQ": return v == t;
      case "RANGE_OUT": {
        double t2 = rule.getThreshold2() == null ? t : rule.getThreshold2().doubleValue();
        double lo = Math.min(t, t2);
        double hi = Math.max(t, t2);
        return v < lo || v > hi;
      }
      default: return false;
    }
  }

  private boolean scopeMatches(String scope, Integer disabilityLevel) {
    if (!StringUtils.hasText(scope)) {
      return true;
    }
    if (disabilityLevel == null) {
      return false;
    }
    for (String part : scope.split(",")) {
      if (part.trim().equals(String.valueOf(disabilityLevel))) {
        return true;
      }
    }
    return false;
  }

  private boolean isMorePreferred(SmartAlertRule a, SmartAlertRule b) {
    int sa = severityRank(a.getLevel());
    int sb = severityRank(b.getLevel());
    if (sa != sb) {
      return sa > sb;
    }
    int pa = a.getPriority() == null ? Integer.MAX_VALUE : a.getPriority();
    int pb = b.getPriority() == null ? Integer.MAX_VALUE : b.getPriority();
    return pa < pb;
  }

  private int severityRank(String level) {
    if (level == null) {
      return 0;
    }
    switch (level.toUpperCase()) {
      case "CRITICAL": return 3;
      case "HIGH": return 2;
      case "WARNING": return 1;
      default: return 0;
    }
  }
}
