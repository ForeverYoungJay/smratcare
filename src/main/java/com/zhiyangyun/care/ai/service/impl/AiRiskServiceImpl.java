package com.zhiyangyun.care.ai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.ai.entity.AiRiskModel;
import com.zhiyangyun.care.ai.entity.AiRiskScore;
import com.zhiyangyun.care.ai.mapper.AiRiskModelMapper;
import com.zhiyangyun.care.ai.mapper.AiRiskScoreMapper;
import com.zhiyangyun.care.ai.model.AiRiskEvaluation;
import com.zhiyangyun.care.ai.model.AiRiskSummaryResponse;
import com.zhiyangyun.care.ai.model.AiRiskTrendPointResponse;
import com.zhiyangyun.care.ai.service.AiRiskRuleEngine;
import com.zhiyangyun.care.ai.service.AiRiskService;
import com.zhiyangyun.care.assessment.entity.AssessmentRecord;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderMedicalOutingRecord;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderMedicalOutingRecordMapper;
import com.zhiyangyun.care.health.entity.HealthArchive;
import com.zhiyangyun.care.health.entity.HealthDataRecord;
import com.zhiyangyun.care.health.entity.HealthMedicationRegistration;
import com.zhiyangyun.care.health.mapper.HealthArchiveMapper;
import com.zhiyangyun.care.health.mapper.HealthDataRecordMapper;
import com.zhiyangyun.care.health.mapper.HealthMedicationRegistrationMapper;
import com.zhiyangyun.care.life.entity.IncidentReport;
import com.zhiyangyun.care.life.mapper.IncidentReportMapper;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.mapper.SmartAlertMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AiRiskServiceImpl implements AiRiskService {
  private static final List<String> RISK_TYPES = List.of("FALL", "PRESSURE_ULCER", "READMISSION");
  private static final List<String> SEDATIVE_KEYWORDS = List.of(
      "地西泮", "艾司唑仑", "阿普唑仑", "劳拉西泮", "氯硝西泮", "唑吡坦", "佐匹克隆", "镇静", "安眠", "安定");
  private static final DateTimeFormatter ALERT_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final AiRiskModelMapper modelMapper;
  private final AiRiskScoreMapper scoreMapper;
  private final AiRiskRuleEngine ruleEngine;
  private final ElderMapper elderMapper;
  private final IncidentReportMapper incidentReportMapper;
  private final HealthMedicationRegistrationMapper medicationRegistrationMapper;
  private final AssessmentRecordMapper assessmentRecordMapper;
  private final HealthArchiveMapper healthArchiveMapper;
  private final HealthDataRecordMapper healthDataRecordMapper;
  private final ElderMedicalOutingRecordMapper medicalOutingRecordMapper;
  private final SmartAlertMapper smartAlertMapper;
  private final ObjectMapper objectMapper;

  public AiRiskServiceImpl(
      AiRiskModelMapper modelMapper,
      AiRiskScoreMapper scoreMapper,
      AiRiskRuleEngine ruleEngine,
      ElderMapper elderMapper,
      IncidentReportMapper incidentReportMapper,
      HealthMedicationRegistrationMapper medicationRegistrationMapper,
      AssessmentRecordMapper assessmentRecordMapper,
      HealthArchiveMapper healthArchiveMapper,
      HealthDataRecordMapper healthDataRecordMapper,
      ElderMedicalOutingRecordMapper medicalOutingRecordMapper,
      SmartAlertMapper smartAlertMapper,
      ObjectMapper objectMapper) {
    this.modelMapper = modelMapper;
    this.scoreMapper = scoreMapper;
    this.ruleEngine = ruleEngine;
    this.elderMapper = elderMapper;
    this.incidentReportMapper = incidentReportMapper;
    this.medicationRegistrationMapper = medicationRegistrationMapper;
    this.assessmentRecordMapper = assessmentRecordMapper;
    this.healthArchiveMapper = healthArchiveMapper;
    this.healthDataRecordMapper = healthDataRecordMapper;
    this.medicalOutingRecordMapper = medicalOutingRecordMapper;
    this.smartAlertMapper = smartAlertMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public List<AiRiskModel> listModels(Long orgId) {
    List<AiRiskModel> orgModels = modelMapper.selectList(Wrappers.lambdaQuery(AiRiskModel.class)
        .eq(AiRiskModel::getIsDeleted, 0)
        .eq(orgId != null, AiRiskModel::getOrgId, orgId));
    List<AiRiskModel> globals = modelMapper.selectList(Wrappers.lambdaQuery(AiRiskModel.class)
        .eq(AiRiskModel::getIsDeleted, 0)
        .isNull(AiRiskModel::getOrgId));
    Map<String, AiRiskModel> byType = new LinkedHashMap<>();
    for (AiRiskModel model : globals) {
      byType.put(model.getRiskType(), model);
    }
    for (AiRiskModel model : orgModels) {
      byType.put(model.getRiskType(), model);
    }
    return new ArrayList<>(byType.values());
  }

  @Override
  public AiRiskModel saveModel(Long orgId, Long staffId, AiRiskModel request) {
    if (request.getRiskType() == null || request.getRiskType().isBlank()) {
      throw new IllegalArgumentException("riskType 不能为空");
    }
    String riskType = request.getRiskType().toUpperCase(Locale.ROOT);
    AiRiskModel existing = modelMapper.selectOne(Wrappers.lambdaQuery(AiRiskModel.class)
        .eq(AiRiskModel::getIsDeleted, 0)
        .eq(orgId != null, AiRiskModel::getOrgId, orgId)
        .eq(AiRiskModel::getRiskType, riskType)
        .orderByDesc(AiRiskModel::getId)
        .last("LIMIT 1"));
    if (existing == null) {
      existing = new AiRiskModel();
      existing.setTenantId(orgId);
      existing.setOrgId(orgId);
      existing.setRiskType(riskType);
      existing.setCreatedBy(staffId);
    }
    if (request.getModelName() != null && !request.getModelName().isBlank()) {
      existing.setModelName(request.getModelName());
    } else if (existing.getModelName() == null) {
      existing.setModelName(riskType + " 自定义模型");
    }
    if (request.getRuleJson() != null && !request.getRuleJson().isBlank()) {
      validateRuleJson(request.getRuleJson());
      existing.setRuleJson(request.getRuleJson());
    }
    existing.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
    existing.setRemark(request.getRemark());
    if (existing.getId() == null) {
      if (existing.getRuleJson() == null || existing.getRuleJson().isBlank()) {
        throw new IllegalArgumentException("ruleJson 不能为空");
      }
      modelMapper.insert(existing);
    } else {
      modelMapper.updateById(existing);
    }
    return existing;
  }

  @Override
  public int recompute(Long orgId, Long staffId, Long elderId, String riskType) {
    List<AiRiskModel> models = listModels(orgId).stream()
        .filter(m -> m.getEnabled() == null || m.getEnabled() == 1)
        .filter(m -> riskType == null || riskType.isBlank() || riskType.equalsIgnoreCase(m.getRiskType()))
        .toList();
    if (models.isEmpty()) {
      return 0;
    }
    List<ElderProfile> elders = loadElders(orgId, elderId);
    int count = 0;
    for (ElderProfile elder : elders) {
      Map<String, Double> factors = collectFactors(orgId, elder);
      for (AiRiskModel model : models) {
        AiRiskEvaluation evaluation = ruleEngine.evaluate(model.getRuleJson(), factors);
        AiRiskScore score = upsertScore(orgId, staffId, elder, model, evaluation);
        if ("HIGH".equals(score.getRiskLevel()) && score.getAlertId() == null) {
          Long alertId = createHighRiskAlert(orgId, elder, score);
          if (alertId != null) {
            score.setAlertId(alertId);
            scoreMapper.updateById(score);
          }
        }
        count++;
      }
    }
    return count;
  }

  @Override
  public IPage<AiRiskScore> pageScores(Long orgId, long pageNo, long pageSize,
      String riskType, String riskLevel, Long elderId, String keyword) {
    LocalDate latestDate = latestAssessDate(orgId);
    if (latestDate == null) {
      return new Page<>(pageNo, pageSize, 0);
    }
    return scoreMapper.selectPage(new Page<>(pageNo, pageSize),
        Wrappers.lambdaQuery(AiRiskScore.class)
            .eq(AiRiskScore::getIsDeleted, 0)
            .eq(orgId != null, AiRiskScore::getOrgId, orgId)
            .eq(AiRiskScore::getAssessDate, latestDate)
            .eq(riskType != null && !riskType.isBlank(), AiRiskScore::getRiskType, riskType)
            .eq(riskLevel != null && !riskLevel.isBlank(), AiRiskScore::getRiskLevel, riskLevel)
            .eq(elderId != null, AiRiskScore::getElderId, elderId)
            .like(keyword != null && !keyword.isBlank(), AiRiskScore::getElderName, keyword)
            .orderByDesc(AiRiskScore::getScore));
  }

  @Override
  public AiRiskSummaryResponse summary(Long orgId) {
    AiRiskSummaryResponse response = new AiRiskSummaryResponse();
    LocalDate latestDate = latestAssessDate(orgId);
    if (latestDate == null) {
      return response;
    }
    List<AiRiskScore> scores = scoreMapper.selectList(Wrappers.lambdaQuery(AiRiskScore.class)
        .eq(AiRiskScore::getIsDeleted, 0)
        .eq(orgId != null, AiRiskScore::getOrgId, orgId)
        .eq(AiRiskScore::getAssessDate, latestDate));
    response.setElderCount(scores.stream().map(AiRiskScore::getElderId).distinct().count());
    response.setHighCount(scores.stream().filter(s -> "HIGH".equals(s.getRiskLevel())).count());
    response.setMediumCount(scores.stream().filter(s -> "MEDIUM".equals(s.getRiskLevel())).count());
    response.setLowCount(scores.stream().filter(s -> "LOW".equals(s.getRiskLevel())).count());
    for (String type : RISK_TYPES) {
      AiRiskSummaryResponse.TypeStat stat = new AiRiskSummaryResponse.TypeStat();
      stat.setRiskType(type);
      stat.setHighCount(scores.stream()
          .filter(s -> type.equals(s.getRiskType()) && "HIGH".equals(s.getRiskLevel())).count());
      stat.setMediumCount(scores.stream()
          .filter(s -> type.equals(s.getRiskType()) && "MEDIUM".equals(s.getRiskLevel())).count());
      stat.setLowCount(scores.stream()
          .filter(s -> type.equals(s.getRiskType()) && "LOW".equals(s.getRiskLevel())).count());
      response.getTypeStats().add(stat);
    }
    return response;
  }

  @Override
  public List<AiRiskTrendPointResponse> trend(Long orgId, Long elderId, String riskType, int days) {
    if (elderId == null || riskType == null || riskType.isBlank()) {
      return List.of();
    }
    LocalDate from = LocalDate.now().minusDays(Math.max(days, 7));
    return scoreMapper.selectList(Wrappers.lambdaQuery(AiRiskScore.class)
            .eq(AiRiskScore::getIsDeleted, 0)
            .eq(orgId != null, AiRiskScore::getOrgId, orgId)
            .eq(AiRiskScore::getElderId, elderId)
            .eq(AiRiskScore::getRiskType, riskType)
            .ge(AiRiskScore::getAssessDate, from)
            .orderByAsc(AiRiskScore::getAssessDate)).stream()
        .map(score -> {
          AiRiskTrendPointResponse point = new AiRiskTrendPointResponse();
          point.setAssessDate(score.getAssessDate());
          point.setScore(score.getScore());
          point.setRiskLevel(score.getRiskLevel());
          return point;
        }).toList();
  }

  private LocalDate latestAssessDate(Long orgId) {
    AiRiskScore latest = scoreMapper.selectOne(Wrappers.lambdaQuery(AiRiskScore.class)
        .eq(AiRiskScore::getIsDeleted, 0)
        .eq(orgId != null, AiRiskScore::getOrgId, orgId)
        .orderByDesc(AiRiskScore::getAssessDate)
        .orderByDesc(AiRiskScore::getId)
        .last("LIMIT 1"));
    return latest == null ? null : latest.getAssessDate();
  }

  private List<ElderProfile> loadElders(Long orgId, Long elderId) {
    if (elderId != null) {
      ElderProfile elder = elderMapper.selectById(elderId);
      if (elder == null || (elder.getIsDeleted() != null && elder.getIsDeleted() == 1)
          || (orgId != null && !orgId.equals(elder.getOrgId()))) {
        return List.of();
      }
      return List.of(elder);
    }
    return elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getStatus, 1));
  }

  /** 汇集各业务模块数据为风险因子取值（缺失因子不放入 map，评分时跳过）。 */
  private Map<String, Double> collectFactors(Long orgId, ElderProfile elder) {
    Map<String, Double> factors = new HashMap<>();
    LocalDateTime now = LocalDateTime.now();

    if (elder.getBirthDate() != null) {
      factors.put("AGE", (double) Period.between(elder.getBirthDate(), LocalDate.now()).getYears());
    }

    // 近90天跌倒史：不良事件上报
    Long fallCount = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(IncidentReport::getElderId, elder.getId())
        .ge(IncidentReport::getIncidentTime, now.minusDays(90))
        .and(w -> w.like(IncidentReport::getIncidentType, "跌倒")
            .or().eq(IncidentReport::getIncidentType, "FALL")));
    factors.put("FALL_HISTORY_90D", fallCount == null ? 0d : fallCount.doubleValue());

    // 近30天镇静类用药：用药登记
    List<HealthMedicationRegistration> medications = medicationRegistrationMapper.selectList(
        Wrappers.lambdaQuery(HealthMedicationRegistration.class)
            .eq(HealthMedicationRegistration::getIsDeleted, 0)
            .eq(orgId != null, HealthMedicationRegistration::getOrgId, orgId)
            .eq(HealthMedicationRegistration::getElderId, elder.getId())
            .ge(HealthMedicationRegistration::getRegisterTime, now.minusDays(30)));
    long sedativeCount = medications.stream()
        .filter(m -> m.getDrugName() != null
            && SEDATIVE_KEYWORDS.stream().anyMatch(k -> m.getDrugName().contains(k)))
        .count();
    factors.put("SEDATIVE_MED_30D", (double) sedativeCount);

    // 失能等级：最新评估记录 levelCode，兜底长者照护等级
    AssessmentRecord assessment = assessmentRecordMapper.selectOne(
        Wrappers.lambdaQuery(AssessmentRecord.class)
            .eq(AssessmentRecord::getIsDeleted, 0)
            .eq(orgId != null, AssessmentRecord::getOrgId, orgId)
            .eq(AssessmentRecord::getElderId, elder.getId())
            .eq(AssessmentRecord::getStatus, "COMPLETED")
            .orderByDesc(AssessmentRecord::getAssessmentDate)
            .orderByDesc(AssessmentRecord::getId)
            .last("LIMIT 1"));
    Double disability = resolveDisabilityLevel(
        assessment == null ? null : assessment.getLevelCode(), elder.getCareLevel());
    if (disability != null) {
      factors.put("DISABILITY_LEVEL", disability);
    }

    // 健康档案：卧床 / 失禁 / 慢病数量
    HealthArchive archive = healthArchiveMapper.selectOne(Wrappers.lambdaQuery(HealthArchive.class)
        .eq(HealthArchive::getIsDeleted, 0)
        .eq(orgId != null, HealthArchive::getOrgId, orgId)
        .eq(HealthArchive::getElderId, elder.getId())
        .orderByDesc(HealthArchive::getId)
        .last("LIMIT 1"));
    String archiveText = archive == null ? ""
        : safe(archive.getChronicDisease()) + safe(archive.getMedicalHistory()) + safe(archive.getRehabilitationRecord());
    String careLevelText = safe(elder.getCareLevel());
    factors.put("BEDRIDDEN",
        archiveText.contains("卧床") || careLevelText.contains("特级") || careLevelText.contains("特护") ? 1d : 0d);
    factors.put("INCONTINENCE", archiveText.contains("失禁") ? 1d : 0d);
    factors.put("CHRONIC_DISEASE_COUNT", (double) countChronicDiseases(
        archive == null ? null : archive.getChronicDisease()));

    // 健康数据：BMI / 白蛋白（如有）
    Double bmi = latestNumericHealthData(orgId, elder.getId(), List.of("BMI", "bmi"));
    if (bmi != null) {
      factors.put("BMI", bmi);
    }
    Double albumin = latestNumericHealthData(orgId, elder.getId(), List.of("ALBUMIN", "白蛋白"));
    if (albumin != null) {
      factors.put("ALBUMIN", albumin);
    }

    // 近半年住院/就医外出次数
    Long hospitalization = medicalOutingRecordMapper.selectCount(
        Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
            .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
            .eq(orgId != null, ElderMedicalOutingRecord::getOrgId, orgId)
            .eq(ElderMedicalOutingRecord::getElderId, elder.getId())
            .ge(ElderMedicalOutingRecord::getOutingDate, LocalDate.now().minusDays(180)));
    factors.put("HOSPITALIZATION_180D", hospitalization == null ? 0d : hospitalization.doubleValue());

    return factors;
  }

  /** 失能等级归一：3=重度/完全失能，2=中度，1=轻度，0=能力完好。 */
  private Double resolveDisabilityLevel(String levelCode, String careLevel) {
    String text = safe(levelCode) + safe(careLevel);
    if (text.isBlank()) {
      return null;
    }
    if (text.contains("重度") || text.contains("完全") || text.contains("特级") || text.contains("特护")) {
      return 3d;
    }
    if (text.contains("中度") || text.contains("半失能")) {
      return 2d;
    }
    if (text.contains("轻度") || text.contains("介助")) {
      return 1d;
    }
    return 0d;
  }

  private int countChronicDiseases(String chronicDisease) {
    if (chronicDisease == null || chronicDisease.isBlank()) {
      return 0;
    }
    return (int) java.util.Arrays.stream(chronicDisease.split("[、，,;；/\\s]+"))
        .filter(s -> !s.isBlank())
        .filter(s -> !s.equals("无"))
        .count();
  }

  private Double latestNumericHealthData(Long orgId, Long elderId, List<String> dataTypes) {
    HealthDataRecord record = healthDataRecordMapper.selectOne(Wrappers.lambdaQuery(HealthDataRecord.class)
        .eq(HealthDataRecord::getIsDeleted, 0)
        .eq(orgId != null, HealthDataRecord::getOrgId, orgId)
        .eq(HealthDataRecord::getElderId, elderId)
        .in(HealthDataRecord::getDataType, dataTypes)
        .orderByDesc(HealthDataRecord::getMeasuredAt)
        .orderByDesc(HealthDataRecord::getId)
        .last("LIMIT 1"));
    if (record == null || record.getDataValue() == null) {
      return null;
    }
    try {
      return Double.parseDouble(record.getDataValue().replaceAll("[^0-9.\\-]", ""));
    } catch (Exception ex) {
      return null;
    }
  }

  private AiRiskScore upsertScore(Long orgId, Long staffId, ElderProfile elder,
      AiRiskModel model, AiRiskEvaluation evaluation) {
    LocalDate today = LocalDate.now();
    AiRiskScore score = scoreMapper.selectOne(Wrappers.lambdaQuery(AiRiskScore.class)
        .eq(AiRiskScore::getIsDeleted, 0)
        .eq(orgId != null, AiRiskScore::getOrgId, orgId)
        .eq(AiRiskScore::getElderId, elder.getId())
        .eq(AiRiskScore::getRiskType, model.getRiskType())
        .eq(AiRiskScore::getAssessDate, today)
        .orderByDesc(AiRiskScore::getId)
        .last("LIMIT 1"));
    if (score == null) {
      score = new AiRiskScore();
      score.setTenantId(orgId);
      score.setOrgId(orgId);
      score.setElderId(elder.getId());
      score.setRiskType(model.getRiskType());
      score.setAssessDate(today);
      score.setCreatedBy(staffId);
    }
    score.setElderName(elder.getFullName());
    score.setModelId(model.getId());
    score.setScore(BigDecimal.valueOf(evaluation.getScore()));
    score.setRiskLevel(evaluation.getLevel());
    score.setFactorJson(writeJson(evaluation.getFactors()));
    score.setAssessTime(LocalDateTime.now());
    score.setSource(staffId == null ? "AUTO" : "MANUAL");
    if (score.getId() == null) {
      scoreMapper.insert(score);
    } else {
      scoreMapper.updateById(score);
    }
    return score;
  }

  /** 高风险自动预警：复用 smart 告警中心（去重：同长者同类型存在未关闭的 AI 风险告警则不重复生成）。 */
  private Long createHighRiskAlert(Long orgId, ElderProfile elder, AiRiskScore score) {
    String alertType = "AI_RISK_" + score.getRiskType();
    SmartAlert existing = smartAlertMapper.selectOne(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(orgId != null, SmartAlert::getOrgId, orgId)
        .eq(SmartAlert::getElderId, elder.getId())
        .eq(SmartAlert::getAlertType, alertType)
        .ne(SmartAlert::getStatus, "RESOLVED")
        .orderByDesc(SmartAlert::getId)
        .last("LIMIT 1"));
    if (existing != null) {
      return existing.getId();
    }
    LocalDateTime now = LocalDateTime.now();
    SmartAlert alert = new SmartAlert();
    alert.setTenantId(orgId);
    alert.setOrgId(orgId);
    alert.setElderId(elder.getId());
    alert.setAlertNo("AI-" + now.format(ALERT_NO_FORMATTER) + "-" + Math.abs((elder.getId() + score.getRiskType()).hashCode() % 10000));
    alert.setAlertType(alertType);
    alert.setLevel("HIGH");
    alert.setTitle(riskTypeName(score.getRiskType()) + "高风险：" + safe(elder.getFullName()));
    alert.setContent("AI 风险评分 " + score.getScore() + " 分（等级 HIGH），请及时评估干预。来源：AI 健康风险预测");
    alert.setStatus("OPEN");
    alert.setFirstTriggeredAt(now);
    alert.setLatestTriggeredAt(now);
    alert.setEscalationCount(0);
    alert.setNotifyFamily(0);
    alert.setCreatedBy(0L);
    alert.setIsDeleted(0);
    smartAlertMapper.insert(alert);
    return alert.getId();
  }

  private String riskTypeName(String riskType) {
    return switch (riskType == null ? "" : riskType) {
      case "FALL" -> "跌倒";
      case "PRESSURE_ULCER" -> "压疮";
      case "READMISSION" -> "再入院";
      default -> riskType;
    };
  }

  private void validateRuleJson(String ruleJson) {
    try {
      var node = objectMapper.readTree(ruleJson);
      if (!node.path("factors").isArray() || !node.path("levels").isArray()) {
        throw new IllegalArgumentException("ruleJson 必须包含 factors 与 levels 数组");
      }
    } catch (IllegalArgumentException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IllegalArgumentException("ruleJson 不是合法 JSON");
    }
  }

  private String writeJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception ex) {
      return "[]";
    }
  }

  private String safe(String value) {
    return value == null ? "" : value;
  }
}
