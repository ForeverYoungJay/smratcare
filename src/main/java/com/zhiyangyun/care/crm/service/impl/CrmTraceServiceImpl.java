package com.zhiyangyun.care.crm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.entity.CrmContractWorkflowLog;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.entity.CrmLeadAssignLog;
import com.zhiyangyun.care.crm.entity.CrmSalesReportSnapshot;
import com.zhiyangyun.care.crm.entity.CrmStageTransitionLog;
import com.zhiyangyun.care.crm.mapper.CrmContractWorkflowLogMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadAssignLogMapper;
import com.zhiyangyun.care.crm.mapper.CrmSalesReportSnapshotMapper;
import com.zhiyangyun.care.crm.mapper.CrmStageTransitionLogMapper;
import com.zhiyangyun.care.crm.model.trace.CrmContractWorkflowLogResponse;
import com.zhiyangyun.care.crm.model.trace.CrmLeadAssignLogResponse;
import com.zhiyangyun.care.crm.model.trace.CrmSalesReportSnapshotResponse;
import com.zhiyangyun.care.crm.model.trace.CrmStageTransitionLogResponse;
import com.zhiyangyun.care.crm.service.CrmTraceService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class CrmTraceServiceImpl implements CrmTraceService {
  private final CrmLeadAssignLogMapper leadAssignLogMapper;
  private final CrmStageTransitionLogMapper stageTransitionLogMapper;
  private final CrmContractWorkflowLogMapper contractWorkflowLogMapper;
  private final CrmSalesReportSnapshotMapper salesReportSnapshotMapper;
  private final ObjectMapper objectMapper;

  public CrmTraceServiceImpl(
      CrmLeadAssignLogMapper leadAssignLogMapper,
      CrmStageTransitionLogMapper stageTransitionLogMapper,
      CrmContractWorkflowLogMapper contractWorkflowLogMapper,
      CrmSalesReportSnapshotMapper salesReportSnapshotMapper,
      ObjectMapper objectMapper) {
    this.leadAssignLogMapper = leadAssignLogMapper;
    this.stageTransitionLogMapper = stageTransitionLogMapper;
    this.contractWorkflowLogMapper = contractWorkflowLogMapper;
    this.salesReportSnapshotMapper = salesReportSnapshotMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public void recordLeadAssignment(String actionType, String detail, CrmLead before, CrmLead after, Object context) {
    if (after == null && before == null) {
      return;
    }
    Long beforeOwnerId = before == null ? null : before.getOwnerStaffId();
    Long afterOwnerId = after == null ? null : after.getOwnerStaffId();
    String beforeOwnerName = blankToNull(before == null ? null : before.getOwnerStaffName());
    String afterOwnerName = blankToNull(after == null ? null : after.getOwnerStaffName());
    if (Objects.equals(beforeOwnerId, afterOwnerId) && Objects.equals(beforeOwnerName, afterOwnerName)) {
      return;
    }
    CrmLead current = after == null ? before : after;
    if (current == null || current.getTenantId() == null || current.getOrgId() == null || current.getId() == null) {
      return;
    }
    CrmLeadAssignLog log = new CrmLeadAssignLog();
    log.setTenantId(current.getTenantId());
    log.setOrgId(current.getOrgId());
    log.setLeadId(current.getId());
    log.setFromOwnerStaffId(beforeOwnerId);
    log.setFromOwnerStaffName(beforeOwnerName);
    log.setToOwnerStaffId(afterOwnerId);
    log.setToOwnerStaffName(afterOwnerName);
    log.setAssignedBy(AuthContext.getStaffId());
    log.setAssignedByName(firstNonBlank(AuthContext.getUsername(), "SYSTEM"));
    log.setAssignedAt(LocalDateTime.now());
    log.setRemark(composeRemark(actionType, detail, context));
    leadAssignLogMapper.insert(log);
  }

  @Override
  public void recordLeadStageTransition(String actionType, String detail, CrmLead before, CrmLead after, Object context) {
    if (!hasLeadTransition(before, after, actionType)) {
      return;
    }
    CrmLead current = after == null ? before : after;
    if (current == null || current.getTenantId() == null || current.getOrgId() == null) {
      return;
    }
    CrmStageTransitionLog log = new CrmStageTransitionLog();
    log.setTenantId(current.getTenantId());
    log.setOrgId(current.getOrgId());
    log.setEntityType("LEAD");
    log.setLeadId(current.getId());
    log.setTransitionType(blankToNull(actionType));
    log.setSource(resolveSource(actionType));
    log.setFromStage(before == null ? null : blankToNull(before.getFlowStage()));
    log.setToStage(after == null ? null : blankToNull(after.getFlowStage()));
    log.setFromStatus(before == null ? null : stringify(before.getStatus()));
    log.setToStatus(after == null ? null : stringify(after.getStatus()));
    log.setFromOwnerDept(before == null ? null : blankToNull(before.getCurrentOwnerDept()));
    log.setToOwnerDept(after == null ? null : blankToNull(after.getCurrentOwnerDept()));
    log.setRemark(composeRemark(actionType, detail, context));
    log.setOperatedBy(AuthContext.getStaffId());
    log.setOperatedByName(firstNonBlank(AuthContext.getUsername(), "SYSTEM"));
    log.setOperatedAt(LocalDateTime.now());
    stageTransitionLogMapper.insert(log);
  }

  @Override
  public void recordContractWorkflow(String actionType, String detail, CrmContract before, CrmContract after, Object context) {
    if (!hasContractWorkflow(before, after, actionType)) {
      return;
    }
    CrmContract current = after == null ? before : after;
    if (current == null || current.getTenantId() == null || current.getOrgId() == null || current.getId() == null) {
      return;
    }
    CrmContractWorkflowLog log = new CrmContractWorkflowLog();
    log.setTenantId(current.getTenantId());
    log.setOrgId(current.getOrgId());
    log.setContractId(current.getId());
    log.setLeadId(current.getLeadId());
    log.setActionType(blankToNull(actionType));
    log.setBeforeStatus(before == null ? null : blankToNull(before.getStatus()));
    log.setAfterStatus(after == null ? null : blankToNull(after.getStatus()));
    log.setBeforeFlowStage(before == null ? null : blankToNull(before.getFlowStage()));
    log.setAfterFlowStage(after == null ? null : blankToNull(after.getFlowStage()));
    log.setBeforeChangeWorkflow(before == null ? null : blankToNull(before.getChangeWorkflowStatus()));
    log.setAfterChangeWorkflow(after == null ? null : blankToNull(after.getChangeWorkflowStatus()));
    log.setRemark(composeRemark(actionType, detail, context));
    Map<String, Object> snapshotPayload = new LinkedHashMap<>();
    snapshotPayload.put("before", contractSnapshot(before));
    snapshotPayload.put("after", contractSnapshot(after));
    snapshotPayload.put("context", context);
    log.setSnapshotJson(toJson(snapshotPayload));
    log.setOperatedBy(AuthContext.getStaffId());
    log.setOperatedByName(firstNonBlank(AuthContext.getUsername(), "SYSTEM"));
    log.setOperatedAt(LocalDateTime.now());
    contractWorkflowLogMapper.insert(log);

    if (hasContractStageTransition(before, after)) {
      CrmStageTransitionLog stageLog = new CrmStageTransitionLog();
      stageLog.setTenantId(current.getTenantId());
      stageLog.setOrgId(current.getOrgId());
      stageLog.setEntityType("CONTRACT");
      stageLog.setLeadId(current.getLeadId());
      stageLog.setContractId(current.getId());
      stageLog.setTransitionType(blankToNull(actionType));
      stageLog.setSource(resolveSource(actionType));
      stageLog.setFromStage(before == null ? null : blankToNull(before.getFlowStage()));
      stageLog.setToStage(after == null ? null : blankToNull(after.getFlowStage()));
      stageLog.setFromStatus(before == null ? null : blankToNull(before.getStatus()));
      stageLog.setToStatus(after == null ? null : blankToNull(after.getStatus()));
      stageLog.setFromOwnerDept(before == null ? null : blankToNull(before.getCurrentOwnerDept()));
      stageLog.setToOwnerDept(after == null ? null : blankToNull(after.getCurrentOwnerDept()));
      stageLog.setRemark(composeRemark(actionType, detail, context));
      stageLog.setOperatedBy(AuthContext.getStaffId());
      stageLog.setOperatedByName(firstNonBlank(AuthContext.getUsername(), "SYSTEM"));
      stageLog.setOperatedAt(LocalDateTime.now());
      stageTransitionLogMapper.insert(stageLog);
    }
  }

  @Override
  public void saveSnapshot(Long tenantId, Long orgId, String snapshotType, LocalDate windowFrom, LocalDate windowTo, String snapshotKey, Object metrics) {
    if (tenantId == null || orgId == null || blankToNull(snapshotType) == null) {
      return;
    }
    CrmSalesReportSnapshot snapshot = new CrmSalesReportSnapshot();
    snapshot.setTenantId(tenantId);
    snapshot.setOrgId(orgId);
    snapshot.setSnapshotType(snapshotType.trim());
    snapshot.setSnapshotDate(windowTo == null ? LocalDate.now() : windowTo);
    snapshot.setWindowFrom(windowFrom);
    snapshot.setWindowTo(windowTo);
    snapshot.setSnapshotKey(blankToNull(snapshotKey));
    snapshot.setMetricsJson(toJson(metrics));
    snapshot.setGeneratedBy(AuthContext.getStaffId());
    snapshot.setGeneratedByName(firstNonBlank(AuthContext.getUsername(), "SYSTEM"));
    snapshot.setGeneratedAt(LocalDateTime.now());
    salesReportSnapshotMapper.insert(snapshot);
  }

  @Override
  public List<CrmLeadAssignLogResponse> listLeadAssignments(Long tenantId, Long leadId, int limit) {
    int safeLimit = Math.max(1, Math.min(limit, 100));
    return leadAssignLogMapper.selectList(Wrappers.lambdaQuery(CrmLeadAssignLog.class)
            .eq(CrmLeadAssignLog::getIsDeleted, 0)
            .eq(tenantId != null, CrmLeadAssignLog::getTenantId, tenantId)
            .eq(CrmLeadAssignLog::getLeadId, leadId)
            .orderByDesc(CrmLeadAssignLog::getAssignedAt)
            .orderByDesc(CrmLeadAssignLog::getCreateTime)
            .last("LIMIT " + safeLimit))
        .stream()
        .map(this::toLeadAssignResponse)
        .toList();
  }

  @Override
  public List<CrmStageTransitionLogResponse> listLeadStageTransitions(Long tenantId, Long leadId, int limit) {
    int safeLimit = Math.max(1, Math.min(limit, 100));
    return stageTransitionLogMapper.selectList(Wrappers.lambdaQuery(CrmStageTransitionLog.class)
            .eq(CrmStageTransitionLog::getIsDeleted, 0)
            .eq(tenantId != null, CrmStageTransitionLog::getTenantId, tenantId)
            .eq(CrmStageTransitionLog::getEntityType, "LEAD")
            .eq(CrmStageTransitionLog::getLeadId, leadId)
            .orderByDesc(CrmStageTransitionLog::getOperatedAt)
            .orderByDesc(CrmStageTransitionLog::getCreateTime)
            .last("LIMIT " + safeLimit))
        .stream()
        .map(this::toStageTransitionResponse)
        .toList();
  }

  @Override
  public List<CrmContractWorkflowLogResponse> listContractWorkflowLogs(Long tenantId, Long contractId, int limit) {
    int safeLimit = Math.max(1, Math.min(limit, 100));
    return contractWorkflowLogMapper.selectList(Wrappers.lambdaQuery(CrmContractWorkflowLog.class)
            .eq(CrmContractWorkflowLog::getIsDeleted, 0)
            .eq(tenantId != null, CrmContractWorkflowLog::getTenantId, tenantId)
            .eq(CrmContractWorkflowLog::getContractId, contractId)
            .orderByDesc(CrmContractWorkflowLog::getOperatedAt)
            .orderByDesc(CrmContractWorkflowLog::getCreateTime)
            .last("LIMIT " + safeLimit))
        .stream()
        .map(this::toContractWorkflowResponse)
        .toList();
  }

  @Override
  public List<CrmSalesReportSnapshotResponse> listSnapshots(Long tenantId, String snapshotType, int limit) {
    int safeLimit = Math.max(1, Math.min(limit, 60));
    return salesReportSnapshotMapper.selectList(Wrappers.lambdaQuery(CrmSalesReportSnapshot.class)
            .eq(CrmSalesReportSnapshot::getIsDeleted, 0)
            .eq(tenantId != null, CrmSalesReportSnapshot::getTenantId, tenantId)
            .eq(blankToNull(snapshotType) != null, CrmSalesReportSnapshot::getSnapshotType, blankToNull(snapshotType))
            .orderByDesc(CrmSalesReportSnapshot::getSnapshotDate)
            .orderByDesc(CrmSalesReportSnapshot::getGeneratedAt)
            .last("LIMIT " + safeLimit))
        .stream()
        .map(this::toSnapshotResponse)
        .toList();
  }

  private boolean hasLeadTransition(CrmLead before, CrmLead after, String actionType) {
    if ("CREATE".equalsIgnoreCase(actionType) || "DELETE".equalsIgnoreCase(actionType) || "HANDOFF_ASSESSMENT".equalsIgnoreCase(actionType)) {
      return true;
    }
    return !Objects.equals(blankToNull(before == null ? null : before.getFlowStage()), blankToNull(after == null ? null : after.getFlowStage()))
        || !Objects.equals(stringify(before == null ? null : before.getStatus()), stringify(after == null ? null : after.getStatus()))
        || !Objects.equals(blankToNull(before == null ? null : before.getCurrentOwnerDept()), blankToNull(after == null ? null : after.getCurrentOwnerDept()));
  }

  private boolean hasContractWorkflow(CrmContract before, CrmContract after, String actionType) {
    return blankToNull(actionType) != null
        || hasContractStageTransition(before, after)
        || !Objects.equals(blankToNull(before == null ? null : before.getStatus()), blankToNull(after == null ? null : after.getStatus()))
        || !Objects.equals(blankToNull(before == null ? null : before.getChangeWorkflowStatus()), blankToNull(after == null ? null : after.getChangeWorkflowStatus()));
  }

  private boolean hasContractStageTransition(CrmContract before, CrmContract after) {
    return !Objects.equals(blankToNull(before == null ? null : before.getFlowStage()), blankToNull(after == null ? null : after.getFlowStage()))
        || !Objects.equals(blankToNull(before == null ? null : before.getCurrentOwnerDept()), blankToNull(after == null ? null : after.getCurrentOwnerDept()));
  }

  private String composeRemark(String actionType, String detail, Object context) {
    String base = firstNonBlank(blankToNull(detail), blankToNull(actionType));
    if (context == null) {
      return base;
    }
    String json = toJson(context);
    if (json == null || json.isBlank()) {
      return base;
    }
    if (base == null) {
      return json;
    }
    return base + " | " + json;
  }

  private String resolveSource(String actionType) {
    if ("ADMISSION_SYNC".equalsIgnoreCase(actionType)) {
      return "SYSTEM";
    }
    return "SERVICE";
  }

  private Map<String, Object> contractSnapshot(CrmContract contract) {
    if (contract == null) {
      return null;
    }
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("id", contract.getId());
    snapshot.put("leadId", contract.getLeadId());
    snapshot.put("contractNo", contract.getContractNo());
    snapshot.put("status", contract.getStatus());
    snapshot.put("flowStage", contract.getFlowStage());
    snapshot.put("changeWorkflowStatus", contract.getChangeWorkflowStatus());
    snapshot.put("currentOwnerDept", contract.getCurrentOwnerDept());
    snapshot.put("contractStatus", contract.getContractStatus());
    return snapshot;
  }

  private String toJson(Object value) {
    if (value == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException ex) {
      return "{\"error\":\"json_serialize_failed\"}";
    }
  }

  private String stringify(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private String firstNonBlank(String first, String second) {
    String normalizedFirst = blankToNull(first);
    if (normalizedFirst != null) {
      return normalizedFirst;
    }
    return blankToNull(second);
  }

  private CrmLeadAssignLogResponse toLeadAssignResponse(CrmLeadAssignLog item) {
    CrmLeadAssignLogResponse response = new CrmLeadAssignLogResponse();
    response.setId(item.getId());
    response.setLeadId(item.getLeadId());
    response.setFromOwnerStaffId(item.getFromOwnerStaffId());
    response.setFromOwnerStaffName(item.getFromOwnerStaffName());
    response.setToOwnerStaffId(item.getToOwnerStaffId());
    response.setToOwnerStaffName(item.getToOwnerStaffName());
    response.setAssignedBy(item.getAssignedBy());
    response.setAssignedByName(item.getAssignedByName());
    response.setAssignedAt(item.getAssignedAt());
    response.setRemark(item.getRemark());
    return response;
  }

  private CrmStageTransitionLogResponse toStageTransitionResponse(CrmStageTransitionLog item) {
    CrmStageTransitionLogResponse response = new CrmStageTransitionLogResponse();
    response.setId(item.getId());
    response.setEntityType(item.getEntityType());
    response.setLeadId(item.getLeadId());
    response.setContractId(item.getContractId());
    response.setTransitionType(item.getTransitionType());
    response.setSource(item.getSource());
    response.setFromStage(item.getFromStage());
    response.setToStage(item.getToStage());
    response.setFromStatus(item.getFromStatus());
    response.setToStatus(item.getToStatus());
    response.setFromOwnerDept(item.getFromOwnerDept());
    response.setToOwnerDept(item.getToOwnerDept());
    response.setRemark(item.getRemark());
    response.setOperatedBy(item.getOperatedBy());
    response.setOperatedByName(item.getOperatedByName());
    response.setOperatedAt(item.getOperatedAt());
    return response;
  }

  private CrmContractWorkflowLogResponse toContractWorkflowResponse(CrmContractWorkflowLog item) {
    CrmContractWorkflowLogResponse response = new CrmContractWorkflowLogResponse();
    response.setId(item.getId());
    response.setContractId(item.getContractId());
    response.setLeadId(item.getLeadId());
    response.setActionType(item.getActionType());
    response.setBeforeStatus(item.getBeforeStatus());
    response.setAfterStatus(item.getAfterStatus());
    response.setBeforeFlowStage(item.getBeforeFlowStage());
    response.setAfterFlowStage(item.getAfterFlowStage());
    response.setBeforeChangeWorkflow(item.getBeforeChangeWorkflow());
    response.setAfterChangeWorkflow(item.getAfterChangeWorkflow());
    response.setRemark(item.getRemark());
    response.setSnapshotJson(item.getSnapshotJson());
    response.setOperatedBy(item.getOperatedBy());
    response.setOperatedByName(item.getOperatedByName());
    response.setOperatedAt(item.getOperatedAt());
    return response;
  }

  private CrmSalesReportSnapshotResponse toSnapshotResponse(CrmSalesReportSnapshot item) {
    CrmSalesReportSnapshotResponse response = new CrmSalesReportSnapshotResponse();
    response.setId(item.getId());
    response.setSnapshotType(item.getSnapshotType());
    response.setSnapshotDate(item.getSnapshotDate());
    response.setWindowFrom(item.getWindowFrom());
    response.setWindowTo(item.getWindowTo());
    response.setSnapshotKey(item.getSnapshotKey());
    response.setMetricsJson(item.getMetricsJson());
    response.setGeneratedBy(item.getGeneratedBy());
    response.setGeneratedByName(item.getGeneratedByName());
    response.setGeneratedAt(item.getGeneratedAt());
    return response;
  }
}
