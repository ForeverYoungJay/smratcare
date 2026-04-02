package com.zhiyangyun.care.finance.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.finance.model.PaymentRequest;
import com.zhiyangyun.care.finance.model.PaymentResponse;
import com.zhiyangyun.care.finance.service.FinanceService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bill")
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class BillPaymentController {
  private final FinanceService financeService;
  private final AuditLogService auditLogService;

  public BillPaymentController(FinanceService financeService, AuditLogService auditLogService) {
    this.financeService = financeService;
    this.auditLogService = auditLogService;
  }

  @PostMapping("/{billId}/pay")
  public Result<PaymentResponse> pay(@PathVariable Long billId, @Valid @RequestBody PaymentRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    PaymentResponse response = financeService.pay(billId, request, staffId);
    BigDecimal amount = request.getAmount() == null ? BigDecimal.ZERO : request.getAmount();
    BigDecimal paidAfter = response.getPaidAmount() == null ? BigDecimal.ZERO : response.getPaidAmount();
    BigDecimal outstandingAfter = response.getOutstandingAmount() == null ? BigDecimal.ZERO : response.getOutstandingAmount();
    Map<String, Object> beforeSnapshot = new LinkedHashMap<>();
    beforeSnapshot.put("billId", billId);
    beforeSnapshot.put("paidAmount", paidAfter.subtract(amount));
    beforeSnapshot.put("outstandingAmount", outstandingAfter.add(amount));
    Map<String, Object> afterSnapshot = new LinkedHashMap<>();
    afterSnapshot.put("billId", response.getBillId());
    afterSnapshot.put("paidAmount", response.getPaidAmount());
    afterSnapshot.put("outstandingAmount", response.getOutstandingAmount());
    afterSnapshot.put("status", response.getStatus());
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("method", request.getMethod());
    context.put("amount", request.getAmount());
    context.put("externalTxnId", request.getExternalTxnId());
    context.put("paidAt", request.getPaidAt());
    context.put("remark", request.getRemark());
    auditLogService.recordStructured(
        orgId, orgId, staffId, AuthContext.getUsername(),
        "FIN_BILL_PAY", "FINANCE_BILL", billId,
        "账单收款 金额=" + request.getAmount()
            + " 方式=" + request.getMethod()
            + (request.getExternalTxnId() == null || request.getExternalTxnId().isBlank()
                ? ""
                : (" 外部流水=" + request.getExternalTxnId().trim())),
        beforeSnapshot, afterSnapshot, context);
    return Result.ok(response);
  }

  @PostMapping("/{billId}/invalidate")
  public Result<Void> invalidate(@PathVariable Long billId) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    financeService.invalidateBill(billId, staffId);
    auditLogService.record(
        orgId, orgId, staffId, AuthContext.getUsername(),
        "FIN_BILL_INVALIDATE", "FINANCE_BILL", billId,
        "作废账单");
    return Result.ok(null);
  }
}
