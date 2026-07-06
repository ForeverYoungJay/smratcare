package com.zhiyangyun.care.medicalcare.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.medicalcare.entity.MedicalEmergencyEvent;
import com.zhiyangyun.care.medicalcare.entity.MedicalRescueRecord;
import com.zhiyangyun.care.medicalcare.model.MedicalEmergencyEventRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalEmergencyTransitionRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalRescueRecordRequest;
import com.zhiyangyun.care.medicalcare.service.MedicalEmergencyService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 120急救对接接口（急救事件状态机 + 抢救记录单）。 */
@RestController
@RequestMapping("/api/medical/emergency")
public class MedicalEmergencyController {

  /** 急救发起/处置：医护与护理人员均可操作。 */
  private static final String OPERATOR =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String VIEWER =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN','STAFF')";

  private final MedicalEmergencyService emergencyService;

  public MedicalEmergencyController(MedicalEmergencyService emergencyService) {
    this.emergencyService = emergencyService;
  }

  @GetMapping("/events/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<MedicalEmergencyEvent>> pageEvents(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String outcome) {
    return Result.ok(emergencyService.pageEvents(AuthContext.getOrgId(), pageNo, pageSize, elderId, status, outcome));
  }

  @GetMapping("/events/{id}")
  @PreAuthorize(VIEWER)
  public Result<MedicalEmergencyEvent> getEvent(@PathVariable("id") Long id) {
    return Result.ok(emergencyService.getEvent(AuthContext.getOrgId(), id));
  }

  /** 一键发起急救事件（可携带 IoT SOS 告警 alertId）。 */
  @PostMapping("/events")
  @PreAuthorize(OPERATOR)
  public Result<MedicalEmergencyEvent> createEvent(@Valid @RequestBody MedicalEmergencyEventRequest request) {
    return Result.ok(emergencyService.createEvent(AuthContext.getOrgId(), request));
  }

  @PostMapping("/events/{id}/transition")
  @PreAuthorize(OPERATOR)
  public Result<MedicalEmergencyEvent> transition(@PathVariable("id") Long id,
      @Valid @RequestBody MedicalEmergencyTransitionRequest request) {
    return Result.ok(emergencyService.transition(AuthContext.getOrgId(), id, request));
  }

  @PostMapping("/rescue")
  @PreAuthorize(OPERATOR)
  public Result<MedicalRescueRecord> saveRescueRecord(@Valid @RequestBody MedicalRescueRecordRequest request) {
    return Result.ok(emergencyService.saveRescueRecord(AuthContext.getOrgId(), request));
  }

  @GetMapping("/rescue/by-event/{eventId}")
  @PreAuthorize(VIEWER)
  public Result<MedicalRescueRecord> getRescueRecord(@PathVariable("eventId") Long eventId) {
    return Result.ok(emergencyService.getRescueRecordByEvent(AuthContext.getOrgId(), eventId));
  }
}
