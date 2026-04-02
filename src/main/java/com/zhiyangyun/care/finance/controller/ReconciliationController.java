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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/finance")
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @GetMapping(value = "/reconcile/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
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
    List<ReconciliationDaily> rows = reconciliationDailyMapper.selectList(wrapper);
    List<String> headers = List.of("对账日期", "收款总额", "退款总额", "净收款", "是否异常", "备注", "生成时间");
    List<List<String>> body = rows.stream()
        .map(item -> List.of(
            stringOf(item.getReconcileDate()),
            stringOf(item.getTotalReceived()),
            stringOf(item.getTotalRefund()),
            stringOf(item.getNetReceived()),
            item.getMismatchFlag() != null && item.getMismatchFlag() == 1 ? "异常" : "正常",
            stringOf(item.getRemark()),
            stringOf(item.getCreateTime())))
        .toList();
    return csvResponse("finance-reconcile-history", headers, body);
  }

  private ResponseEntity<byte[]> csvResponse(String filenamePrefix, List<String> headers, List<List<String>> rows) {
    StringBuilder builder = new StringBuilder();
    builder.append('\uFEFF');
    builder.append(headers.stream().map(this::escapeCsv).collect(Collectors.joining(","))).append('\n');
    for (List<String> row : rows) {
      builder.append(row.stream().map(this::escapeCsv).collect(Collectors.joining(","))).append('\n');
    }
    byte[] bytes = builder.toString().getBytes(StandardCharsets.UTF_8);
    String filename = filenamePrefix + "-" + LocalDate.now() + ".csv";
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .body(bytes);
  }

  private String escapeCsv(String value) {
    String text = value == null ? "" : value;
    boolean needQuote = text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");
    if (!needQuote) {
      return text;
    }
    return "\"" + text.replace("\"", "\"\"") + "\"";
  }

  private String stringOf(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
