package com.zhiyangyun.care.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.ai.model.AiRiskEvaluation;
import com.zhiyangyun.care.ai.model.AiRiskFactorResult;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 健康风险评分规则引擎（纯逻辑）。规则 JSON 结构：
 *
 * <pre>
 * {"factors":[{"code":"AGE","name":"年龄","weight":1.0,
 *              "bands":[{"gte":80,"score":25},{"gte":70,"score":15}]}],
 *  "levels":[{"level":"HIGH","gte":60},{"level":"MEDIUM","gte":30},{"level":"LOW","gte":0}]}
 * </pre>
 *
 * <p>每个因子按 bands 顺序取第一个命中的档位分（gte/lte 同时存在时须同时满足），乘以 weight 累加；
 * 因子取值缺失（null）不计分。总分按 levels（按 gte 从高到低给出）分级。
 */
@Component
public class AiRiskRuleEngine {
  private static final ObjectMapper JSON = new ObjectMapper();

  /**
   * @param ruleJson 模型规则 JSON
   * @param factorValues 采集到的因子取值（缺失因子可不含 key 或值为 null）
   */
  public AiRiskEvaluation evaluate(String ruleJson, Map<String, Double> factorValues) {
    AiRiskEvaluation evaluation = new AiRiskEvaluation();
    JsonNode root = parse(ruleJson);
    if (root == null) {
      evaluation.setLevel("LOW");
      return evaluation;
    }
    double total = 0;
    JsonNode factors = root.path("factors");
    if (factors.isArray()) {
      for (JsonNode factor : factors) {
        String code = factor.path("code").asText(null);
        if (code == null || code.isBlank()) {
          continue;
        }
        String name = factor.path("name").asText(code);
        double weight = factor.path("weight").asDouble(1.0);
        Double value = factorValues == null ? null : factorValues.get(code);
        double score = value == null ? 0 : bandScore(factor.path("bands"), value) * weight;
        total += score;
        evaluation.getFactors().add(new AiRiskFactorResult(code, name, value, round(score)));
      }
    }
    evaluation.setScore(round(total));
    evaluation.setLevel(grade(root.path("levels"), total));
    return evaluation;
  }

  private double bandScore(JsonNode bands, double value) {
    if (!bands.isArray()) {
      return 0;
    }
    for (JsonNode band : bands) {
      boolean gteOk = !band.has("gte") || value >= band.path("gte").asDouble();
      boolean lteOk = !band.has("lte") || value <= band.path("lte").asDouble();
      if ((band.has("gte") || band.has("lte")) && gteOk && lteOk) {
        return band.path("score").asDouble(0);
      }
    }
    return 0;
  }

  private String grade(JsonNode levels, double total) {
    String best = "LOW";
    double bestGte = Double.NEGATIVE_INFINITY;
    if (levels.isArray()) {
      for (JsonNode level : levels) {
        double gte = level.path("gte").asDouble(0);
        if (total >= gte && gte >= bestGte) {
          bestGte = gte;
          best = level.path("level").asText("LOW");
        }
      }
    }
    return best;
  }

  private JsonNode parse(String ruleJson) {
    if (ruleJson == null || ruleJson.isBlank()) {
      return null;
    }
    try {
      return JSON.readTree(ruleJson);
    } catch (Exception ex) {
      return null;
    }
  }

  private double round(double value) {
    return Math.round(value * 100) / 100.0;
  }
}
