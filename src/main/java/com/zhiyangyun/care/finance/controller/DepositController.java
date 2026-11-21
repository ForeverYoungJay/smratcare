package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.finance.entity.FinanceDepositStandard;
import com.zhiyangyun.care.finance.model.DepositAccountView;
import com.zhiyangyun.care.finance.model.DepositStandardRequest;
import com.zhiyangyun.care.finance.model.DepositSummary;
import com.zhiyangyun.care.finance.model.DepositTransactionRequest;
import com.zhiyangyun.care.finance.model.DepositTransactionView;
import com.zhiyangyun.care.finance.service.DepositService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/deposit")
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class DepositController {
  private final DepositService depositService;
  private final AuditLogService auditLogService;

  public DepositController(DepositService depositService, AuditLogService auditLogService) {
    this.depositService = depositService;
    this.auditLogService = auditLogService;
  }

  @GetMapping("/standards")
  public Result<List<FinanceDepositStandard>> standards() {
    return Result.ok(depositService.standardList());
  }

  @PostMapping("/standards")
  public Result<FinanceDepositStandard> saveStandard(@Valid @RequestBody DepositStandardRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    FinanceDepositStandard standard = depositService.saveStandard(request, staffId);
    auditLogService.record(
        orgId, orgId, staffId, AuthContext.getUsername(),
        "FIN_DEPOSIT_STANDARD_SAVE", "FINANCE_DEPOSIT_STANDARD", standard.getId(),
        "保存押金标准 " + standard.getStandardName() + " 金额=" + standard.getAmount()
            + " 护理等级=" + (standard.getCareLevel() == null ? "通用" : standard.getCareLevel()));
    return Result.ok(standard);
  }

  @DeleteMapping("/standards/{standardId}")
  public Result<Void> deleteStandard(@PathVariable Long standardId) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    depositService.deleteStandard(standardId);
    auditLogService.record(
        orgId, orgId, staffId, AuthContext.getUsername(),
        "FIN_DEPOSIT_STANDARD_DELETE", "FINANCE_DEPOSIT_STANDARD", standardId,
        "删除押金标准");
    return Result.ok(null);
  }

  @GetMapping("/page")
  public Result<IPage<DepositAccountView>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    return Result.ok(depositService.page(pageNo, pageSize, status, keyword));
  }

  @GetMapping("/summary")
  public Result<DepositSummary> summary() {
    return Result.ok(depositService.summary());
  }

  @GetMapping("/{elderId}")
  public Result<DepositAccountView> detail(@PathVariable Long elderId) {
    return Result.ok(depositService.detail(elderId));
  }

  @GetMapping("/{elderId}/transactions")
  public Result<List<DepositTransactionView>> transactions(@PathVariable Long elderId) {
    return Result.ok(depositService.transactions(elderId));
  }

  @PostMapping("/{elderId}/refresh-standard")
  public Result<DepositAccountView> refreshStandard(@PathVariable Long elderId) {
    return Result.ok(depositService.refreshStandard(elderId));
  }

  @PostMapping("/pay")
  public Result<DepositAccountView> pay(@Valid @RequestBody DepositTransactionRequest request) {
    return Result.ok(register("PAY", request));
  }

  @PostMapping("/deduct")
  public Result<DepositAccountView> deduct(@Valid @RequestBody DepositTransactionRequest request) {
    return Result.ok(register("DEDUCT", request));
  }

  @PostMapping("/refund")
  public Result<DepositAccountView> refund(@Valid @RequestBody DepositTransactionRequest request) {
    return Result.ok(register("REFUND", request));
  }

  private DepositAccountView register(String txnType, DepositTransactionRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    DepositAccountView view = switch (txnType) {
      case "DEDUCT" -> depositService.registerDeduct(request, staffId);
      case "REFUND" -> depositService.registerRefund(request, staffId);
      default -> depositService.registerPay(request, staffId);
    };
    String action = switch (txnType) {
      case "DEDUCT" -> "押金扣款";
      case "REFUND" -> "押金退还";
      default -> "押金缴纳";
    };
    auditLogService.record(
        orgId, orgId, staffId, AuthContext.getUsername(),
        "FIN_DEPOSIT_" + txnType, "FINANCE_DEPOSIT_ACCOUNT", view.getId(),
        action + " 长者=" + view.getElderName() + " 金额=" + request.getAmount()
            + " 在押余额=" + view.getBalanceAmount()
            + (request.getReason() == null || request.getReason().isBlank()
                ? "" : (" 原因=" + request.getReason().trim())));
    return view;
  }
}
