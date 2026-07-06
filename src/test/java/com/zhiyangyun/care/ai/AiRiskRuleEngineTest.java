package com.zhiyangyun.care.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.ai.model.AiRiskEvaluation;
import com.zhiyangyun.care.ai.service.AiRiskRuleEngine;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 风险评分规则引擎单测：档位命中/权重/lte 因子/缺失因子跳过/分级。 */
class AiRiskRuleEngineTest {

  private final AiRiskRuleEngine engine = new AiRiskRuleEngine();

  private static final String FALL_RULE = """
      {"factors":[
        {"code":"AGE","name":"年龄","weight":1.0,
         "bands":[{"gte":80,"score":25},{"gte":70,"score":15},{"gte":65,"score":8}]},
        {"code":"FALL_HISTORY_90D","name":"近90天跌倒次数","weight":1.0,
         "bands":[{"gte":2,"score":40},{"gte":1,"score":25}]},
        {"code":"SEDATIVE_MED_30D","name":"镇静类用药","weight":1.0,"bands":[{"gte":1,"score":15}]},
        {"code":"DISABILITY_LEVEL","name":"失能等级","weight":1.0,
         "bands":[{"gte":3,"score":20},{"gte":2,"score":12},{"gte":1,"score":6}]}],
       "levels":[{"level":"HIGH","gte":60},{"level":"MEDIUM","gte":30},{"level":"LOW","gte":0}]}
      """;

  @Test
  void highRiskWhenMultipleFactorsHit() {
    Map<String, Double> factors = Map.of(
        "AGE", 82d,
        "FALL_HISTORY_90D", 2d,
        "SEDATIVE_MED_30D", 1d,
        "DISABILITY_LEVEL", 3d);
    AiRiskEvaluation evaluation = engine.evaluate(FALL_RULE, factors);
    assertEquals(25 + 40 + 15 + 20, evaluation.getScore(), 1e-9);
    assertEquals("HIGH", evaluation.getLevel());
    assertEquals(4, evaluation.getFactors().size());
  }

  @Test
  void picksFirstMatchingBandOnly() {
    AiRiskEvaluation evaluation = engine.evaluate(FALL_RULE, Map.of("AGE", 72d));
    assertEquals(15, evaluation.getScore(), 1e-9);
    assertEquals("LOW", evaluation.getLevel());
  }

  @Test
  void missingFactorContributesZero() {
    Map<String, Double> factors = new HashMap<>();
    factors.put("AGE", 90d);
    // 其余因子缺失
    AiRiskEvaluation evaluation = engine.evaluate(FALL_RULE, factors);
    assertEquals(25, evaluation.getScore(), 1e-9);
    assertTrue(evaluation.getFactors().stream()
        .filter(f -> !"AGE".equals(f.getCode()))
        .allMatch(f -> f.getScore() == 0 && f.getValue() == null));
  }

  @Test
  void weightMultipliesBandScore() {
    String rule = """
        {"factors":[{"code":"X","name":"x","weight":2.0,"bands":[{"gte":1,"score":10}]}],
         "levels":[{"level":"HIGH","gte":20},{"level":"LOW","gte":0}]}
        """;
    AiRiskEvaluation evaluation = engine.evaluate(rule, Map.of("X", 5d));
    assertEquals(20, evaluation.getScore(), 1e-9);
    assertEquals("HIGH", evaluation.getLevel());
  }

  @Test
  void lteBandForLowBmi() {
    String rule = """
        {"factors":[{"code":"BMI","name":"BMI","weight":1.0,"bands":[{"lte":18.5,"score":15}]}],
         "levels":[{"level":"MEDIUM","gte":10},{"level":"LOW","gte":0}]}
        """;
    assertEquals(15, engine.evaluate(rule, Map.of("BMI", 17.2)).getScore(), 1e-9);
    assertEquals(0, engine.evaluate(rule, Map.of("BMI", 22.0)).getScore(), 1e-9);
    // BMI 缺失时不触发 lte 档位
    assertEquals(0, engine.evaluate(rule, Map.of()).getScore(), 1e-9);
  }

  @Test
  void mediumLevelGrading() {
    AiRiskEvaluation evaluation = engine.evaluate(FALL_RULE, Map.of(
        "AGE", 75d,
        "SEDATIVE_MED_30D", 1d));
    assertEquals(30, evaluation.getScore(), 1e-9);
    assertEquals("MEDIUM", evaluation.getLevel());
  }

  @Test
  void invalidJsonFallsBackToLow() {
    AiRiskEvaluation evaluation = engine.evaluate("{not-json", Map.of("AGE", 90d));
    assertEquals(0, evaluation.getScore(), 1e-9);
    assertEquals("LOW", evaluation.getLevel());
  }
}
