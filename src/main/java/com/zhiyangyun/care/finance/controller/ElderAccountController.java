package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.finance.model.ElderAccountAdjustRequest;
import com.zhiyangyun.care.finance.model.ElderAccountLogResponse;
import com.zhiyangyun.care.finance.model.ElderAccountResponse;
import com.zhiyangyun.care.finance.model.ElderAccountUpdateRequest;
import com.zhiyangyun.care.finance.service.ElderAccountService;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/account")
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class ElderAccountController {
  private final ElderAccountService accountService;
  private final AuditLogService auditLogService;

  public ElderAccountController(ElderAccountService accountService, AuditLogService auditLogService) {
    this.accountService = accountService;
    this.auditLogService = auditLogService;
  }

  @GetMapping("/page")
  public Result<IPage<ElderAccountResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(accountService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword));
  }

  @GetMapping("/log/page")
  public Result<IPage<ElderAccountLogResponse>> logPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) Long accountId,
      @RequestParam(required = false) String keyword) {
    return Result.ok(accountService.logPage(AuthContext.getOrgId(), pageNo, pageSize, elderId, accountId, keyword));
  }

  @GetMapping("/log/print")
  public ResponseEntity<byte[]> printLog(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) Long accountId,
      @RequestParam(required = false) String keyword) {
    byte[] pdf = accountService.exportLogPdf(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId, accountId, keyword);
    String filename = "elder-account-log-" + System.currentTimeMillis() + ".pdf";
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
        .body(pdf);
  }

  @PostMapping("/adjust")
  public Result<ElderAccountResponse> adjust(@Valid @RequestBody ElderAccountAdjustRequest request) {
    ElderAccountResponse beforeSnapshot = request.getElderId() == null
        ? null
        : accountService.getOrCreate(AuthContext.getOrgId(), request.getElderId(), AuthContext.getStaffId());
    ElderAccountResponse response = accountService.adjust(AuthContext.getOrgId(), AuthContext.getStaffId(), request);
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("direction", request.getDirection());
    context.put("fundType", request.getFundType());
    context.put("amount", request.getAmount());
    context.put("remark", request.getRemark());
    auditLogService.recordStructured(
        AuthContext.getOrgId(),
        AuthContext.getOrgId(),
        AuthContext.getStaffId(),
        AuthContext.getUsername(),
        "FIN_ACCOUNT_ADJUST",
        "ELDER_ACCOUNT",
        response.getId(),
        "老人账户调整",
        beforeSnapshot,
        response,
        context);
    return Result.ok(response);
  }

  @PostMapping("/update")
  public Result<ElderAccountResponse> update(@RequestBody ElderAccountUpdateRequest request) {
    ElderAccountResponse beforeSnapshot = request.getElderId() == null
        ? null
        : accountService.getOrCreate(AuthContext.getOrgId(), request.getElderId(), AuthContext.getStaffId());
    ElderAccountResponse response = accountService.updateAccount(AuthContext.getOrgId(), AuthContext.getStaffId(), request);
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("creditLimit", request.getCreditLimit());
    context.put("warnThreshold", request.getWarnThreshold());
    context.put("status", request.getStatus());
    context.put("remark", request.getRemark());
    auditLogService.recordStructured(
        AuthContext.getOrgId(),
        AuthContext.getOrgId(),
        AuthContext.getStaffId(),
        AuthContext.getUsername(),
        "FIN_ACCOUNT_UPDATE",
        "ELDER_ACCOUNT",
        response.getId(),
        "老人账户设置更新",
        beforeSnapshot,
        response,
        context);
    return Result.ok(response);
  }

  @GetMapping("/warnings")
  public Result<List<ElderAccountResponse>> warnings() {
    return Result.ok(accountService.warnings(AuthContext.getOrgId()));
  }
}
