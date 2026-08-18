package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.finance.model.FinanceVoucherIssueRequest;
import com.zhiyangyun.care.finance.model.FinanceVoucherUsageView;
import com.zhiyangyun.care.finance.model.FinanceVoucherView;
import com.zhiyangyun.care.finance.service.FinanceVoucherService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/voucher")
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class FinanceVoucherController {
  private final FinanceVoucherService financeVoucherService;
  private final AuditLogService auditLogService;

  public FinanceVoucherController(FinanceVoucherService financeVoucherService, AuditLogService auditLogService) {
    this.financeVoucherService = financeVoucherService;
    this.auditLogService = auditLogService;
  }

  @GetMapping("/page")
  public Result<IPage<FinanceVoucherView>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    return Result.ok(financeVoucherService.page(pageNo, pageSize, elderId, status, keyword));
  }

  @GetMapping("/usable")
  public Result<List<FinanceVoucherView>> usable(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) BigDecimal billAmount,
      @RequestParam(required = false) String onDate) {
    LocalDate targetDate = (onDate == null || onDate.isBlank()) ? LocalDate.now() : LocalDate.parse(onDate);
    return Result.ok(financeVoucherService.listUsable(elderId, billAmount, targetDate));
  }

  @GetMapping("/usage")
  public Result<List<FinanceVoucherUsageView>> usage(@RequestParam(required = false) Long voucherId) {
    return Result.ok(financeVoucherService.usageList(voucherId));
  }

  @PostMapping("/issue")
  public Result<List<FinanceVoucherView>> issue(@Valid @RequestBody FinanceVoucherIssueRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    List<FinanceVoucherView> created = financeVoucherService.issue(request, staffId);
    auditLogService.record(
        orgId, orgId, staffId, AuthContext.getUsername(),
        "FIN_VOUCHER_ISSUE", "FINANCE_VOUCHER", null,
        "发放消费券 " + created.size() + " 张 名称=" + request.getVoucherName()
            + " 面额=" + request.getFaceAmount());
    return Result.ok(created);
  }

  @PostMapping("/{voucherId}/revoke")
  public Result<Void> revoke(@PathVariable Long voucherId, @RequestParam(required = false) String reason) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    financeVoucherService.revoke(voucherId, reason, staffId);
    auditLogService.record(
        orgId, orgId, staffId, AuthContext.getUsername(),
        "FIN_VOUCHER_REVOKE", "FINANCE_VOUCHER", voucherId,
        "作废消费券" + (reason == null || reason.isBlank() ? "" : (" 原因=" + reason.trim())));
    return Result.ok(null);
  }
}
