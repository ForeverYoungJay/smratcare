package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/payment")
public class PaymentRecordController {
  private final PaymentRecordMapper paymentRecordMapper;

  public PaymentRecordController(PaymentRecordMapper paymentRecordMapper) {
    this.paymentRecordMapper = paymentRecordMapper;
  }

  @GetMapping("/page")
  public Result<IPage<PaymentRecord>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long billId,
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(PaymentRecord.class)
        .eq(PaymentRecord::getIsDeleted, 0)
        .eq(orgId != null, PaymentRecord::getOrgId, orgId)
        .eq(billId != null, PaymentRecord::getBillMonthlyId, billId)
        .orderByDesc(PaymentRecord::getPaidAt);
    if (from != null && !from.isBlank()) {
      LocalDateTime start = LocalDate.parse(from).atStartOfDay();
      wrapper.ge(PaymentRecord::getPaidAt, start);
    }
    if (to != null && !to.isBlank()) {
      LocalDateTime end = LocalDate.parse(to).plusDays(1).atStartOfDay();
      wrapper.lt(PaymentRecord::getPaidAt, end);
    }
    return Result.ok(paymentRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }
}
