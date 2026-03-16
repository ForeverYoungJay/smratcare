package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import com.zhiyangyun.care.finance.model.PaymentRecordItem;
import com.zhiyangyun.care.finance.model.PaymentRequest;
import com.zhiyangyun.care.finance.model.PaymentResponse;
import com.zhiyangyun.care.finance.service.FinanceService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

  public PaymentRecordController(PaymentRecordMapper paymentRecordMapper, FinanceService financeService, StaffMapper staffMapper) {
    this.paymentRecordMapper = paymentRecordMapper;
    this.financeService = financeService;
    this.staffMapper = staffMapper;
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
    var wrapper = Wrappers.lambdaQuery(PaymentRecord.class)
        .eq(PaymentRecord::getIsDeleted, 0)
        .eq(orgId != null, PaymentRecord::getOrgId, orgId)
        .eq(billId != null, PaymentRecord::getBillMonthlyId, billId)
        .eq(method != null && !method.isBlank(), PaymentRecord::getPayMethod, method)
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
    Long operatorId = AuthContext.getStaffId();
    return Result.ok(financeService.updatePaymentRecord(paymentId, request, operatorId));
  }
}
