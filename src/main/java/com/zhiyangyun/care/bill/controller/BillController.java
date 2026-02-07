package com.zhiyangyun.care.bill.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.bill.model.BillDetailResponse;
import com.zhiyangyun.care.bill.model.BillGenerateResponse;
import com.zhiyangyun.care.bill.service.BillService;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bill")
public class BillController {
  private final BillService billService;
  private final BillMonthlyMapper billMonthlyMapper;

  public BillController(BillService billService, BillMonthlyMapper billMonthlyMapper) {
    this.billService = billService;
    this.billMonthlyMapper = billMonthlyMapper;
  }

  @PostMapping("/generate")
  public BillGenerateResponse generate(
      @RequestParam @Pattern(regexp = "\\d{4}-\\d{2}") String month) {
    return billService.generateMonthlyBills(month);
  }

  @GetMapping("/{elderId}")
  public BillDetailResponse detail(
      @PathVariable Long elderId,
      @RequestParam @Pattern(regexp = "\\d{4}-\\d{2}") String month) {
    return billService.getBillDetail(elderId, month);
  }

  @GetMapping("/page")
  public Result<IPage<BillMonthly>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String month,
      @RequestParam(required = false) Long elderId) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(BillMonthly.class)
        .eq(BillMonthly::getIsDeleted, 0)
        .eq(orgId != null, BillMonthly::getOrgId, orgId);
    if (month != null && !month.isBlank()) {
      wrapper.eq(BillMonthly::getBillMonth, month);
    }
    if (elderId != null) {
      wrapper.eq(BillMonthly::getElderId, elderId);
    }
    wrapper.orderByDesc(BillMonthly::getBillMonth);
    return Result.ok(billMonthlyMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }
}
