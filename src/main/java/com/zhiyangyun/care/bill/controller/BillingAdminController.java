package com.zhiyangyun.care.bill.controller;

import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillingCareLevelFee;
import com.zhiyangyun.care.bill.entity.BillingConfigEntry;
import com.zhiyangyun.care.bill.model.BillingCareLevelFeeRequest;
import com.zhiyangyun.care.bill.model.BillingCareLevelFeeResponse;
import com.zhiyangyun.care.bill.model.BillingConfigRequest;
import com.zhiyangyun.care.bill.model.BillingConfigResponse;
import com.zhiyangyun.care.bill.service.BillingConfigService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/billing")
@PreAuthorize("hasAnyRole('FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class BillingAdminController {
  private final BillingConfigService billingConfigService;

  public BillingAdminController(BillingConfigService billingConfigService) {
    this.billingConfigService = billingConfigService;
  }

  @PostMapping("/config")
  public BillingConfigResponse upsertConfig(@Valid @RequestBody BillingConfigRequest request) {
    Long scopedOrgId = AuthContext.resolveAccessibleOrgId(request.getOrgId());
    BillingConfigEntry entry = new BillingConfigEntry();
    entry.setOrgId(scopedOrgId);
    entry.setConfigKey(request.getConfigKey());
    entry.setConfigValue(request.getConfigValue());
    entry.setEffectiveMonth(request.getEffectiveMonth());
    entry.setStatus(request.getStatus());
    entry.setRemark(request.getRemark());

    BillingConfigEntry saved = billingConfigService.upsertConfig(entry);
    return toResponse(saved);
  }

  @GetMapping("/config")
  public List<BillingConfigResponse> listConfigs(
      @RequestParam Long orgId,
      @RequestParam(required = false) @Pattern(regexp = "\\d{4}-\\d{2}") String month) {
    Long scopedOrgId = AuthContext.resolveAccessibleOrgId(orgId);
    return billingConfigService.listConfigs(scopedOrgId, month).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @PostMapping("/care-level")
  public BillingCareLevelFeeResponse upsertCareLevel(@Valid @RequestBody BillingCareLevelFeeRequest request) {
    Long scopedOrgId = AuthContext.resolveAccessibleOrgId(request.getOrgId());
    BillingCareLevelFee fee = new BillingCareLevelFee();
    fee.setOrgId(scopedOrgId);
    fee.setCareLevel(request.getCareLevel());
    fee.setFeeMonthly(request.getFeeMonthly());
    fee.setEffectiveMonth(request.getEffectiveMonth());
    fee.setStatus(request.getStatus());
    fee.setRemark(request.getRemark());

    BillingCareLevelFee saved = billingConfigService.upsertCareLevelFee(fee);
    return toResponse(saved);
  }

  @GetMapping("/care-level")
  public List<BillingCareLevelFeeResponse> listCareLevels(
      @RequestParam Long orgId,
      @RequestParam(required = false) @Pattern(regexp = "\\d{4}-\\d{2}") String month) {
    Long scopedOrgId = AuthContext.resolveAccessibleOrgId(orgId);
    return billingConfigService.listCareLevelFees(scopedOrgId, month).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  private BillingConfigResponse toResponse(BillingConfigEntry entry) {
    BillingConfigResponse response = new BillingConfigResponse();
    response.setId(entry.getId());
    response.setOrgId(entry.getOrgId());
    response.setConfigKey(entry.getConfigKey());
    response.setConfigValue(entry.getConfigValue());
    response.setEffectiveMonth(entry.getEffectiveMonth());
    response.setStatus(entry.getStatus());
    response.setRemark(entry.getRemark());
    return response;
  }

  private BillingCareLevelFeeResponse toResponse(BillingCareLevelFee fee) {
    BillingCareLevelFeeResponse response = new BillingCareLevelFeeResponse();
    response.setId(fee.getId());
    response.setOrgId(fee.getOrgId());
    response.setCareLevel(fee.getCareLevel());
    response.setFeeMonthly(fee.getFeeMonthly());
    response.setEffectiveMonth(fee.getEffectiveMonth());
    response.setStatus(fee.getStatus());
    response.setRemark(fee.getRemark());
    return response;
  }
}
