package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.finance.model.ElderAccountAdjustRequest;
import com.zhiyangyun.care.finance.model.ElderAccountLogResponse;
import com.zhiyangyun.care.finance.model.ElderAccountResponse;
import com.zhiyangyun.care.finance.model.ElderAccountUpdateRequest;
import com.zhiyangyun.care.finance.service.ElderAccountService;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/account")
public class ElderAccountController {
  private final ElderAccountService accountService;

  public ElderAccountController(ElderAccountService accountService) {
    this.accountService = accountService;
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
    return Result.ok(accountService.adjust(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @PostMapping("/update")
  public Result<ElderAccountResponse> update(@RequestBody ElderAccountUpdateRequest request) {
    return Result.ok(accountService.updateAccount(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @GetMapping("/warnings")
  public Result<List<ElderAccountResponse>> warnings() {
    return Result.ok(accountService.warnings(AuthContext.getOrgId()));
  }
}
