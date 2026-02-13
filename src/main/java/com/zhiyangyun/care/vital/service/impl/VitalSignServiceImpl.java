package com.zhiyangyun.care.vital.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.vital.entity.VitalSignRecord;
import com.zhiyangyun.care.vital.entity.VitalThresholdConfig;
import com.zhiyangyun.care.vital.mapper.VitalSignRecordMapper;
import com.zhiyangyun.care.vital.mapper.VitalThresholdConfigMapper;
import com.zhiyangyun.care.vital.model.VitalRecordRequest;
import com.zhiyangyun.care.vital.model.VitalRecordResponse;
import com.zhiyangyun.care.vital.service.VitalSignService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class VitalSignServiceImpl implements VitalSignService {
  private final VitalSignRecordMapper recordMapper;
  private final VitalThresholdConfigMapper thresholdMapper;
  private final ElderMapper elderMapper;
  private final StaffMapper staffMapper;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public VitalSignServiceImpl(
      VitalSignRecordMapper recordMapper,
      VitalThresholdConfigMapper thresholdMapper,
      ElderMapper elderMapper,
      StaffMapper staffMapper) {
    this.recordMapper = recordMapper;
    this.thresholdMapper = thresholdMapper;
    this.elderMapper = elderMapper;
    this.staffMapper = staffMapper;
  }

  @Override
  public VitalRecordResponse record(VitalRecordRequest request) {
    ElderProfile elder = elderMapper.selectById(request.getElderId());
    if (elder == null) {
      throw new IllegalArgumentException("Elder not found");
    }

    String valueJson = toJson(request.getValueJson());
    AbnormalResult abnormal = evaluateAbnormal(elder.getOrgId(), request.getType(), valueJson);

    VitalSignRecord record = new VitalSignRecord();
    record.setOrgId(elder.getOrgId());
    record.setElderId(request.getElderId());
    record.setType(request.getType());
    record.setValueJson(valueJson);
    record.setMeasuredAt(request.getMeasuredAt());
    record.setRecordedByStaffId(request.getRecordedByStaffId());
    record.setAbnormalFlag(abnormal.abnormal ? 1 : 0);
    record.setRemark(request.getRemark());
    recordMapper.insert(record);

    String elderName = elder == null ? null : elder.getFullName();
    String staffName = resolveStaffName(record.getRecordedByStaffId());
    return toResponse(record, abnormal.reason, elderName, staffName);
  }

  @Override
  public List<VitalRecordResponse> listByElder(Long elderId, LocalDateTime from, LocalDateTime to, String type) {
    var wrapper = Wrappers.lambdaQuery(VitalSignRecord.class)
        .eq(VitalSignRecord::getElderId, elderId)
        .eq(VitalSignRecord::getIsDeleted, 0)
        .eq(type != null && !type.isBlank(), VitalSignRecord::getType, type)
        .ge(from != null, VitalSignRecord::getMeasuredAt, from)
        .le(to != null, VitalSignRecord::getMeasuredAt, to)
        .orderByDesc(VitalSignRecord::getMeasuredAt);
    List<VitalSignRecord> records = recordMapper.selectList(wrapper);
    ElderProfile elder = elderMapper.selectById(elderId);
    String elderName = elder == null ? null : elder.getFullName();
    Map<Long, String> staffNameMap = resolveStaffNames(records);
    List<VitalRecordResponse> responses = new ArrayList<>();
    for (VitalSignRecord record : records) {
      AbnormalResult abnormal = evaluateAbnormal(record.getOrgId(), record.getType(), record.getValueJson());
      responses.add(toResponse(record, abnormal.reason, elderName, staffNameMap.get(record.getRecordedByStaffId())));
    }
    return responses;
  }

  @Override
  public VitalRecordResponse latest(Long elderId) {
    VitalSignRecord record = recordMapper.selectOne(
        Wrappers.lambdaQuery(VitalSignRecord.class)
            .eq(VitalSignRecord::getElderId, elderId)
            .eq(VitalSignRecord::getIsDeleted, 0)
            .orderByDesc(VitalSignRecord::getMeasuredAt)
            .last("LIMIT 1"));
    if (record == null) {
      return null;
    }
    AbnormalResult abnormal = evaluateAbnormal(record.getOrgId(), record.getType(), record.getValueJson());
    ElderProfile elder = elderMapper.selectById(record.getElderId());
    String elderName = elder == null ? null : elder.getFullName();
    String staffName = resolveStaffName(record.getRecordedByStaffId());
    return toResponse(record, abnormal.reason, elderName, staffName);
  }

  @Override
  public List<VitalRecordResponse> abnormalToday() {
    LocalDate today = LocalDate.now();
    LocalDateTime start = today.atStartOfDay();
    LocalDateTime end = today.plusDays(1).atStartOfDay();
    List<VitalSignRecord> records = recordMapper.selectList(
        Wrappers.lambdaQuery(VitalSignRecord.class)
            .eq(VitalSignRecord::getAbnormalFlag, 1)
            .ge(VitalSignRecord::getMeasuredAt, start)
            .lt(VitalSignRecord::getMeasuredAt, end)
            .eq(VitalSignRecord::getIsDeleted, 0)
            .orderByDesc(VitalSignRecord::getMeasuredAt));
    Map<Long, String> elderNameMap = resolveElderNames(records);
    Map<Long, String> staffNameMap = resolveStaffNames(records);
    List<VitalRecordResponse> responses = new ArrayList<>();
    for (VitalSignRecord record : records) {
      AbnormalResult abnormal = evaluateAbnormal(record.getOrgId(), record.getType(), record.getValueJson());
      responses.add(toResponse(record, abnormal.reason,
          elderNameMap.get(record.getElderId()),
          staffNameMap.get(record.getRecordedByStaffId())));
    }
    return responses;
  }

  private VitalRecordResponse toResponse(VitalSignRecord record, String abnormalReason, String elderName,
                                         String staffName) {
    VitalRecordResponse response = new VitalRecordResponse();
    response.setId(record.getId());
    response.setElderId(record.getElderId());
    response.setElderName(elderName);
    response.setType(record.getType());
    response.setValueJson(record.getValueJson());
    response.setMeasuredAt(record.getMeasuredAt());
    response.setRecordedByStaffId(record.getRecordedByStaffId());
    response.setRecordedByStaffName(staffName);
    response.setAbnormalFlag(record.getAbnormalFlag() != null && record.getAbnormalFlag() == 1);
    response.setAbnormalReason(abnormalReason);
    response.setRemark(record.getRemark());
    return response;
  }

  private String resolveStaffName(Long staffId) {
    if (staffId == null) {
      return null;
    }
    StaffAccount staff = staffMapper.selectById(staffId);
    if (staff == null || staff.getIsDeleted() != null && staff.getIsDeleted() == 1) {
      return null;
    }
    return staff.getRealName();
  }

  private Map<Long, String> resolveStaffNames(List<VitalSignRecord> records) {
    List<Long> staffIds = records.stream()
        .map(VitalSignRecord::getRecordedByStaffId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    if (staffIds.isEmpty()) {
      return Map.of();
    }
    return staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
            .in(StaffAccount::getId, staffIds)
            .eq(StaffAccount::getIsDeleted, 0))
        .stream()
        .collect(java.util.stream.Collectors.toMap(StaffAccount::getId, StaffAccount::getRealName, (a, b) -> a));
  }

  private Map<Long, String> resolveElderNames(List<VitalSignRecord> records) {
    List<Long> elderIds = records.stream()
        .map(VitalSignRecord::getElderId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    if (elderIds.isEmpty()) {
      return Map.of();
    }
    return elderMapper.selectBatchIds(elderIds)
        .stream()
        .collect(java.util.stream.Collectors.toMap(ElderProfile::getId, ElderProfile::getFullName, (a, b) -> a));
  }

  private AbnormalResult evaluateAbnormal(Long orgId, String type, String valueJson) {
    Map<String, VitalThresholdConfig> thresholds = loadThresholds(orgId, type);
    if (thresholds.isEmpty()) {
      return AbnormalResult.normal();
    }

    try {
      JsonNode node = objectMapper.readTree(valueJson);
      if ("BP".equalsIgnoreCase(type)) {
        StringBuilder reason = new StringBuilder();
        boolean abnormal = false;
        abnormal |= checkMetric("SBP", node.path("sbp"), thresholds, reason);
        abnormal |= checkMetric("DBP", node.path("dbp"), thresholds, reason);
        if (abnormal) {
          return new AbnormalResult(true, reason.toString().trim());
        }
        return AbnormalResult.normal();
      }

      String metric = metricForType(type);
      JsonNode valueNode = node.path(metricToJsonField(metric));
      boolean abnormal = checkMetric(metric, valueNode, thresholds, new StringBuilder());
      if (abnormal) {
        String reason = buildReason(metric, valueNode, thresholds);
        return new AbnormalResult(true, reason);
      }
      return AbnormalResult.normal();
    } catch (Exception ex) {
      return AbnormalResult.normal();
    }
  }

  private String toJson(JsonNode node) {
    if (node == null) {
      return "{}";
    }
    try {
      return objectMapper.writeValueAsString(node);
    } catch (Exception ex) {
      return "{}";
    }
  }

  private boolean checkMetric(String metricCode, JsonNode valueNode, Map<String, VitalThresholdConfig> thresholds,
      StringBuilder reason) {
    VitalThresholdConfig config = thresholds.get(metricCode);
    if (config == null) {
      config = thresholds.get("DEFAULT");
    }
    if (config == null || valueNode == null || valueNode.isMissingNode() || !valueNode.isNumber()) {
      return false;
    }
    BigDecimal value = valueNode.decimalValue();
    if (config.getMinValue() != null && value.compareTo(config.getMinValue()) < 0) {
      reason.append(metricCode).append(" below ")
          .append(config.getMinValue()).append(" ");
      return true;
    }
    if (config.getMaxValue() != null && value.compareTo(config.getMaxValue()) > 0) {
      reason.append(metricCode).append(" above ")
          .append(config.getMaxValue()).append(" ");
      return true;
    }
    return false;
  }

  private String buildReason(String metricCode, JsonNode valueNode, Map<String, VitalThresholdConfig> thresholds) {
    VitalThresholdConfig config = thresholds.get(metricCode);
    if (config == null) {
      config = thresholds.get("DEFAULT");
    }
    if (config == null) {
      return null;
    }
    return metricCode + " out of range (" + config.getMinValue() + "-" + config.getMaxValue() + ") value="
        + (valueNode == null || valueNode.isMissingNode() ? "null" : valueNode.asText());
  }

  private Map<String, VitalThresholdConfig> loadThresholds(Long orgId, String type) {
    List<VitalThresholdConfig> configs = thresholdMapper.selectList(
        Wrappers.lambdaQuery(VitalThresholdConfig.class)
            .eq(VitalThresholdConfig::getOrgId, orgId)
            .eq(VitalThresholdConfig::getType, type)
            .eq(VitalThresholdConfig::getStatus, 1)
            .eq(VitalThresholdConfig::getIsDeleted, 0));

    Map<String, VitalThresholdConfig> map = new HashMap<>();
    for (VitalThresholdConfig config : configs) {
      String key = config.getMetricCode() == null ? "DEFAULT" : config.getMetricCode().toUpperCase();
      map.put(key, config);
    }
    return map;
  }

  private String metricForType(String type) {
    if (type == null) {
      return "DEFAULT";
    }
    return type.toUpperCase();
  }

  private String metricToJsonField(String metric) {
    if ("TEMP".equals(metric)) {
      return "temp";
    }
    if ("BS".equals(metric)) {
      return "glucose";
    }
    if ("SPO2".equals(metric)) {
      return "spo2";
    }
    if ("HR".equals(metric)) {
      return "hr";
    }
    return metric.toLowerCase();
  }

  private static class AbnormalResult {
    private final boolean abnormal;
    private final String reason;

    private AbnormalResult(boolean abnormal, String reason) {
      this.abnormal = abnormal;
      this.reason = reason;
    }

    static AbnormalResult normal() {
      return new AbnormalResult(false, null);
    }
  }
}
