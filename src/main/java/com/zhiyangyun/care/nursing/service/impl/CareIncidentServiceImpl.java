package com.zhiyangyun.care.nursing.service.impl;

import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;

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

  /** 升级去向 → 接收待办的岗位 */
  private static final Map<String, String> ESCALATE_ROLE = Map.of(
      "MEDICAL", "MEDICAL_MINISTER", "LOGISTICS", "LOGISTICS_MINISTER", "ADMIN_DUTY", "DIRECTOR");
  private static final String TODO_PREFIX = "【护理异常】";

  private final CareIncidentMapper incidentMapper;
  private final CareIncidentActionMapper actionMapper;
  private final StaffMapper staffMapper;
  private final OaTodoMapper oaTodoMapper;

  public CareIncidentServiceImpl(CareIncidentMapper incidentMapper, CareIncidentActionMapper actionMapper,
      StaffMapper staffMapper, OaTodoMapper oaTodoMapper) {
    this.incidentMapper = incidentMapper;
    this.actionMapper = actionMapper;
    this.staffMapper = staffMapper;
    this.oaTodoMapper = oaTodoMapper;
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
    operatorName = displayName(operatorId, operatorName);
    CareIncident incident = new CareIncident();
    incident.setTenantId(tenantId);
    incident.setOrgId(tenantId);
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
    insertWithNumber(incident);
    appendAction(incident, "REPORT", null, "REPORTED", incident.getDescription(), operatorId, operatorName);
    // 有责任人通知责任人，没有就通知护士长受理
    notify(incident, "待受理", incident.getOwnerId() != null ? staffList(incident.getOwnerId())
        : staffMapper.selectByRoleCode(tenantId, "NURSING_MINISTER"), operatorId);
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
    operatorName = displayName(operatorId, operatorName);
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
    switch (action) {
      case "ASSIGN" -> notify(incident, "请负责处置", staffList(incident.getOwnerId()), operatorId);
      case "ESCALATE" -> notify(incident, "已升级，请协同处置",
          staffMapper.selectByRoleCode(tenantId, ESCALATE_ROLE.get(incident.getEscalateTo())), operatorId);
      case "SUBMIT_REVIEW" -> notify(incident, "待复核结案", staffMapper.selectByRoleCode(tenantId, "NURSING_MINISTER"), operatorId);
      case "CLOSE" -> closeTodos(incident);
      default -> { }
    }
    return detail(incident);
  }

  /** 编号按当天已有最大号 +1；并发上报撞号时唯一键会拒绝，换下一个号重试 */
  private void insertWithNumber(CareIncident incident) {
    for (int attempt = 0; ; attempt++) {
      incident.setIncidentNo(nextIncidentNo(incident.getTenantId(), attempt));
      try {
        incidentMapper.insert(incident);
        return;
      } catch (DuplicateKeyException ex) {
        incident.setId(null);
        if (attempt >= 4) {
          throw new IllegalStateException("事件编号生成冲突，请稍后重试");
        }
      }
    }
  }

  private String nextIncidentNo(Long tenantId, int offset) {
    String prefix = "YC" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    CareIncident last = incidentMapper.selectOne(Wrappers.lambdaQuery(CareIncident.class)
        .eq(CareIncident::getTenantId, tenantId)
        .likeRight(CareIncident::getIncidentNo, prefix)
        .orderByDesc(CareIncident::getIncidentNo)
        .last("LIMIT 1"));
    int seq = last == null ? 0 : Integer.parseInt(last.getIncidentNo().substring(prefix.length()));
    return prefix + String.format("%03d", seq + 1 + offset);
  }

  /** 操作人显示真实姓名，查不到时退回登录名 */
  private String displayName(Long staffId, String fallback) {
    StaffAccount staff = staffId == null ? null : staffMapper.selectById(staffId);
    return staff != null && staff.getRealName() != null && !staff.getRealName().isBlank() ? staff.getRealName() : fallback;
  }

  private List<StaffAccount> staffList(Long staffId) {
    StaffAccount staff = staffId == null ? null : staffMapper.selectById(staffId);
    return staff == null ? List.of() : List.of(staff);
  }

  /** 发到「我的待办」；操作人自己不给自己发，高风险 1 小时内到期，其余当天 */
  private void notify(CareIncident incident, String what, List<StaffAccount> receivers, Long operatorId) {
    LocalDateTime due = "HIGH".equals(incident.getSeverity()) ? LocalDateTime.now().plusHours(1) : LocalDateTime.now().plusHours(8);
    String title = TODO_PREFIX + incident.getIncidentNo() + " " + what;
    String place = incident.getElderName() != null ? incident.getElderName() : incident.getLocation();
    for (StaffAccount receiver : receivers == null ? List.<StaffAccount>of() : receivers) {
      if (receiver.getId().equals(operatorId)) {
        continue;
      }
      OaTodo todo = new OaTodo();
      todo.setTenantId(incident.getTenantId());
      todo.setOrgId(incident.getOrgId());
      todo.setTitle(title);
      todo.setContent((place == null ? "" : place + "：") + incident.getDescription());
      todo.setDueTime(due);
      todo.setStatus("OPEN");
      todo.setAssigneeId(receiver.getId());
      todo.setAssigneeName(receiver.getRealName());
      todo.setCreatedBy(operatorId == null ? 0L : operatorId);
      oaTodoMapper.insert(todo);
    }
  }

  private void closeTodos(CareIncident incident) {
    OaTodo done = new OaTodo();
    done.setStatus("DONE");
    oaTodoMapper.update(done, Wrappers.lambdaUpdate(OaTodo.class)
        .eq(OaTodo::getOrgId, incident.getOrgId())
        .eq(OaTodo::getStatus, "OPEN")
        .likeRight(OaTodo::getTitle, TODO_PREFIX + incident.getIncidentNo() + " "));
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
