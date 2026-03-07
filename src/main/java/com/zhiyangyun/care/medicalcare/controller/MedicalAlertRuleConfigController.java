package com.zhiyangyun.care.medicalcare.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.medicalcare.entity.MedicalAlertRuleConfig;
import com.zhiyangyun.care.medicalcare.mapper.MedicalAlertRuleConfigMapper;
import com.zhiyangyun.care.medicalcare.model.MedicalAlertRuleConfigRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalAlertRuleConfigResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medical-care/alert-rules")
public class MedicalAlertRuleConfigController {
  private final MedicalAlertRuleConfigMapper mapper;

  public MedicalAlertRuleConfigController(MedicalAlertRuleConfigMapper mapper) {
    this.mapper = mapper;
  }

  @GetMapping
  public Result<MedicalAlertRuleConfigResponse> getRule() {
    Long orgId = AuthContext.getOrgId();
    MedicalAlertRuleConfig config = mapper.selectOne(Wrappers.lambdaQuery(MedicalAlertRuleConfig.class)
        .eq(MedicalAlertRuleConfig::getIsDeleted, 0)
        .eq(orgId != null, MedicalAlertRuleConfig::getOrgId, orgId)
        .last("LIMIT 1"));
    return Result.ok(toResponse(config));
  }

  @PutMapping
  public Result<MedicalAlertRuleConfigResponse> saveRule(@Valid @RequestBody MedicalAlertRuleConfigRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    MedicalAlertRuleConfig config = mapper.selectOne(Wrappers.lambdaQuery(MedicalAlertRuleConfig.class)
        .eq(MedicalAlertRuleConfig::getIsDeleted, 0)
        .eq(orgId != null, MedicalAlertRuleConfig::getOrgId, orgId)
        .last("LIMIT 1"));
    if (config == null) {
      config = new MedicalAlertRuleConfig();
      config.setTenantId(orgId);
      config.setOrgId(orgId);
      config.setCreatedBy(staffId);
      applyRequest(config, request);
      mapper.insert(config);
    } else {
      applyRequest(config, request);
      config.setUpdatedBy(staffId);
      mapper.updateById(config);
    }
    return Result.ok(toResponse(config));
  }

  private void applyRequest(MedicalAlertRuleConfig config, MedicalAlertRuleConfigRequest request) {
    config.setMedicationHighDosageThreshold(normalizeThreshold(request.getMedicationHighDosageThreshold(), 10, 1, 999));
    config.setOverdueHoursThreshold(normalizeThreshold(request.getOverdueHoursThreshold(), 12, 1, 168));
    config.setAbnormalInspectionRequirePhoto(normalizeSwitch(request.getAbnormalInspectionRequirePhoto(), 1));
    config.setHandoverAutoFillConfirmTime(normalizeSwitch(request.getHandoverAutoFillConfirmTime(), 1));
    config.setAutoCreateNursingLogFromInspection(normalizeSwitch(request.getAutoCreateNursingLogFromInspection(), 1));
    config.setAutoRaiseTaskFromAbnormal(normalizeSwitch(request.getAutoRaiseTaskFromAbnormal(), 1));
    config.setAutoCarryResidentContext(normalizeSwitch(request.getAutoCarryResidentContext(), 1));
    config.setHandoverRiskKeywords(normalizeText(request.getHandoverRiskKeywords(), "异常,风险,事故,上报"));
  }

  private MedicalAlertRuleConfigResponse toResponse(MedicalAlertRuleConfig config) {
    MedicalAlertRuleConfigResponse response = new MedicalAlertRuleConfigResponse();
    if (config == null) {
      response.setMedicationHighDosageThreshold(10);
      response.setOverdueHoursThreshold(12);
      response.setAbnormalInspectionRequirePhoto(1);
      response.setHandoverAutoFillConfirmTime(1);
      response.setAutoCreateNursingLogFromInspection(1);
      response.setAutoRaiseTaskFromAbnormal(1);
      response.setAutoCarryResidentContext(1);
      response.setHandoverRiskKeywords("异常,风险,事故,上报");
      return response;
    }
    response.setMedicationHighDosageThreshold(normalizeThreshold(config.getMedicationHighDosageThreshold(), 10, 1, 999));
    response.setOverdueHoursThreshold(normalizeThreshold(config.getOverdueHoursThreshold(), 12, 1, 168));
    response.setAbnormalInspectionRequirePhoto(normalizeSwitch(config.getAbnormalInspectionRequirePhoto(), 1));
    response.setHandoverAutoFillConfirmTime(normalizeSwitch(config.getHandoverAutoFillConfirmTime(), 1));
    response.setAutoCreateNursingLogFromInspection(normalizeSwitch(config.getAutoCreateNursingLogFromInspection(), 1));
    response.setAutoRaiseTaskFromAbnormal(normalizeSwitch(config.getAutoRaiseTaskFromAbnormal(), 1));
    response.setAutoCarryResidentContext(normalizeSwitch(config.getAutoCarryResidentContext(), 1));
    response.setHandoverRiskKeywords(normalizeText(config.getHandoverRiskKeywords(), "异常,风险,事故,上报"));
    return response;
  }

  private Integer normalizeThreshold(Integer value, int defaultValue, int min, int max) {
    if (value == null) {
      return defaultValue;
    }
    if (value < min) {
      return min;
    }
    if (value > max) {
      return max;
    }
    return value;
  }

  private Integer normalizeSwitch(Integer value, int defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    return value == 0 ? 0 : 1;
  }

  private String normalizeText(String value, String fallback) {
    if (value == null) {
      return fallback;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? fallback : trimmed;
  }
}
