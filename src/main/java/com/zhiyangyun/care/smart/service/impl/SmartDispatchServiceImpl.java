package com.zhiyangyun.care.smart.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.life.entity.IncidentReport;
import com.zhiyangyun.care.life.mapper.IncidentReportMapper;
import com.zhiyangyun.care.schedule.entity.StaffSchedule;
import com.zhiyangyun.care.schedule.mapper.StaffScheduleMapper;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.entity.SmartAlertDispatch;
import com.zhiyangyun.care.smart.mapper.SmartAlertDispatchMapper;
import com.zhiyangyun.care.smart.mapper.SmartAlertMapper;
import com.zhiyangyun.care.smart.model.SmartDispatchActionRequest;
import com.zhiyangyun.care.smart.service.SmartDispatchService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SmartDispatchServiceImpl implements SmartDispatchService {

  private static final String TRIGGERED = "TRIGGERED";
  private static final String ASSIGNED = "ASSIGNED";
  private static final String RESPONDED = "RESPONDED";
  private static final String ONSITE = "ONSITE";
  private static final String HANDLED = "HANDLED";
  private static final String REVIEWED = "REVIEWED";
  private static final int AUTO_SCAN_LIMIT = 200;
  /** 值班护理员角色（自动派单指派对象）。 */
  private static final String DUTY_ROLE = "NURSING_EMPLOYEE";
  /** 超时升级接收角色（部长）。 */
  private static final String ESCALATION_ROLE = "NURSING_MINISTER";

  private final SmartAlertDispatchMapper dispatchMapper;
  private final SmartAlertMapper alertMapper;
  private final StaffScheduleMapper staffScheduleMapper;
  private final StaffMapper staffMapper;
  private final IncidentReportMapper incidentReportMapper;
  private final ElderMapper elderMapper;

  public SmartDispatchServiceImpl(
      SmartAlertDispatchMapper dispatchMapper,
      SmartAlertMapper alertMapper,
      StaffScheduleMapper staffScheduleMapper,
      StaffMapper staffMapper,
      IncidentReportMapper incidentReportMapper,
      ElderMapper elderMapper) {
    this.dispatchMapper = dispatchMapper;
    this.alertMapper = alertMapper;
    this.staffScheduleMapper = staffScheduleMapper;
    this.staffMapper = staffMapper;
    this.incidentReportMapper = incidentReportMapper;
    this.elderMapper = elderMapper;
  }

  @Override
  @Transactional
  public int autoDispatchOpenAlerts() {
    List<SmartAlert> alerts = alertMapper.selectList(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .in(SmartAlert::getStatus, List.of("OPEN", "ACKNOWLEDGED"))
        .in(SmartAlert::getLevel, List.of("HIGH", "CRITICAL"))
        .orderByDesc(SmartAlert::getId)
        .last("limit " + AUTO_SCAN_LIMIT));
    int count = 0;
    for (SmartAlert alert : alerts) {
      Long exists = dispatchMapper.selectCount(Wrappers.lambdaQuery(SmartAlertDispatch.class)
          .eq(SmartAlertDispatch::getIsDeleted, 0)
          .eq(SmartAlertDispatch::getAlertId, alert.getId()));
      if (exists != null && exists > 0) {
        continue;
      }
      doCreateForAlert(alert, null);
      count++;
    }
    return count;
  }

  @Override
  @Transactional
  public SmartAlertDispatch createForAlert(SmartAlert alert, Long ruleId) {
    SmartAlertDispatch exists = dispatchMapper.selectOne(Wrappers.lambdaQuery(SmartAlertDispatch.class)
        .eq(SmartAlertDispatch::getIsDeleted, 0)
        .eq(SmartAlertDispatch::getAlertId, alert.getId())
        .last("LIMIT 1"));
    if (exists != null) {
      return exists;
    }
    return doCreateForAlert(alert, ruleId);
  }

  private SmartAlertDispatch doCreateForAlert(SmartAlert alert, Long ruleId) {
    LocalDateTime now = LocalDateTime.now();
    SmartAlertDispatch dispatch = new SmartAlertDispatch();
    dispatch.setOrgId(alert.getOrgId());
    dispatch.setTenantId(alert.getTenantId());
    dispatch.setAlertId(alert.getId());
    dispatch.setElderId(alert.getElderId());
    dispatch.setDeviceId(alert.getDeviceId());
    dispatch.setRuleId(ruleId);
    dispatch.setLevel(alert.getLevel());
    dispatch.setDispatchStatus(TRIGGERED);
    dispatch.setTriggeredAt(alert.getFirstTriggeredAt() != null ? alert.getFirstTriggeredAt() : now);
    dispatch.setResponseDeadline(now.plusMinutes(responseWindowMinutes(alert.getLevel())));
    dispatch.setEscalationCount(0);
    StaffAccount onDuty = findOnDutyStaff(alert.getOrgId(), now);
    if (onDuty != null) {
      dispatch.setDispatchStatus(ASSIGNED);
      dispatch.setAssigneeId(onDuty.getId());
      dispatch.setAssigneeName(onDuty.getRealName() != null ? onDuty.getRealName() : onDuty.getUsername());
      dispatch.setAssignedAt(now);
    }
    dispatchMapper.insert(dispatch);
    return dispatch;
  }

  /** 从排班表找当班护理员：今日排班且当前时刻在班段内，并具备值班护理员角色。 */
  private StaffAccount findOnDutyStaff(Long orgId, LocalDateTime now) {
    if (orgId == null) {
      return null;
    }
    List<StaffSchedule> schedules = staffScheduleMapper.selectList(Wrappers.lambdaQuery(StaffSchedule.class)
        .eq(StaffSchedule::getIsDeleted, 0)
        .eq(StaffSchedule::getOrgId, orgId)
        .eq(StaffSchedule::getDutyDate, LocalDate.now())
        .ne(StaffSchedule::getStatus, 0));
    Set<Long> onDutyIds = schedules.stream()
        .filter(s -> withinShift(s, now))
        .map(StaffSchedule::getStaffId)
        .collect(Collectors.toSet());
    if (onDutyIds.isEmpty()) {
      return null;
    }
    List<StaffAccount> candidates = staffMapper.selectByRoleCode(orgId, DUTY_ROLE);
    return candidates.stream()
        .filter(s -> onDutyIds.contains(s.getId()))
        .findFirst()
        .orElse(null);
  }

  private boolean withinShift(StaffSchedule schedule, LocalDateTime now) {
    if (schedule.getStartTime() != null && now.isBefore(schedule.getStartTime())) {
      return false;
    }
    if (schedule.getEndTime() != null && now.isAfter(schedule.getEndTime())) {
      return false;
    }
    return true;
  }

  @Override
  @Transactional
  public SmartAlertDispatch assign(SmartDispatchActionRequest request) {
    SmartAlertDispatch d = load(request.getDispatchId());
    d.setDispatchStatus(ASSIGNED);
    d.setAssigneeId(request.getAssigneeId());
    d.setAssigneeName(request.getAssigneeName());
    d.setAssignedAt(LocalDateTime.now());
    dispatchMapper.updateById(d);
    return d;
  }

  @Override
  @Transactional
  public SmartAlertDispatch respond(Long dispatchId) {
    SmartAlertDispatch d = load(dispatchId);
    d.setDispatchStatus(RESPONDED);
    d.setRespondedAt(LocalDateTime.now());
    dispatchMapper.updateById(d);
    return d;
  }

  @Override
  @Transactional
  public SmartAlertDispatch onsite(Long dispatchId) {
    SmartAlertDispatch d = load(dispatchId);
    d.setDispatchStatus(ONSITE);
    d.setOnsiteAt(LocalDateTime.now());
    dispatchMapper.updateById(d);
    return d;
  }

  @Override
  @Transactional
  public SmartAlertDispatch handle(SmartDispatchActionRequest request) {
    SmartAlertDispatch d = load(request.getDispatchId());
    d.setDispatchStatus(HANDLED);
    d.setHandledAt(LocalDateTime.now());
    if (StringUtils.hasText(request.getNote())) {
      d.setHandleNote(request.getNote());
    }
    if (request.getIncidentId() != null) {
      d.setIncidentId(request.getIncidentId());
    } else if (Boolean.TRUE.equals(request.getCreateIncident())) {
      IncidentReport incident = createIncidentFromDispatch(d, request);
      d.setIncidentId(incident.getId());
    }
    dispatchMapper.updateById(d);
    return d;
  }

  /** 处置时一键生成不良事件（incident_report），沿用不良事件模块的等级/状态口径。 */
  private IncidentReport createIncidentFromDispatch(SmartAlertDispatch d, SmartDispatchActionRequest request) {
    SmartAlert alert = d.getAlertId() == null ? null : alertMapper.selectById(d.getAlertId());
    LocalDateTime now = LocalDateTime.now();
    IncidentReport incident = new IncidentReport();
    incident.setTenantId(d.getOrgId());
    incident.setOrgId(d.getOrgId());
    incident.setElderId(d.getElderId());
    incident.setElderName(resolveElderName(d.getElderId()));
    incident.setReporterName(StringUtils.hasText(d.getAssigneeName())
        ? d.getAssigneeName()
        : (AuthContext.getUsername() != null ? AuthContext.getUsername() : "智慧告警派单"));
    incident.setIncidentTime(d.getTriggeredAt() != null ? d.getTriggeredAt() : now);
    incident.setIncidentType(StringUtils.hasText(request.getIncidentType())
        ? request.getIncidentType().trim()
        : (alert != null ? alert.getAlertType() : "SMART_ALERT"));
    incident.setLevel("CRITICAL".equalsIgnoreCase(d.getLevel()) ? "MAJOR" : "NORMAL");
    incident.setLocation(alert == null ? null : alert.getLocation());
    incident.setDescription(StringUtils.hasText(request.getNote())
        ? request.getNote()
        : (alert != null && StringUtils.hasText(alert.getTitle()) ? alert.getTitle() : "智慧告警处置生成"));
    incident.setActionTaken(request.getNote());
    incident.setStatus("OPEN");
    incident.setCreatedBy(AuthContext.getStaffId());
    incidentReportMapper.insert(incident);
    return incident;
  }

  private String resolveElderName(Long elderId) {
    if (elderId == null) {
      return null;
    }
    ElderProfile elder = elderMapper.selectById(elderId);
    return elder == null ? null : elder.getFullName();
  }

  @Override
  @Transactional
  public SmartAlertDispatch review(SmartDispatchActionRequest request) {
    SmartAlertDispatch d = load(request.getDispatchId());
    d.setDispatchStatus(REVIEWED);
    d.setReviewedAt(LocalDateTime.now());
    if (StringUtils.hasText(request.getNote())) {
      d.setReviewNote(request.getNote());
    }
    dispatchMapper.updateById(d);
    return d;
  }

  @Override
  public IPage<SmartAlertDispatch> page(int pageNo, int pageSize, String status, String level,
      Long assigneeId) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(SmartAlertDispatch.class)
        .eq(SmartAlertDispatch::getIsDeleted, 0)
        .eq(orgId != null, SmartAlertDispatch::getOrgId, orgId)
        .eq(StringUtils.hasText(status), SmartAlertDispatch::getDispatchStatus, status)
        .eq(StringUtils.hasText(level), SmartAlertDispatch::getLevel, level)
        .eq(assigneeId != null, SmartAlertDispatch::getAssigneeId, assigneeId)
        .orderByDesc(SmartAlertDispatch::getId);
    return dispatchMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  @Transactional
  public int escalateOverdue() {
    LocalDateTime now = LocalDateTime.now();
    List<SmartAlertDispatch> overdue = dispatchMapper.selectList(Wrappers.lambdaQuery(SmartAlertDispatch.class)
        .eq(SmartAlertDispatch::getIsDeleted, 0)
        .in(SmartAlertDispatch::getDispatchStatus, List.of(TRIGGERED, ASSIGNED))
        .isNotNull(SmartAlertDispatch::getResponseDeadline)
        .lt(SmartAlertDispatch::getResponseDeadline, now));
    for (SmartAlertDispatch d : overdue) {
      d.setEscalationCount((d.getEscalationCount() == null ? 0 : d.getEscalationCount()) + 1);
      d.setResponseDeadline(now.plusMinutes(responseWindowMinutes(d.getLevel())));
      StaffAccount minister = findEscalationTarget(d.getOrgId());
      if (minister != null) {
        String ministerName = minister.getRealName() != null ? minister.getRealName() : minister.getUsername();
        d.setEscalatedToId(minister.getId());
        d.setEscalatedToName(ministerName);
        // 升级后由部长接管处置
        d.setAssigneeId(minister.getId());
        d.setAssigneeName(ministerName);
        if (TRIGGERED.equals(d.getDispatchStatus())) {
          d.setDispatchStatus(ASSIGNED);
          d.setAssignedAt(now);
        }
      }
      d.setEscalatedAt(now);
      dispatchMapper.updateById(d);
    }
    return overdue.size();
  }

  private StaffAccount findEscalationTarget(Long orgId) {
    if (orgId == null) {
      return null;
    }
    List<StaffAccount> ministers = staffMapper.selectByRoleCode(orgId, ESCALATION_ROLE);
    return ministers.isEmpty() ? null : ministers.get(0);
  }

  private long responseWindowMinutes(String level) {
    if ("CRITICAL".equalsIgnoreCase(level)) {
      return 5;
    }
    if ("HIGH".equalsIgnoreCase(level)) {
      return 15;
    }
    return 30;
  }

  private SmartAlertDispatch load(Long dispatchId) {
    SmartAlertDispatch d = dispatchMapper.selectById(dispatchId);
    if (d == null || Integer.valueOf(1).equals(d.getIsDeleted())) {
      throw new IllegalArgumentException("派单不存在: " + dispatchId);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(d.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的派单");
    }
    return d;
  }
}
