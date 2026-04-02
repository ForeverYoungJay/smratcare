package com.zhiyangyun.care.audit.service.impl;

import com.zhiyangyun.care.audit.entity.AuditLog;
import com.zhiyangyun.care.audit.mapper.AuditLogMapper;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl implements AuditLogService {
  private final AuditLogMapper auditLogMapper;
  private final ObjectMapper objectMapper;

  public AuditLogServiceImpl(AuditLogMapper auditLogMapper, ObjectMapper objectMapper) {
    this.auditLogMapper = auditLogMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public void record(Long tenantId, Long orgId, Long actorId, String actorName,
                     String actionType, String entityType, Long entityId, String detail) {
    recordStructured(tenantId, orgId, actorId, actorName, actionType, entityType, entityId, detail, null, null, null);
  }

  @Override
  public void recordStructured(Long tenantId, Long orgId, Long actorId, String actorName,
                               String actionType, String entityType, Long entityId, String detail,
                               Object beforeSnapshot, Object afterSnapshot, Object context) {
    if (tenantId == null || orgId == null || actionType == null || entityType == null) {
      return;
    }
    AuditLog log = new AuditLog();
    log.setTenantId(tenantId);
    log.setOrgId(orgId);
    log.setActorId(actorId);
    log.setActorName(actorName);
    log.setActionType(actionType);
    log.setEntityType(entityType);
    log.setEntityId(entityId);
    log.setDetail(detail);
    log.setBeforeSnapshot(writeJson(beforeSnapshot));
    log.setAfterSnapshot(writeJson(afterSnapshot));
    log.setContextJson(writeJson(context));
    auditLogMapper.insert(log);
  }

  private String writeJson(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof String text) {
      return text;
    }
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException ex) {
      return String.valueOf(value);
    }
  }
}
