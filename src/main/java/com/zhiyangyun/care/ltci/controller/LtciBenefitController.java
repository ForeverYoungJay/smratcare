package com.zhiyangyun.care.ltci.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.ltci.entity.LtciServicePackage;
import com.zhiyangyun.care.ltci.entity.LtciSettlement;
import com.zhiyangyun.care.ltci.model.LtciBenefitRequest;
import com.zhiyangyun.care.ltci.model.LtciInsuredRequest;
import com.zhiyangyun.care.ltci.model.LtciServiceRecordRequest;
import com.zhiyangyun.care.ltci.service.LtciBenefitService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 长护险待遇与结算接口。 */
@RestController
@RequestMapping("/api/ltci")
public class LtciBenefitController {

  private static final String OPERATOR =
      "hasAnyRole('NURSING_EMPLOYEE','NURSING_MINISTER','MEDICAL_MINISTER','FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String MANAGER =
      "hasAnyRole('NURSING_MINISTER','MEDICAL_MINISTER','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String FINANCE =
      "hasAnyRole('FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String VIEWER =
      "hasAnyRole('NURSING_EMPLOYEE','NURSING_MINISTER','MEDICAL_MINISTER','FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN','STAFF')";

  private final LtciBenefitService benefitService;

  public LtciBenefitController(LtciBenefitService benefitService) {
    this.benefitService = benefitService;
  }

  @PostMapping("/insured")
  @PreAuthorize(MANAGER)
  public Result<Long> registerInsured(@Valid @RequestBody LtciInsuredRequest request) {
    return Result.ok(benefitService.registerInsured(request));
  }

  @PostMapping("/benefits")
  @PreAuthorize(MANAGER)
  public Result<Long> grantBenefit(@Valid @RequestBody LtciBenefitRequest request) {
    return Result.ok(benefitService.grantBenefit(request));
  }

  @GetMapping("/packages")
  @PreAuthorize(VIEWER)
  public Result<List<LtciServicePackage>> packages() {
    return Result.ok(benefitService.listPackages(AuthContext.getOrgId()));
  }

  @PostMapping("/service-records")
  @PreAuthorize(OPERATOR)
  public Result<Long> addServiceRecord(@Valid @RequestBody LtciServiceRecordRequest request) {
    return Result.ok(benefitService.addServiceRecord(request));
  }

  @PostMapping("/settlements/generate")
  @PreAuthorize(FINANCE)
  public Result<LtciSettlement> generate(
      @RequestParam Long elderId,
      @RequestParam String month) {
    return Result.ok(benefitService.generateSettlement(AuthContext.getOrgId(), elderId, month));
  }

  @GetMapping("/settlements/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<LtciSettlement>> pageSettlements(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String month,
      @RequestParam(required = false) String status) {
    return Result.ok(benefitService.pageSettlements(pageNo, pageSize, elderId, month, status));
  }

  @PostMapping("/settlements/{id}/submit")
  @PreAuthorize(FINANCE)
  public Result<Void> submit(@PathVariable("id") Long id) {
    benefitService.submitSettlement(id);
    return Result.ok(null);
  }
}
