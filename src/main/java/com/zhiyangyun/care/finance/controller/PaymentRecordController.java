package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import com.zhiyangyun.care.finance.model.PaymentRecordItem;
import com.zhiyangyun.care.finance.model.PaymentRequest;
import com.zhiyangyun.care.finance.model.PaymentResponse;
import com.zhiyangyun.care.finance.service.FinanceService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/payment")
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class PaymentRecordController {
  private final PaymentRecordMapper paymentRecordMapper;
  private final FinanceService financeService;
  private final StaffMapper staffMapper;
  private final AuditLogService auditLogService;

  public PaymentRecordController(PaymentRecordMapper paymentRecordMapper, FinanceService financeService,
      StaffMapper staffMapper, AuditLogService auditLogService) {
    this.paymentRecordMapper = paymentRecordMapper;
    this.financeService = financeService;
    this.staffMapper = staffMapper;
    this.auditLogService = auditLogService;
  }

  @GetMapping("/page")
  public Result<IPage<PaymentRecordItem>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long billId,
      @RequestParam(required = false) String method,
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to) {
    Long orgId = AuthContext.getOrgId();
    String normalizedMethod = method == null ? null : method.trim().toUpperCase();
    var wrapper = Wrappers.lambdaQuery(PaymentRecord.class)
        .eq(PaymentRecord::getIsDeleted, 0)
        .eq(orgId != null, PaymentRecord::getOrgId, orgId)
        .eq(billId != null, PaymentRecord::getBillMonthlyId, billId)
        .eq(normalizedMethod != null && !normalizedMethod.isBlank(), PaymentRecord::getPayMethod, normalizedMethod)
        .orderByDesc(PaymentRecord::getPaidAt);
    if (from != null && !from.isBlank()) {
      LocalDateTime start = LocalDate.parse(from).atStartOfDay();
      wrapper.ge(PaymentRecord::getPaidAt, start);
    }
    if (to != null && !to.isBlank()) {
      LocalDateTime end = LocalDate.parse(to).plusDays(1).atStartOfDay();
      wrapper.lt(PaymentRecord::getPaidAt, end);
    }
    IPage<PaymentRecord> page = paymentRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, StaffAccount> staffMap = page.getRecords().stream()
        .map(PaymentRecord::getOperatorStaffId)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.collectingAndThen(Collectors.toList(), ids -> ids.isEmpty()
            ? Map.<Long, StaffAccount>of()
            : staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
                .eq(StaffAccount::getIsDeleted, 0)
                .in(StaffAccount::getId, ids)).stream()
                .collect(Collectors.toMap(StaffAccount::getId, item -> item, (a, b) -> a))));
    IPage<PaymentRecordItem> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> {
      PaymentRecordItem row = new PaymentRecordItem();
      row.setId(item.getId());
      row.setBillMonthlyId(item.getBillMonthlyId());
      row.setAmount(item.getAmount());
      row.setPayMethod(item.getPayMethod());
      row.setPaidAt(item.getPaidAt());
      row.setOperatorStaffId(item.getOperatorStaffId());
      StaffAccount operator = staffMap.get(item.getOperatorStaffId());
      row.setOperatorStaffName(operator == null ? null : operator.getRealName());
      row.setRemark(item.getRemark());
      row.setExternalTxnId(item.getExternalTxnId());
      row.setCreateTime(item.getCreateTime());
      return row;
    }).toList());
    return Result.ok(resp);
  }

  @PutMapping("/{paymentId}")
  public Result<PaymentResponse> updatePayment(
      @PathVariable Long paymentId,
      @Valid @RequestBody PaymentRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    PaymentRecord before = paymentRecordMapper.selectOne(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getId, paymentId)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(orgId != null, PaymentRecord::getOrgId, orgId)
            .last("LIMIT 1"));
    PaymentResponse response = financeService.updatePaymentRecord(paymentId, request, operatorId);
    Map<String, Object> beforeSnapshot = new LinkedHashMap<>();
    if (before != null) {
      beforeSnapshot.put("amount", before.getAmount());
      beforeSnapshot.put("payMethod", before.getPayMethod());
      beforeSnapshot.put("paidAt", before.getPaidAt());
      beforeSnapshot.put("externalTxnId", before.getExternalTxnId());
      beforeSnapshot.put("remark", before.getRemark());
    }
    Map<String, Object> afterSnapshot = new LinkedHashMap<>();
    afterSnapshot.put("paymentId", paymentId);
    afterSnapshot.put("amount", request.getAmount());
    afterSnapshot.put("payMethod", request.getMethod());
    afterSnapshot.put("paidAt", request.getPaidAt());
    afterSnapshot.put("externalTxnId", request.getExternalTxnId());
    afterSnapshot.put("remark", request.getRemark());
    afterSnapshot.put("billStatus", response.getStatus());
    afterSnapshot.put("outstandingAmount", response.getOutstandingAmount());
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("billId", response.getBillId());
    auditLogService.recordStructured(
        orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_PAYMENT_UPDATE", "FINANCE_PAYMENT", paymentId,
        "修改收款 金额=" + request.getAmount()
            + " 方式=" + request.getMethod()
            + " 时间=" + request.getPaidAt(),
        beforeSnapshot, afterSnapshot, context);
    return Result.ok(response);
  }
}
