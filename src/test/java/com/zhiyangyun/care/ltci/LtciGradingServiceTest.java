package com.zhiyangyun.care.ltci;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.ltci.model.LtciGradingResult;
import com.zhiyangyun.care.ltci.service.impl.LtciGradingServiceImpl;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 失能等级判级内核单测：对齐国家标准模板，覆盖等级 0-5、组合升级与非法输入。 */
class LtciGradingServiceTest {

  private final LtciGradingServiceImpl grading = new LtciGradingServiceImpl(new ObjectMapper());

  private static final String INDICATORS = """
      {"dimensions":[
        {"code":"ADL","name":"日常生活活动能力","indicators":[
          {"code":"adl_feeding","max":10},{"code":"adl_bathing","max":5},
          {"code":"adl_grooming","max":5},{"code":"adl_dressing","max":10},
          {"code":"adl_bowel","max":10},{"code":"adl_bladder","max":10},
          {"code":"adl_toilet","max":10},{"code":"adl_transfer","max":15},
          {"code":"adl_walking","max":15},{"code":"adl_stairs","max":10}]},
        {"code":"COG","name":"认知能力","indicators":[
          {"code":"cog_orientation_time","max":2},{"code":"cog_orientation_person","max":2},
          {"code":"cog_memory","max":2}]},
        {"code":"PER","name":"感知觉与沟通","indicators":[
          {"code":"per_consciousness","max":2},{"code":"per_vision","max":2},
          {"code":"per_hearing","max":2},{"code":"per_communication","max":2}]}
      ]}""";

  private static final String RULE = """
      {"method":"COMBINATION","primary":"ADL",
       "bands":[
        {"min":90,"max":100.0001,"level":0,"label":"能力完好"},
        {"min":70,"max":90,"level":1,"label":"轻度失能"},
        {"min":50,"max":70,"level":2,"label":"中度失能"},
        {"min":30,"max":50,"level":3,"label":"中度失能"},
        {"min":10,"max":30,"level":4,"label":"重度失能"},
        {"min":0,"max":10,"level":5,"label":"重度失能"}],
       "escalation":{"cognitiveSevereBelow":30,"perceptionSevereBelow":30,"maxLevel":5,"step":1},
       "levelLabels":{"0":"能力完好","1":"轻度失能","2":"中度失能","3":"中度失能","4":"重度失能","5":"重度失能"}}""";

  /** 全维度正常人 → 能力完好(0)。 */
  @Test
  void level0_fullyIndependent() {
    Map<String, Double> a = adl(10, 5, 5, 10, 10, 10, 10, 15, 15, 10); // ADL=100
    cog(a, 2, 2, 2);
    per(a, 2, 2, 2, 2);
    LtciGradingResult r = grading.judge(INDICATORS, RULE, a);
    assertEquals(0, r.getDisabilityLevel());
    assertEquals("能力完好", r.getLevelLabel());
    assertFalse(r.isEscalated());
  }

  /** ADL=60 且认知/感知觉正常 → 中度失能(2)。 */
  @Test
  void level2_moderate() {
    Map<String, Double> a = adl(10, 0, 0, 10, 10, 0, 10, 15, 5, 0); // 10+10+10+10+15+5=60
    cog(a, 2, 2, 2);
    per(a, 2, 2, 2, 2);
    LtciGradingResult r = grading.judge(INDICATORS, RULE, a);
    assertEquals(60.0, r.getAdlScore().doubleValue());
    assertEquals(2, r.getDisabilityLevel());
    assertFalse(r.isEscalated());
  }

  /** ADL=40 → 中度失能(3)。 */
  @Test
  void level3_moderate() {
    Map<String, Double> a = adl(10, 0, 0, 10, 10, 0, 10, 0, 0, 0); // 40
    cog(a, 2, 2, 2);
    per(a, 2, 2, 2, 2);
    LtciGradingResult r = grading.judge(INDICATORS, RULE, a);
    assertEquals(3, r.getDisabilityLevel());
  }

  /** ADL=0 → 重度失能(5)。 */
  @Test
  void level5_severe() {
    Map<String, Double> a = adl(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    cog(a, 0, 0, 0);
    per(a, 0, 0, 0, 0);
    LtciGradingResult r = grading.judge(INDICATORS, RULE, a);
    assertEquals(5, r.getDisabilityLevel());
  }

  /** ADL=60(本应 level2)但认知严重受损 → 组合升级到 level3。 */
  @Test
  void escalation_cognitiveSevere_upgradesLevel() {
    Map<String, Double> a = adl(10, 0, 0, 10, 10, 0, 10, 15, 5, 0); // ADL=60 → level2
    cog(a, 0, 0, 0); // 认知得分 0 < 30
    per(a, 2, 2, 2, 2);
    LtciGradingResult r = grading.judge(INDICATORS, RULE, a);
    assertEquals(3, r.getDisabilityLevel());
    assertTrue(r.isEscalated());
  }

  /** 缺项按 0 计：不传任何作答 → ADL=0 → level5。 */
  @Test
  void missingAnswers_treatedAsZero() {
    LtciGradingResult r = grading.judge(INDICATORS, RULE, new HashMap<>());
    assertEquals(5, r.getDisabilityLevel());
  }

  /** 非法模板 JSON → IllegalArgumentException。 */
  @Test
  void invalidTemplate_throws() {
    assertThrows(IllegalArgumentException.class,
        () -> grading.judge("{", RULE, new HashMap<>()));
  }

  // ---- helpers ----

  private Map<String, Double> adl(double feeding, double bathing, double grooming, double dressing,
      double bowel, double bladder, double toilet, double transfer, double walking, double stairs) {
    Map<String, Double> a = new HashMap<>();
    a.put("adl_feeding", feeding);
    a.put("adl_bathing", bathing);
    a.put("adl_grooming", grooming);
    a.put("adl_dressing", dressing);
    a.put("adl_bowel", bowel);
    a.put("adl_bladder", bladder);
    a.put("adl_toilet", toilet);
    a.put("adl_transfer", transfer);
    a.put("adl_walking", walking);
    a.put("adl_stairs", stairs);
    return a;
  }

  private void cog(Map<String, Double> a, double time, double person, double memory) {
    a.put("cog_orientation_time", time);
    a.put("cog_orientation_person", person);
    a.put("cog_memory", memory);
  }

  private void per(Map<String, Double> a, double consciousness, double vision, double hearing,
      double communication) {
    a.put("per_consciousness", consciousness);
    a.put("per_vision", vision);
    a.put("per_hearing", hearing);
    a.put("per_communication", communication);
  }
}
