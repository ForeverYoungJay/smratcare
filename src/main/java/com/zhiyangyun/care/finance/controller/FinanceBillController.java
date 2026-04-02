package com.zhiyangyun.care.finance.controller;

import com.zhiyangyun.care.audit.service.AuditLogService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.bill.model.BillDetailResponse;
import com.zhiyangyun.care.bill.service.BillService;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import com.zhiyangyun.care.finance.model.FinanceBillBindElderRequest;
import com.zhiyangyun.care.finance.model.FinanceBillDetailResponse;
import com.zhiyangyun.care.finance.model.PaymentRecordItem;
import com.zhiyangyun.care.finance.model.StoreOrderSummary;
import com.zhiyangyun.care.finance.service.FinanceMonthLockService;
import com.zhiyangyun.care.store.entity.StoreOrder;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/finance/bill")
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class FinanceBillController {
  private final BillService billService;
  private final BillMonthlyMapper billMonthlyMapper;
  private final ElderMapper elderMapper;
  private final PaymentRecordMapper paymentRecordMapper;
  private final StoreOrderMapper storeOrderMapper;
  private final StaffMapper staffMapper;
  private final AuditLogService auditLogService;
  private final FinanceMonthLockService financeMonthLockService;

  public FinanceBillController(
      BillService billService,
      BillMonthlyMapper billMonthlyMapper,
      ElderMapper elderMapper,
      PaymentRecordMapper paymentRecordMapper,
      StoreOrderMapper storeOrderMapper,
      StaffMapper staffMapper,
      AuditLogService auditLogService,
      FinanceMonthLockService financeMonthLockService) {
    this.billService = billService;
    this.billMonthlyMapper = billMonthlyMapper;
    this.elderMapper = elderMapper;
    this.paymentRecordMapper = paymentRecordMapper;
    this.storeOrderMapper = storeOrderMapper;
    this.staffMapper = staffMapper;
    this.auditLogService = auditLogService;
    this.financeMonthLockService = financeMonthLockService;
  }

  @GetMapping("/{billId}")
  public Result<FinanceBillDetailResponse> detail(@PathVariable Long billId) {
    Long orgId = AuthContext.getOrgId();
    BillMonthly monthly = billMonthlyMapper.selectOne(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getId, billId)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getIsDeleted, 0)
            .last("LIMIT 1"));
    if (monthly == null) {
      return Result.error(404, "账单不存在、已删除或无权限访问（ID: " + billId + "）");
    }

    FinanceBillDetailResponse resp = new FinanceBillDetailResponse();
    resp.setBillId(monthly.getId());
    resp.setElderId(monthly.getElderId());
    resp.setBillMonth(monthly.getBillMonth());
    resp.setTotalAmount(monthly.getTotalAmount());
    resp.setPaidAmount(monthly.getPaidAmount());
    resp.setOutstandingAmount(monthly.getOutstandingAmount());
    resp.setStatus(monthly.getStatus());

    ElderProfile elder = elderMapper.selectById(monthly.getElderId());
    resp.setElderName(elder == null ? null : elder.getFullName());

    BillDetailResponse billDetail = billService.getBillDetailById(billId);
    resp.getItems().addAll(billDetail.getItems());

    List<PaymentRecord> payments = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getBillMonthlyId, billId)
            .eq(PaymentRecord::getIsDeleted, 0)
            .orderByDesc(PaymentRecord::getPaidAt));
    List<Long> operatorIds = payments.stream()
        .map(PaymentRecord::getOperatorStaffId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    java.util.Map<Long, StaffAccount> staffMap = operatorIds.isEmpty()
        ? java.util.Map.of()
        : staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
            .in(StaffAccount::getId, operatorIds)
            .eq(StaffAccount::getIsDeleted, 0))
            .stream()
            .collect(java.util.stream.Collectors.toMap(StaffAccount::getId, item -> item, (a, b) -> a));
    List<PaymentRecordItem> paymentItems = new ArrayList<>();
    for (PaymentRecord record : payments) {
      PaymentRecordItem item = new PaymentRecordItem();
      item.setId(record.getId());
      item.setBillMonthlyId(record.getBillMonthlyId());
      item.setAmount(record.getAmount());
      item.setPayMethod(record.getPayMethod());
      item.setPaidAt(record.getPaidAt());
      item.setOperatorStaffId(record.getOperatorStaffId());
      StaffAccount operator = staffMap.get(record.getOperatorStaffId());
      item.setOperatorStaffName(operator == null ? null : operator.getRealName());
      item.setRemark(record.getRemark());
      item.setExternalTxnId(record.getExternalTxnId());
      item.setCreateTime(record.getCreateTime());
      paymentItems.add(item);
    }
    resp.setPayments(paymentItems);

    YearMonth ym = YearMonth.parse(monthly.getBillMonth());
    LocalDate startDate = ym.atDay(1);
    LocalDate endDate = ym.atEndOfMonth();
    LocalDateTime startTime = startDate.atStartOfDay();
    LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();
    List<StoreOrder> orders = storeOrderMapper.selectList(
        Wrappers.lambdaQuery(StoreOrder.class)
            .eq(orgId != null, StoreOrder::getOrgId, orgId)
            .eq(StoreOrder::getElderId, monthly.getElderId())
            .ge(StoreOrder::getCreateTime, startTime)
            .lt(StoreOrder::getCreateTime, endTime)
            .eq(StoreOrder::getIsDeleted, 0)
            .orderByDesc(StoreOrder::getCreateTime));
    List<StoreOrderSummary> summaries = new ArrayList<>();
    for (StoreOrder order : orders) {
      StoreOrderSummary summary = new StoreOrderSummary();
      summary.setId(order.getId());
      summary.setOrderNo(order.getOrderNo());
      summary.setElderId(order.getElderId());
      summary.setElderName(resp.getElderName());
      summary.setTotalAmount(order.getTotalAmount());
      summary.setPayableAmount(order.getPayableAmount());
      summary.setOrderStatus(order.getOrderStatus());
      summary.setPayStatus(order.getPayStatus());
      summary.setPayTime(order.getPayTime());
      summary.setCreateTime(order.getCreateTime());
      summaries.add(summary);
    }
    resp.setStoreOrders(summaries);

    return Result.ok(resp);
  }

  @PostMapping("/{billId}/bind-elder")
  @PreAuthorize("hasAnyRole('FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<FinanceBillDetailResponse> bindElder(
      @PathVariable Long billId,
      @Valid @RequestBody FinanceBillBindElderRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    String username = AuthContext.getUsername();
    BillMonthly monthly = billMonthlyMapper.selectOne(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getId, billId)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getIsDeleted, 0)
            .last("LIMIT 1"));
    if (monthly == null) {
      return Result.error(404, "账单不存在");
    }
    financeMonthLockService.assertMonthEditable(orgId, YearMonth.parse(monthly.getBillMonth()), "修复账单");
    ElderProfile elder = elderMapper.selectOne(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getId, request.getElderId())
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(orgId != null, ElderProfile::getOrgId, orgId)
            .last("LIMIT 1"));
    if (elder == null) {
      throw new IllegalArgumentException("老人不存在");
    }
    Long beforeElderId = monthly.getElderId();
    monthly.setElderId(elder.getId());
    billMonthlyMapper.updateById(monthly);
    Map<String, Object> beforeSnapshot = new LinkedHashMap<>();
    beforeSnapshot.put("elderId", beforeElderId);
    beforeSnapshot.put("billMonth", monthly.getBillMonth());
    beforeSnapshot.put("status", monthly.getStatus());
    Map<String, Object> afterSnapshot = new LinkedHashMap<>();
    afterSnapshot.put("elderId", elder.getId());
    afterSnapshot.put("billMonth", monthly.getBillMonth());
    afterSnapshot.put("status", monthly.getStatus());
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("remark", request.getRemark());
    auditLogService.recordStructured(
        orgId, orgId, staffId, username,
        "FIN_BILL_BIND_ELDER", "FINANCE_BILL", monthly.getId(),
        "修复账单老人: " + beforeElderId + "->" + elder.getId()
            + (request.getRemark() == null || request.getRemark().isBlank() ? "" : ("；备注:" + request.getRemark().trim())),
        beforeSnapshot, afterSnapshot, context);
    return detail(billId);
  }
}
