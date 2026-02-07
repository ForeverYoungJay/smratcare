package com.zhiyangyun.care.finance.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.finance.model.PaymentRequest;
import com.zhiyangyun.care.finance.model.PaymentResponse;
import com.zhiyangyun.care.finance.service.FinanceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bill")
public class BillPaymentController {
  private final FinanceService financeService;

  public BillPaymentController(FinanceService financeService) {
    this.financeService = financeService;
  }

  @PostMapping("/{billId}/pay")
  public Result<PaymentResponse> pay(@PathVariable Long billId, @Valid @RequestBody PaymentRequest request) {
    Long staffId = AuthContext.getStaffId();
    return Result.ok(financeService.pay(billId, request, staffId));
  }
}
