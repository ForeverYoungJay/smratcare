package com.zhiyangyun.care.assessment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.zhiyangyun.care.assessment.controller.AssessmentRecordController;
import com.zhiyangyun.care.nursing.entity.CareLevel;
import java.util.List;
import org.junit.jupiter.api.Test;

class CareLevelMatchTest {

  private static CareLevel level(String code, String name, Integer enabled) {
    CareLevel level = new CareLevel();
    level.setLevelCode(code);
    level.setLevelName(name);
    level.setEnabled(enabled);
    return level;
  }

  @Test
  void matches_by_code_exact_name_or_name_prefix() {
    List<CareLevel> levels = List.of(
        level("L0", "0级：能力完好", 1),
        level("2级", "二级护理", 1),
        level("L3", "3级", 1));
    assertEquals("0级：能力完好", AssessmentRecordController.matchCareLevel("0级", levels));
    assertEquals("二级护理", AssessmentRecordController.matchCareLevel("2级", levels));
    assertEquals("3级", AssessmentRecordController.matchCareLevel(" 3级 ", levels));
  }

  @Test
  void ignores_disabled_or_unknown_levels() {
    List<CareLevel> levels = List.of(level("L1", "1级：轻度失能", 0));
    assertNull(AssessmentRecordController.matchCareLevel("1级", levels));
    assertNull(AssessmentRecordController.matchCareLevel("4级", levels));
    assertNull(AssessmentRecordController.matchCareLevel(null, levels));
  }
}
