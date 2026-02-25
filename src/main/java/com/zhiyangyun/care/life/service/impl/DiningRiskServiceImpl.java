package com.zhiyangyun.care.life.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.life.entity.DiningDish;
import com.zhiyangyun.care.life.entity.DiningRiskThresholdConfig;
import com.zhiyangyun.care.life.entity.HealthBasicRecord;
import com.zhiyangyun.care.life.mapper.DiningDishMapper;
import com.zhiyangyun.care.life.mapper.DiningRiskThresholdConfigMapper;
import com.zhiyangyun.care.life.mapper.HealthBasicRecordMapper;
import com.zhiyangyun.care.life.model.DiningRiskCheckResponse;
import com.zhiyangyun.care.life.model.DiningRiskReasonItem;
import com.zhiyangyun.care.life.service.DiningRiskService;
import com.zhiyangyun.care.store.entity.DiseaseForbiddenTag;
import com.zhiyangyun.care.store.entity.ElderDisease;
import com.zhiyangyun.care.store.entity.ProductTag;
import com.zhiyangyun.care.store.mapper.DiseaseForbiddenTagMapper;
import com.zhiyangyun.care.store.mapper.ElderDiseaseMapper;
import com.zhiyangyun.care.store.mapper.ProductTagMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DiningRiskServiceImpl implements DiningRiskService {
  private final ElderMapper elderMapper;
  private final DiningDishMapper diningDishMapper;
  private final ElderDiseaseMapper elderDiseaseMapper;
  private final DiseaseForbiddenTagMapper forbiddenTagMapper;
  private final ProductTagMapper productTagMapper;
  private final HealthBasicRecordMapper healthBasicRecordMapper;
  private final DiningRiskThresholdConfigMapper thresholdConfigMapper;

  public DiningRiskServiceImpl(
      ElderMapper elderMapper,
      DiningDishMapper diningDishMapper,
      ElderDiseaseMapper elderDiseaseMapper,
      DiseaseForbiddenTagMapper forbiddenTagMapper,
      ProductTagMapper productTagMapper,
      HealthBasicRecordMapper healthBasicRecordMapper,
      DiningRiskThresholdConfigMapper thresholdConfigMapper) {
    this.elderMapper = elderMapper;
    this.diningDishMapper = diningDishMapper;
    this.elderDiseaseMapper = elderDiseaseMapper;
    this.forbiddenTagMapper = forbiddenTagMapper;
    this.productTagMapper = productTagMapper;
    this.healthBasicRecordMapper = healthBasicRecordMapper;
    this.thresholdConfigMapper = thresholdConfigMapper;
  }

  @Override
  public DiningRiskCheckResponse check(Long orgId, Long elderId, String elderName, String dishNamesRaw) {
    ElderProfile elder = ensureElder(orgId, elderId, elderName);
    List<String> dishNames = splitDishNames(dishNamesRaw);

    DiningRiskCheckResponse response = new DiningRiskCheckResponse();
    response.setElderId(elder.getId());
    response.setElderName(elder.getFullName());
    response.setDishNames(String.join(",", dishNames));

    if (dishNames.isEmpty()) {
      response.setAllowed(true);
      response.setCanOverride(false);
      response.setBlockedDishNames(Collections.emptyList());
      response.setReasons(Collections.emptyList());
      response.setMessage("未提供菜品，跳过风险校验");
      return response;
    }

    List<DiningDish> dishes = diningDishMapper.selectList(Wrappers.lambdaQuery(DiningDish.class)
        .eq(DiningDish::getIsDeleted, 0)
        .eq(orgId != null, DiningDish::getOrgId, orgId)
        .in(DiningDish::getDishName, dishNames));

    Set<String> blockedDishNames = new LinkedHashSet<>();
    List<DiningRiskReasonItem> reasons = new ArrayList<>();

    validateDiseaseForbiddenTags(orgId, elder.getId(), dishes, blockedDishNames, reasons);
    validateHealthThresholds(orgId, elder.getId(), dishes, blockedDishNames, reasons);

    response.setBlockedDishNames(new ArrayList<>(blockedDishNames));
    response.setReasons(reasons);

    if (blockedDishNames.isEmpty()) {
      response.setAllowed(true);
      response.setCanOverride(false);
      response.setMessage("未命中风险规则");
    } else {
      response.setAllowed(false);
      response.setCanOverride(true);
      response.setMessage("命中风险规则，需申请放行审批");
    }
    return response;
  }

  private ElderProfile ensureElder(Long orgId, Long elderId, String elderName) {
    if (elderId != null) {
      ElderProfile elder = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
          .eq(ElderProfile::getIsDeleted, 0)
          .eq(orgId != null, ElderProfile::getOrgId, orgId)
          .eq(ElderProfile::getId, elderId)
          .last("LIMIT 1"));
      if (elder == null) {
        throw new IllegalArgumentException("老人不存在或不在当前机构");
      }
      return elder;
    }
    if (elderName == null || elderName.isBlank()) {
      throw new IllegalArgumentException("请提供老人ID或姓名");
    }
    ElderProfile elder = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getFullName, elderName)
        .last("LIMIT 1"));
    if (elder == null) {
      throw new IllegalArgumentException("未找到老人档案");
    }
    return elder;
  }

  private List<String> splitDishNames(String dishNamesRaw) {
    if (dishNamesRaw == null || dishNamesRaw.isBlank()) {
      return List.of();
    }
    return Arrays.stream(dishNamesRaw.split("[,，、;；]"))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .distinct()
        .toList();
  }

  private void validateDiseaseForbiddenTags(
      Long orgId,
      Long elderId,
      List<DiningDish> dishes,
      Set<String> blockedDishNames,
      List<DiningRiskReasonItem> reasons) {
    if (dishes.isEmpty()) {
      return;
    }

    List<Long> diseaseIds = elderDiseaseMapper.selectList(Wrappers.lambdaQuery(ElderDisease.class)
            .eq(ElderDisease::getIsDeleted, 0)
            .eq(orgId != null, ElderDisease::getOrgId, orgId)
            .eq(ElderDisease::getElderId, elderId))
        .stream()
        .map(ElderDisease::getDiseaseId)
        .distinct()
        .toList();
    if (diseaseIds.isEmpty()) {
      return;
    }

    Set<Long> forbiddenTagIds = forbiddenTagMapper.selectList(Wrappers.lambdaQuery(DiseaseForbiddenTag.class)
            .eq(DiseaseForbiddenTag::getIsDeleted, 0)
            .eq(orgId != null, DiseaseForbiddenTag::getOrgId, orgId)
            .in(DiseaseForbiddenTag::getDiseaseId, diseaseIds))
        .stream()
        .map(DiseaseForbiddenTag::getTagId)
        .collect(Collectors.toSet());
    if (forbiddenTagIds.isEmpty()) {
      return;
    }

    Map<Long, ProductTag> tagMap = productTagMapper.selectList(Wrappers.lambdaQuery(ProductTag.class)
            .eq(ProductTag::getIsDeleted, 0)
            .eq(orgId != null, ProductTag::getOrgId, orgId)
            .in(ProductTag::getId, forbiddenTagIds))
        .stream()
        .collect(Collectors.toMap(ProductTag::getId, t -> t));

    Set<String> keywordTags = new HashSet<>();
    for (Long tagId : forbiddenTagIds) {
      ProductTag tag = tagMap.get(tagId);
      if (tag == null) {
        continue;
      }
      if (tag.getTagName() != null && !tag.getTagName().isBlank()) {
        keywordTags.add(tag.getTagName().toLowerCase(Locale.ROOT));
      }
      if (tag.getTagCode() != null && !tag.getTagCode().isBlank()) {
        keywordTags.add(tag.getTagCode().toLowerCase(Locale.ROOT));
      }
    }

    for (DiningDish dish : dishes) {
      Set<Long> dishTagIds = parseTagIds(dish.getTagIds());
      boolean matched = dishTagIds.stream().anyMatch(forbiddenTagIds::contains);
      if (!matched && !keywordTags.isEmpty()) {
        String tags = dish.getAllergenTags() == null ? "" : dish.getAllergenTags().toLowerCase(Locale.ROOT);
        matched = keywordTags.stream().anyMatch(tags::contains);
      }
      if (!matched) {
        continue;
      }
      blockedDishNames.add(dish.getDishName());
      reasons.add(new DiningRiskReasonItem("DISEASE_TAG", "FORBIDDEN_TAG", "命中疾病禁忌标签"));
    }
  }

  private void validateHealthThresholds(
      Long orgId,
      Long elderId,
      List<DiningDish> dishes,
      Set<String> blockedDishNames,
      List<DiningRiskReasonItem> reasons) {
    if (dishes.isEmpty()) {
      return;
    }
    HealthBasicRecord latest = healthBasicRecordMapper.selectOne(Wrappers.lambdaQuery(HealthBasicRecord.class)
        .eq(HealthBasicRecord::getIsDeleted, 0)
        .eq(orgId != null, HealthBasicRecord::getOrgId, orgId)
        .eq(HealthBasicRecord::getElderId, elderId)
        .orderByDesc(HealthBasicRecord::getRecordDate)
        .orderByDesc(HealthBasicRecord::getUpdateTime)
        .last("LIMIT 1"));
    if (latest == null) {
      return;
    }

    Map<String, BigDecimal> thresholds = loadThresholds(orgId);
    BigDecimal sbpMax = thresholds.getOrDefault("SBP_MAX", BigDecimal.valueOf(140));
    BigDecimal dbpMax = thresholds.getOrDefault("DBP_MAX", BigDecimal.valueOf(90));
    BigDecimal bmiMax = thresholds.getOrDefault("BMI_MAX", BigDecimal.valueOf(28));

    int[] bp = parseBloodPressure(latest.getBloodPressure());
    boolean highBp = bp[0] > 0 && bp[1] > 0
        && (BigDecimal.valueOf(bp[0]).compareTo(sbpMax) >= 0 || BigDecimal.valueOf(bp[1]).compareTo(dbpMax) >= 0);
    boolean highBmi = latest.getBmi() != null && latest.getBmi().compareTo(bmiMax) >= 0;

    for (DiningDish dish : dishes) {
      String nutrition = dish.getNutritionInfo() == null ? "" : dish.getNutritionInfo().toLowerCase(Locale.ROOT);
      String category = dish.getDishCategory() == null ? "" : dish.getDishCategory().toLowerCase(Locale.ROOT);
      if (highBp && containsAny(nutrition, "高盐", "咸", "重口")) {
        blockedDishNames.add(dish.getDishName());
        reasons.add(new DiningRiskReasonItem("HEALTH_BP", "HIGH_BP", "老人血压偏高，拦截高盐/重口味菜品"));
      }
      if (highBmi && (containsAny(nutrition, "高脂", "高糖", "油炸", "甜") || containsAny(category, "油炸", "甜品"))) {
        blockedDishNames.add(dish.getDishName());
        reasons.add(new DiningRiskReasonItem("HEALTH_BMI", "HIGH_BMI", "老人BMI偏高，拦截高脂/高糖菜品"));
      }
    }
  }

  private Map<String, BigDecimal> loadThresholds(Long orgId) {
    List<DiningRiskThresholdConfig> configs = thresholdConfigMapper.selectList(Wrappers.lambdaQuery(DiningRiskThresholdConfig.class)
        .eq(DiningRiskThresholdConfig::getIsDeleted, 0)
        .eq(orgId != null, DiningRiskThresholdConfig::getOrgId, orgId)
        .eq(DiningRiskThresholdConfig::getEnabled, 1));
    Map<String, BigDecimal> map = new HashMap<>();
    for (DiningRiskThresholdConfig config : configs) {
      if (config.getMetricCode() != null && config.getThresholdValue() != null) {
        map.put(config.getMetricCode().toUpperCase(Locale.ROOT), config.getThresholdValue());
      }
    }
    return map;
  }

  private Set<Long> parseTagIds(String tagIds) {
    if (tagIds == null || tagIds.isBlank()) {
      return Set.of();
    }
    Set<Long> ids = new HashSet<>();
    for (String item : tagIds.split(",")) {
      try {
        ids.add(Long.parseLong(item.trim()));
      } catch (NumberFormatException ignore) {
      }
    }
    return ids;
  }

  private boolean containsAny(String source, String... keywords) {
    for (String keyword : keywords) {
      if (source.contains(keyword)) {
        return true;
      }
    }
    return false;
  }

  private int[] parseBloodPressure(String bp) {
    if (bp == null || bp.isBlank()) {
      return new int[] {0, 0};
    }
    String normalized = bp.replace('／', '/').replace(" ", "");
    String[] parts = normalized.split("/");
    if (parts.length != 2) {
      return new int[] {0, 0};
    }
    try {
      int sbp = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
      int dbp = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
      return new int[] {sbp, dbp};
    } catch (NumberFormatException ex) {
      return new int[] {0, 0};
    }
  }
}
