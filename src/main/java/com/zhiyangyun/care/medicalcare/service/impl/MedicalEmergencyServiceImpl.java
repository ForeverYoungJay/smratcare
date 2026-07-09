package com.zhiyangyun.care.medicalcare.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.support.ElderResolveSupport;
import com.zhiyangyun.care.medicalcare.entity.MedicalEmergencyEvent;
import com.zhiyangyun.care.medicalcare.entity.MedicalRescueRecord;
import com.zhiyangyun.care.medicalcare.mapper.MedicalEmergencyEventMapper;
import com.zhiyangyun.care.medicalcare.mapper.MedicalRescueRecordMapper;
import com.zhiyangyun.care.medicalcare.model.MedicalEmergencyEventRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalEmergencyTransitionRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalRescueRecordRequest;
import com.zhiyangyun.care.medicalcare.service.MedicalEmergencyService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MedicalEmergencyServiceImpl implements MedicalEmergencyService {

  private static final List<String> OUTCOMES = List.of("RETURNED", "HOSPITALIZED", "DECEASED");

  private final MedicalEmergencyEventMapper eventMapper;
  private final com.zhiyangyun.care.family.support.FamilyNoticePublisher familyNoticePublisher;
  private final MedicalRescueRecordMapper rescueMapper;
  private final ElderResolveSupport elderResolveSupport;

  public MedicalEmergencyServiceImpl(
      MedicalEmergencyEventMapper eventMapper,
      MedicalRescueRecordMapper rescueMapper,
      ElderResolveSupport elderResolveSupport,
      com.zhiyangyun.care.family.support.FamilyNoticePublisher familyNoticePublisher) {
    this.eventMapper = eventMapper;
    this.rescueMapper = rescueMapper;
    this.elderResolveSupport = elderResolveSupport;
    this.familyNoticePublisher = familyNoticePublisher;
  }

  @Override
  public IPage<MedicalEmergencyEvent> pageEvents(Long orgId, int pageNo, int pageSize, Long elderId, String status,
      String outcome) {
    var wrapper = Wrappers.lambdaQuery(MedicalEmergencyEvent.class)
        .eq(MedicalEmergencyEvent::getIsDeleted, 0)
        .eq(orgId != null, MedicalEmergencyEvent::getOrgId, orgId)
        .eq(elderId != null, MedicalEmergencyEvent::getElderId, elderId)
        .eq(StringUtils.hasText(status), MedicalEmergencyEvent::getStatus, status)
        .eq(StringUtils.hasText(outcome), MedicalEmergencyEvent::getOutcome, outcome)
        .orderByDesc(MedicalEmergencyEvent::getEventTime)
        .orderByDesc(MedicalEmergencyEvent::getId);
    return eventMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  public MedicalEmergencyEvent getEvent(Long orgId, Long id) {
    return loadEvent(orgId, id);
  }

  @Override
  @Transactional
  public MedicalEmergencyEvent createEvent(Long orgId, MedicalEmergencyEventRequest request) {
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    MedicalEmergencyEvent event = new MedicalEmergencyEvent();
    event.setTenantId(orgId);
    event.setOrgId(orgId);
    event.setElderId(elderId);
    event.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    event.setAlertId(request.getAlertId());
    event.setEventTime(request.getEventTime() != null ? request.getEventTime() : LocalDateTime.now());
    event.setLocation(request.getLocation());
    event.setSymptom(request.getSymptom());
    event.setReporterId(AuthContext.getStaffId());
    event.setReporterName(AuthContext.getUsername());
    event.setStatus("INITIATED");
    event.setRemark(request.getRemark());
    event.setCreatedBy(AuthContext.getStaffId());
    event.setIsDeleted(0);
    eventMapper.insert(event);
    // 家属侧即时告警：紧急事件同步进入家属消息中心（标题“紧急”触发一级提醒）
    familyNoticePublisher.publish(orgId,
        "紧急：" + event.getElderName() + " 突发状况，机构已启动急救流程",
        "长者 " + event.getElderName() + " 于 " + event.getEventTime() + " 出现：" + defaultSymptom(event.getSymptom())
            + "。医护人员已到场处置，如已呼叫120或送医，进展会持续在此通知，请保持电话畅通。");
    return event;
  }

  private String defaultSymptom(String symptom) {
    return symptom == null || symptom.isBlank() ? "突发不适" : symptom;
  }

  @Override
  @Transactional
  public MedicalEmergencyEvent transition(Long orgId, Long id, MedicalEmergencyTransitionRequest request) {
    MedicalEmergencyEvent event = loadEvent(orgId, id);
    String action = request.getAction() == null ? "" : request.getAction().toUpperCase(Locale.ROOT);
    String status = event.getStatus();
    LocalDateTime now = LocalDateTime.now();
    switch (action) {
      case "CALL" -> {
        requireStatus(status, action, "INITIATED");
        event.setStatus("CALLED");
        event.setCallTime(request.getCallTime() != null ? request.getCallTime() : now);
        event.setCallOperatorId(AuthContext.getStaffId());
        event.setCallOperatorName(AuthContext.getUsername());
      }
      case "DEPART" -> {
        requireStatus(status, action, "CALLED");
        if (!StringUtils.hasText(request.getHospitalName())) {
          throw new IllegalArgumentException("送医需填写医院名称");
        }
        event.setStatus("TRANSFERRED");
        event.setHospitalName(request.getHospitalName());
        event.setEscortName(request.getEscortName());
        event.setDepartTime(request.getDepartTime() != null ? request.getDepartTime() : now);
      }
      case "OUTCOME" -> {
        requireStatus(status, action, "TRANSFERRED");
        if (!OUTCOMES.contains(request.getOutcome())) {
          throw new IllegalArgumentException("无效的转归类型: " + request.getOutcome());
        }
        event.setStatus(request.getOutcome());
        event.setOutcome(request.getOutcome());
        event.setOutcomeTime(now);
        event.setOutcomeNote(request.getOutcomeNote());
      }
      case "CLOSE" -> {
        requireStatus(status, action, "RETURNED", "HOSPITALIZED", "DECEASED");
        event.setStatus("CLOSED");
        if (StringUtils.hasText(request.getOutcomeNote())) {
          event.setOutcomeNote(request.getOutcomeNote());
        }
      }
      case "CANCEL" -> {
        requireStatus(status, action, "INITIATED", "CALLED");
        event.setStatus("CLOSED");
        event.setOutcome("CANCELED");
        event.setOutcomeTime(now);
        event.setOutcomeNote(request.getOutcomeNote());
      }
      default -> throw new IllegalArgumentException("无效的急救事件操作: " + request.getAction());
    }
    if (StringUtils.hasText(request.getRemark())) {
      event.setRemark(request.getRemark());
    }
    eventMapper.updateById(event);
    return event;
  }

  @Override
  @Transactional
  public MedicalRescueRecord saveRescueRecord(Long orgId, MedicalRescueRecordRequest request) {
    MedicalEmergencyEvent event = loadEvent(orgId, request.getEventId());
    MedicalRescueRecord record = rescueMapper.selectOne(Wrappers.lambdaQuery(MedicalRescueRecord.class)
        .eq(MedicalRescueRecord::getIsDeleted, 0)
        .eq(MedicalRescueRecord::getEventId, event.getId())
        .last("LIMIT 1"));
    boolean isNew = record == null;
    if (isNew) {
      record = new MedicalRescueRecord();
      record.setTenantId(event.getTenantId());
      record.setOrgId(event.getOrgId());
      record.setEventId(event.getId());
      record.setElderId(event.getElderId());
      record.setCreatedBy(AuthContext.getStaffId());
      record.setIsDeleted(0);
    }
    record.setTimelineJson(request.getTimelineJson());
    record.setParticipants(request.getParticipants());
    record.setDrugsUsed(request.getDrugsUsed());
    record.setMeasures(request.getMeasures());
    record.setResult(request.getResult());
    record.setResultNote(request.getResultNote());
    record.setStartTime(request.getStartTime());
    record.setEndTime(request.getEndTime());
    record.setRecorderId(AuthContext.getStaffId());
    record.setRecorderName(AuthContext.getUsername());
    record.setRemark(request.getRemark());
    if (isNew) {
      rescueMapper.insert(record);
    } else {
      rescueMapper.updateById(record);
    }
    return record;
  }

  @Override
  public MedicalRescueRecord getRescueRecordByEvent(Long orgId, Long eventId) {
    return rescueMapper.selectOne(Wrappers.lambdaQuery(MedicalRescueRecord.class)
        .eq(MedicalRescueRecord::getIsDeleted, 0)
        .eq(orgId != null, MedicalRescueRecord::getOrgId, orgId)
        .eq(MedicalRescueRecord::getEventId, eventId)
        .last("LIMIT 1"));
  }

  private void requireStatus(String current, String action, String... allowed) {
    for (String status : allowed) {
      if (status.equals(current)) {
        return;
      }
    }
    throw new IllegalArgumentException("当前状态 " + current + " 不允许执行 " + action);
  }

  private MedicalEmergencyEvent loadEvent(Long orgId, Long id) {
    MedicalEmergencyEvent event = eventMapper.selectById(id);
    if (event == null || Integer.valueOf(1).equals(event.getIsDeleted())) {
      throw new IllegalArgumentException("急救事件不存在: " + id);
    }
    if (orgId != null && !orgId.equals(event.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的急救事件");
    }
    return event;
  }
}
