package com.zhiyangyun.care.ltci.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.ltci.entity.LtciAssessment;
import com.zhiyangyun.care.ltci.model.LtciAcceptRequest;
import com.zhiyangyun.care.ltci.model.LtciApplyRequest;
import com.zhiyangyun.care.ltci.model.LtciDisputeRequest;
import com.zhiyangyun.care.ltci.model.LtciScoreRequest;
import com.zhiyangyun.care.ltci.service.LtciAssessmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 长护险失能评估接口。 */
@RestController
@RequestMapping("/api/ltci/assessments")
public class LtciAssessmentController {

  private static final String OPERATOR =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String MANAGER =
      "hasAnyRole('MEDICAL_MINISTER','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String VIEWER =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN','STAFF')";

  private final LtciAssessmentService assessmentService;

  public LtciAssessmentController(LtciAssessmentService assessmentService) {
    this.assessmentService = assessmentService;
  }

  @PostMapping("/apply")
  @PreAuthorize(OPERATOR)
  public Result<Long> apply(@Valid @RequestBody LtciApplyRequest request) {
    return Result.ok(assessmentService.apply(request));
  }

  @PostMapping("/accept")
  @PreAuthorize(MANAGER)
  public Result<Void> accept(@Valid @RequestBody LtciAcceptRequest request) {
    assessmentService.accept(request);
    return Result.ok(null);
  }

  @PostMapping("/score")
  @PreAuthorize(OPERATOR)
  public Result<LtciAssessment> score(@Valid @RequestBody LtciScoreRequest request) {
    return Result.ok(assessmentService.score(request));
  }

  @PostMapping("/{id}/notify")
  @PreAuthorize(MANAGER)
  public Result<Void> notifyResult(@PathVariable("id") Long id) {
    assessmentService.notifyResult(id);
    return Result.ok(null);
  }

  @PostMapping("/dispute")
  @PreAuthorize(OPERATOR)
  public Result<Long> dispute(@Valid @RequestBody LtciDisputeRequest request) {
    return Result.ok(assessmentService.dispute(request));
  }

  @GetMapping("/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<LtciAssessment>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Integer disabilityLevel) {
    return Result.ok(assessmentService.pageAssessments(pageNo, pageSize, elderId, status, disabilityLevel));
  }

  @GetMapping("/elders/{elderId}/current")
  @PreAuthorize(VIEWER)
  public Result<LtciAssessment> currentLevel(@PathVariable("elderId") Long elderId) {
    return Result.ok(assessmentService.currentEffective(elderId));
  }
}
