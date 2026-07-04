package com.zhiyangyun.care.smart.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.entity.SmartAlertDispatch;
import com.zhiyangyun.care.smart.mapper.SmartAlertDispatchMapper;
import com.zhiyangyun.care.smart.mapper.SmartAlertMapper;
import com.zhiyangyun.care.smart.model.SmartDispatchActionRequest;
import com.zhiyangyun.care.smart.service.SmartDispatchService;
import java.time.LocalDateTime;
import java.util.List;
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

  private final SmartAlertDispatchMapper dispatchMapper;
  private final SmartAlertMapper alertMapper;

  public SmartDispatchServiceImpl(SmartAlertDispatchMapper dispatchMapper, SmartAlertMapper alertMapper) {
    this.dispatchMapper = dispatchMapper;
    this.alertMapper = alertMapper;
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
      createForAlert(alert);
      count++;
    }
    return count;
  }

  private void createForAlert(SmartAlert alert) {
    LocalDateTime now = LocalDateTime.now();
    SmartAlertDispatch dispatch = new SmartAlertDispatch();
    dispatch.setOrgId(alert.getOrgId());
    dispatch.setAlertId(alert.getId());
    dispatch.setElderId(alert.getElderId());
    dispatch.setDeviceId(alert.getDeviceId());
    dispatch.setLevel(alert.getLevel());
    dispatch.setDispatchStatus(TRIGGERED);
    dispatch.setTriggeredAt(alert.getFirstTriggeredAt() != null ? alert.getFirstTriggeredAt() : now);
    dispatch.setResponseDeadline(now.plusMinutes(responseWindowMinutes(alert.getLevel())));
    dispatch.setEscalationCount(0);
    dispatchMapper.insert(dispatch);
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
    }
    dispatchMapper.updateById(d);
    return d;
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
      dispatchMapper.updateById(d);
    }
    return overdue.size();
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
