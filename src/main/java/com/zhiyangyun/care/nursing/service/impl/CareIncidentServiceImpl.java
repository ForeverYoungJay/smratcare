package com.zhiyangyun.care.nursing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.nursing.entity.CareIncident;
import com.zhiyangyun.care.nursing.entity.CareIncidentAction;
import com.zhiyangyun.care.nursing.mapper.CareIncidentActionMapper;
import com.zhiyangyun.care.nursing.mapper.CareIncidentMapper;
import com.zhiyangyun.care.nursing.model.CareIncidentActionRequest;
import com.zhiyangyun.care.nursing.model.CareIncidentDetailResponse;
import com.zhiyangyun.care.nursing.model.CareIncidentRequest;
import com.zhiyangyun.care.nursing.model.CareIncidentSummaryResponse;
import com.zhiyangyun.care.nursing.service.CareIncidentService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CareIncidentServiceImpl implements CareIncidentService {
  private static final String INVALID_TRANSITION = "当前状态不允许该操作";
  private static final Set<String> ESCALATE_TARGETS = Set.of("MEDICAL", "LOGISTICS", "ADMIN_DUTY");

  private final CareIncidentMapper incidentMapper;
  private final CareIncidentActionMapper actionMapper;

  public CareIncidentServiceImpl(CareIncidentMapper incidentMapper, CareIncidentActionMapper actionMapper) {
    this.incidentMapper = incidentMapper;
    this.actionMapper = actionMapper;
  }

  @Override
  public IPage<CareIncident> page(Long tenantId, long pageNo, long pageSize, String status, String incidentType,
      String severity, String keyword, LocalDate dateFrom, LocalDate dateTo, Long elderId, Long relatedTaskId) {
    var wrapper = Wrappers.lambdaQuery(CareIncident.class)
        .eq(CareIncident::getIsDeleted, 0)
        .eq(CareIncident::getTenantId, tenantId)
        .eq(hasText(status), CareIncident::getStatus, status)
        .eq(hasText(incidentType), CareIncident::getIncidentType, incidentType)
        .eq(hasText(severity), CareIncident::getSeverity, severity)
        .eq(elderId != null, CareIncident::getElderId, elderId)
        .eq(relatedTaskId != null, CareIncident::getRelatedTaskId, relatedTaskId)
        .ge(dateFrom != null, CareIncident::getOccurredAt, dateFrom == null ? null : dateFrom.atStartOfDay())
        .lt(dateTo != null, CareIncident::getOccurredAt, dateTo == null ? null : dateTo.plusDays(1).atStartOfDay())
        .orderByDesc(CareIncident::getOccurredAt)
        .orderByDesc(CareIncident::getCreateTime);
    if (hasText(keyword)) {
      wrapper.and(w -> w.like(CareIncident::getElderName, keyword).or()
          .like(CareIncident::getIncidentNo, keyword).or()
          .like(CareIncident::getLocation, keyword));
    }
    return incidentMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  public CareIncidentSummaryResponse summary(Long tenantId, YearMonth month) {
    LocalDateTime from = month.atDay(1).atStartOfDay();
    LocalDateTime to = month.plusMonths(1).atDay(1).atStartOfDay();
    List<CareIncident> monthRows = incidentMapper.selectList(Wrappers.lambdaQuery(CareIncident.class)
        .eq(CareIncident::getIsDeleted, 0)
        .eq(CareIncident::getTenantId, tenantId)
        .ge(CareIncident::getOccurredAt, from)
        .lt(CareIncident::getOccurredAt, to));
    CareIncidentSummaryResponse response = new CareIncidentSummaryResponse();
    response.setMonth(month.toString());
    response.setMonthTotal(monthRows.size());
    response.setProcessingCount(monthRows.stream()
        .filter(i -> Set.of("ACCEPTED", "PROCESSING", "ESCALATED").contains(i.getStatus())).count());
    response.setPendingReviewCount(monthRows.stream().filter(i -> "PENDING_REVIEW".equals(i.getStatus())).count());
    response.setClosedCount(monthRows.stream().filter(i -> "CLOSED".equals(i.getStatus())).count());
    response.setOpenHighCount(monthRows.stream()
        .filter(i -> "HIGH".equals(i.getSeverity()) && !"CLOSED".equals(i.getStatus())).count());
    return response;
  }

  @Override
  public CareIncidentDetailResponse get(Long id, Long tenantId) {
    CareIncident incident = load(id, tenantId);
    return incident == null ? null : detail(incident);
  }

  @Override
  @Transactional
  public CareIncidentDetailResponse create(Long tenantId, CareIncidentRequest request, Long operatorId,
      String operatorName) {
    CareIncident incident = new CareIncident();
    incident.setTenantId(tenantId);
    incident.setOrgId(tenantId);
    incident.setIncidentNo(nextIncidentNo(tenantId));
    incident.setElderId(request.getElderId());
    incident.setElderName(trim(request.getElderName()));
    incident.setLocation(trim(request.getLocation()));
    incident.setIncidentType(request.getIncidentType());
    incident.setSeverity(request.getSeverity());
    incident.setOccurredAt(request.getOccurredAt());
    incident.setReporterId(operatorId);
    incident.setReporterName(operatorName);
    incident.setDescription(trim(request.getDescription()));
    incident.setMeasures(trim(request.getMeasures()));
    incident.setNotifyTargets(trim(request.getNotifyTargets()));
    incident.setOwnerId(request.getOwnerId());
    incident.setOwnerName(trim(request.getOwnerName()));
    incident.setRelatedTaskId(request.getRelatedTaskId());
    incident.setStatus("REPORTED");
    incidentMapper.insert(incident);
    appendAction(incident, "REPORT", null, "REPORTED", incident.getDescription(), operatorId, operatorName);
    return detail(incident);
  }

  @Override
  @Transactional
  public CareIncidentDetailResponse act(Long id, Long tenantId, CareIncidentActionRequest request, Long operatorId,
      String operatorName, boolean manager) {
    CareIncident incident = load(id, tenantId);
    if (incident == null) {
      return null;
    }
    String action = request.getAction() == null ? "" : request.getAction().trim().toUpperCase();
    String note = trim(request.getNote());
    String from = incident.getStatus();
    String to;
    switch (action) {
      case "ACCEPT" -> to = requireFrom(from, "ACCEPTED", "REPORTED");
      case "PROCESS" -> to = requireFrom(from, "PROCESSING", "ACCEPTED", "ESCALATED");
      case "ESCALATE" -> {
        to = requireFrom(from, "ESCALATED", "ACCEPTED", "PROCESSING");
        String target = request.getEscalateTo() == null ? null : request.getEscalateTo().trim().toUpperCase();
        if (!ESCALATE_TARGETS.contains(target)) {
          throw new IllegalStateException("请选择升级对象（MEDICAL/LOGISTICS/ADMIN_DUTY）");
        }
        incident.setEscalateTo(target);
      }
      case "SUBMIT_REVIEW" -> {
        to = requireFrom(from, "PENDING_REVIEW", "PROCESSING", "ESCALATED");
        requireNote(note, "请填写处置结果");
      }
      case "CLOSE" -> {
        requireManager(manager);
        to = requireFrom(from, "CLOSED", "PENDING_REVIEW");
        if (incident.getOwnerId() != null && incident.getOwnerId().equals(operatorId)) {
          throw new AccessDeniedException("复核人不能是事件负责人本人");
        }
        incident.setReviewerId(operatorId);
        incident.setReviewerName(operatorName);
        incident.setReviewNote(note);
        incident.setClosedAt(LocalDateTime.now());
      }
      case "REOPEN" -> {
        requireManager(manager);
        to = requireFrom(from, "PROCESSING", "PENDING_REVIEW");
        requireNote(note, "请填写退回原因");
      }
      case "CORRECT" -> {
        requireNote(note, "请填写更正说明");
        to = from;
      }
      case "ASSIGN" -> {
        requireManager(manager);
        if (request.getOwnerId() == null) {
          throw new IllegalStateException("请选择负责人");
        }
        incident.setOwnerId(request.getOwnerId());
        incident.setOwnerName(trim(request.getOwnerName()));
        to = from;
        if (note == null) {
          note = "指派负责人：" + (incident.getOwnerName() == null ? incident.getOwnerId() : incident.getOwnerName());
        }
      }
      default -> throw new IllegalStateException(INVALID_TRANSITION);
    }
    if (!"CORRECT".equals(action)) {
      incident.setStatus(to);
      incidentMapper.updateById(incident);
    }
    appendAction(incident, action, from, to, note, operatorId, operatorName);
    return detail(incident);
  }

  private String nextIncidentNo(Long tenantId) {
    String prefix = "YC" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    // shortcut: count+1 can collide under concurrent reports (unique key rejects the second), upgrade to a sequence table if that happens in practice
    long count = incidentMapper.selectCount(Wrappers.lambdaQuery(CareIncident.class)
        .eq(CareIncident::getTenantId, tenantId)
        .likeRight(CareIncident::getIncidentNo, prefix));
    return prefix + String.format("%03d", count + 1);
  }

  private CareIncident load(Long id, Long tenantId) {
    CareIncident incident = incidentMapper.selectById(id);
    if (incident == null || !incident.getTenantId().equals(tenantId)
        || (incident.getIsDeleted() != null && incident.getIsDeleted() == 1)) {
      return null;
    }
    return incident;
  }

  private CareIncidentDetailResponse detail(CareIncident incident) {
    List<CareIncidentAction> actions = actionMapper.selectList(Wrappers.lambdaQuery(CareIncidentAction.class)
        .eq(CareIncidentAction::getIncidentId, incident.getId())
        .orderByAsc(CareIncidentAction::getCreateTime)
        .orderByAsc(CareIncidentAction::getId));
    return new CareIncidentDetailResponse(incident, actions);
  }

  private void appendAction(CareIncident incident, String action, String from, String to, String note,
      Long operatorId, String operatorName) {
    CareIncidentAction row = new CareIncidentAction();
    row.setTenantId(incident.getTenantId());
    row.setIncidentId(incident.getId());
    row.setAction(action);
    row.setFromStatus(from);
    row.setToStatus(to);
    row.setNote(note);
    row.setOperatorId(operatorId);
    row.setOperatorName(operatorName);
    actionMapper.insert(row);
  }

  private String requireFrom(String current, String target, String... allowed) {
    for (String status : allowed) {
      if (status.equals(current)) {
        return target;
      }
    }
    throw new IllegalStateException(INVALID_TRANSITION);
  }

  private void requireManager(boolean manager) {
    if (!manager) {
      throw new AccessDeniedException("仅护士长及以上可执行该操作");
    }
  }

  private void requireNote(String note, String message) {
    if (note == null) {
      throw new IllegalStateException(message);
    }
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private String trim(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
