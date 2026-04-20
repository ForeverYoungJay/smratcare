package com.zhiyangyun.care.elder.service.impl;

import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderLifecycleEvent;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderLifecycleEventMapper;
import com.zhiyangyun.care.elder.model.ElderDepartureType;
import com.zhiyangyun.care.elder.model.ElderLifecycleStatus;
import com.zhiyangyun.care.elder.service.ElderDepartureSyncService;
import com.zhiyangyun.care.elder.service.ElderLifecycleStateService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElderLifecycleStateServiceImpl implements ElderLifecycleStateService {
  private final ElderMapper elderMapper;
  private final ElderLifecycleEventMapper elderLifecycleEventMapper;
  private final ElderDepartureSyncService elderDepartureSyncService;

  public ElderLifecycleStateServiceImpl(
      ElderMapper elderMapper,
      ElderLifecycleEventMapper elderLifecycleEventMapper,
      ElderDepartureSyncService elderDepartureSyncService) {
    this.elderMapper = elderMapper;
    this.elderLifecycleEventMapper = elderLifecycleEventMapper;
    this.elderDepartureSyncService = elderDepartureSyncService;
  }

  @Override
  @Transactional
  public void transition(
      ElderProfile elder,
      String lifecycleStatus,
      String departureType,
      String eventType,
      String reason,
      String bizRefType,
      Long bizRefId,
      Long operatorId) {
    if (elder == null || elder.getId() == null) {
      return;
    }
    String targetLifecycleStatus = ElderLifecycleStatus.normalize(lifecycleStatus);
    String targetDepartureType = ElderDepartureType.normalize(departureType);
    if (targetLifecycleStatus == null) {
      targetLifecycleStatus = ElderLifecycleStatus.fromLegacyStatus(elder.getStatus(), elder.getDepartureType());
    }
    if (targetDepartureType == null
        && (ElderLifecycleStatus.DISCHARGED.equals(targetLifecycleStatus)
            || ElderLifecycleStatus.DECEASED.equals(targetLifecycleStatus))) {
      targetDepartureType = ElderLifecycleStatus.DECEASED.equals(targetLifecycleStatus)
          ? ElderDepartureType.DEATH
          : ElderDepartureType.NORMAL;
    }

    String beforeLifecycleStatus = ElderLifecycleStatus.normalize(elder.getLifecycleStatus());
    String beforeDepartureType = ElderDepartureType.normalize(elder.getDepartureType());
    elder.setLifecycleStatus(targetLifecycleStatus);
    elder.setDepartureType(targetDepartureType);
    elder.setLifecycleUpdatedAt(LocalDateTime.now());
    elder.setStatus(ElderLifecycleStatus.toLegacyStatus(targetLifecycleStatus, elder.getStatus()));
    elderMapper.updateById(elder);

    ElderLifecycleEvent event = new ElderLifecycleEvent();
    event.setTenantId(elder.getTenantId());
    event.setOrgId(elder.getOrgId());
    event.setElderId(elder.getId());
    event.setEventType(eventType);
    event.setFromLifecycleStatus(beforeLifecycleStatus);
    event.setToLifecycleStatus(targetLifecycleStatus);
    event.setFromDepartureType(beforeDepartureType);
    event.setToDepartureType(targetDepartureType);
    event.setBizRefType(bizRefType);
    event.setBizRefId(bizRefId);
    event.setReason(reason);
    event.setCreatedBy(operatorId);
    elderLifecycleEventMapper.insert(event);

    elder.setLastLifecycleEventId(event.getId());
    elderMapper.updateById(elder);
    elderDepartureSyncService.syncAfterLifecycleChange(elder, beforeLifecycleStatus, targetLifecycleStatus, operatorId);
  }

  @Override
  @Transactional
  public void transitionFromLegacyStatus(
      ElderProfile elder,
      Integer legacyStatus,
      String departureType,
      String eventType,
      String reason,
      String bizRefType,
      Long bizRefId,
      Long operatorId) {
    if (elder == null) {
      return;
    }
    elder.setStatus(legacyStatus);
    transition(
        elder,
        ElderLifecycleStatus.fromLegacyStatus(legacyStatus, departureType),
        departureType,
        eventType,
        reason,
        bizRefType,
        bizRefId,
        operatorId);
  }
}
