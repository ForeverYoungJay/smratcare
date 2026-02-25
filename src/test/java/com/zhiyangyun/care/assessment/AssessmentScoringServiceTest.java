package com.zhiyangyun.care.assessment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhiyangyun.care.assessment.entity.AssessmentScaleTemplate;
import com.zhiyangyun.care.assessment.mapper.AssessmentScaleTemplateMapper;
import com.zhiyangyun.care.assessment.service.impl.AssessmentScoringServiceImpl;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AssessmentScoringServiceTest {
  private final AssessmentScoringServiceImpl scoringService =
      new AssessmentScoringServiceImpl(Mockito.mock(AssessmentScaleTemplateMapper.class));

  @Test
  void score_should_calculate_and_match_level() {
    AssessmentScaleTemplate template = new AssessmentScaleTemplate();
    template.setScoreRulesJson("[{\"key\":\"q1\",\"weight\":1},{\"key\":\"q2\",\"weight\":0.5}]");
    template.setLevelRulesJson("[{\"min\":0,\"max\":3,\"level\":\"LOW\"},{\"min\":3.01,\"max\":10,\"level\":\"HIGH\"}]");

    var result = scoringService.score(template, "{\"q1\":2,\"q2\":4}");

    assertEquals(new BigDecimal("4.00"), result.getScore());
    assertEquals("HIGH", result.getLevelCode());
  }

  @Test
  void score_should_reject_invalid_json() {
    AssessmentScaleTemplate template = new AssessmentScaleTemplate();
    template.setScoreRulesJson("[]");
    template.setLevelRulesJson("[]");

    assertThrows(IllegalArgumentException.class, () -> scoringService.score(template, "{"));
  }
}
