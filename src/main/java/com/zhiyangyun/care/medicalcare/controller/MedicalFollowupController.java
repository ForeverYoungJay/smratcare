package com.zhiyangyun.care.medicalcare.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.medicalcare.entity.MedicalFollowupPlan;
import com.zhiyangyun.care.medicalcare.entity.MedicalFollowupRecord;
import com.zhiyangyun.care.medicalcare.model.MedicalFollowupPlanRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalFollowupRecordRequest;
import com.zhiyangyun.care.medicalcare.service.MedicalFollowupService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 慢病随访接口（随访计划 + 随访记录 + 到期提醒）。 */
@RestController
@RequestMapping("/api/medical/followup")
public class MedicalFollowupController {

  private static final String DOCTOR =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String VIEWER =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN','STAFF')";

  private final MedicalFollowupService followupService;

  public MedicalFollowupController(MedicalFollowupService followupService) {
    this.followupService = followupService;
  }

  @GetMapping("/plans/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<MedicalFollowupPlan>> pagePlans(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String diseaseType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Boolean dueOnly) {
    return Result.ok(followupService.pagePlans(AuthContext.getOrgId(), pageNo, pageSize, elderId, diseaseType, status, dueOnly));
  }

  @PostMapping("/plans")
  @PreAuthorize(DOCTOR)
  public Result<MedicalFollowupPlan> createPlan(@Valid @RequestBody MedicalFollowupPlanRequest request) {
    return Result.ok(followupService.createPlan(AuthContext.getOrgId(), request));
  }

  @PutMapping("/plans/{id}")
  @PreAuthorize(DOCTOR)
  public Result<MedicalFollowupPlan> updatePlan(@PathVariable("id") Long id,
      @Valid @RequestBody MedicalFollowupPlanRequest request) {
    return Result.ok(followupService.updatePlan(AuthContext.getOrgId(), id, request));
  }

  @PostMapping("/plans/{id}/status")
  @PreAuthorize(DOCTOR)
  public Result<MedicalFollowupPlan> updatePlanStatus(@PathVariable("id") Long id, @RequestParam String status) {
    return Result.ok(followupService.updatePlanStatus(AuthContext.getOrgId(), id, status));
  }

  @GetMapping("/records/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<MedicalFollowupRecord>> pageRecords(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) Long planId,
      @RequestParam(required = false) Long elderId) {
    return Result.ok(followupService.pageRecords(AuthContext.getOrgId(), pageNo, pageSize, planId, elderId));
  }

  @PostMapping("/records")
  @PreAuthorize(DOCTOR)
  public Result<MedicalFollowupRecord> createRecord(@Valid @RequestBody MedicalFollowupRecordRequest request) {
    return Result.ok(followupService.createRecord(AuthContext.getOrgId(), request));
  }

  @GetMapping("/plans/due")
  @PreAuthorize(VIEWER)
  public Result<List<MedicalFollowupPlan>> listDuePlans(@RequestParam(required = false) String date) {
    LocalDate target = date == null || date.isBlank() ? LocalDate.now() : LocalDate.parse(date);
    return Result.ok(followupService.listDuePlans(AuthContext.getOrgId(), target));
  }

  /** 手动触发到期随访提醒生成（定时任务每日自动执行）。 */
  @PostMapping("/reminders/generate")
  @PreAuthorize(DOCTOR)
  public Result<Integer> generateReminders() {
    return Result.ok(followupService.generateDueReminders(LocalDate.now()));
  }
}
