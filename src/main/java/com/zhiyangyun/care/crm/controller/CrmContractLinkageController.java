package com.zhiyangyun.care.crm.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.crm.model.CrmContractLinkageResponse;
import com.zhiyangyun.care.crm.service.CrmContractLinkageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crm/contracts")
public class CrmContractLinkageController {
  private final CrmContractLinkageService linkageService;

  public CrmContractLinkageController(CrmContractLinkageService linkageService) {
    this.linkageService = linkageService;
  }

  @GetMapping("/linkage")
  public Result<CrmContractLinkageResponse> linkageByElder(@RequestParam Long elderId) {
    return Result.ok(linkageService.getByElderId(AuthContext.getOrgId(), elderId));
  }

  @GetMapping("/{leadId}/linkage")
  public Result<CrmContractLinkageResponse> linkageByLead(@PathVariable Long leadId) {
    return Result.ok(linkageService.getByLeadId(AuthContext.getOrgId(), leadId));
  }
}
