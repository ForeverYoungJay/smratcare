package com.zhiyangyun.care.elder.service;

import com.zhiyangyun.care.elder.entity.ElderProfile;

public interface ElderLifecycleStateService {
  void transition(
      ElderProfile elder,
      String lifecycleStatus,
      String departureType,
      String eventType,
      String reason,
      String bizRefType,
      Long bizRefId,
      Long operatorId);

  void transitionFromLegacyStatus(
      ElderProfile elder,
      Integer legacyStatus,
      String departureType,
      String eventType,
      String reason,
      String bizRefType,
      Long bizRefId,
      Long operatorId);
}
