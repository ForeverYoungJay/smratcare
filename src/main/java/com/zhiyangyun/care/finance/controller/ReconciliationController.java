package com.zhiyangyun.care.finance.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.finance.entity.ReconciliationDaily;
import com.zhiyangyun.care.finance.mapper.ReconciliationDailyMapper;
import com.zhiyangyun.care.finance.model.ReconcileResponse;
import com.zhiyangyun.care.finance.service.FinanceService;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/finance")
public class ReconciliationController {
  private final FinanceService financeService;
  private final ReconciliationDailyMapper reconciliationDailyMapper;

  public ReconciliationController(
      FinanceService financeService,
      ReconciliationDailyMapper reconciliationDailyMapper) {
    this.financeService = financeService;
    this.reconciliationDailyMapper = reconciliationDailyMapper;
  }

  @PostMapping("/reconcile")
  public Result<ReconcileResponse> reconcile(
      @RequestParam @NotNull LocalDate date) {
    return Result.ok(financeService.reconcile(AuthContext.getOrgId(), date));
  }

  @GetMapping("/reconcile/page")
  public Result<IPage<ReconciliationDaily>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(ReconciliationDaily.class)
        .eq(ReconciliationDaily::getIsDeleted, 0)
        .eq(orgId != null, ReconciliationDaily::getOrgId, orgId)
        .orderByDesc(ReconciliationDaily::getReconcileDate);
    if (from != null && !from.isBlank()) {
      wrapper.ge(ReconciliationDaily::getReconcileDate, LocalDate.parse(from));
    }
    if (to != null && !to.isBlank()) {
      wrapper.le(ReconciliationDaily::getReconcileDate, LocalDate.parse(to));
    }
    IPage<ReconciliationDaily> page = reconciliationDailyMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return Result.ok(page);
  }
}
