package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.finance.model.FinanceReminderSummary;
import com.zhiyangyun.care.finance.model.FinanceReminderView;
import com.zhiyangyun.care.finance.service.FinanceReminderService;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/reminder")
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class FinanceReminderController {
  private final FinanceReminderService reminderService;
  private final AuditLogService auditLogService;

  public FinanceReminderController(FinanceReminderService reminderService, AuditLogService auditLogService) {
    this.reminderService = reminderService;
    this.auditLogService = auditLogService;
  }

  @GetMapping("/page")
  public Result<IPage<FinanceReminderView>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String reminderType) {
    return Result.ok(reminderService.page(pageNo, pageSize, status, reminderType));
  }

  @GetMapping("/summary")
  public Result<FinanceReminderSummary> summary() {
    return Result.ok(reminderService.summary());
  }

  @PostMapping("/{reminderId}/handle")
  public Result<FinanceReminderView> handle(
      @PathVariable Long reminderId,
      @RequestParam(required = false) String remark) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    FinanceReminderView view = reminderService.handle(reminderId, remark);
    auditLogService.record(
        orgId, orgId, staffId, AuthContext.getUsername(),
        "FIN_REMINDER_HANDLE", "FINANCE_REMINDER", reminderId,
        "标记提醒已处理：" + view.getTitle());
    return Result.ok(view);
  }

  @PostMapping("/handle-all")
  public Result<Map<String, Integer>> handleAll(
      @RequestParam(required = false) String reminderType,
      @RequestParam(required = false) String remark) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    int handled = reminderService.handleAll(reminderType, remark);
    auditLogService.record(
        orgId, orgId, staffId, AuthContext.getUsername(),
        "FIN_REMINDER_HANDLE_ALL", "FINANCE_REMINDER", null,
        "一键处理提醒 " + handled + " 条"
            + (reminderType == null || reminderType.isBlank() ? "（全部类型）" : ("，类型=" + reminderType)));
    return Result.ok(Map.of("handled", handled));
  }

  /** 手工补跑生成，便于当天验证与补漏；仍按 dedupeKey 幂等。 */
  @PostMapping("/generate")
  public Result<Map<String, Integer>> generate() {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    int careFee = reminderService.generateNextMonthCareFeeReminder(orgId, today);
    int deposit = reminderService.generateDepositShortfallReminders(orgId, today);
    int electricity = reminderService.generateElectricityReminders(orgId, today);
    return Result.ok(Map.of(
        "careFee", careFee,
        "deposit", deposit,
        "electricity", electricity));
  }
}
