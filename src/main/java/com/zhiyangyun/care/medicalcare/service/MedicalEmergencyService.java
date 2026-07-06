package com.zhiyangyun.care.medicalcare.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.medicalcare.entity.MedicalEmergencyEvent;
import com.zhiyangyun.care.medicalcare.entity.MedicalRescueRecord;
import com.zhiyangyun.care.medicalcare.model.MedicalEmergencyEventRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalEmergencyTransitionRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalRescueRecordRequest;

/** 120急救对接服务。 */
public interface MedicalEmergencyService {

  IPage<MedicalEmergencyEvent> pageEvents(Long orgId, int pageNo, int pageSize, Long elderId, String status,
      String outcome);

  MedicalEmergencyEvent getEvent(Long orgId, Long id);

  MedicalEmergencyEvent createEvent(Long orgId, MedicalEmergencyEventRequest request);

  /** 状态机流转：INITIATED→CALLED→TRANSFERRED→RETURNED/HOSPITALIZED→CLOSED；INITIATED/CALLED 可 CANCEL。 */
  MedicalEmergencyEvent transition(Long orgId, Long id, MedicalEmergencyTransitionRequest request);

  MedicalRescueRecord saveRescueRecord(Long orgId, MedicalRescueRecordRequest request);

  MedicalRescueRecord getRescueRecordByEvent(Long orgId, Long eventId);
}
