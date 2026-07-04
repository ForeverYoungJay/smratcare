package com.zhiyangyun.care.smart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.smart.entity.SmartAlertRule;
import com.zhiyangyun.care.smart.model.SmartRuleMatch;
import com.zhiyangyun.care.smart.service.SmartAlertRuleEngine;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 场景规则引擎单测：存在型/阈值型/多规则择优/适用范围。 */
class SmartAlertRuleEngineTest {

  private final SmartAlertRuleEngine engine = new SmartAlertRuleEngine();

  private SmartAlertRule rule(Long id, String code, String eventType, String metric, String op,
      Double th, String level, Integer priority) {
    SmartAlertRule r = new SmartAlertRule();
    r.setId(id);
    r.setRuleCode(code);
    r.setRuleName(code);
    r.setEventType(eventType);
    r.setMetricKey(metric);
    r.setOperator(op);
    r.setThreshold(th == null ? null : BigDecimal.valueOf(th));
    r.setLevel(level);
    r.setPriority(priority);
    r.setEnabled(1);
    r.setAutoDispatch(1);
    r.setNotifyFamily("SOS".equals(code) || "FALL".equals(code) ? 1 : 0);
    return r;
  }

  @Test
  void presentRuleTriggers() {
    var rules = List.of(rule(1L, "FALL", "FALL", null, "PRESENT", null, "CRITICAL", 10));
    SmartRuleMatch m = engine.evaluate("FALL", null, Map.of(), null, rules);
    assertTrue(m.isMatched());
    assertEquals("CRITICAL", m.getLevel());
    assertTrue(m.isAutoDispatch());
    assertTrue(m.isNotifyFamily());
  }

  @Test
  void thresholdRuleMatchesWhenExceeded() {
    var rules = List.of(rule(6L, "HR_HIGH", "VITAL", "heartRate", "GT", 120d, "HIGH", 50));
    assertTrue(engine.evaluate("VITAL", null, Map.of("heartRate", 130d), null, rules).isMatched());
    assertFalse(engine.evaluate("VITAL", null, Map.of("heartRate", 100d), null, rules).isMatched());
  }

  @Test
  void picksMostSevereAmongMatches() {
    var rules = List.of(
        rule(6L, "HR_HIGH", "VITAL", "heartRate", "GT", 120d, "HIGH", 50),
        rule(8L, "SPO2_LOW", "VITAL", "spo2", "LT", 90d, "CRITICAL", 20));
    SmartRuleMatch m = engine.evaluate("VITAL", null, Map.of("heartRate", 130d, "spo2", 85d), null, rules);
    assertTrue(m.isMatched());
    assertEquals("SPO2_LOW", m.getRuleCode());
    assertEquals("CRITICAL", m.getLevel());
  }

  @Test
  void priorityBreaksTieAtSameSeverity() {
    var rules = List.of(
        rule(1L, "A", "STAY", null, "PRESENT", null, "HIGH", 40),
        rule(2L, "B", "STAY", null, "PRESENT", null, "HIGH", 30));
    assertEquals("B", engine.evaluate("STAY", null, Map.of(), null, rules).getRuleCode());
  }

  @Test
  void missingMetricDoesNotMatch() {
    var rules = List.of(rule(6L, "HR_HIGH", "VITAL", "heartRate", "GT", 120d, "HIGH", 50));
    assertFalse(engine.evaluate("VITAL", null, Map.of("spo2", 95d), null, rules).isMatched());
  }

  @Test
  void disabilityScopeFilters() {
    SmartAlertRule r = rule(3L, "BED", "BED_EXIT", null, "PRESENT", null, "HIGH", 30);
    r.setDisabilityLevelScope("4,5");
    var rules = List.of(r);
    assertFalse(engine.evaluate("BED_EXIT", null, Map.of(), 2, rules).isMatched());
    assertTrue(engine.evaluate("BED_EXIT", null, Map.of(), 4, rules).isMatched());
    // 未知失能等级：限定范围的规则不匹配
    assertFalse(engine.evaluate("BED_EXIT", null, Map.of(), null, rules).isMatched());
  }

  @Test
  void unknownEventOrEmptyRulesNoMatch() {
    assertFalse(engine.evaluate("UNKNOWN", null, Map.of(), null,
        List.of(rule(1L, "FALL", "FALL", null, "PRESENT", null, "CRITICAL", 10))).isMatched());
    assertFalse(engine.evaluate("FALL", null, Map.of(), null, List.of()).isMatched());
  }
}
