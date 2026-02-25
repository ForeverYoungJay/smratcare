package com.zhiyangyun.care.assessment.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.assessment.entity.AssessmentScaleTemplate;
import com.zhiyangyun.care.assessment.mapper.AssessmentScaleTemplateMapper;
import com.zhiyangyun.care.assessment.model.AssessmentScoreResult;
import com.zhiyangyun.care.assessment.service.AssessmentScoringService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class AssessmentScoringServiceImpl implements AssessmentScoringService {
  private final AssessmentScaleTemplateMapper templateMapper;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public AssessmentScoringServiceImpl(AssessmentScaleTemplateMapper templateMapper) {
    this.templateMapper = templateMapper;
  }

  @Override
  public AssessmentScaleTemplate getActiveTemplate(Long orgId, Long templateId) {
    return templateMapper.selectOne(Wrappers.lambdaQuery(AssessmentScaleTemplate.class)
        .eq(AssessmentScaleTemplate::getId, templateId)
        .eq(orgId != null, AssessmentScaleTemplate::getOrgId, orgId)
        .eq(AssessmentScaleTemplate::getIsDeleted, 0)
        .eq(AssessmentScaleTemplate::getStatus, 1)
        .last("LIMIT 1"));
  }

  @Override
  public AssessmentScoreResult score(AssessmentScaleTemplate template, String detailJson) {
    if (template == null) {
      throw new IllegalArgumentException("Template not found");
    }
    if (detailJson == null || detailJson.isBlank()) {
      throw new IllegalArgumentException("detailJson required");
    }

    try {
      JsonNode answerNode = objectMapper.readTree(detailJson);
      JsonNode scoreRules = objectMapper.readTree(template.getScoreRulesJson() == null ? "[]" : template.getScoreRulesJson());
      JsonNode levelRules = objectMapper.readTree(template.getLevelRulesJson() == null ? "[]" : template.getLevelRulesJson());

      BigDecimal score = BigDecimal.ZERO;
      if (scoreRules.isArray()) {
        for (JsonNode rule : scoreRules) {
          String key = rule.path("key").asText(null);
          if (key == null || key.isBlank()) {
            continue;
          }
          BigDecimal weight = decimal(rule.path("weight"), BigDecimal.ONE);
          BigDecimal value = decimal(answerNode.path(key), BigDecimal.ZERO);
          score = score.add(value.multiply(weight));
        }
      }

      score = score.setScale(2, RoundingMode.HALF_UP);
      String level = matchLevel(levelRules, score);

      AssessmentScoreResult result = new AssessmentScoreResult();
      result.setScore(score);
      result.setLevelCode(level);
      result.setReason("AUTO_BY_TEMPLATE");
      return result;
    } catch (IllegalArgumentException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid assessment rule or detail json");
    }
  }

  private String matchLevel(JsonNode levelRules, BigDecimal score) {
    if (levelRules == null || !levelRules.isArray()) {
      return null;
    }
    for (JsonNode rule : levelRules) {
      BigDecimal min = decimal(rule.path("min"), null);
      BigDecimal max = decimal(rule.path("max"), null);
      if ((min == null || score.compareTo(min) >= 0) && (max == null || score.compareTo(max) <= 0)) {
        String level = rule.path("level").asText(null);
        if (level != null && !level.isBlank()) {
          return level;
        }
      }
    }
    return null;
  }

  private BigDecimal decimal(JsonNode node, BigDecimal defaultVal) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return defaultVal;
    }
    if (node.isNumber()) {
      return node.decimalValue();
    }
    if (node.isTextual()) {
      String text = node.asText().trim();
      if (text.isEmpty()) {
        return defaultVal;
      }
      try {
        return new BigDecimal(text);
      } catch (NumberFormatException ex) {
        return defaultVal;
      }
    }
    return defaultVal;
  }
}
