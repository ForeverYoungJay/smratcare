package com.zhiyangyun.care.medicalcare.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.medicalcare.entity.MedicalRoundsPlan;
import com.zhiyangyun.care.medicalcare.entity.MedicalRoundsRecord;
import com.zhiyangyun.care.medicalcare.model.MedicalRoundsPlanRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalRoundsRecordRequest;
import com.zhiyangyun.care.medicalcare.service.MedicalRoundsService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 医生巡诊接口（排班计划 + 巡诊记录）。 */
@RestController
@RequestMapping("/api/medical/rounds")
public class MedicalRoundsController {

  private static final String DOCTOR =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String VIEWER =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN','STAFF')";

  private final MedicalRoundsService roundsService;

  public MedicalRoundsController(MedicalRoundsService roundsService) {
    this.roundsService = roundsService;
  }

  @GetMapping("/plans/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<MedicalRoundsPlan>> pagePlans(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) Long doctorId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    return Result.ok(roundsService.pagePlans(AuthContext.getOrgId(), pageNo, pageSize, doctorId, status, dateFrom, dateTo));
  }

  @PostMapping("/plans")
  @PreAuthorize(DOCTOR)
  public Result<MedicalRoundsPlan> createPlan(@Valid @RequestBody MedicalRoundsPlanRequest request) {
    return Result.ok(roundsService.createPlan(AuthContext.getOrgId(), request));
  }

  @PostMapping("/plans/{id}/status")
  @PreAuthorize(DOCTOR)
  public Result<MedicalRoundsPlan> updatePlanStatus(@PathVariable("id") Long id, @RequestParam String status) {
    return Result.ok(roundsService.updatePlanStatus(AuthContext.getOrgId(), id, status));
  }

  @GetMapping("/records/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<MedicalRoundsRecord>> pageRecords(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) Long planId,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String resultLevel) {
    return Result.ok(roundsService.pageRecords(AuthContext.getOrgId(), pageNo, pageSize, planId, elderId, resultLevel));
  }

  @PostMapping("/records")
  @PreAuthorize(DOCTOR)
  public Result<MedicalRoundsRecord> createRecord(@Valid @RequestBody MedicalRoundsRecordRequest request) {
    return Result.ok(roundsService.createRecord(AuthContext.getOrgId(), request));
  }
}
