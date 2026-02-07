package com.zhiyangyun.care.finance.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.finance.model.ReconcileResponse;
import com.zhiyangyun.care.finance.service.FinanceService;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance")
public class ReconciliationController {
  private final FinanceService financeService;

  public ReconciliationController(FinanceService financeService) {
    this.financeService = financeService;
  }

  @PostMapping("/reconcile")
  public Result<ReconcileResponse> reconcile(
      @RequestParam @NotNull LocalDate date) {
    return Result.ok(financeService.reconcile(AuthContext.getOrgId(), date));
  }
}
