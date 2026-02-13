package com.zhiyangyun.care.audit.service.impl;

import com.zhiyangyun.care.audit.entity.AuditLog;
import com.zhiyangyun.care.audit.mapper.AuditLogMapper;
import com.zhiyangyun.care.audit.service.AuditLogService;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl implements AuditLogService {
  private final AuditLogMapper auditLogMapper;

  public AuditLogServiceImpl(AuditLogMapper auditLogMapper) {
    this.auditLogMapper = auditLogMapper;
  }

  @Override
  public void record(Long tenantId, Long orgId, Long actorId, String actorName,
                     String actionType, String entityType, Long entityId, String detail) {
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
    auditLogMapper.insert(log);
  }
}
