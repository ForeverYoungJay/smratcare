package com.zhiyangyun.care.ltci.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.ltci.model.LtciGradingResult;
import com.zhiyangyun.care.ltci.service.LtciGradingService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class LtciGradingServiceImpl implements LtciGradingService {

  private final ObjectMapper objectMapper;

  public LtciGradingServiceImpl(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public LtciGradingResult judge(String indicatorsJson, String combineRuleJson,
      Map<String, Double> answers) {
    if (indicatorsJson == null || indicatorsJson.isBlank()
        || combineRuleJson == null || combineRuleJson.isBlank()) {
      throw new IllegalArgumentException("评估模板不完整：缺少指标定义或组合规则");
    }
    if (answers == null) {
      throw new IllegalArgumentException("评估作答不能为空");
    }
    final JsonNode indicators;
    final JsonNode rule;
    try {
      indicators = objectMapper.readTree(indicatorsJson);
      rule = objectMapper.readTree(combineRuleJson);
    } catch (Exception ex) {
      throw new IllegalArgumentException("评估模板 JSON 解析失败: " + ex.getMessage(), ex);
    }

    JsonNode dimensions = indicators.path("dimensions");
    if (!dimensions.isArray() || dimensions.isEmpty()) {
      throw new IllegalArgumentException("评估模板缺少一级指标(dimensions)");
    }

    BigDecimal adl = null;
    BigDecimal cognitive = null;
    BigDecimal perception = null;
    for (JsonNode dim : dimensions) {
      String code = dim.path("code").asText("");
      BigDecimal score = dimensionScore(dim, answers);
      switch (code) {
        case "ADL" -> adl = score;
        case "COG" -> cognitive = score;
        case "PER" -> perception = score;
        default -> { /* 允许扩展维度，不参与默认组合 */ }
      }
    }
    if (adl == null) {
      throw new IllegalArgumentException("评估模板缺少 ADL(日常生活活动能力)维度");
    }

    String primaryCode = rule.path("primary").asText("ADL");
    BigDecimal primaryScore = switch (primaryCode) {
      case "COG" -> cognitive;
      case "PER" -> perception;
      default -> adl;
    };
    if (primaryScore == null) {
      throw new IllegalArgumentException("组合规则指定的主维度不存在: " + primaryCode);
    }

    JsonNode bands = rule.path("bands");
    if (!bands.isArray() || bands.isEmpty()) {
      throw new IllegalArgumentException("组合规则缺少等级区间(bands)");
    }
    int level = -1;
    String label = null;
    double ps = primaryScore.doubleValue();
    for (JsonNode band : bands) {
      double min = band.path("min").asDouble(Double.NEGATIVE_INFINITY);
      double max = band.path("max").asDouble(Double.POSITIVE_INFINITY);
      if (ps >= min && ps < max) {
        level = band.path("level").asInt();
        label = band.path("label").asText(null);
        break;
      }
    }
    if (level < 0) {
      throw new IllegalArgumentException("主维度得分未命中任何等级区间: " + ps);
    }

    // 组合升级：认知或感知觉严重受损时，在主维度判级基础上上调失能等级。
    boolean escalated = false;
    JsonNode esc = rule.path("escalation");
    if (!esc.isMissingNode()) {
      int maxLevel = esc.path("maxLevel").asInt(5);
      int step = esc.path("step").asInt(1);
      boolean cogSevere = cognitive != null && esc.has("cognitiveSevereBelow")
          && cognitive.doubleValue() < esc.path("cognitiveSevereBelow").asDouble();
      boolean perSevere = perception != null && esc.has("perceptionSevereBelow")
          && perception.doubleValue() < esc.path("perceptionSevereBelow").asDouble();
      if ((cogSevere || perSevere) && level < maxLevel) {
        level = Math.min(level + step, maxLevel);
        escalated = true;
        String relabel = rule.path("levelLabels").path(String.valueOf(level)).asText(null);
        if (relabel != null && !relabel.isBlank()) {
          label = relabel;
        }
      }
    }

    LtciGradingResult result = new LtciGradingResult();
    result.setAdlScore(adl);
    result.setCognitiveScore(cognitive);
    result.setPerceptionScore(perception);
    result.setTotalScore(sum(adl, cognitive, perception));
    result.setDisabilityLevel(level);
    result.setLevelLabel(label);
    result.setEscalated(escalated);
    return result;
  }

  /** 维度得分 = 命中二级指标实际得分之和 / 满分之和 × 100，缺项按 0 计，超界裁剪。 */
  private BigDecimal dimensionScore(JsonNode dim, Map<String, Double> answers) {
    JsonNode items = dim.path("indicators");
    if (!items.isArray() || items.isEmpty()) {
      return BigDecimal.ZERO;
    }
    double got = 0d;
    double full = 0d;
    for (JsonNode item : items) {
      String code = item.path("code").asText("");
      double max = item.path("max").asDouble(0d);
      full += max;
      Double raw = answers.get(code);
      double v = raw == null ? 0d : raw;
      if (v < 0) {
        v = 0d;
      }
      if (v > max) {
        v = max;
      }
      got += v;
    }
    if (full <= 0) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(got / full * 100d).setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal sum(BigDecimal... values) {
    BigDecimal total = BigDecimal.ZERO;
    for (BigDecimal v : values) {
      if (v != null) {
        total = total.add(v);
      }
    }
    return total.setScale(2, RoundingMode.HALF_UP);
  }
}
