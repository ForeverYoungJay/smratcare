package com.zhiyangyun.care.elder.service;

import com.zhiyangyun.care.elder.entity.ElderProfile;

public interface ElderDepartureSyncService {
  void syncAfterLifecycleChange(ElderProfile elder, String beforeLifecycleStatus, String targetLifecycleStatus, Long operatorId);
}
