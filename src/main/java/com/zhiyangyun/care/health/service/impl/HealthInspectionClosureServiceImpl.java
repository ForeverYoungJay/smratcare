package com.zhiyangyun.care.health.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.health.entity.HealthInspection;
import com.zhiyangyun.care.health.entity.HealthNursingLog;
import com.zhiyangyun.care.health.mapper.HealthInspectionMapper;
import com.zhiyangyun.care.health.mapper.HealthNursingLogMapper;
import com.zhiyangyun.care.health.service.HealthInspectionClosureService;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthInspectionClosureServiceImpl implements HealthInspectionClosureService {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final HealthInspectionMapper inspectionMapper;
  private final HealthNursingLogMapper nursingLogMapper;

  public HealthInspectionClosureServiceImpl(HealthInspectionMapper inspectionMapper, HealthNursingLogMapper nursingLogMapper) {
    this.inspectionMapper = inspectionMapper;
    this.nursingLogMapper = nursingLogMapper;
  }

  @Override
  @Transactional
  public void syncFromInspection(HealthInspection inspection, Long createdBy) {
    if (inspection == null || inspection.getId() == null || inspection.getOrgId() == null || !isAbnormal(inspection)) {
      return;
    }
    HealthNursingLog existing = nursingLogMapper.selectOne(Wrappers.lambdaQuery(HealthNursingLog.class)
        .eq(HealthNursingLog::getIsDeleted, 0)
        .eq(HealthNursingLog::getOrgId, inspection.getOrgId())
        .eq(HealthNursingLog::getSourceInspectionId, inspection.getId())
        .last("LIMIT 1"));
    if (existing != null) {
      return;
    }

    HealthNursingLog log = new HealthNursingLog();
    log.setTenantId(inspection.getTenantId());
    log.setOrgId(inspection.getOrgId());
    log.setElderId(inspection.getElderId());
    log.setElderName(inspection.getElderName());
    log.setSourceInspectionId(inspection.getId());
    log.setLogTime(LocalDateTime.now());
    log.setLogType("INSPECTION_FOLLOW_UP");
    log.setContent(buildFollowUpContent(inspection));
    log.setStaffName(inspection.getInspectorName());
    log.setStatus("PENDING");
    log.setCreatedBy(createdBy);
    nursingLogMapper.insert(log);

    if (inspection.getStatus() == null || inspection.getStatus().isBlank() || "NORMAL".equalsIgnoreCase(inspection.getStatus())) {
      inspection.setStatus("FOLLOWING");
      inspectionMapper.updateById(inspection);
    }
  }

  @Override
  @Transactional
  public void syncFromNursingLog(HealthNursingLog nursingLog) {
    if (nursingLog == null || nursingLog.getSourceInspectionId() == null || nursingLog.getOrgId() == null) {
      return;
    }
    HealthInspection inspection = inspectionMapper.selectOne(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(HealthInspection::getOrgId, nursingLog.getOrgId())
        .eq(HealthInspection::getId, nursingLog.getSourceInspectionId())
        .last("LIMIT 1"));
    if (inspection == null) {
      return;
    }
    String status = nursingLog.getStatus() == null ? "" : nursingLog.getStatus().toUpperCase(Locale.ROOT);
    inspection.setStatus("DONE".equals(status) || "CLOSED".equals(status) ? "CLOSED" : "FOLLOWING");
    inspectionMapper.updateById(inspection);
  }

  @Override
  @Transactional
  public void syncAfterNursingLogDeleted(Long orgId, Long sourceInspectionId) {
    if (orgId == null || sourceInspectionId == null) {
      return;
    }
    HealthInspection inspection = inspectionMapper.selectOne(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(HealthInspection::getOrgId, orgId)
        .eq(HealthInspection::getId, sourceInspectionId)
        .last("LIMIT 1"));
    if (inspection == null) {
      return;
    }
    long totalLogs = nursingLogMapper.selectCount(Wrappers.lambdaQuery(HealthNursingLog.class)
        .eq(HealthNursingLog::getIsDeleted, 0)
        .eq(HealthNursingLog::getOrgId, orgId)
        .eq(HealthNursingLog::getSourceInspectionId, sourceInspectionId));
    long closedLogs = nursingLogMapper.selectCount(Wrappers.lambdaQuery(HealthNursingLog.class)
        .eq(HealthNursingLog::getIsDeleted, 0)
        .eq(HealthNursingLog::getOrgId, orgId)
        .eq(HealthNursingLog::getSourceInspectionId, sourceInspectionId)
        .in(HealthNursingLog::getStatus, "DONE", "CLOSED"));
    if (closedLogs > 0) {
      inspection.setStatus("CLOSED");
    } else if (totalLogs > 0) {
      inspection.setStatus("FOLLOWING");
    } else {
      inspection.setStatus(isAbnormal(inspection) ? "ABNORMAL" : "NORMAL");
    }
    inspectionMapper.updateById(inspection);
  }

  private static boolean isAbnormal(HealthInspection inspection) {
    if (inspection.getStatus() != null && "ABNORMAL".equalsIgnoreCase(inspection.getStatus())) {
      return true;
    }
    return inspection.getResult() != null && inspection.getResult().contains("异常");
  }

  private static String buildFollowUpContent(HealthInspection inspection) {
    String result = inspection.getResult() == null ? "" : inspection.getResult();
    String action = inspection.getFollowUpAction() == null ? "" : inspection.getFollowUpAction();
    String otherNote = inspection.getOtherNote() == null ? "" : inspection.getOtherNote();
    long photoCount = countAttachmentPhotos(inspection.getAttachmentUrls());
    String photoText = photoCount > 0 ? ("；附件照片: " + photoCount + "张") : "";
    String noteText = otherNote.isBlank() ? "" : ("；说明: " + otherNote);
    return "巡检异常跟进: " + inspection.getInspectionItem() + "；结果: " + result + "；措施: " + action + photoText + noteText;
  }

  private static long countAttachmentPhotos(String value) {
    if (value == null || value.isBlank()) {
      return 0L;
    }
    String text = value.trim();
    if (text.startsWith("[")) {
      try {
        JsonNode node = OBJECT_MAPPER.readTree(text);
        if (node != null && node.isArray()) {
          return node.size();
        }
      } catch (Exception ignore) {
        long count = text.chars().filter(ch -> ch == '{').count();
        if (count > 0) {
          return count;
        }
        return text.chars().filter(ch -> ch == '"').count() / 2;
      }
    }
    return text.chars().filter(ch -> ch == ',').count() + 1;
  }
}
