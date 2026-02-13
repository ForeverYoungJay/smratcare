package com.zhiyangyun.care.audit.service;

public interface AuditLogService {
  void record(Long tenantId, Long orgId, Long actorId, String actorName,
              String actionType, String entityType, Long entityId, String detail);
}
